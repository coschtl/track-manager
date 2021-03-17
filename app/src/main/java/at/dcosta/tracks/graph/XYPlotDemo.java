package at.dcosta.tracks.graph;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.androidplot.ui.Anchor;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.ScalingXYSeries;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import at.dcosta.tracks.R;

/**
 * Demonstrates animating XYSeries data from a zero value up/down to the actual values set.  Once
 * the animation completes, labels for each point are made visible.
 * <p>
 * IMPORTANT: This example makes use of {@link ValueAnimator} which is only available in
 * SDK level 11 and later..
 */
public class XYPlotDemo extends Activity {

    private XYPlot plot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_profile);

        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        // create a couple arrays of y-values to plot:
        final Number[] seriesTimes = {1554497596000L, 1554497716000L, 1554497795000L, 1554497820000L, 1554497832000L, 1554497892000L, 1554498132000L, 1554498306000L, 1554498360000L, 1554498373000L};
        Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};
        Number[] series2Numbers = {5, 2, 10, 50, 20, 10, 40, 20, 80, 40};
        Number[] series3Numbers = {85, 82, 60, 90, 82, 87, 150, 173, 100, 70};

        // turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(seriesTimes), Arrays.asList(series1Numbers), "Height");
        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(seriesTimes), Arrays.asList(series2Numbers), "Distance");
        XYSeries series3 = new SimpleXYSeries(
                Arrays.asList(seriesTimes), Arrays.asList(series3Numbers), "Pulse");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        final LineAndPointFormatter series1Format =
                new LineAndPointFormatter(this, R.xml.speed_line);
        series1Format.setPointLabeler(null);

        final LineAndPointFormatter series2Format =
                new LineAndPointFormatter(this, R.xml.speed_line);
        series2Format.getLinePaint().setColor(Color.RED);
        series2Format.setPointLabeler(null);

        final LineAndPointFormatter series3Format =
                new LineAndPointFormatter(this, R.xml.speed_line);
        series3Format.getLinePaint().setColor(Color.CYAN);
        series3Format.setPointLabeler(null);

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        series1Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        series2Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        series3Format.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Uniform));

        // wrap each series in instances of ScalingXYSeries before adding to the plot
        // so that we can animate the series values below:
        final ScalingXYSeries scalingSeries1 = new ScalingXYSeries(series1, 0, ScalingXYSeries.Mode.Y_ONLY);
        plot.addSeries(scalingSeries1, series1Format);

        final ScalingXYSeries scalingSeries2 = new ScalingXYSeries(series2, 0, ScalingXYSeries.Mode.Y_ONLY);
        plot.addSeries(scalingSeries2, series2Format);

        final ScalingXYSeries scalingSeries3 = new ScalingXYSeries(series3, 0, ScalingXYSeries.Mode.Y_ONLY);
        plot.addSeries(scalingSeries3, series3Format);

        final long start = 1554497550000L;
        final long end = 1554498450000L;

        plot.getGraph().setMarginRight(100);
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM)
                .setFormat(new Format() {
                    private final DateFormat dateFormat = new SimpleDateFormat("hh:mm");

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

                    @Override
                    public StringBuffer format(Object obj,
                                               @NonNull StringBuffer toAppendTo,
                                               @NonNull FieldPosition pos) {
                        int i = ((Number) obj).intValue();
                        System.out.println("LEFT: " + i);

                        //die werte müssen auf den DAtenbereich 0...100 normiert werden
                        // also min der Series=0, max der Series = 100
                        if (i == 0) {
                            return toAppendTo;
                        }
                        return toAppendTo.append(i * 20).append("~").append(i / 5f);
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
                        System.out.println("Right: " + i);
                        if (i == 0) {
                            return toAppendTo;
                        }
                        return toAppendTo.append(i);
                    }

                    @Override
                    public Object parseObject(String source, @NonNull ParsePosition pos) {
                        return null;
                    }
                });

        plot.setRangeBoundaries(0, 200, BoundaryMode.FIXED);
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
                paint.setColor(Color.GREEN);
                paint.setTextSize(textSize);
                int pos = text.indexOf('~');
                super.drawLabel(canvas, text.substring(0, pos), paint, x, y - textMove, isOrigin);
                paint.setColor(Color.RED);
                super.drawLabel(canvas, text.substring(pos + 1), paint, x, y + textMove, isOrigin);
            }

            @Override
            public void drawLabel(Canvas canvas, XYGraphWidget.LineLabelStyle style, Number val, float x, float y, boolean isOrigin) {
                super.drawLabel(canvas, style, val, x, y, isOrigin);
            }
        });
        plot.getGraph().setLineLabelRenderer(XYGraphWidget.Edge.RIGHT, new XYGraphWidget.LineLabelRenderer() {
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
                paint.setColor(Color.GREEN);
                paint.setTextSize(textSize);
                int pos = text.indexOf('~');
                if (pos > 0) {
                    super.drawLabel(canvas, text.substring(0, pos), paint, x, y - textMove, isOrigin);
                    paint.setColor(Color.RED);
                    super.drawLabel(canvas, text.substring(pos + 1), paint, x, y + textMove, isOrigin);
                } else {
                    paint.setColor(Color.CYAN);
                    super.drawLabel(canvas, text, paint, x + 10, y, isOrigin);
                }
            }

            @Override
            public void drawLabel(Canvas canvas, XYGraphWidget.LineLabelStyle style, Number val, float x, float y, boolean isOrigin) {
                super.drawLabel(canvas, style, val, x, y, isOrigin);
            }
        });
        plot.setRangeStep(StepMode.SUBDIVIDE, 11);
        plot.setRangeLabel(null);
        plot.setDomainLabel(null);
        plot.setDomainBoundaries(start, end, BoundaryMode.FIXED);
        long stepCount = (end - start) / 150000L + 1;
        plot.setDomainStep(StepMode.SUBDIVIDE, stepCount);

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
                scalingSeries1.setScale(scale);
                scalingSeries2.setScale(scale);
                scalingSeries3.setScale(scale);
                plot.redraw();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // the animation is over, so show point labels:
                series1Format.getPointLabelFormatter().getTextPaint().setColor(Color.WHITE);
                series2Format.getPointLabelFormatter().getTextPaint().setColor(Color.WHITE);
                series3Format.getPointLabelFormatter().getTextPaint().setColor(Color.WHITE);
                plot.redraw();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        // the animation will run for 1.5 seconds:
        animator.setDuration(1500);
        animator.start();
    }
}
