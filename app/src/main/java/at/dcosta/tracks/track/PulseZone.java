package at.dcosta.tracks.track;

public interface PulseZone {

    double getMaxPercent();

    double getMinPercent();

    int getMaxPulse();

    int getMinPulse();

    int getZoneId();

    String getLabel();
}
