package at.dcosta.tracker;

import android.location.Location;
import at.dcosta.tracker.TrackerCommand.SendTrack;
import at.dcosta.tracker.TrackerCommand.StopSending;

public interface LocationTransmission {

	public void endTrack(StopSending stopSendCmd, String deviceId);

	public void init();

	public void send(Location location);

	public void start(SendTrack sendCmd, String deviceId);

}
