package at.dcosta.tracks.validator;

import at.dcosta.tracks.track.Point;

public class TwiceExpSmooth implements HeightSmoothener {
    private final double alpha;
    private final double beta;
    private double smoothedData;
    private double trend;
    private double level;
    private int addCount;

    public TwiceExpSmooth(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    public double process(Point point) {
        double height = point.getHeight();
        if (addCount == 0) {
            smoothedData = height;
            level = height;
            addCount++;
            return height;
        }
        if (addCount == 1) {
            trend = height - smoothedData;
            addCount++;
            return height;
        }
        smoothedData = trend + level;
        double nextLevel = alpha * height + (1 - alpha) * (level + trend);
        trend = beta * (nextLevel - level) + (1 - beta) * trend;
        level = nextLevel;
        return smoothedData;
    }
}
