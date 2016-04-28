package at.dcosta.tracks.track;

import java.text.SimpleDateFormat;
import java.util.Date;

import at.dcosta.tracks.track.file.TrackListener;
import at.dcosta.tracks.validator.DistanceValidator;

public class TrackStatistic implements TrackListener {

	private Point lastPoint;
	private final Distance totalDistance;
	private long timeMoving;
	private Date startTime;

	public TrackStatistic() {
		totalDistance = new Distance();
	}

	public Date getEndTime() {
		return lastPoint.getTimeStampAsDate();
	}

	public int getMovingTimeSeconds() {
		return (int) (timeMoving / 1000l);
	}

	public Date getStartTime() {
		return startTime;
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
	}

	@Override
	public void processPoint(Point point, Distance distance, DistanceValidator validator) {
		lastPoint = point;
		if (validator.isValid() && validator.isMoving()) {
			totalDistance.add(distance);
			timeMoving += distance.getTime();
		}
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
}
