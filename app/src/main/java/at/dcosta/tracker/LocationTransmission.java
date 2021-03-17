package at.dcosta.tracker;

import android.location.Location;

import at.dcosta.tracker.TrackerCommand.SendTrack;
import at.dcosta.tracker.TrackerCommand.StopSending;

public interface LocationTransmission {

    void endTrack(StopSending stopSendCmd, String deviceId);

    void init();

    void send(Location location);

    void start(SendTrack sendCmd, String deviceId);

}
