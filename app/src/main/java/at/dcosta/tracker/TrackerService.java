package at.dcosta.tracker;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
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
import android.telephony.TelephonyManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import at.dcosta.tracker.TrackerCommand.ResumeTrack;
import at.dcosta.tracker.TrackerCommand.SendTrack;
import at.dcosta.tracker.TrackerCommand.StartTrack;
import at.dcosta.tracker.TrackerCommand.StopSending;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.file.TmgrReader;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.PermissionUtil;
import at.dcosta.tracks.util.TrackIO;

public class TrackerService extends Service implements LocationListener {

	public static final int MSG_GET_TRACK = 1;
	private static final long SAVE_INTERVAL_MILLIS = 1000L * 60 * 5;
	private static final int SAVE_INTERVAL_LENGTH = 60;
	private final Messenger messenger = new Messenger(new IncomingHandler(this));
	private List<Point> recordedTrack;
	private List<Location> trackOnMap;
	private String filename;
	private LocationManager lm;
	private GpsStatus gpsStatus = null;
	private long minUpdateTime;
	private float minDistance;
	private String deviceId;
	private LocationTransmission transmission;
	private long lastTrackSaveTime;

	// @Override
	// public IBinder onBind(Intent intent) {
	// // We don't provide binding, so return null
	// return null;
	// }
	private long lastTrackSaveLength;

	private void connectIfNecessary(long minUpdateTime, float minDistance) {
		// minUpdateTime = 1000l;
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			PermissionUtil.showLocationPermissionMissingWarning(this);
			return;
		}
		if (TrackerStatus.connected) {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdateTime, minDistance, this);
		} else if (minUpdateTime != this.minUpdateTime || minDistance != this.minDistance) {
			lm.removeUpdates(this);
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minUpdateTime, minDistance, this);
		}
	}

	private void connectOnline() {
		connectIfNecessary(1000L, 10f);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

	@Override
	public void onCreate() {
		System.out.println("trackerService onCreate");
		recordedTrack = new ArrayList<Point>();
		trackOnMap = new ArrayList<Location>();

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			PermissionUtil.showLocationPermissionMissingWarning(this);
			return;
		}
		gpsStatus = lm.getGpsStatus(gpsStatus);

		transmission = LocationTransmissionFactory.getLocationTransmission(Configuration.getInstance(this).getServerProperties().getTrackingProtocol());
		deviceId = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
	}

	@Override
	public void onDestroy() {
		System.out.println("trackerService onDestroy");
		TrackerStatus.clear();
		lm.removeUpdates(this);
		saveTrack();
	}

	@Override
	public void onLocationChanged(Location location) {
		System.out.println("updateLocation on service: " + location);
		TrackerStatus.lastLocation = location;
		if (TrackerStatus.connected) {
			trackOnMap.add(location);
		}
		if (TrackerStatus.recording) {
			recordedTrack.add(new Point(location));
			if (lastTrackSaveTime + SAVE_INTERVAL_MILLIS < System.currentTimeMillis() || lastTrackSaveLength + SAVE_INTERVAL_LENGTH < recordedTrack.size()) {
				saveTrack();
				lastTrackSaveLength = recordedTrack.size();
				lastTrackSaveTime = System.currentTimeMillis();
			}
		}
		if (TrackerStatus.sending) {
			transmission.send(location);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		System.out.println("onProviderDisabled");

	}

	@Override
	public void onProviderEnabled(String provider) {
		System.out.println("onProviderEnabled");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		TrackerCommand command = (TrackerCommand) intent.getExtras().getSerializable("command");
		System.out.println("start " + startId + ": " + command.getCommand());

		switch (command.getCommand()) {
			case CONNECT:
				pauseGps();
				TrackerStatus.connected = true;
				break;
			case START_TRACK:
				filename = ((StartTrack) command).getFilename();
				recordedTrack.clear();
				connectOnline();
				TrackerStatus.setRecording();
				break;
			case PAUSE_TRACK:
				TrackerStatus.recording = false;
				pauseGps();
				saveTrack();
				break;
			case RESUME_TRACK:
				filename = ((ResumeTrack) command).getFilename();
				recordedTrack = TrackIO.loadTmgrTrack(TrackIO.getRecordedTrackFile(filename));
				connectOnline();
				TrackerStatus.setRecording();
				break;
			case STOP_TRACK:
				TrackerStatus.recording = false;
				saveTrack();
				if (TrackerStatus.isNotActive()) {
					stopSelf();
				}
				break;
			case SEND:
				connectOnline();
				TrackerStatus.setSending();
				transmission.start((SendTrack) command, deviceId);
				break;
			case STOP_SENDING:
				TrackerStatus.sending = false;
				transmission.endTrack((StopSending) command, deviceId);
				if (TrackerStatus.isNotActive()) {
					stopSelf();
				}
				break;
			case SHUTDOWN:
				TrackerStatus.clear();
				stopSelf();
				break;
		}

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		System.out.println("onStatusChanged: " + provider + ", status=" + status + ", extras: " + extras);
	}

	private void pauseGps() {
		connectIfNecessary(10000L, 50f);
	}

	private void saveTrack() {
		if (filename != null && recordedTrack.size() > 0) {
			File file = TrackIO.getRecordedTrackFile(filename + TmgrReader.SUFFIX);
			System.out.println("saving " + recordedTrack.size() + " points to " + file);
			TrackIO.writeTmgrTrack(file, recordedTrack);
		}
	}

	private static class IncomingHandler extends Handler {
		WeakReference<TrackerService> trackerServiceRef;

		IncomingHandler(TrackerService trackerService) {
			trackerServiceRef = new WeakReference<TrackerService>(trackerService);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_GET_TRACK) {
				try {
					msg.replyTo.send(Message.obtain(null, MSG_GET_TRACK, trackerServiceRef.get().trackOnMap));
				} catch (RemoteException e) {
					e.printStackTrace();
					// ignore
				}
			} else {
				super.handleMessage(msg);
			}
		}
	}
}
