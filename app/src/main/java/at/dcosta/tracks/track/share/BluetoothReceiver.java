package at.dcosta.tracks.track.share;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.tracks.R;
import at.dcosta.tracks.db.TrackDbAdapter;

public class BluetoothReceiver extends AbstractBluetoothTransfer implements TrackSharingListener {

    private AcceptThread receivingThread;
    private int receiverCount;
    private TextView trackCountView;

    @Override
    protected void btReady() {
        startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        receivingThread = new AcceptThread(this, trackDbAdapter);
        receivingThread.start();
    }

    @Override
    public void comunicationDone(boolean successful) {
        receiverCount++;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                trackCountView.setText(Integer.toString(receiverCount));
            }
        });
    }

    @Override
    public void comunicationStarts() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tracksharing_receiver);
        trackCountView = (TextView) findViewById(R.id.track_count);
        initBluetooth();
    }

    @Override
    protected void onDestroy() {
        if (receivingThread != null) {
            receivingThread.cancel();
        }
        super.onDestroy();
    }

    private class AcceptThread extends ReceiverThread {
        private final BluetoothServerSocket serverSocket;

        public AcceptThread(TrackSharingListener listener, TrackDbAdapter trackDbAdapter) {
            super(listener, trackDbAdapter);
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(BluetoothSender.BT_NAME, BluetoothSender.BT_UUID);
            } catch (IOException e) {
            }
            System.out.println("ServerSocket created");
            serverSocket = tmp;
        }

        /**
         * Will cancel the listening socket, and cause the thread to finish
         */
        public void cancel() {
            try {
                serverSocket.close();
            } catch (Exception e) {
            }
        }

        private void manageConnectedSocket(BluetoothSocket socket) {
            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                TrackToShare track;
                System.out.println("start reading objects");
                listener.comunicationStarts();
                while ((track = readFromStream(in)) != null) {
                    listener.comunicationDone(true);
                    writeToStream(out, TransferStatus.OK);
                    persist(track);
                }
                System.out.println("no more objects");
            } catch (IOException e) {
                System.err.println("Unable to receive track: " + e.toString());
            } finally {
                IOUtil.close(out);
                IOUtil.close(in);
            }
            IOUtil.close(socket);
        }

        @Override
        public void run() {
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    final BluetoothSocket socket = serverSocket.accept();
                    // If a connection was accepted
                    if (socket != null) {
                        // Do work to manage the connection (in a separate thread)
                        new Thread() {
                            @Override
                            public void run() {
                                manageConnectedSocket(socket);
                            }
                        }.start();

                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

}
