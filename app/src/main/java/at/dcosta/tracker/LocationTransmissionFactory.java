package at.dcosta.tracker;

public class LocationTransmissionFactory {

    public static LocationTransmission getLocationTransmission(String protocol) {
        if ("http".equalsIgnoreCase(protocol)) {
            return new HttpLocationTransmission();
        }
        throw new IllegalArgumentException("unknown protocol: " + protocol);
    }

}
