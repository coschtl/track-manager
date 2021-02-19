package at.dcosta.tracks.track.share;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.tracks.db.TrackDbAdapter;

public class NetworkReceiver {

    private final TrackSharingListener listener;
    private final int port;
    private AcceptThread acceptThread;

    public NetworkReceiver(int port, TrackSharingListener listener) {
        this.port = port;
        this.listener = listener;
    }

    public void close() {
        if (acceptThread != null && acceptThread.isAlive()) {
            acceptThread.cancel();
        }
    }

    public void startReceiveTracks(TrackDbAdapter trackDbAdapter) {
        acceptThread = new AcceptThread(listener, port, trackDbAdapter);
        acceptThread.start();
    }

    private class AcceptThread extends ReceiverThread {
        private ServerSocket serverSocket;

        public AcceptThread(TrackSharingListener listener, int port, TrackDbAdapter trackDbAdapter) {
            super(listener, trackDbAdapter);
            try {
                serverSocket = new ServerSocket(port);
            } catch (IOException e) {
                System.err.println("Unable to open Server Socket: " + e.toString());
            }
            System.out.println("ServerSocket created");
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                IOUtil.close(serverSocket);
            } catch (Exception e) {
            }
        }

        private void manageConnectedSocket(Socket socket) {
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            int count = 0;
            int err = 0;
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                TrackToShare track;
                System.out.println("start reading objects");
                while ((track = readFromStream(in)) != null) {
                    System.out.println("got object");
                    writeToStream(out, TransferStatus.OK);
                    if (persist(track)) {
                        count++;
                    } else {
                        err++;
                    }
                }
                System.out.println("no more objects");
            } catch (IOException e) {
                System.err.println("Unable to receive track: " + e.toString());
            } finally {
                IOUtil.close(out);
                IOUtil.close(in);
            }
            IOUtil.close(socket);
            // listener.comunicationDone(BluetoothSender.class, count, err);
        }

        @Override
        public void run() {
            Socket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    cancel();
                    break;
                }
            }
        }
    }

}
