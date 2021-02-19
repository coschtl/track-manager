package at.dcosta.tracks;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;

public class OsmTrackOverlay extends Overlay {

	private final ArrayList<GeoPoint> geoPoints = new ArrayList<GeoPoint>();

	private final boolean markLastPoint;

	public OsmTrackOverlay(Context context, boolean markLastPoint) {
		super(context);
		this.markLastPoint = markLastPoint;
	}

	public void addPoint(GeoPoint geoPoint) {
		geoPoints.add(geoPoint);
	}

	public void clearPoints() {
		geoPoints.clear();
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (geoPoints.size() > 0) {
			Paint paint;
			paint = new Paint();
			paint.setColor(Color.RED);
			paint.setAntiAlias(true);
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(2);

			Projection projection = mapView.getProjection();
			Point p1 = new Point();
			Point p2 = new Point();
			GeoPoint last = geoPoints.get(0);
			for (int i = 1; i < geoPoints.size(); i++) {
				GeoPoint act = geoPoints.get(i);
				projection.toPixels(last, p1);
				projection.toPixels(act, p2);
				canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
				last = act;
			}
			if (markLastPoint) {
				projection.toPixels(last, p2);
				float len = 5f;
				canvas.drawLine(p2.x - len, p2.y + len, p2.x + len, p2.y - len, paint);
				canvas.drawLine(p2.x - len, p2.y - len, p2.x + len, p2.y + len, paint);
			}
		}
	}

	public ArrayList<GeoPoint> getPoints() {
		return geoPoints;
	}

}
