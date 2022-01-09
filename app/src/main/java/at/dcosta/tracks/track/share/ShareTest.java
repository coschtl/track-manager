package at.dcosta.tracks.track.share;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.tracks.TrackCopy;
import at.dcosta.tracks.TrackManager;
import at.dcosta.tracks.combat.SAFContent;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.file.ParsingException;
import at.dcosta.tracks.util.TrackIO;

public class ShareTest {

    private final TrackDbAdapter trackDbAdapter;

    private TrackToShare[] data;

    public ShareTest(TrackDbAdapter trackDbAdapter) {
        this.trackDbAdapter = trackDbAdapter;
    }

    public void doTest() {
        receive();
        send();

    }

    private void receive() {
        new Thread() {
            private boolean persist(TrackToShare track) {
                TrackDescriptionNG origTrack = track.getDescription();
                origTrack.setActivityFactory(trackDbAdapter.getActivityFactory());
                //try {
                TrackCopy.copyTrack(TrackManager.context(), origTrack, track.getTrack(), origTrack.getName(), 0, trackDbAdapter);
                //} catch (ParsingException e) {
                //	System.err.println("receive: Can not save received track: " + e.toString());
                //	return false;
                //}
                return true;
            }

            private TrackToShare readFromStream(ObjectInputStream in) {
                try {
                    return (TrackToShare) in.readObject();
                } catch (EOFException e) {
                    return null;
                } catch (Exception e) {
                    System.err.println("receive: Unable to read track: " + e.toString());
                }
                return null;
            }

            @Override
            public void run() {
                try {
                    ServerSocket server = new ServerSocket(10856);
                    Socket socket = server.accept();

                    ObjectOutputStream out = null;
                    ObjectInputStream in = null;
                    int count = 0;
                    int err = 0;
                    try {
                        in = new ObjectInputStream(socket.getInputStream());
                        out = new ObjectOutputStream(socket.getOutputStream());
                        out.flush();
                        TrackToShare track;
                        System.out.println("receive: start reading objects");
                        while ((track = readFromStream(in)) != null) {
                            System.out.println("receive: got object");
                            writeToStream(out, TransferStatus.OK);
                            if (persist(track)) {
                                count++;
                            } else {
                                err++;
                            }
                        }
                        System.out.println("receive: no more objects");
                    } catch (IOException e) {
                        System.err.println("receive: Unable to receive track: " + e.toString());
                    } finally {
                        IOUtil.close(out);
                        IOUtil.close(in);
                        IOUtil.close(socket);
                        IOUtil.close(server);
                    }
                    System.out.println("receive: received " + count + " tracks");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void writeToStream(ObjectOutputStream out, TransferStatus status) throws IOException {
                out.writeObject(status);
                out.flush();
            }
        }.start();
    }

    private void send() {
        TrackDescriptionNG descr = null;
        Iterator<TrackDescriptionNG> it = trackDbAdapter.fetchAllEntries(false);
        while (it.hasNext()) {
            descr = it.next();
        }
        try {
            data = new TrackToShare[]{new TrackToShare(descr, TrackIO.loadTrack(new SAFContent(TrackManager.context(), descr.getPathUri(), descr.getStartTime())))};
        } catch (ParsingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println("start sending track " + descr.getName() + " with " + data[0].getTrack().size() + " points");
        new Thread() {

            private TransferStatus readFromStream(ObjectInputStream in) {
                try {
                    return (TransferStatus) in.readObject();
                } catch (Exception e) {
                    System.err.println("send: Unable to read transfer status: " + e.toString());
                }
                return TransferStatus.UNKNOWN;
            }

            @Override
            public void run() {

                ObjectOutputStream out = null;
                ObjectInputStream in = null;
                Socket socket = null;
                int count = 0;
                try {
                    socket = new Socket(InetAddress.getByName("127.0.0.1"), 10856);
                    out = new ObjectOutputStream(socket.getOutputStream());
                    out.flush();
                    in = new ObjectInputStream(socket.getInputStream());
                    System.out.println("send: start writing");
                    for (TrackToShare track : data) {
                        System.out.println("send: writing next");
                        writeToStream(out, track);
                        TransferStatus status = readFromStream(in);
                        System.out.println("send: got status");
                        if (status == TransferStatus.RETRANSFER) {
                            writeToStream(out, track);
                            status = readFromStream(in);
                        }
                        if (status == TransferStatus.OK) {
                            count++;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("send: Unable to transfer track: " + e.toString());
                } finally {
                    IOUtil.close(out);
                    IOUtil.close(in);
                    IOUtil.close(socket);
                }
                System.out.println("sent " + count + " tracks");
            }

            private void writeToStream(ObjectOutputStream out, TrackToShare track) throws IOException {
                out.writeObject(track);
                out.flush();
            }
        }.start();
    }
}
