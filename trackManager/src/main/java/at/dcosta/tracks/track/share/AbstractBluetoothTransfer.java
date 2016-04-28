package at.dcosta.tracks.track.share;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import at.dcosta.tracks.R;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.util.Configuration;

public abstract class AbstractBluetoothTransfer extends Activity implements TrackSharingListener {

	public static final String BT_NAME = "TrackManager";
	public static final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final int REQUEST_ENABLE_BT = 2583;
	public static final int REQUEST_DISABLE_BT = 2584;
	public static final int TRACK_RECEIVED_OK = 65;

	public static void shutdownBluetoothIfActivatedByApp() {
		Configuration config = Configuration.getInstance();
		if (config.getSingleValueDbProperty("btActivatedByApp").getBooleanValue(false)) {
			config.updateSingleValueProperty("btActivatedByApp", "false");
			BluetoothAdapter.getDefaultAdapter().disable();
		}
	}

	protected BluetoothAdapter bluetoothAdapter;
	protected SQLiteOpenHelper databaseHelper;
	protected TrackDbAdapter trackDbAdapter;

	protected abstract void btReady();

	protected void initBluetooth() {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Toast.makeText(this, R.string.bluetooth_not_avaliable, Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		if (bluetoothAdapter.isEnabled()) {
			btReady();
		} else {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK) {
			Toast.makeText(this, R.string.bluetooth_not_allowed, Toast.LENGTH_LONG).show();
			finish();
		} else {
			Configuration.getInstance().updateSingleValueProperty("btActivatedByApp", "true");
			btReady();
		}
	}

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		databaseHelper = Configuration.getInstance(this).getDatabaseHelper();
		trackDbAdapter = new TrackDbAdapter(databaseHelper, this);
	}

	@Override
	protected void onPause() {
		trackDbAdapter.close();
		super.onPause();
	}
}
