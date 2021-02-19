package at.dcosta.tracks.graph;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.FillDirection;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import at.dcosta.tracks.R;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.MinMaxValue;

//import com.androidplot.util.ValPixConverter;

public class TrackProfile2 extends Activity implements OnGestureListener, OnTouchListener, OnClickListener {

    private final List<PointF> points = new ArrayList<PointF>();
    private MODE mode = MODE.HEIGHT_TIME;

    private XYPlot plot;
    private GestureDetector gestureDetector;
    private String path;
    private TrackDbAdapter trackDbAdapter;
    private Configuration config;
    private TrackProfileData profileData;
    private int aktCursorId = -1;
    private long xRangeMin, xRangeMax, yRangeMin, yRangeMax;
    private long trackId;
    private boolean doNotAskForDeletion, doNotAskForDeletionChanged;

    private void askForDeletePoint() {
        doNotAskForDeletionChanged = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_point).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteAktPointFromTrack();
                dialog.cancel();
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (doNotAskForDeletionChanged) {
                    doNotAskForDeletion = false;
                }
                dialog.cancel();
            }
        }).setSingleChoiceItems(R.array.do_not_ask_again, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                doNotAskForDeletion = true;
                doNotAskForDeletionChanged = true;
            }
        });
        builder.create().show();
    }

    private void changeMode(float velocityX) {
        int modeId;
        if (velocityX > 0) {
            modeId = mode.ordinal() - 1;
            if (modeId < 0) {
                modeId = MODE.values().length - 1;
            }
        } else {
            modeId = mode.ordinal() + 1;
            if (modeId == MODE.values().length) {
                modeId = 0;
            }
        }
        points.clear();
        removeCursors();
        mode = MODE.values()[modeId];
    }

    private void deleteAktPointFromTrack() {
        List<Point> track = config.getTrackCache().load(trackId);
        track.remove(aktCursorId);
        config.getTrackCache().save(trackId, track);
        points.clear();
        plot.clear();
        drawGraph();
        moveCursorToLeft();
    }

    private void drawGraph() {
        LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(255, 0, 0), // line color
                Color.rgb(255, 0, 0), // point color
                null, null, FillDirection.BOTTOM);

        TrackDescriptionNG track = trackDbAdapter.findEntryByPath(path);
        if (mode == MODE.HEIGHT_DISTANCE || track.getStartTime().equals(track.getEndTime())) {
            profileData = new HeightDistanceData();
        } else if (mode == MODE.HEIGHT_TIME) {
            profileData = new HeightTimeData();
        }

        String name = track.getName();
        trackId = track.getId();
        Iterator<Point> pointIterator = config.getTrackCache().load(trackId).iterator();
        while (pointIterator.hasNext()) {
            profileData.processPoint(pointIterator.next());
        }
        plot.setTitle(name == null ? new File(path).getName() : name);

        plot.addSeries(profileData.getXYSeries(), seriesFormat);
        plot.setDomainLabel(profileData.getXLabel(this));
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(profileData.getXFormat());

        plot.setRangeLabel(profileData.getYLabel(this));
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(profileData.getYFormat());

        MinMaxValue yMinMax = profileData.getYMinMax();
        long yInterval = profileData.getYInterval();
        yRangeMin = roundDown(yMinMax.minAsLong(), yInterval);
        yRangeMax = roundUp(yMinMax.maxAsLong(), yInterval);
        plot.setRangeBoundaries(yRangeMin, yRangeMax, BoundaryMode.FIXED);
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL, yInterval);

        MinMaxValue xMinMax = profileData.getXMinMax();
        long domainInterval = profileData.getXInterval();
        xRangeMin = roundDown(xMinMax.minAsLong(), domainInterval);
        xRangeMax = roundUp(xMinMax.maxAsLong(), domainInterval);
        plot.setDomainBoundaries(xRangeMin, xRangeMax, BoundaryMode.FIXED);
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, domainInterval);

//        plot.getLegend().setVisible(true);
//        plot.getLegend().setHeight(300f);
        plot.getTitle().setWidth(200f);
        plot.getTitle().setHeight(200f);
        plot.setOnTouchListener(this);
        PanZoom.attach(plot);

        plot.redraw();
    }

    private PointF findTrackPoint(float x) {
        if (points() == null) {
            return null;
        }
        int i = 0;
        for (PointF akt : points) {
            if (akt.x >= x) {
                aktCursorId = i;
                return akt;
            }
            i++;
        }
        return null;
    }

    private void moveCursorToLeft() {
        if (aktCursorId > 0) {
            showCursorAt(points().get(--aktCursorId));
        } else {
            removeCursors();
        }
    }

    private void moveCursorToRight() {
        if (aktCursorId < points().size() - 1) {
            showCursorAt(points().get(++aktCursorId));
        } else {
            removeCursors();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //xxx     case R.id.but_prev:
            //xxx        moveCursorToLeft();
            //xxx      break;
            //xxx   case R.id.but_next:
            //xxx       moveCursorToRight();
            //xxx       break;
            case R.id.but_delete:
                if (aktCursorId >= 0) {
                    if (doNotAskForDeletion) {
                        deleteAktPointFromTrack();
                    } else {
                        askForDeletePoint();
                    }
                }
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = Configuration.getInstance(this);
        setContentView(R.layout.track_profile);
        trackDbAdapter = new TrackDbAdapter(config.getDatabaseHelper(), this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            path = extras.getString(TrackDescriptionNG.KEY_PATH);
        }
        plot = (XYPlot) findViewById(R.id.plot);
        gestureDetector = new GestureDetector(this, this);

        drawGraph();
        //xxx   findViewById(R.id.but_prev).setOnClickListener(this);
        // xxx   findViewById(R.id.but_delete).setOnClickListener(this);
        //xxx  findViewById(R.id.but_next).setOnClickListener(this);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (Math.abs(velocityX) > config.getWipeSensitivity()) {
            changeMode(velocityX);
            drawGraph();
            return true;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // System.out.println("onlongpress");
    }

    @Override
    protected void onPause() {
        trackDbAdapter.close();
        super.onPause();
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // ignore
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean ret = gestureDetector.onTouchEvent(event);
        if (event.getPointerCount() == 1) {
            PointF point = findTrackPoint(event.getX());
            if (point == null) {
                point = new PointF(event.getX(), event.getY());
            }
            ret = showCursorAt(point);
        }
        return ret;
    }

    private List<PointF> points() {
        if (points.size() == 0) {
            RectF gridRect = plot.getGraph().getGridRect();
            if (gridRect != null) {
                XYSeries xySeries = profileData.getXYSeries();
                for (int i = 0; i < xySeries.size(); i++) {
                    PointF valuePoint = new PointF(xySeries.getX(i).floatValue(), xySeries.getY(i).floatValue());
                    // PointF pixPoint = ValPixConverter.valToPix(valuePoint.x, valuePoint.y, gridRect, xRangeMin, xRangeMax, yRangeMin, yRangeMax);
                    //points.add(pixPoint);
                    points.add(valuePoint);
                }
            }
        }
        return points;
    }

    private void removeCursors() {
        XYGraphWidget graphWidget = plot.getGraph();
        graphWidget.setDomainCursorPosition(-1.0f);
        graphWidget.setRangeCursorPosition(-1.0f);
    }

    private long roundDown(long l, long interval) {
        return interval * (l / interval);
    }

    private long roundUp(long l, long interval) {
        return interval * ((l + interval) / interval);
    }

    private boolean showCursorAt(PointF point) {
        if (plot.containsPoint(point.x, point.y)) {
            plot.setCursorPosition(point.x, point.y);
            // Paint cursor = plot.getGraph().getCursorLabelPaint();
            Paint cursor = plot.getGraph().getDomainCursorPaint();
            cursor.setTextSize(25);
            //plot.getGraph().getCursorLabelBackgroundPaint().setAlpha(0);
            plot.getGraph().getGridBackgroundPaint().setAlpha(0);
            plot.redraw();
            return true;
        }
        return false;
    }

    private enum MODE {
        HEIGHT_TIME, HEIGHT_DISTANCE
    }

}
