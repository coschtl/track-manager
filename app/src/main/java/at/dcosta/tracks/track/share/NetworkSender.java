package at.dcosta.tracks.track.share;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import at.dcosta.android.fw.IOUtil;

public class NetworkSender {

    private final TrackSharingListener listener;
    private final String targetIp;
    private final int port;

    public NetworkSender(String targetIp, int port, TrackSharingListener listener) {
        this.targetIp = targetIp;
        this.port = port;
        this.listener = listener;
    }

    public void send(TrackToShare... data) {
        SendingThread thread = new SendingThread(listener, targetIp, port, data);
        thread.start();
        System.out.println("sender Thread started");
    }

    private static class SendingThread extends SenderThread {
        private final String targetIp;
        private final int port;
        private Socket socket;

        public SendingThread(TrackSharingListener listener, String targetIp, int port, TrackToShare... tracks) {
            super(listener, tracks);
            this.targetIp = targetIp;
            this.port = port;
        }

        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            IOUtil.close(socket);
        }

        @Override
        public void run() {
            try {
                socket = new Socket(InetAddress.getByName(targetIp), port);
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                System.err.println("Unable to connect: " + connectException.toString());
                cancel();
                return;
            }

            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            int count = 0;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
                System.out.println("start writing");
                for (TrackToShare track : tracks) {
                    System.out.println(" writing next");
                    writeToStream(out, track);
                    TransferStatus status = readFromStream(in);
                    System.out.println("got status");
                    if (status == TransferStatus.RETRANSFER) {
                        writeToStream(out, track);
                        status = readFromStream(in);
                    }
                    if (status == TransferStatus.OK) {
                        count++;
                    }
                }
            } catch (IOException e) {
                System.err.println("Unable to transfer track: " + e.toString());
            } finally {
                IOUtil.close(out);
                IOUtil.close(in);
            }
            cancel();
            // listener.comunicationDone(NetworkSender.class, count, tracks.length - count);
        }

    }

}
