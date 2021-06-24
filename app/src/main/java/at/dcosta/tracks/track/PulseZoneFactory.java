package at.dcosta.tracks.track;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import at.dcosta.tracks.util.Configuration;

public class PulseZoneFactory {

    private final double maxPulse;
    private final Map<Integer, PulseZone> id2Zone;

    public PulseZoneFactory(Configuration config, Date trackDate) {
        maxPulse = calculateMaxPulse(config, trackDate);
        id2Zone = new LinkedHashMap<>();
        calculateZones();
    }

    public static int getZoneCount() {
        return Zone.values().length;
    }

    public static Zone[] getZones() {
        return Zone.values();
    }

    public Collection<PulseZone> getPulseZones() {
        return id2Zone.values();
    }

    public PulseZone getPulseZoneByZoneId(int zoneId) {
        return id2Zone.get(zoneId);
    }

    public PulseZone getPulseZoneByPulse(double pulse) {
        if (pulse < 1.0) {
            return id2Zone.get(0);
        }
        double perc = pulse / maxPulse;
        for (PulseZone zone : id2Zone.values()) {
            if (perc >= zone.getMinPercent() && perc < zone.getMaxPercent()) {
                return zone;
            }
        }
        return id2Zone.get(id2Zone.size() - 1);
    }

    private void calculateZones() {
        for (Zone zone : Zone.values()) {
            id2Zone.put(zone.getZoneId(), new PulseZone() {

                @Override
                public double getMaxPercent() {
                    return zone.getMaxPercent();
                }

                @Override
                public double getMinPercent() {
                    return zone.getMinPercent();
                }

                @Override
                public int getMaxPulse() {
                    return (int) (maxPulse * zone.getMaxPercent());
                }

                @Override
                public int getMinPulse() {
                    return (int) (maxPulse * zone.getMinPercent());
                }

                @Override
                public int getZoneId() {
                    return zone.getZoneId();
                }

                @Override
                public String getLabel() {
                    return zone.getLabel();
                }
            });
        }
    }

    private double calculateMaxPulse(Configuration config, Date trackDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(trackDate.getTime());
        int trackYear = cal.get(Calendar.YEAR);
        cal.setTime(config.getBirthday());
        int age = trackYear - cal.get(Calendar.YEAR);

        double maxPulseValue;
        if (age < 40) {
            maxPulseValue = 220 - age;
            if (config.isMale()) {
                maxPulseValue += 6;
            }
        } else {
            maxPulseValue = 208.0 - (age * 0.7);
        }
        return maxPulseValue;
    }


    public enum Zone {
        CALM(0, "<60%", 0.0, 0.6),
        FAT_LOW(1, "60% - 65%", 0.6, 0.65),
        FAT_HIGH(2, "65% - 70%", 0.65, 0.7),
        AEROBIC_LOW(3, "70% - 75%", 0.7, 0.75),
        AEROBIC_HIGH(4, "75% - 80%", 0.75, 0.8),
        ANAEROBIC_LOW(5, "80% - 85%", 0.8, 0.85),
        ANAEROBIC_HIGH(6, "85% - 90%", 0.85, 0.9),
        MAXIMUM_LOW(7, "90% - 95%", 0.9, 0.95),
        MAXIMUM_HIGH(8, "> 95%", 0.95, 1.0);

        private final int zoneId;
        private final String label;
        private final double minPercent;
        private final double maxPercent;

        Zone(int zoneId, String label, double minPercent, double maxPercent) {
            this.zoneId = zoneId;
            this.label = label;
            this.minPercent = minPercent;
            this.maxPercent = maxPercent;
        }

        public String getLabel() {
            return label;
        }

        public double getMinPercent() {
            return minPercent;
        }

        public double getMaxPercent() {
            return maxPercent;
        }

        public int getZoneId() {
            return zoneId;
        }
    }
}
