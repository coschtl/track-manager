package at.dcosta.tracks.track.share;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import at.dcosta.android.fw.Alarm;
import at.dcosta.android.fw.Alarm.AlarmReceiver;
import at.dcosta.android.fw.IOUtil;
import at.dcosta.android.fw.props.Property;
import at.dcosta.tracks.R;
import at.dcosta.tracks.combat.SAFContent;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.file.ParsingException;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.TrackIO;

public class BluetoothSender extends AbstractBluetoothTransfer implements OnClickListener, TrackSharingListener, AlarmReceiver {

    private static final String PROPERTY_LAST_BLUETOOTH_PARTNER = "lastBluetoothPartner";
    private static final String PROPERTY_BT_SEND_TIMEOUT = "bluetoothSendTimeoutSeconds";
    private final Configuration config = Configuration.getInstance();
    private List<BluetoothDevice> devices;
    private RadioGroup radioButtonGroup;
    private String lastBluetoothPartner;
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                addDeviceToList(device);
            }
        }
    };
    private TextView done, sendingText;
    private Button sendButton;
    private RelativeLayout sending;
    private ProgressBar progressBar;
    private Alarm alarm;
    private SendingThread sendingThread;

    private void addDeviceToList(BluetoothDevice device) {
        System.out.println(device.getName() + ": " + device.getAddress());
        if (!devices.contains(device)) {
            RadioButton button = createRadioButton(device.getName(), devices.size());
            if (lastBluetoothPartner != null && lastBluetoothPartner.equals(device.getName())) {
                System.out.println("same as last time: " + device.getName());
                button.setChecked(true);
            }
            devices.add(device);
            radioButtonGroup.addView(button);
        }
    }

    @Override
    public void alarm() {
        if (sendingThread != null && sendingThread.isAlive()) {
            sendingThread.abort();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                sendingText.setVisibility(View.GONE);
                done.setText(R.string.label_sending_timeout);
                done.setTextColor(Color.RED);
                done.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void btReady() {
        sendButton = findViewById(R.id.send_buton);
        sendingText = findViewById(R.id.sending_text);
        done = findViewById(R.id.done);
        done.setVisibility(View.GONE);
        sendButton.setOnClickListener(this);

        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            addDeviceToList(device);
        }
        RadioButton more = createRadioButton(getString(R.string.label_discover_devices), Integer.MAX_VALUE);
        more.setOnClickListener(this);
        radioButtonGroup.addView(more);
    }

    private void cancelDiscovery() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            // ignore
        }
        try {
            if (bluetoothAdapter != null) {
                bluetoothAdapter.cancelDiscovery();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    public void comunicationDone(boolean successfull) {
        alarm.stop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                done.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void comunicationStarts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                done.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private RadioButton createRadioButton(String text, int id) {
        RadioButton button = new RadioButton(this);
        button.setText(text);
        button.setId(id);
        return button;
    }

    private void discoverDevices() {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter); // Don't forget to unregister during onDestroy
        bluetoothAdapter.startDiscovery();
    }

    private long getSendTimeout() {
        Property sendTimeout = config.getSingleValueDbProperty(PROPERTY_BT_SEND_TIMEOUT);
        if (sendTimeout.getValue() == null) {
            return -1;
        }
        try {
            return Long.parseLong(sendTimeout.getValue()) * 1000L;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == Integer.MAX_VALUE) {
            radioButtonGroup.removeView(v);
            discoverDevices();
        } else if (v instanceof Button) {
            BluetoothDevice selected = devices.get(radioButtonGroup.getCheckedRadioButtonId());
            config.updateSingleValueProperty(PROPERTY_LAST_BLUETOOTH_PARTNER, selected.getName());
            System.out.println("sending starts...");
            long rowId = getIntent().getLongExtra(TrackDescriptionNG.KEY_ID, -1);
            TrackDescriptionNG trackDescription = trackDbAdapter.fetchEntry(rowId);
            try {
                TrackToShare track = new TrackToShare(trackDescription, TrackIO.loadTrack(new SAFContent(this, trackDescription.getPathUri(), trackDescription.getStartTime())));
                sendingThread = new SendingThread(this, selected, track);
                alarm = new Alarm(this);
                sendButton.setEnabled(false);
                alarm.start(getSendTimeout());
                sendingThread.start();
                sending.setVisibility(View.VISIBLE);
            } catch (ParsingException e) {
                System.err.println("Can not load Track to send: " + e.toString());
            }
        }
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastBluetoothPartner = config.getSingleValueDbProperty(PROPERTY_LAST_BLUETOOTH_PARTNER).getValue();

        System.out.println("lastBluetoothPartner: " + lastBluetoothPartner);
        setContentView(R.layout.bluetooth_sender);

        done = findViewById(R.id.done);
        done.setVisibility(View.GONE);

        sending = findViewById(R.id.sending);
        sending.setVisibility(View.GONE);
        progressBar = findViewById(R.id.send_progressBar);
        progressBar.setVisibility(View.GONE);

        devices = new ArrayList<BluetoothDevice>();
        radioButtonGroup = findViewById(R.id.devices);
        initBluetooth();
    }

    @Override
    protected void onDestroy() {
        cancelDiscovery();
        super.onDestroy();
    }

    private class SendingThread extends SenderThread {
        private final BluetoothSocket socket;

        public SendingThread(TrackSharingListener listener, BluetoothDevice device, TrackToShare... tracks) {
            super(listener, tracks);
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // BT_UUID is the app's UUID string, also used by the server code
                // Method m = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
                // tmp = (BluetoothSocket) m.invoke(device, 1);
                tmp = device.createRfcommSocketToServiceRecord(BluetoothSender.BT_UUID);
            } catch (IOException e) {
                System.err.println("CAN NOT open BT-Socket: " + e.toString());
                e.printStackTrace();
            }
            socket = tmp;
        }

        public void abort() {
            IOUtil.close(socket);
        }

        @Override
        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                socket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                System.err.println("Unable to connect: " + connectException.toString());
                IOUtil.close(socket);
                return;
            }

            ObjectOutputStream out = null;
            ObjectInputStream in = null;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());
                System.out.println("start writing");
                for (TrackToShare track : tracks) {
                    System.out.println(" writing next");
                    listener.comunicationStarts();
                    writeToStream(out, track);
                    TransferStatus status = readFromStream(in);
                    System.out.println("got status");
                    if (status == TransferStatus.RETRANSFER) {
                        writeToStream(out, track);
                        status = readFromStream(in);
                    }
                    listener.comunicationDone(status == TransferStatus.OK);
                }
            } catch (IOException e) {
                System.err.println("Unable to transfer track: " + e.toString());
            } finally {
                IOUtil.close(out);
                IOUtil.close(in);
                IOUtil.close(socket);
            }

        }
    }

}
