package at.dcosta.tracks;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.file.SAFFileLocator;
import at.dcosta.tracks.track.file.legacy.PhotoIdexer;
import at.dcosta.tracks.track.share.BluetoothReceiver;
import at.dcosta.tracks.track.share.BluetoothSender;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.util.Configuration;

public class TrackManager extends AppCompatActivity implements OnGestureListener, OnTouchListener, OnClickListener, OnDateSetListener, OnDoubleTapListener {

    public static final double DATE_AREA_BORDER = 130.0;
    public static final int ID_GOTO_DATE = Menu.FIRST;
    public static final int MENU_PREFS = ID_GOTO_DATE + 1;
    public static final int MENU_ADD_TRACK_FOLDER = MENU_PREFS + 1;
    public static final int MENU_CLEAR_TRACK_FOLDERS = MENU_ADD_TRACK_FOLDER + 1;
    public static final int MENU_PHOTO_FOLDER = MENU_CLEAR_TRACK_FOLDERS + 1;
    public static final int MENU_RECEIVE_BT = MENU_PHOTO_FOLDER + 1;
    public static final int MENU_EXPORT_DB = MENU_RECEIVE_BT + 1;
    public static final int MENU_IMPORT_DB = MENU_EXPORT_DB + 1;
    public static final int MENU_SHOW_MULTI_DAYS = MENU_IMPORT_DB + 1;
    public static final int MENU_RESCAN_PHOTOS = MENU_SHOW_MULTI_DAYS + 1;
    public static final int MENU_RECREATE_STATISTICS = MENU_RESCAN_PHOTOS + 1;
    public static final int MENU_DEMO = 99;
    // public static final int MENU_RECEIVE_BT_TEST = MENU_RECEIVE_BT + 1;
    public static final int ID_LOAD_TRACK = 10;
    public static final int APP_STORAGE_ACCESS_REQUEST_CODE = 100;
    public static final int REQUEST_ACTION_OPEN_TRACK_DOCUMENT_TREE = 101;
    private static final Logger LOGGER = Logger.getLogger(TrackManager.class.getName());
    private static final String DATE_SHOWN = "dateShown";
    private static Context context;
    private final Calendar calendar = Calendar.getInstance();
    private GestureDetector gestureDetector;
    private TrackDbAdapter trackDbAdapter;
    private PropertyDbAdapter propertyDbAdapter;
    private Month<TrackDescriptionNG> month;
    private Configuration config;
    private boolean mustCheckExternalStorageManagerPermission = true;
    private boolean mustCheckTrackDirReadPermission = true;


    public static Context context() {
        return context;
    }

    private String askForWorkingDir() {
        String workingDir = (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ?
                Environment.getExternalStorageDirectory().getPath() :
                getExternalFilesDir(null).getPath()
        )
                + "/trackManager";
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
                System.err.println("WorkingDir-parent '" + workingDir.getParent() + "' exists: " + workingDir.getParentFile().isDirectory());
                wd = askForWorkingDir();
                config.setWorkingDir(wd);
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
        if (propertyDbAdapter != null) {
            trackDbAdapter.close();
        }
        if (trackDbAdapter != null) {
            propertyDbAdapter.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_STORAGE_ACCESS_REQUEST_CODE) {
            LOGGER.info("External storageManager permission RESULT_OK: " + (resultCode == RESULT_OK));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    LOGGER.info("Permission for file access granted");
                }
            } else {
                System.out.println("Permission not needed: API too low");
            }
            if (!assurePermissions()) {
                renderUi();
            }
            return;
        }
        if (requestCode == REQUEST_ACTION_OPEN_TRACK_DOCUMENT_TREE) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                config.addTrackFolder(uri);
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            if (!assurePermissions()) {
                renderUi();
            }
        }
        if (BluetoothSender.REQUEST_ENABLE_BT == requestCode) {
            System.out.println("BT active: " + (resultCode == RESULT_OK));
            return;
        }
        if (ID_LOAD_TRACK == resultCode) {
            renderCalendar();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit_application).setMessage(R.string.are_you_sure_exit).setCancelable(false)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        PackageManager pkMgr = getApplicationContext().getPackageManager();

                        if (pkMgr.checkPermission("android.permission.ACCESS_FINE_LOCATION", "at.dcosta.tracker") == PackageManager.PERMISSION_GRANTED &&
                                pkMgr.checkPermission("android.permission.READ_PHONE_STATE", "at.dcosta.tracker") == PackageManager.PERMISSION_GRANTED) {
                            Intent intent = new Intent(TrackManager.this, TrackerService.class);
                            intent.putExtra("command", TrackerCommand.SHUTDOWN);
                            startService(intent);
                        }
                        if (pkMgr.checkPermission("android.permission.BLUETOOTH_ADMIN", "at.dcosta.tracks") == PackageManager.PERMISSION_GRANTED &&
                                pkMgr.checkPermission("android.permission.BLUETOOTH", "at.dcosta.tracks") == PackageManager.PERMISSION_GRANTED) {
                            BluetoothSender.shutdownBluetoothIfActivatedByApp();
                        }
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
                System.out.println("-------------------> latest time: " + trackDbAdapter.findLatestEndTimeEpochMillis());
                closeDbs();
                intent = new Intent(this, Loader.class);
                intent.putExtra(Loader.KEY_ACTION, Loader.ACTION_LOAD_NEW_TRACKS_AND_PHOTOS);
                startActivityForResult(intent, ID_LOAD_TRACK);
                renderCalendar();
                break;
        }
    }

    private boolean assurePermissions() {
        if (CombatFactory.isLegacy()) {
            return false;
        }
        if (mustCheckExternalStorageManagerPermission) {
            mustCheckExternalStorageManagerPermission = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                Toast.makeText(this, "Please allow access of external storage", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, APP_STORAGE_ACCESS_REQUEST_CODE);
                return true;
            }
        }
        if (mustCheckTrackDirReadPermission) {
            mustCheckTrackDirReadPermission = false;
            if (!foldersValid(config.getTrackFolders())) {
                config.clearTrackFolders();
                askForTrackFolder();
                return true;
            }
        }
        return false;
    }

    private void askForTrackFolder() {
        Toast.makeText(this, "Please select track folder", Toast.LENGTH_LONG).show();
        assureDocumentTreeReadPermission("Android/data", REQUEST_ACTION_OPEN_TRACK_DOCUMENT_TREE);
    }

    private boolean foldersValid(List<Uri> folders) {
        if (folders == null || folders.isEmpty()) {
            return false;
        }
        for (Uri uri : folders) {
            if (uri == null || uri.toString().trim().length() == 0) {
                return false;
            }
            if (!CombatFactory.getFileLocator(this).fileExists(uri.toString(), true)) {
                return false;
            }
        }
        return true;
    }

    private void assureDocumentTreeReadPermission(String startDir, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            StorageManager sm = (StorageManager) getApplicationContext().getSystemService(Context.STORAGE_SERVICE);
            Intent intent = sm.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
            Uri uri = intent.getParcelableExtra(DocumentsContract.EXTRA_INITIAL_URI);
            String scheme = uri.toString();
            scheme = scheme.replace("/root/", "/document/");
            startDir = startDir.replace("/", "%2F");
            scheme += "%3A" + startDir;
            uri = Uri.parse(scheme);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        synchronized (TrackManager.class) {
            if (context == null) {
                context = this;
            }
        }
        super.onCreate(savedInstanceState);
        config = Configuration.getInstance(this);
        if (!assurePermissions()) {
            renderUi();
        }

        // Intent i = new Intent(this, BluetoothTransfer.class);
        // startActivityForResult(i, 1);
        // new BluetoothTransfer().init();
        // NetServer server = new NetServer();
        // server.start();
        // NetClient client = new NetClient();
        // System.out.println(client.searchForServers());
        // server.stop();
    }

    private void renderUi() {
        if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.calendar);
        } else {
            setContentView(R.layout.calendar_landscape);
        }
        findViewById(R.id.but_list).setOnClickListener(this);
        findViewById(R.id.but_search).setOnClickListener(this);
        //findViewById(R.id.but_track_recording).setOnClickListener(this);
        findViewById(R.id.but_reload).setOnClickListener(this);
        gestureDetector = new GestureDetector(this, this);
        gestureDetector.setOnDoubleTapListener(this);
        trackDbAdapter = new TrackDbAdapter(config.getDatabaseHelper(), this);
        propertyDbAdapter = config.getPropertyDbAdapter();
        assureWorkingDirs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        int order = 0;
        // testing
        // menu.add(0, MENU_DEMO, order++, "Androidplot DEMO");
//        menu.add(0, 47110815, order++, "list tracks");
        menu.add(0, ID_GOTO_DATE, order++, R.string.menu_goto_date);
        menu.add(0, MENU_PREFS, order++, R.string.menu_preferencies);
        if (!CombatFactory.isLegacy()) {
            menu.add(0, MENU_ADD_TRACK_FOLDER, order++, R.string.menu_add_track_folder);
            menu.add(0, MENU_CLEAR_TRACK_FOLDERS, order++, R.string.menu_clear_track_folders);
        }
        menu.add(0, MENU_RECEIVE_BT, order++, R.string.menu_receive_tracks);
        menu.add(0, MENU_EXPORT_DB, order++, R.string.menu_export_db);
        menu.add(0, MENU_IMPORT_DB, order++, R.string.menu_import_db);
        menu.add(0, MENU_SHOW_MULTI_DAYS, order++, R.string.menu_show_multi_activity_days);
        if (CombatFactory.isLegacy()) {
            menu.add(0, MENU_RESCAN_PHOTOS, order++, R.string.menu_rescan_phtots);
        }
        menu.add(0, MENU_RECREATE_STATISTICS, order++, R.string.menu_recreate_statistics);
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
        final RelativeLayout progress = findViewById(R.id.progress);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        switch (item.getItemId()) {
//            case 47110815:
//                SAFFileLocator locator = new SAFFileLocator(this);
//                locator.listFiles(config.getTrackFolders().get(0),-1);
//                return true;
            case MENU_DEMO:
                closeDbs();
                intent = new Intent(this, at.dcosta.tracks.graph.XYPlotDemo.class);
                startActivity(intent);
                return true;
            case MENU_PREFS:
                closeDbs();
                // intent = new Intent(this, PropertyList.class);
                IdHolder idHolder = IdHolder.DEFAULT.setConfigurationXmlResource(Configuration.AVAILABLE_PROPS_XML);
                intent = new Intent(this, at.dcosta.android.fw.props.gui.PropertyList.class);
                intent.putExtra(IdHolder.class.getName(), idHolder);
                startActivityForResult(intent, 1);
                return true;
            case MENU_ADD_TRACK_FOLDER:
                askForTrackFolder();
                return true;
            case MENU_CLEAR_TRACK_FOLDERS:
                config.clearTrackFolders();
                return true;
            case ID_GOTO_DATE:
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
                progress.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Calendar cal = Calendar.getInstance();
                            String prefix = "exportedDatabase";
                            String suffix = ".dat";
                            String file = config.getWorkingDir() + "/" + prefix + new SimpleDateFormat("_yyyyMMdd_HHmm").format(new Date()) + suffix;
                            backupIO.backup(file, progressBar);
                            File aktFile = new File(config.getWorkingDir() + "/" + prefix + suffix);
                            if (aktFile.exists()) {
                                aktFile.delete();
                            }
                            try (InputStream in = new BufferedInputStream(new FileInputStream(file));
                                 OutputStream out = new BufferedOutputStream(new FileOutputStream(aktFile))) {
                                byte[] buffer = new byte[1024];
                                int lengthRead;
                                while ((lengthRead = in.read(buffer)) > 0) {
                                    out.write(buffer, 0, lengthRead);
                                    out.flush();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(TrackManager.this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              progress.setVisibility(View.GONE);
                                          }
                                      }
                        );
                    }
                }).start();
                ssa.close();
                return true;
            case MENU_IMPORT_DB:
                ssa = new SavedSearchesDbAdapter(config.getDatabaseHelper(), this);
                backupIO = new BackupIO(trackDbAdapter, propertyDbAdapter, ssa);
                progress.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    try {
                        backupIO.restore(config.getWorkingDir() + "/exportedDatabase.dat", TrackManager.this, progressBar);
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(TrackManager.this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );

                    }
                    runOnUiThread(() -> progress.setVisibility(View.GONE)
                    );
                }).start();
                ssa.close();
                return true;
            case MENU_SHOW_MULTI_DAYS:
                intent = new Intent(TrackManager.this, TrackList.class);
                intent.putExtra(TrackSearch.MULTIACTIVITY_DAYS_ONLY, true);
                intent.putExtra(TrackList.KEY_DATE, 1L);
                startActivity(intent);
                return true;
            case MENU_RESCAN_PHOTOS:
                closeDbs();
                PhotoIdexer.clear();
                intent = new Intent(this, Loader.class);
                intent.putExtra(Loader.KEY_ACTION, Loader.ACTION_RESCAN_PHOTOS);
                startActivityForResult(intent, ID_LOAD_TRACK);
                renderCalendar();
                return true;
            case MENU_RECREATE_STATISTICS:
                progress.setVisibility(View.VISIBLE);
                final float increment = (100f / (float) trackDbAdapter.countAllEntries(false));
                new Thread(new Runnable() {
                    public void run() {
                        Iterator<TrackDescriptionNG> it = trackDbAdapter.fetchAllEntries(false);
                        ActivityFactory activityFactory = new ActivityFactory(TrackManager.this);
                        float totalInc = 0f;
                        while (it.hasNext()) {
                            TrackDescriptionNG td = it.next();
                            String icon = td.getActivity() == null ? null : td.getActivity().getIcon();
                            TrackEdit.updateTrack(TrackManager.this, td, icon, activityFactory);
                            trackDbAdapter.updateEntry(td);
                            totalInc += increment;
                            progressBar.setProgress((int) totalInc);
                        }
                        runOnUiThread(new Runnable() {
                                          @Override
                                          public void run() {
                                              progress.setVisibility(View.GONE);
                                          }
                                      }
                        );
                    }
                }).start();
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
        month = new Month<TrackDescriptionNG>(calendar);

        TextView monthView = findViewById(R.id.month);
        monthView.setText(month.getName());
        GridView daysView = findViewById(R.id.days);
        Iterator<TrackDescriptionNG> iter = trackDbAdapter.findEntries(DateUtil.getStartOfMonth(month.getMonth(), month.getYear()),
                DateUtil.getEndOfMonth(month.getMonth(), month.getYear()));
        Map<Date, List<TrackDescriptionNG>> m = new HashMap<Date, List<TrackDescriptionNG>>();
        while (iter.hasNext()) {
            TrackDescriptionNG descr = iter.next();
            Date trackDay = DateUtil.getDay(descr.getStartTime());
            List<TrackDescriptionNG> l;
            if (m.containsKey(trackDay)) {
                l = m.get(trackDay);
            } else {
                l = new ArrayList<TrackDescriptionNG>();
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
                Day<TrackDescriptionNG> day = month.getDay(position);
                if (day.hasContent()) {
                    Intent intent = new Intent(TrackManager.this, TrackList.class);
                    intent.putExtra(TrackList.KEY_DATE, DateUtil.getDay(day.getDay(), day.getMonth().getMonth(), day.getMonth().getYear()).getTime());
                    startActivity(intent);
                }
            }
        });
    }

    private void showDateChooser() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, android.R.style.Theme_Holo_Dialog, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}