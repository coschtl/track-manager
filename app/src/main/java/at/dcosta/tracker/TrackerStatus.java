package at.dcosta.tracker;

import android.location.Location;

import java.io.Serializable;

public class TrackerStatus implements Serializable {

    private static final long serialVersionUID = 8978480326406912460L;
    public static boolean recording;
    public static boolean sending;
    public static boolean connected;
    public static Location lastLocation;

    public static final void clear() {
        connected = false;
        recording = false;
        sending = false;
    }

    public static final boolean isActive() {
        return connected || sending || recording;
    }

    public static final void setRecording() {
        recording = true;
        connected = true;
    }

    public static final void setSending() {
        sending = true;
        connected = true;
    }

}