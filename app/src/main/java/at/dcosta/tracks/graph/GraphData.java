package at.dcosta.tracks.graph;

import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.validator.DistanceValidator;

public class GraphData {

    public static final int HIGHT_RESOLUTION = -1;

    private final DataSeries<Long> timestamps;
    private final DataSeries<Integer> heights;
    private final DataSeries<Float> speed;
    private final DataSeries<Integer> pulses;

    private Point previousPoint;
    private int pulse;

    public GraphData() {
        timestamps = new DataSeries<>();
        heights = new DataSeries<>();
        speed = new DataSeries<>();
        pulses = new DataSeries<>();
    }

    public DataSeries<Long> getTimeData() {
        return timestamps;
    }

    public DataSeries<Integer> getHeightData() {
        return heights;
    }

    public DataSeries<Float> getSpeedData() {
        return speed;
    }

    public DataSeries<Integer> getPulseData() {
        return pulses;
    }

    public void processPoint(Point p) {
        int h = p.getHeight();
        int diff = previousPoint == null ? 0 : previousPoint.getHeight() - h;
        if (Math.abs(diff) > HIGHT_RESOLUTION) {
            timestamps.add(p.getTimeStampAsLong());
            heights.add(h);
            if (speed.isEmpty()) {
                speed.add(0.0f);
            } else {
                long hDist = p.getDistance(previousPoint).getHorizontal();
                long vDist = p.getDistance(previousPoint).getVertical();
                long dist = (long) Math.sqrt(hDist * hDist + vDist * vDist);
                float v = (dist * 3600f) / (p.getTimeStampAsLong() - previousPoint.getTimeStampAsLong());
                speed.add(v);
            }
            if (p.getPulse() > 10) {
                pulse = p.getPulse();
            }
            pulses.add(pulse);
            previousPoint = p;
        } else {
            System.out.println("throw point away: " + p + " (previous point was: " + previousPoint + ")");
        }
    }

    public void processPoint(Point p, Distance distance, DistanceValidator validator) {
        processPoint(p);
    }

    public void remove(int index) {
        timestamps.getValues().remove(index);
        heights.getValues().remove(index);
        speed.getValues().remove(index);
        pulses.getValues().remove(index);
    }
}