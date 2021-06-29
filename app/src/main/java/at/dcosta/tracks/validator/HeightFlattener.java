package at.dcosta.tracks.validator;

import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.util.Configuration;

public class HeightFlattener {

    private final boolean disabled;

    private final HeightSmoothener smoothener;

    public HeightFlattener(Configuration config) {
        disabled = config.getSingleValueDbProperty("heightFlattenerDisabled").getBooleanValue(false);
        //smoothener = new TwiceExpSmooth(0.4, 0.4);
        smoothener = new GradientDirectionFlattener();
    }

    public int getFattenedHeight(Point p) {
        if (disabled) {
            return p.getHeight();
        }
        return (int) smoothener.process(p);
    }
}
