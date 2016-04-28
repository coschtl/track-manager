package at.dcosta.tracks.track;

import java.text.DecimalFormat;

import at.dcosta.android.fw.DateUtil;

public class Distance {

	public static final Distance ZERO = new Distance();

	public static String getKm(double m) {
		return new DecimalFormat("#0.0 km").format(m / 1000d);
	}

	private int vertical, verticalUp;
	private long time;

	private double horizontal;

	public void add(Distance distance) {
		horizontal += distance.getHorizontal();
		time += distance.getTime();
		vertical += distance.getVertical();
		if (distance.getVertical() > 0) {
			verticalUp += distance.getVertical();
		}
	}

	public void clear() {
		vertical = 0;
		verticalUp = 0;
		time = 0;
		horizontal = 0;

	}

	public double getHorizontal() {
		return horizontal;
	}

	public long getTime() {
		return time;
	}

	public int getVertical() {
		return vertical;
	}

	public int getVerticalUp() {
		return verticalUp;
	}

	public void setHorizontal(double horizontal) {
		this.horizontal = horizontal;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setVertical(int vertical) {
		this.vertical = vertical;
		if (vertical > 0) {
			verticalUp = vertical;
		}
	}

	@Override
	public String toString() {
		return new StringBuilder().append(Distance.getKm(getHorizontal())).append(getVerticalUp()).append(" HM").append(" in ")
				.append(DateUtil.durationMillisToString(getTime())).toString();
	}
}
