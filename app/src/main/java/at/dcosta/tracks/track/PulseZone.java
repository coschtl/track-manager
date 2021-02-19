package at.dcosta.tracks.track;

import java.util.HashMap;
import java.util.Map;

public enum PulseZone {

    CALM(0, "<60%", 0.0, 0.6),
    FAT_LOW(1, "60% - 65%", 0.6, 0.65),
    FAT_HIGH(2, "65% - 70%", 0.65, 0.7),
    AEROBIC_LOW(3, "70% - 75%", 0.7, 0.75),
    AEROBIC_HIGH(4, "75% - 80%", 0.75, 0.8),
    ANAEROBIC_LOW(5, "80% - 85%", 0.8, 0.85),
    ANAEROBIC_HIGH(6, "85% - 90%", 0.85, 0.9),
    MAXIMUM_LOW(7, "90% - 95%", 0.9, 0.95),
    MAXIMUM_HIGH(8, "> 95%", 0.95, 1.0);

    private static final Map<Integer, PulseZone> ID_2_ZONE;

    static {
        ID_2_ZONE = new HashMap<>();
        for (PulseZone zone : PulseZone.values()) {
            ID_2_ZONE.put(zone.getZoneId(), zone);
        }
    }

    private final int zoneId;
    private final String label;
    private final double minPercent;
    private final double maxPercent;

    PulseZone(int zoneId, String label, double minPercent, double maxPercent) {
        this.zoneId = zoneId;
        this.label = label;
        this.minPercent = minPercent;
        this.maxPercent = maxPercent;
    }

    public static PulseZone fromZoneId(int zoneId) {
        return ID_2_ZONE.get(zoneId);
    }

    public double getMaxPercent() {
        return maxPercent;
    }

    public double getMinPercent() {
        return minPercent;
    }

    public int getZoneId() {
        return zoneId;
    }

    public String getLabel() {
        return label;
    }
}
