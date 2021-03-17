package at.dcosta.tracks.track;

import java.text.SimpleDateFormat;
import java.util.Date;

import at.dcosta.tracks.track.file.TrackListener;
import at.dcosta.tracks.validator.DistanceValidator;

public class TrackStatistic implements TrackListener {

    private final Distance totalDistance;
    private final PulseStatistic pulseStatistic;
    private Point lastPoint;
    private long timeMoving;
    private Date startTime;

    public TrackStatistic() {
        totalDistance = new Distance();
        pulseStatistic = new PulseStatistic();
    }

    public Date getEndTime() {
        return lastPoint.getTimeStampAsDate();
    }

    public int getMovingTimeSeconds() {
        return (int) (timeMoving / 1000L);
    }

    public Date getStartTime() {
        return startTime;
    }

    public int getAvgPulse() {
        return pulseStatistic.getAvgPulse();
    }

    public int getMaxPulse() {
        return pulseStatistic.getMaxPulse();
    }

    public Distance getTotalDistance() {
        return totalDistance;
    }

    public long getTotalTime() {
        return getEndTime().getTime() - getStartTime().getTime();
    }

    public boolean hasTimeData() {
        return startTime != null && startTime.getTime() > 0;
    }

    @Override
    public void processPoint(Point point) {
        lastPoint = point;
        startTime = point.getTimeStampAsDate();
        pulseStatistic.add(point);
    }

    @Override
    public void processPoint(Point point, Distance distance, DistanceValidator validator) {
        lastPoint = point;
        if (validator.isValid() && validator.isMoving()) {
            totalDistance.add(distance);
            timeMoving += distance.getTime();
        }
        pulseStatistic.add(point);
    }

    public void reset() {
        totalDistance.clear();
        timeMoving = 0;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(getStartTime())).append(": ").append(getTotalDistance().toString())
                .toString();
    }

    private static class PulseStatistic {
        private long sum;
        private int max;
        private int count;

        public void add(Point p) {
            int pulse = p.getPulse();
            if (pulse > 0) {
                sum += pulse;
                if (pulse > max) {
                    max = pulse;
                }
                count++;
            }
        }

        public int getAvgPulse() {
            return sum == 0 ? 0 : (int) (sum / count);
        }

        public int getMaxPulse() {
            return max;
        }
    }
}
