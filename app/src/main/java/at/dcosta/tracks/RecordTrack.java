package at.dcosta.tracks;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.ref.WeakReference;
import java.util.List;

import at.dcosta.tracker.TrackerCommand;
import at.dcosta.tracker.TrackerService;
import at.dcosta.tracker.TrackerStatus;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.PermissionUtil;
import at.dcosta.tracks.util.PointUtil;
import at.dcosta.tracks.util.TrackIO;

public class RecordTrack extends Activity implements OnMapReadyCallback, OnClickListener, OnLongClickListener, LocationListener {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private final Messenger messenger = new Messenger(new IncomingHandler(this));
    private TrackOverlay trackOverlay;
    private MapView mapView;
    private ImageButton playStopButton, sendButton, gpsButton;
    private LocationManager lm;
    private Location lastLocation;
    private ServerProperties serverProperties;
    private Messenger messengerService = null;
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            messengerService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                Message msg = Message.obtain(null, TrackerService.MSG_GET_TRACK, this.hashCode(), 0);
                msg.replyTo = messenger;
                messengerService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            System.out.println("remote_service_connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            messengerService = null;

            // As part of the sample, tell the user what happened.
            System.out.println("remote_service_disconnected");
        }
    };
    private GoogleMap map;
    private String trackName;
    private AskTrackName selectTrackName;
    private boolean doCenter = false;
    private boolean neverCentered;

    private void askForTrackName() {
        selectTrackName = new AskTrackName(RecordTrack.this);
        selectTrackName.setOnClickListener(this);
        selectTrackName.show();
    }

    private void evaluateServerProperties() {
        Configuration cfg = Configuration.getInstance();
        serverProperties = cfg.getServerProperties();

        if (!serverProperties.isValid()) {
            warnIllegalServerConfig();
        }
    }

    private void initButtons() {
        playStopButton = findViewById(R.id.but_play_stop);
        playStopButton.setOnClickListener(this);
        setPlayStopIcon();

        sendButton = findViewById(R.id.but_send);
        sendButton.setOnClickListener(this);
        setSendIcon();

        gpsButton = findViewById(R.id.but_gps);
        gpsButton.setOnClickListener(this);
        gpsButton.setOnLongClickListener(this);
        setGpsIcon();
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, TrackerService.class);
        TrackerCommand cmd = null;
        switch (view.getId()) {
            case R.id.but_gps:
                if (TrackerStatus.connected) {
                    if (!TrackerStatus.recording && !TrackerStatus.sending) {
                        cmd = TrackerCommand.SHUTDOWN;
                        lm.removeUpdates(this);
                    } else {
                        return;
                    }
                } else {
                    cmd = TrackerCommand.CONNECT;
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        PermissionUtil.showLocationPermissionMissingWarning(this);
                        return;
                    }
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 20, this);
                }
                TrackerStatus.connected = !TrackerStatus.connected;
                setGpsIcon();
                break;
            case R.id.but_play_stop:
                if (TrackerStatus.recording) {
                    cmd = TrackerCommand.STOP_TRACK;
                    TrackerStatus.recording = !TrackerStatus.recording;
                    setPlayStopIcon();
                    setGpsIcon();
                } else {
                    askForTrackName();
                    return;
                }
                break;
            case R.id.but_send:
                if (!serverProperties.isValid()) {
                    warnIllegalServerConfig();
                    return;
                }
                if (TrackerStatus.sending) {
                    cmd = new TrackerCommand.StopSending(trackName, serverProperties.getServerCloseAddress());
                } else {
                    cmd = new TrackerCommand.SendTrack(trackName, serverProperties.getServerSendAddress());
                }
                TrackerStatus.sending = !TrackerStatus.sending;
                setSendIcon();
                break;
            case R.id.cancel: {
                selectTrackName.dismiss();
                return;
            }
            case R.id.confirm:
                trackName = selectTrackName.getTrackName();
                if (TrackIO.getRecordedTrackFile(trackName).exists()) {
                    Toast.makeText(this, R.string.track_name_exists, Toast.LENGTH_LONG).show();
                    return;
                }
                selectTrackName.dismiss();
                cmd = new TrackerCommand.StartTrack(trackName);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 20, this);
                TrackerStatus.recording = true;
                TrackerStatus.connected = true;
                setPlayStopIcon();
                setGpsIcon();
                break;
            default:
                System.out.println("unknown botton: " + view.getId() + ": " + view);
        }
        intent.putExtra("command", cmd);
        startService(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.record_track);
        initButtons();

        neverCentered = true;
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            warnNoGps();
        }

        Bundle mapViewBundle = null;
        if (bundle != null) {
            mapViewBundle = bundle.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView = findViewById(R.id.map);
        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);

//        mapView = (MapFragment) findViewById(R.id.mapview);
//        mapView.setSatellite(true);
//        mapView.setBuiltInZoomControls(true);
//        MapController controller = mapView.getController();
//        controller.setZoom(15);

        if (trackOverlay == null) {
            trackOverlay = new TrackOverlay(true);
        }
//        mapView.getOverlays().add(trackOverlay);
//        mapView.invalidate();
        evaluateServerProperties();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_SATELLITE)
                .compassEnabled(false)
                .rotateGesturesEnabled(false)
                .tiltGesturesEnabled(false);
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
        trackOverlay.getPoints().forEach(p -> polylineOptions.add(p));
        Polyline polyline = map.addPolyline(polylineOptions);
        polyline.setWidth(5);
        polyline.setColor(trackOverlay.getTrackColor());
        polyline.setJointType(JointType.ROUND);
        this.map = map;
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (lastLocation == null || location.distanceTo(lastLocation) > 20) {
            // System.out.println("accept: " + location);
            LatLng geoPoint = PointUtil.createGeoPoint(location);
            trackOverlay.addPoint(geoPoint);
            if (doCenter || neverCentered) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(geoPoint, 5f));
                mapView.invalidate();
                neverCentered = false;
            }
            lastLocation = location;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        doCenter = !doCenter;
        return true;
    }

    @Override
    protected void onPause() {
        unbindService(mConnection);
        lm.removeUpdates(this);
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onProviderDisabled(String provider) {
        warnNoGps();
    }

    @Override
    public void onProviderEnabled(String provider) {
        // not needed
    }

    @Override
    protected void onResume() {
        System.out.println("onResume");

        // register at the service
        // the service will send us a message continuing the temporary track when it is ready
        bindService(new Intent(RecordTrack.this, TrackerService.class), mConnection, Context.BIND_AUTO_CREATE);
        neverCentered = true;
        if (TrackerStatus.connected) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtil.showLocationPermissionMissingWarning(this);
                return;
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 20, this);
        }
        setGpsIcon();
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    private void setGpsIcon() {
        gpsButton.setImageResource(TrackerStatus.connected ? R.mipmap.satellite_active : R.mipmap.satellite);
        gpsButton.invalidate();
    }

    private void setPlayStopIcon() {
        playStopButton.setImageResource(TrackerStatus.recording ? R.mipmap.stop : R.mipmap.play);
        playStopButton.invalidate();
    }

    private void setSendIcon() {
        sendButton.setImageResource(TrackerStatus.sending ? R.mipmap.wifi_active : R.mipmap.wifi);
        sendButton.invalidate();
    }

    private void warnIllegalServerConfig() {
        Toast.makeText(this, "Warning: trackingServer, serverSavePath, or serverClosePath not set inside Properties!", Toast.LENGTH_LONG).show();
    }

    private void warnNoGps() {
        Toast.makeText(this, R.string.activate_gps, Toast.LENGTH_LONG).show();
    }

    private static class IncomingHandler extends Handler {

        WeakReference<RecordTrack> reference;

        public IncomingHandler(RecordTrack recordTrack) {
            reference = new WeakReference<RecordTrack>(recordTrack);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TrackerService.MSG_GET_TRACK) {
                final RecordTrack recordTrack = reference.get();
                synchronized (recordTrack) {
                    recordTrack.trackOverlay.clearPoints();
                    for (Location location : (List<Location>) msg.obj) {
                        recordTrack.trackOverlay.addPoint(PointUtil.createGeoPoint(location));
                    }
                }
            } else {
                super.handleMessage(msg);
            }
        }
    }

}
