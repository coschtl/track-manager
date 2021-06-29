package at.dcosta.tracks.validator;

import at.dcosta.tracks.track.Point;

public class GradientDirectionFlattener implements HeightSmoothener {

    private Direction lastDirection;
    private Point lastPoint;

    public double process(Point p) {
        if (lastPoint == null) {
            lastPoint = p;
            return p.getHeight();
        }
        Direction direction = p.getHeight() - lastPoint.getHeight() >= 0 ? Direction.UP : Direction.DOWN;
        double height;
        if (lastDirection == null || lastDirection == direction) {
            height = p.getHeight();
            lastPoint = p;
        } else {
            height = lastPoint.getHeight();
        }
        lastDirection = direction;
        return height;
    }

    private enum Direction {
        UP, DOWN
    }
}
