package at.dcosta.tracks;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import at.dcosta.android.fw.props.Property;
import at.dcosta.tracks.util.Configuration;

public class TrackOverlay {

    private final ArrayList<LatLng> geoPoints = new ArrayList<LatLng>();

    private final boolean markLastPoint;

    public TrackOverlay(boolean markLastPoint) {
        super();
        this.markLastPoint = markLastPoint;
    }

    public void addPoint(LatLng geoPoint) {
        geoPoints.add(geoPoint);
    }

    public void clearPoints() {
        geoPoints.clear();
    }

//    @Override
//    public boolean draw(Canvas canvas, MapFragment mapView, boolean shadow, long when) {
//        super.draw(canvas, mapView, shadow);
//        if (geoPoints.size() > 0) {
//            Paint paint;
//            paint = new Paint();
//            paint.setColor(getTrackColor());
//            paint.setAntiAlias(true);
//            paint.setStyle(Style.STROKE);
//            paint.setStrokeWidth(2);
//
//            Projection projection = mapView.getProjection();
//            Point p1 = new Point();
//            Point p2 = new Point();
//            LatLng last = geoPoints.get(0);
//            for (int i = 1; i < geoPoints.size(); i++) {
//                LatLng act = geoPoints.get(i);
//                projection.toPixels(last, p1);
//                projection.toPixels(act, p2);
//                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
//                last = act;
//            }
//            if (markLastPoint) {
//                projection.toPixels(last, p2);
//                float len = 5f;
//                canvas.drawLine(p2.x - len, p2.y + len, p2.x + len, p2.y - len, paint);
//                canvas.drawLine(p2.x - len, p2.y - len, p2.x + len, p2.y + len, paint);
//            }
//        }
//        return true;
//    }

    public ArrayList<LatLng> getPoints() {
        return geoPoints;
    }

    public int getTrackColor() {
        Property color = Configuration.getInstance().getSingleValueDbProperty("trackColor");
        if ("Blue".equals(color.getValue())) {
        } else if ("Red".equals(color.getValue())) {
            return Color.BLUE;
        } else if ("Green".equals(color.getValue())) {
            return Color.GREEN;
        } else if ("Black".equals(color.getValue())) {
            return Color.BLACK;
        } else if ("Cyan".equals(color.getValue())) {
            return Color.CYAN;
        } else if ("Magenta".equals(color.getValue())) {
            return Color.MAGENTA;
        } else if ("Yellow".equals(color.getValue())) {
            return Color.YELLOW;
        }
        return Color.BLUE;
    }

}
