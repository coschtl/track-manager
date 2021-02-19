package at.dcosta.tracks.validator;

import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.Configuration;

public class HeightFlattener {

    private final FixedLengthLiFoBuffer<Double> buffer;
    private final boolean disabled;
    private final TwiceExpSmooth smoothener;

    public HeightFlattener(Configuration config) {
        disabled = config.getSingleValueDbProperty("heightFlattenerDisabled").getBooleanValue(false);
        buffer = new FixedLengthLiFoBuffer<Double>(5);
        smoothener = new TwiceExpSmooth(0.4, 0.4);
    }

    public int getFattenedHeight(Point p) {
        if (disabled) {
            return p.getHeight();
        }
        return (int) smoothener.add(p.getHeight());
    }

    private static class TwiceExpSmooth {
        private final double alpha;
        private final double beta;
        private double smoothedData;
        private double trend;
        private double level;
        private double nextLevel;

        private int addCount;

        public TwiceExpSmooth(double alpha, double beta) {
            this.alpha = alpha;
            this.beta = beta;
        }

        public double add(double d) {
            if (addCount == 0) {
                smoothedData = d;
                level = d;
                addCount++;
                return d;
            }
            if (addCount == 1) {
                trend = d - smoothedData;
                addCount++;
                return d;
            }
            smoothedData = trend + level;
            nextLevel = alpha * d + (1 - alpha) * (level + trend);
            trend = beta * (nextLevel - level) + (1 - beta) * trend;
            level = nextLevel;
            return smoothedData;
        }
    }
}
