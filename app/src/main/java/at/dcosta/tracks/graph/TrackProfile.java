package at.dcosta.tracks.graph;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.androidplot.ui.Anchor;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.OrderedXYSeries;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.ScalingXYSeries;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import at.dcosta.tracks.R;
import at.dcosta.tracks.TrackEdit;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.graph.util.GraphUtil;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.file.PointReader;
import at.dcosta.tracks.track.file.TrackReader;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.MinMaxValue;

//import com.androidplot.util.ValPixConverter;

public class TrackProfile extends Activity implements OnTouchListener, OnClickListener {

    private XYPlot plot;
    private GestureDetector gestureDetector;
    private String path;
    private TrackDbAdapter trackDbAdapter;
    private TrackDescriptionNG track;
    private Configuration config;
    private GraphData graphData;
    private int timeIndex;
    private SimpleXYSeries heightsSeries;
    private boolean trackChanged;
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

    private void deleteAktPointFromTrack() {
        List<Point> trackPoints = config.getTrackCache().load(track.getId());
        trackPoints.remove(timeIndex);
        config.getTrackCache().save(track.getId(), trackPoints);
        for (int i = timeIndex; i < heightsSeries.size() - 1; i++) {
            heightsSeries.setX(heightsSeries.getX(i + 1), i);
            heightsSeries.setY(heightsSeries.getY(i + 1), i);
        }
        heightsSeries.removeLast();
        plot.redraw();
        trackChanged = true;
        moveCursorToLeft();
    }

    private void drawGraph() {
        track = trackDbAdapter.findEntryByPath(path);
        graphData = new GraphData();

        String name = track.getName();
        Iterator<Point> pointIterator = config.getTrackCache().load(track.getId()).iterator();
        while (pointIterator.hasNext()) {
            graphData.processPoint(pointIterator.next());
        }
        plot.setTitle(name == null ? new File(path).getName() : name);
        final DataSeries<Integer> heightData = graphData.getHeightData();
        DataSeries<Float> speedData = graphData.getSpeedData();
        DataSeries<Integer> pulseData = graphData.getPulseData();

        final double heightFactor = (heightData.getMinMax().maxAsInt() - heightData.getMinMax().minAsInt()) / 95.0;
        final double speedFactor = (speedData.getMinMax().maxAsLong() - speedData.getMinMax().minAsLong()) / 25.0;
        final double pulseFactor = (pulseData.getMinMax().maxAsInt() - pulseData.getMinMax().minAsInt()) / 90.0;

        List<Integer> normalizedHeightData = new ArrayList<>();
        for (Integer h : heightData.getValues()) {
            normalizedHeightData.add(h - heightData.getMinMax().minAsInt());
        }

        heightsSeries = createSeries(graphData, normalizedHeightData, "Height");
        final ScalingXYSeries scalingHeights = addSeries(heightsSeries, R.xml.hight_line);
        final ScalingXYSeries scalingSpeed = addSeries(createSeries(graphData, speedData.getValues(), "Speed"), R.xml.speed_line);
        final ScalingXYSeries scalingPulses = addSeries(createSeries(graphData, pulseData.getValues(), "Pulse"), R.xml.pulse_line);


        plot.getGraph().setMarginRight(100);
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM)
                .setFormat(new Format() {
                    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm");

                    @Override
                    public StringBuffer format(Object obj,
                                               @NonNull StringBuffer toAppendTo,
                                               @NonNull FieldPosition pos) {
                        Date date = new Date(((Number) obj).longValue());
                        return toAppendTo.append(dateFormat.format(date));
                    }

                    @Override
                    public Object parseObject(String source, @NonNull ParsePosition pos) {
                        return null;
                    }
                });
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT)
                .setFormat(new Format() {
                    final DecimalFormat df = new DecimalFormat("###.#");

                    @Override
                    public StringBuffer format(Object obj,
                                               @NonNull StringBuffer toAppendTo,
                                               @NonNull FieldPosition pos) {
                        int i = ((Number) obj).intValue();
                        if (i == 0) {
                            return toAppendTo;
                        }
                        return toAppendTo.append((int) (i * heightFactor + heightData.getMinMax().minAsInt())).append("~").append(df.format(i * speedFactor));
//                        return toAppendTo.append((int) (i * heightFactor + heightData.getMinMax().minAsInt())).append("~").append(df.format(i * speedFactor));
                    }

                    @Override
                    public Object parseObject(String source, @NonNull ParsePosition pos) {
                        return null;
                    }
                });
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.RIGHT)
                .setFormat(new Format() {
                    @Override
                    public StringBuffer format(Object obj,
                                               @NonNull StringBuffer toAppendTo,
                                               @NonNull FieldPosition pos) {
                        int i = ((Number) obj).intValue();
                        if (i == 0) {
                            return toAppendTo;
                        }
                        return toAppendTo.append((int) (i * pulseFactor));
                    }

                    @Override
                    public Object parseObject(String source, @NonNull ParsePosition pos) {
                        return null;
                    }
                });
        plot.setRangeBoundaries(0, 100, BoundaryMode.FIXED);

        plot.getGraph().setLineLabelRenderer(XYGraphWidget.Edge.LEFT, new XYGraphWidget.LineLabelRenderer() {
            private float textSize;
            private float textMove;

            @Override
            protected void drawLabel(Canvas canvas, String text, Paint paint, float x, float y, boolean isOrigin) {
                if (text == null || text.isEmpty()) {
                    return;
                }
                if (textSize == 0f) {
                    textSize = paint.getTextSize() * 0.8f;
                    textMove = textSize / 1.9f;
                }
                paint.setColor(Color.parseColor("#00AA00"));
                paint.setTextSize(textSize);
                int pos = text.indexOf('~');
                super.drawLabel(canvas, text.substring(0, pos), paint, x, y - textMove, isOrigin);
                paint.setColor(Color.parseColor("#FF5555"));
                super.drawLabel(canvas, text.substring(pos + 1), paint, x, y + textMove, isOrigin);
            }

            @Override
            public void drawLabel(Canvas canvas, XYGraphWidget.LineLabelStyle style, Number val, float x, float y, boolean isOrigin) {
                super.drawLabel(canvas, style, val, x, y, isOrigin);
            }
        });

        plot.getGraph().setLineLabelRenderer(XYGraphWidget.Edge.RIGHT, new XYGraphWidget.LineLabelRenderer() {
            private float textSize;

            @Override
            protected void drawLabel(Canvas canvas, String text, Paint paint, float x, float y, boolean isOrigin) {
                if (text == null || text.isEmpty()) {
                    return;
                }
                if (textSize == 0f) {
                    textSize = paint.getTextSize() * 0.8f;
                }
                paint.setTextSize(textSize);
                int pos = text.indexOf('~');
                paint.setColor(Color.parseColor("#00AAFF"));
                super.drawLabel(canvas, text, paint, x + 10, y, isOrigin);
            }

            @Override
            public void drawLabel(Canvas canvas, XYGraphWidget.LineLabelStyle style, Number val, float x, float y, boolean isOrigin) {
                super.drawLabel(canvas, style, val, x, y, isOrigin);
            }
        });
        plot.setRangeStep(StepMode.SUBDIVIDE, 11);
        plot.setRangeLabel(null);
        plot.setDomainLabel(null);
        MinMaxValue timeMinMax = graphData.getTimeData().getMinMax();
        MinMaxValue boundaryMinMax = GraphUtil.getGraphBoundaries(timeMinMax, 8);

        plot.setDomainBoundaries(boundaryMinMax.minAsLong(), boundaryMinMax.maxAsLong(), BoundaryMode.FIXED);
        plot.setDomainStep(StepMode.SUBDIVIDE, 9);

        plot.getLegend().setMarginLeft(0f);
        plot.getLegend().setAnchor(Anchor.RIGHT_BOTTOM);

        // animate a scale value from a starting val of 0 to a final value of 1:
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);

        // use an animation pattern that begins and ends slowly:
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float scale = valueAnimator.getAnimatedFraction();
                scalingHeights.setScale(scale / heightFactor);
                scalingSpeed.setScale(scale / speedFactor);
                scalingPulses.setScale(scale / pulseFactor);
                plot.redraw();
            }
        });

        // the animation will run for 1 seconds:
        animator.setDuration(1000);
        animator.start();
        TextLabelWidget title = plot.getTitle();
        Paint bg = new Paint();
        bg.setColor(Color.BLACK);
        title.setBackgroundPaint(bg);
        title.setPadding(10f, 10f, 10f, 10f);
        PanZoom panZoom = PanZoom.attach(plot);
        panZoom.setDelegate(this);
        plot.redraw();

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                if (event.getPointerCount() == 1) {
                    updateTimeIndex(event.getX());
                    showCursorAtTimeIndex();
                }
                return false;
            }
        });
    }

    private SimpleXYSeries createSeries(GraphData graphData, List<? extends Number> seriesData, String seriesName) {
        SimpleXYSeries simpleXYSeries = new SimpleXYSeries(graphData.getTimeData().getValues(), seriesData, seriesName);
        simpleXYSeries.setXOrder(OrderedXYSeries.XOrder.ASCENDING);
        return simpleXYSeries;
    }

    private ScalingXYSeries addSeries(SimpleXYSeries xySeries, int xmlCfgId) {
        LineAndPointFormatter formatter = new LineAndPointFormatter(this, xmlCfgId);
        formatter.setPointLabeler(null);
        final ScalingXYSeries scalingSeries = new ScalingXYSeries(xySeries, 0, ScalingXYSeries.Mode.Y_ONLY);
        plot.addSeries(scalingSeries, formatter);
        return scalingSeries;
    }

    private void moveCursorToLeft() {
        if (timeIndex > 0) {
            timeIndex--;
            showCursorAtTimeIndex();
        } else {
            removeCursor();
        }
    }

    private void moveCursorToRight() {
        if (timeIndex < graphData.getTimeData().size() - 1) {
            timeIndex++;
            showCursorAtTimeIndex();
        } else {
            removeCursor();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.but_prev:
                moveCursorToLeft();
                break;
            case R.id.but_next:
                moveCursorToRight();
                break;
            case R.id.but_delete:
                if (timeIndex >= 0) {
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

        drawGraph();

        findViewById(R.id.but_prev).setOnClickListener(this);
        findViewById(R.id.but_delete).setOnClickListener(this);
        findViewById(R.id.but_next).setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        if (trackChanged) {
            TrackReader reader = new PointReader(config.getTrackCache().load(track.getId()), track.getActivity().getDistanceValidator());
            TrackEdit.updateTrack(track, reader, track.getActivity().getIcon());
            trackDbAdapter.updateEntry(track);
        }
        trackDbAdapter.close();
        super.onPause();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return false;
    }

    private void removeCursor() {
        plot.setCursorPosition(-1f, -1f);
        plot.redraw();
    }

    private void updateTimeIndex(float timeOnScreen) {
        long time = plot.screenToSeriesX(timeOnScreen).longValue();
        int index = 0;
        List<Long> td = graphData.getTimeData().getValues();
        for (index = 0; index < td.size(); index++) {
            if (td.get(index) > time) {
                timeIndex = index - 1;
                return;
            }
        }
        timeIndex = 0;
    }

    private void showCursorAtTimeIndex() {
        XYGraphWidget graph = plot.getGraph();
        float time = plot.seriesToScreenX(graphData.getTimeData().getValues().get(timeIndex));
        plot.setCursorPosition(time, -1);
        Paint cursor = graph.getDomainCursorPaint();
        cursor.setColor(Color.MAGENTA);
        cursor.setStrokeWidth(10f);
        graph.getGridBackgroundPaint().setAlpha(0);
        plot.redraw();
        View nav = findViewById(R.id.navigation);
        nav.setVisibility(View.VISIBLE);
    }

}
