package at.dcosta.tracks.track;

import java.text.DecimalFormat;

import at.dcosta.android.fw.DateUtil;

public class Distance {

    public static final Distance ZERO = new Distance();
    private int vertical, verticalUp;
    private long time;
    private long horizontal;

    public static String getKm(long m) {
        return new DecimalFormat("#0.0 km").format(m / 1000d);
    }

    public void add(Distance distance) {
        horizontal += distance.getHorizontal();
        time += distance.getTime();
        vertical += distance.getVertical();
        if (distance.getVertical() > 0) {
            verticalUp += distance.getVertical();
        }
    }

    public Distance clear() {
        vertical = 0;
        verticalUp = 0;
        time = 0;
        horizontal = 0;
        return this;
    }

    public long getHorizontal() {
        return horizontal;
    }

    public Distance setHorizontal(long horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    public long getTime() {
        return time;
    }

    public Distance setTime(long time) {
        this.time = time;
        return this;
    }

    public int getVertical() {
        return vertical;
    }

    public Distance setVertical(int vertical) {
        this.vertical = vertical;
        if (vertical > 0) {
            verticalUp = vertical;
        }
        return this;
    }

    public int getVerticalUp() {
        return verticalUp;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(Distance.getKm(getHorizontal())).append(", ").append(getVerticalUp()).append(" HM").append(" in ")
                .append(DateUtil.durationMillisToString(getTime())).toString();
    }
}
