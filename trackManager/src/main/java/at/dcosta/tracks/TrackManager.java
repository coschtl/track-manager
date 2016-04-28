package at.dcosta.tracks;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.TextView;
import at.dcosta.android.fw.DateUtil;
import at.dcosta.android.fw.gui.calendar.Day;
import at.dcosta.android.fw.gui.calendar.Month;
import at.dcosta.android.fw.props.PropertyDbAdapter;
import at.dcosta.android.fw.props.gui.IdHolder;
import at.dcosta.tracker.TrackerCommand;
import at.dcosta.tracker.TrackerService;
import at.dcosta.tracks.backup.BackupIO;
import at.dcosta.tracks.db.SavedSearchesDbAdapter;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.TrackDescription;
import at.dcosta.tracks.track.share.BluetoothReceiver;
import at.dcosta.tracks.track.share.BluetoothSender;
import at.dcosta.tracks.util.Configuration;

public class TrackManager extends Activity implements OnGestureListener, OnTouchListener, OnClickListener, OnDateSetListener, OnDoubleTapListener {

	private static final String DATE_SHOWN = "dateShown";
	public static final double DATE_AREA_BORDER = 130.0;
	public static final int MENU_GOTO_MONTH = Menu.FIRST;
	public static final int MENU_PREFS = MENU_GOTO_MONTH + 1;
	public static final int MENU_RECEIVE_BT = MENU_PREFS + 1;
	public static final int MENU_EXPORT_DB = MENU_RECEIVE_BT + 1;
	public static final int MENU_IMPORT_DB = MENU_EXPORT_DB + 1;
	// public static final int MENU_RECEIVE_BT_TEST = MENU_RECEIVE_BT + 1;
	public static final int ID_LOAD_TRACK = 10;
	public static final int ID_GOTO_DATE = 11;

	private final Calendar calendar = Calendar.getInstance();
	private GestureDetector gestureDetector;
	private TrackDbAdapter trackDbAdapter;
	private PropertyDbAdapter propertyDbAdapter;
	private Month<TrackDescription> month;
	private Configuration config;

	private String askForWorkingDir() {
		String workingDir = Environment.getExternalStorageDirectory().getPath() + "/trackManager";
		config.setWorkingDir(workingDir);
		return workingDir;
	}

	private void assureWorkingDirs() {
		String wd = config.getWorkingDir();
		if (wd == null) {
			wd = askForWorkingDir();
		}
		File workingDir = new File(wd);
		if (!workingDir.exists() || !workingDir.isDirectory()) {
			boolean success = workingDir.mkdirs();
			if (!success) {
				System.err.println("WorkingDir '" + wd + "' does not exist and can not get created!");
				assureWorkingDirs();
			} else {
				config.setWorkingDir(wd);
			}
		}
		config.setWorkingDir(workingDir.getAbsolutePath());
		if (!config.getRecordedTracksDir().exists()) {
			config.getRecordedTracksDir().mkdir();
		}
		if (!config.getCopiedTracksDir().exists()) {
			config.getCopiedTracksDir().mkdir();
		}
	}

	private void closeDbs() {
		trackDbAdapter.close();
		propertyDbAdapter.close();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (BluetoothSender.REQUEST_ENABLE_BT == requestCode) {
			System.out.println("BT active: " + (resultCode == RESULT_OK));
		}
		if (ID_LOAD_TRACK == resultCode) {
			renderCalendar();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.exit_application).setMessage(R.string.are_you_sure).setCancelable(false)
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						Intent intent = new Intent(TrackManager.this, TrackerService.class);
						intent.putExtra("command", TrackerCommand.SHUTDOWN);
						startService(intent);
						BluetoothSender.shutdownBluetoothIfActivatedByApp();
						TrackManager.this.finish();
						System.exit(0);
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.create().show();
	}

	@Override
	public void onClick(View view) {
		Intent intent;
		switch (view.getId()) {
			case R.id.but_list:
				intent = new Intent(TrackManager.this, TrackList.class);
				intent.putExtra(TrackList.KEY_DATE, DateUtil.getDay(1, month.getMonth(), month.getYear()).getTime());
				intent.putExtra(TrackList.KEY_MENU_ID, R.id.but_month);
				startActivity(intent);
				break;
			case R.id.but_search:
				closeDbs();
				intent = new Intent(this, TrackList.class);
				intent.putExtra(TrackList.KEY_USER_SEARCH, true);
				startActivityForResult(intent, 1);
				break;
			case R.id.but_track_recording:
				closeDbs();
				intent = new Intent(this, config.getRecordTrackClass());
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivityIfNeeded(intent, R.id.but_track_recording);
				break;
			case R.id.but_reload:
				closeDbs();
				intent = new Intent(this, Loader.class);
				intent.putExtra(Loader.KEY_ACTION, Loader.ACTION_LOAD_NEW_TRACKS_AND_PHOTOS);
				startActivityForResult(intent, ID_LOAD_TRACK);
				renderCalendar();
				break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		config = Configuration.getInstance(this);
		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
			setContentView(R.layout.calendar);
		} else {
			setContentView(R.layout.calendar_landscape);
		}
		findViewById(R.id.but_list).setOnClickListener(this);
		findViewById(R.id.but_search).setOnClickListener(this);
		findViewById(R.id.but_track_recording).setOnClickListener(this);
		findViewById(R.id.but_reload).setOnClickListener(this);
		gestureDetector = new GestureDetector(this, this);
		gestureDetector.setOnDoubleTapListener(this);
		trackDbAdapter = new TrackDbAdapter(config.getDatabaseHelper(), this);
		propertyDbAdapter = config.getPropertyDbAdapter();
		assureWorkingDirs();
		// System.out.println("activating BT: ");

		// Intent i = new Intent(this, BluetoothTransfer.class);
		// startActivityForResult(i, 1);
		// new BluetoothTransfer().init();
		// NetServer server = new NetServer();
		// server.start();
		// NetClient client = new NetClient();
		// System.out.println(client.searchForServers());
		// server.stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_GOTO_MONTH, 0, R.string.menu_goto_date);
		menu.add(0, MENU_PREFS, 1, R.string.menu_preferencies);
		menu.add(0, MENU_RECEIVE_BT, 2, R.string.menu_receive_tracks);
		menu.add(0, MENU_EXPORT_DB, 3, R.string.menu_export_db);
		menu.add(0, MENU_IMPORT_DB, 4, R.string.menu_import_db);
		// menu.add(0, MENU_RECEIVE_BT_TEST, 2, "TEST");
		return true;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		String date = new StringBuilder().append(dayOfMonth).append(".").append(monthOfYear + 1).append(".").append(year).toString();
		try {
			calendar.setTime(config.getDateFormat().parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		renderCalendar();
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (e.getRawY() < DATE_AREA_BORDER) {
			calendar.setTime(new Date());
			renderCalendar();
			return true;
		}
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (Math.abs(velocityY) > config.getWipeSensitivity()) {
			int month;
			if (velocityY > 0) {
				month = -1;
			} else {
				month = 1;
			}
			calendar.add(Calendar.MONTH, month);
			renderCalendar();
			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		if (e.getRawY() < DATE_AREA_BORDER) {
			showDateChooser();
		}
		// not needed
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		SavedSearchesDbAdapter ssa;
		BackupIO backupIO;
		switch (item.getItemId()) {
			case MENU_PREFS:
				closeDbs();
				// intent = new Intent(this, PropertyList.class);
				IdHolder idHolder = IdHolder.DEFAULT.setConfigurationXmlResource(Configuration.AVAILABLE_PROPS_XML);
				intent = new Intent(this, at.dcosta.android.fw.props.gui.PropertyList.class);
				intent.putExtra(IdHolder.class.getName(), idHolder);
				startActivityForResult(intent, 1);
				return true;
			case MENU_GOTO_MONTH:
				showDateChooser();
				return true;
			case MENU_RECEIVE_BT:
				intent = new Intent(this, BluetoothReceiver.class);
				intent.putExtra("mode", "receive");
				startActivityForResult(intent, 1);
				return true;
				// case MENU_RECEIVE_BT_TEST:
				// getIPAddress();
				// new NetworkReceiver(10856, null).startReceiveTracks(trackDbAdapter);
				// // new ShareTest(trackDbAdapter).doTest();
				// return true;
			case MENU_EXPORT_DB:
				ssa = new SavedSearchesDbAdapter(config.getDatabaseHelper(), this);
				backupIO = new BackupIO(trackDbAdapter, propertyDbAdapter, ssa);
				try {
					backupIO.backup(config.getWorkingDir() + "/exportedDatabase.dat");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ssa.close();
				return true;
			case MENU_IMPORT_DB:
				ssa = new SavedSearchesDbAdapter(config.getDatabaseHelper(), this);
				backupIO = new BackupIO(trackDbAdapter, propertyDbAdapter, ssa);
				try {
					backupIO.restore(config.getWorkingDir() + "/exportedDatabase.dat");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ssa.close();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeDbs();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Date savedDate = (Date) savedInstanceState.get(DATE_SHOWN);
		if (savedDate != null) {
			calendar.setTime(savedDate);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(DATE_SHOWN, calendar.getTime());
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// not needed
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent me) {
		return gestureDetector.onTouchEvent(me);
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		return gestureDetector.onTouchEvent(me);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			renderCalendar();
		}
	}

	private void printIPAddress() {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress() && InetAddressUtils.isIPv4Address(addr.getHostAddress())) {
						System.out.println(addr.getHostAddress());
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void renderCalendar() {
		month = new Month<TrackDescription>(calendar);

		TextView monthView = (TextView) findViewById(R.id.month);
		monthView.setText(month.getName());
		GridView daysView = (GridView) findViewById(R.id.days);
		Iterator<TrackDescription> iter = trackDbAdapter.findEntries(DateUtil.getStartOfMonth(month.getMonth(), month.getYear()),
				DateUtil.getEndOfMonth(month.getMonth(), month.getYear()));
		Map<Date, List<TrackDescription>> m = new HashMap<Date, List<TrackDescription>>();
		while (iter.hasNext()) {
			TrackDescription descr = iter.next();
			Date trackDay = DateUtil.getDay(descr.getStartTime());
			List<TrackDescription> l;
			if (m.containsKey(trackDay)) {
				l = m.get(trackDay);
			} else {
				l = new ArrayList<TrackDescription>();
			}
			l.add(descr);
			m.put(trackDay, l);
		}
		daysView.setAdapter(new CalendarAdapter(daysView.getContext(), month, m));
		daysView.setOnTouchListener(this);
		daysView.requestLayout();

		daysView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Day<TrackDescription> day = month.getDay(position);
				if (day.hasContent()) {
					Intent intent = new Intent(TrackManager.this, TrackList.class);
					intent.putExtra(TrackList.KEY_DATE, DateUtil.getDay(day.getDay(), day.getMonth().getMonth(), day.getMonth().getYear()).getTime());
					startActivity(intent);
				}
			}
		});
	}

	private void showDateChooser() {
		DatePickerDialog datePickerDialog = new DatePickerDialog(this, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
		datePickerDialog.show();
	}
}