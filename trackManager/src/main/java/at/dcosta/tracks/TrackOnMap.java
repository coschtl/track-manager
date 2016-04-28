package at.dcosta.tracks;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ViewTreeObserver;

import at.dcosta.android.fw.props.Property;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class TrackOnMap extends FragmentActivity
        implements OnMapReadyCallback {

    private static final class TrackPainter implements TrackListener {

        private final PolylineOptions polylineOptions;
        private LatLngBounds.Builder boundsBuilder;

        public TrackPainter(PolylineOptions polylineOptions) {
            this.polylineOptions = polylineOptions;
        }

        private int getE6(double d) {
            return (int) (d * 1000000.0d);
        }

        public LatLngBounds getBounds() {
            return boundsBuilder.build();
        }

        @Override
        public void processPoint(Point p) {
            LatLng point = new LatLng(p.getLat(), p.getLon());
            polylineOptions.add(point);
            if (boundsBuilder == null) {
                boundsBuilder = new LatLngBounds.Builder();
            }
            boundsBuilder.include(point);
        }

        @Override
        public void processPoint(Point p, Distance distance, DistanceValidator validator) {
            processPoint(p);
        }

    }

    private GoogleMap googleMap;
    private TrackPainter painter;

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            long trackId = extras.getLong(TrackDescription.KEY_ID);
            Configuration config = Configuration.getInstance();
            List<Point> pointsFromCache = config.getTrackCache().load(trackId);
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color( getColor(config) );
            painter = new TrackPainter(polylineOptions);
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
            googleMap.addPolyline(polylineOptions);
        }
    }

    private int getColor(Configuration config) {
        Property trackColor = config.getSingleValueDbProperty("trackColor");
        return Color.parseColor(trackColor.getValue());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_track);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.getView().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (painter != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(painter.getBounds(), 50));
                        }
                    }
                });

    }
}
