package at.dcosta.tracks.graph;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import at.dcosta.tracks.R;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.TrackDescription;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.MinMaxValue;

import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.FillDirection;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

public class TrackProfile extends Activity implements OnGestureListener, OnTouchListener, OnClickListener {

	private static enum MODE {
		HEIGHT_TIME, HEIGHT_DISTANCE;
	}

	private MODE mode = MODE.HEIGHT_TIME;

	private XYPlot mySimpleXYPlot;
	private GestureDetector gestureDetector;
	private String path;
	private TrackDbAdapter trackDbAdapter;
	private Configuration config;
	private TrackProfileData profileData;
	private final List<PointF> points = new ArrayList<PointF>();;

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
		mySimpleXYPlot.clear();
		drawGraph();
		moveCursorToLeft();
	}

	private void drawGraph() {
		LineAndPointFormatter seriesFormat = new LineAndPointFormatter(Color.rgb(255, 0, 0), // line color
				Color.rgb(255, 0, 0), // point color
				null, null, FillDirection.BOTTOM);

		TrackDescription track = trackDbAdapter.findEntryByPath(path);
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
		mySimpleXYPlot.setTitle(name == null ? new File(path).getName() : name);

		mySimpleXYPlot.addSeries(profileData.getXYSeries(), seriesFormat);
		mySimpleXYPlot.setDomainLabel(profileData.getXLabel(this));
		mySimpleXYPlot.setDomainValueFormat(profileData.getXFormat());

		mySimpleXYPlot.setRangeLabel(profileData.getYLabel(this));
		mySimpleXYPlot.setRangeValueFormat(profileData.getYFormat());

		MinMaxValue yMinMax = profileData.getYMinMax();
		long yInterval = profileData.getYInterval();
		yRangeMin = roundDown(yMinMax.minAsLong(), yInterval);
		yRangeMax = roundUp(yMinMax.maxAsLong(), yInterval);
		mySimpleXYPlot.setRangeBoundaries(yRangeMin, yRangeMax, BoundaryMode.FIXED);
		mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, yInterval);

		MinMaxValue xMinMax = profileData.getXMinMax();
		long domainInterval = profileData.getXInterval();
		xRangeMin = roundDown(xMinMax.minAsLong(), domainInterval);
		xRangeMax = roundUp(xMinMax.maxAsLong(), domainInterval);
		mySimpleXYPlot.setDomainBoundaries(xRangeMin, xRangeMax, BoundaryMode.FIXED);
		mySimpleXYPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, domainInterval);

		mySimpleXYPlot.getLegendWidget().setVisible(false);
		mySimpleXYPlot.getTitleWidget().setWidth(200f);
		mySimpleXYPlot.setOnTouchListener(this);

		mySimpleXYPlot.redraw();
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
			case R.id.but_prev:
				moveCursorToLeft();
				break;
			case R.id.but_next:
				moveCursorToRight();
				break;
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
			path = extras.getString(TrackDescription.KEY_PATH);
		}
		mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
		gestureDetector = new GestureDetector(this, this);

		drawGraph();
		findViewById(R.id.but_prev).setOnClickListener(this);
		findViewById(R.id.but_delete).setOnClickListener(this);
		findViewById(R.id.but_next).setOnClickListener(this);
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
			RectF gridRect = mySimpleXYPlot.getGraphWidget().getGridRect();
			if (gridRect != null) {
				XYSeries xySeries = profileData.getXYSeries();
				for (int i = 0; i < xySeries.size(); i++) {
					PointF valuePoint = new PointF(xySeries.getX(i).floatValue(), xySeries.getY(i).floatValue());
					PointF pixPoint = ValPixConverter.valToPix(valuePoint.x, valuePoint.y, gridRect, xRangeMin, xRangeMax, yRangeMin, yRangeMax);
					points.add(pixPoint);
				}
			}
		}
		return points;
	}

	private void removeCursors() {
		XYGraphWidget graphWidget = mySimpleXYPlot.getGraphWidget();
		graphWidget.setDomainCursorPosition(-1);
		graphWidget.setRangeCursorPosition(-1);
	}

	private long roundDown(long l, long interval) {
		return interval * (l / interval);
	}

	private long roundUp(long l, long interval) {
		return interval * ((l + interval) / interval);
	}

	private boolean showCursorAt(PointF point) {
		if (mySimpleXYPlot.containsPoint(point.x, point.y)) {
			mySimpleXYPlot.setCursorPosition(point.x, point.y);
			Paint cursor = mySimpleXYPlot.getGraphWidget().getCursorLabelPaint();
			cursor.setTextSize(25);
			mySimpleXYPlot.getGraphWidget().getCursorLabelBackgroundPaint().setAlpha(0);
			mySimpleXYPlot.redraw();
			return true;
		}
		return false;
	}

}
