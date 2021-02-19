package at.dcosta.tracker;

import android.location.Location;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.tracker.TrackerCommand.SendTrack;
import at.dcosta.tracker.TrackerCommand.StopSending;

public class HttpLocationTransmission implements LocationTransmission {

	private final List<Location> cache = new ArrayList<Location>();
	private HttpClient httpClient;
	private HttpPost post;

	public void doSend(Location location) throws IllegalStateException, IOException {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("lat", Double.toString(location.getLatitude())));
		nameValuePairs.add(new BasicNameValuePair("lon", Double.toString(location.getLongitude())));
		nameValuePairs.add(new BasicNameValuePair("time", Long.toString(location.getTime())));
		if (location.getAltitude() != 0) {
			nameValuePairs.add(new BasicNameValuePair("alt", Double.toString(location.getAltitude())));
		}
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		sendPostRequest();
	}

	@Override
	public void endTrack(StopSending stopSendCmd, String deviceId) {
		sendMissedLocations();
		post = new HttpPost(stopSendCmd.getAddress() + deviceId + "/" + stopSendCmd.getTrackName());
		try {
			sendPostRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init() {
		httpClient = new DefaultHttpClient();
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
		HttpConnectionParams.setSoTimeout(httpParams, 3500);
	}

	@Override
	public void send(Location location) {
		if (!sendMissedLocations()) {
			cache.add(location);
		}
		try {
			doSend(location);
		} catch (Exception e) {
			cache.add(location);
		}
	}

	private boolean sendMissedLocations() {
		try {
			if (!cache.isEmpty()) {
				for (Location l : cache) {
					doSend(l);
				}
				cache.clear();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void sendPostRequest() throws IllegalStateException, IOException {
		HttpResponse response = httpClient.execute(post);
		InputStream in = response.getEntity().getContent();
		while (in.read() > -1) {
			// not needed
		}
		IOUtil.close(in);
	}

	@Override
	public void start(SendTrack sendCmd, String deviceId) {
		post = new HttpPost(sendCmd.getAddress() + deviceId + "/" + sendCmd.getTrackName());
	}

}
