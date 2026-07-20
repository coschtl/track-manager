package at.dcosta.tracks.graph;

import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.TrackActivity;
import at.dcosta.tracks.validator.DistanceValidator;
import at.dcosta.tracks.validator.Validators;

public class GraphData {

    public static final int HIGHT_RESOLUTION = -1;

    private final DistanceValidator validator;
    private final DataSeries<Long> timestamps;
    private final DataSeries<Integer> heights;
    private final DataSeries<Float> speed;
    private final DataSeries<Integer> pulses;

    private Point previousPoint;
    private int pulse;

    public GraphData(TrackActivity activity) {
        this.validator =  activity == null ? Validators.DEFAULT : activity.getDistanceValidator();;
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
        int hDiff = Math.abs(diff);
        if (hDiff > HIGHT_RESOLUTION) {
            timestamps.add(p.getTimeStampAsLong());
            addPointHeight(p);
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

    private void addPointHeight(Point p ) {
        if (previousPoint == null) {
            heights.add(p.getHeight());
        } else {
            int hDiff =  Math.abs(previousPoint.getHeight() - p.getHeight());
            int diffSeconds = (int) (p.getTimeStampAsLong() - previousPoint.getTimeStampAsLong())/1000;
            double hDiffPerSec =  (double) hDiff / diffSeconds;
            if ( hDiffPerSec >= validator.getVerticalMin() && hDiffPerSec <= validator.getVerticalMax()) {
                heights.add(p.getHeight());
            } else {
                heights.add(previousPoint.getHeight());
            }
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