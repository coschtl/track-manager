package at.dcosta.tracks.validator;

import at.dcosta.tracks.track.Point;

public interface HeightSmoothener {
    double process(Point p);
}
