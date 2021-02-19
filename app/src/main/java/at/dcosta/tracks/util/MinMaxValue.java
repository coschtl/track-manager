package at.dcosta.tracks.util;

public class MinMaxValue {

    private long min, max;

    public MinMaxValue() {
        clear();
    }

    public void clear() {
        min = Long.MAX_VALUE;
        max = 0;
    }

    public int diffAsInt() {
        return (int) (max - min);
    }

    public int maxAsInt() {
        return (int) max;
    }

    public int minAsInt() {
        return (int) min;
    }

    public long diffAsLong() {
        return max - min;
    }

    public long maxAsLong() {
        return max;
    }

    public long minAsLong() {
        return min;
    }

    public MinMaxValue update(long value) {
        if (value < min) {
            min = value;
        }
        if (value > max) {
            max = value;
        }
        return this;
    }

}
