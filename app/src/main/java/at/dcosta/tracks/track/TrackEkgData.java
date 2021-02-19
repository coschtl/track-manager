package at.dcosta.tracks.track;

import java.util.Calendar;
import java.util.Date;

import at.dcosta.tracks.track.file.TrackListener;
import at.dcosta.tracks.validator.DistanceValidator;

public class TrackEkgData implements TrackListener {

    private final Date birthday;
    private final boolean male;
    private final long[] zones = new long[PulseZone.values().length];

    private long lastPointTime;
    private int lastPulse;
    private long totalZoneTime;
    private double maxPulse;

    public TrackEkgData(Date birthday, boolean male) {
        this.birthday = birthday;
        this.male = male;
    }

    @Override
    public void processPoint(Point point) {
        int pulse = point.getPulse();
        if (pulse < 10) {
            pulse = lastPulse;
        }
        long time = point.getTimeStampAsLong();
        if (lastPointTime == 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);
            int trackYear = cal.get(Calendar.YEAR);
            cal.setTime(birthday);
            int age = trackYear - cal.get(Calendar.YEAR);
            if (age < 40) {
                maxPulse = 220 - age;
                if (male) {
                    maxPulse += 6;
                }
            } else {
                maxPulse = 208.0 - (age * 0.7);
            }
        } else {
            long millis = time - lastPointTime;
            zones[getZone(pulse).getZoneId()] += millis;
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

    private PulseZone getZone(double pulse) {
        double perc = pulse / maxPulse;
        for (PulseZone zone : PulseZone.values()) {
            if (perc >= zone.getMinPercent() && perc < zone.getMaxPercent()) {
                return zone;
            }
        }
        return null;
    }

    @Override
    public void processPoint(Point p, Distance distance, DistanceValidator validator) {
        processPoint(p);
    }
}
