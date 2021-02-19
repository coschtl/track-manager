package at.dcosta.tracks.graph;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.androidplot.util.PixelUtils;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import at.dcosta.tracks.R;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.PulseZone;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.TrackEkgData;
import at.dcosta.tracks.util.Configuration;

public class TrackEkg extends Activity {

    public static final int SELECTED_SEGMENT_OFFSET = 50;
    private static final DecimalFormat DF = new DecimalFormat("#0.#");
    public PieChart pie;
    private PieChart myPieChart;
    private String path;
    private TrackDbAdapter trackDbAdapter;
    private Configuration config;
    private long trackId;
    private TextView donutSizeTextView;
    private boolean active;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = Configuration.getInstance(this);
        setContentView(R.layout.track_ekg);
        trackDbAdapter = new TrackDbAdapter(config.getDatabaseHelper(), this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            path = extras.getString(TrackDescriptionNG.KEY_PATH);
        }
        TrackDescriptionNG track = trackDbAdapter.findEntryByPath(path);
        trackId = track.getId();
        Iterator<Point> pointIterator = config.getTrackCache().load(trackId).iterator();
        Date birthday = config.getBirthday();
        if (birthday == null) {
            Toast t = Toast.makeText(this, "Birthday not set!", Toast.LENGTH_LONG);
            t.show();
            active = false;
            finish();
            return;
        }
        active = true;
        TrackEkgData ekg = new TrackEkgData(birthday, config.isMale());
        while (pointIterator.hasNext()) {
            ekg.processPoint(pointIterator.next());
        }

        myPieChart = (PieChart) findViewById(R.id.myPieChart);
        myPieChart.getLegend().setVisible(false);

        final float padding = PixelUtils.dpToPix(30);
        myPieChart.getPie().setPadding(padding, padding, padding, padding);

        // detect segment clicks:
        myPieChart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                PointF click = new PointF(motionEvent.getX(), motionEvent.getY());
                if (myPieChart.getPie().containsPoint(click)) {
                    Segment segment = myPieChart.getRenderer(PieRenderer.class).getContainingSegment(click);

                    if (segment != null) {
                        final boolean isSelected = getFormatter(segment).getOffset() != 0;
                        deselectAll();
                        setSelected(segment, !isSelected);
                        myPieChart.redraw();
                    }
                }
                return false;
            }

            private SegmentFormatter getFormatter(Segment segment) {
                return myPieChart.getFormatter(segment, PieRenderer.class);
            }

            private void deselectAll() {
                List<Segment> segments = myPieChart.getRegistry().getSeriesList();
                for (Segment segment : segments) {
                    setSelected(segment, false);
                }
            }

            private void setSelected(Segment segment, boolean isSelected) {
                SegmentFormatter f = getFormatter(segment);
                if (isSelected) {
                    f.setOffset(SELECTED_SEGMENT_OFFSET);
                } else {
                    f.setOffset(0);
                }
            }
        });

        EmbossMaskFilter emf = new EmbossMaskFilter(
                new float[]{1, 1, 1}, 0.4f, 10, 8.2f);

        for (int i = 0; i < PulseZone.values().length; i++) {
            addSegment(PulseZone.fromZoneId(i), ekg, emf);
        }

        myPieChart.getBorderPaint().setColor(Color.TRANSPARENT);
        myPieChart.getBackgroundPaint().setColor(Color.TRANSPARENT);
        myPieChart.getTitle().getLabelPaint().setARGB(255, 150, 150, 150);

        int i = 0;
        addPercentToLegend(R.id.text_pulse_0, ekg, i++);
        addPercentToLegend(R.id.text_pulse_1_2, ekg, i++, i++);
        addPercentToLegend(R.id.text_pulse_3_4, ekg, i++, i++);
        addPercentToLegend(R.id.text_pulse_5_6, ekg, i++, i++);
        addPercentToLegend(R.id.text_pulse_7_8, ekg, i++, i++);
    }

    private void addPercentToLegend(int id, TrackEkgData ekg, int... zoneIds) {
        TextView tf = (TextView) findViewById(id);
        float sum = 0;
        for (int zoneId : zoneIds) {
            sum += ekg.getZonePercentExact(zoneId);
        }
        if (sum > 0.09) {
            tf.setText(tf.getText() + ": " + DF.format(sum) + "%");
        }
    }

    private void addSegment(PulseZone pulseZone, TrackEkgData ekg, EmbossMaskFilter emf) {
        int zonePercent = ekg.getZonePercent(pulseZone.getZoneId());
        if (zonePercent > 0) {
            Context ctx = getApplicationContext();
            int colorId = ctx.getResources().getIdentifier("pulse_" + pulseZone.getZoneId(), "color", ctx.getApplicationInfo().packageName);
            int color = ctx.getResources().getColor(colorId);
            SegmentFormatter sf = new SegmentFormatter(this, R.xml.pie_segment_formatter);
            sf.getLabelPaint().setShadowLayer(3, 0, 0, Color.BLACK);
            sf.getFillPaint().setMaskFilter(emf);
            sf.getFillPaint().setColor(color);
            myPieChart.addSegment(new Segment(pulseZone.getLabel(), zonePercent), sf);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setupIntroAnimation();
    }

    protected void setupIntroAnimation() {
        if (!active) {
            return;
        }

        final PieRenderer renderer = myPieChart.getRenderer(PieRenderer.class);
        // start with a zero degrees pie:

        renderer.setExtentDegs(0);
        // animate a scale value from a starting val of 0 to a final value of 1:
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);

        // use an animation pattern that begins and ends slowly:
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float scale = valueAnimator.getAnimatedFraction();
                renderer.setExtentDegs(360 * scale);
                myPieChart.redraw();
            }
        });

        // the animation will run for 1.0 seconds:
        animator.setDuration(1000);
        animator.start();
    }

    @Override
    protected void onPause() {
        trackDbAdapter.close();
        super.onPause();
    }

}
