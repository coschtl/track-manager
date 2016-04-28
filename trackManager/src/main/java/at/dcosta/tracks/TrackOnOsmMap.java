package at.dcosta.tracks;

import java.io.File;
import java.util.List;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import android.os.Bundle;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import at.dcosta.tracks.track.Distance;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.TrackDescription;
import at.dcosta.tracks.track.TrackReaderFactory;
import at.dcosta.tracks.track.file.ParsingException;
import at.dcosta.tracks.track.file.TrackListener;
import at.dcosta.tracks.track.file.TrackReader;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.validator.DistanceValidator;
import at.dcosta.tracks.validator.Validators;


public class TrackOnOsmMap extends MapsActivity {

    private static final class TrackPainter implements TrackListener {

        private final OsmTrackOverlay trackOverlay;
        private GeoPoint start;
        private LatLngBounds.Builder boundsBuilder;

        public TrackPainter(OsmTrackOverlay trackOverlay) {
            this.trackOverlay = trackOverlay;
        }

        private int getE6(double d) {
            return (int) (d * 1000000.0d);
        }

        public GeoPoint getStartPoint() {
            return start;
        }

        public GeoPoint getCenterPoint() {
            LatLng centerPoint = boundsBuilder.build().getCenter();
            return new GeoPoint(getE6(centerPoint.latitude), getE6(centerPoint.longitude));
        }

        @Override
        public void processPoint(Point p) {
            GeoPoint gP = new GeoPoint(getE6(p.getLat()), getE6(p.getLon()));
            trackOverlay.addPoint(gP);
            if (start == null) {
                start = gP;
            }
            if (boundsBuilder == null) {
                boundsBuilder = new LatLngBounds.Builder();
            }
            boundsBuilder.include(new LatLng(p.getLat(), p.getLon()));
        }

        @Override
        public void processPoint(Point p, Distance distance, DistanceValidator validator) {
            processPoint(p);
        }
    }

    private TrackPainter painter;
    private IMapController controller;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.show_osm_track);

        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        controller.setCenter(painter.getCenterPoint());
                    }
                });
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        controller = mapView.getController();
        controller.setZoom(15);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            long trackId = extras.getLong(TrackDescription.KEY_ID);
            OsmTrackOverlay trackOverlay = new OsmTrackOverlay(this, false);
            painter = new TrackPainter(trackOverlay);
            List<Point> pointsFromCache = Configuration.getInstance().getTrackCache().load(trackId);
            if (pointsFromCache.size() > 0) {
                for (Point p : pointsFromCache) {
                    painter.processPoint(p);
                }
            } else {
                String path = extras.getString(TrackDescription.KEY_PATH);
                TrackReader reader = TrackReaderFactory.getTrackReader(new File(path), Validators.DEFAULT);
                reader.setListener(painter);
                try {
                    reader.readTrack();
                } catch (ParsingException e) {
                    e.printStackTrace();
                }
            }
            mapView.getOverlays().add(trackOverlay);
        }
    }
}
