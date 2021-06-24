package at.dcosta.tracks.track;

import at.dcosta.tracks.track.file.TrackListener;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.validator.DistanceValidator;

public class TrackEkgData implements TrackListener {

    private final long[] zones = new long[PulseZoneFactory.getZoneCount()];

    private long lastPointTime;
    private int lastPulse;
    private long totalZoneTime;
    private final Configuration configuration;
    private PulseZoneFactory pulseZoneFactory;

    public TrackEkgData(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void processPoint(Point point) {
        int pulse = point.getPulse();
        if (pulse < 10) {
            pulse = lastPulse;
        }
        long time = point.getTimeStampAsLong();
        if (lastPointTime == 0) {
            pulseZoneFactory = new PulseZoneFactory(configuration, point.getTimeStampAsDate());
        } else {
            long millis = time - lastPointTime;
            zones[pulseZoneFactory.getPulseZoneByPulse(pulse).getZoneId()] += millis;
            totalZoneTime += millis;
        }
        lastPointTime = time;
        lastPulse = pulse;
    }

    public int getZonePercent(int zone) {
        return (int) (100.0 / (double) totalZoneTime * (double) zones[zone]);
    }

    public double getZonePercentExact(int zone) {
        return 100.0 / (double) totalZoneTime * (double) zones[zone];
    }

    public long getZoneTime(int zone) {
        return zones[zone];
    }

    public long getTotalZoneTime() {
        return totalZoneTime;
    }

    public PulseZoneFactory getPulseZoneFactory() {
        return pulseZoneFactory;
    }

    @Override
    public void processPoint(Point p, Distance distance, DistanceValidator validator) {
        processPoint(p);
    }
}
