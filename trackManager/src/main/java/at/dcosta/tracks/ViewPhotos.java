package at.dcosta.tracks;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import at.dcosta.tracks.util.BitmapUtil;
import at.dcosta.tracks.util.Configuration;

public class ViewPhotos extends Activity implements OnGestureListener, OnDoubleTapListener, OnTouchListener, OnScaleGestureListener {

	public static final String KEY_IMAGES = "images";

	private List<String> images;
	private ImageView photoView;
	private int pos;
	private Configuration config;
	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleGestureDetector;
	private BitmapUtil bitmapUtil;
	private Bitmap bitmapImage;
	private BitmapFactory.Options bitmapOptions;

	private final Matrix matrix = new Matrix();
	private final Matrix savedMatrix = new Matrix();
	private float defaultScale;
	private boolean zooming;

	private boolean landscape;

	private String getAktImage() {
		return images.get(pos);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		config = Configuration.getInstance();
		gestureDetector = new GestureDetector(this, this);
		scaleGestureDetector = new ScaleGestureDetector(this, this);
		bitmapUtil = new BitmapUtil(this);

		bitmapOptions = new BitmapFactory.Options();
		bitmapOptions.inTempStorage = new byte[16 * 1024];
		bitmapOptions.inScaled = true;
		bitmapOptions.inDither = true;
		bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		bitmapOptions.outWidth = bitmapUtil.getDisplayWidth();
		bitmapOptions.outHeight = bitmapUtil.getDisplayHeight();
		bitmapOptions.inTargetDensity = (int) bitmapUtil.getDisplayDensity();
		int sampleSize = Configuration.getInstance().getSingleValueDbProperty("photo.sampleSize").getIntValue(2);
		bitmapOptions.inSampleSize = sampleSize;
		setContentView(R.layout.photos);

		photoView = (ImageView) findViewById(R.id.photo);
		Bundle extras = getIntent().getExtras();
		images = extras.getStringArrayList(KEY_IMAGES);
		if (savedInstanceState != null) {
			pos = savedInstanceState.getInt("POS", 0);
		}
		showPhoto();
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		pos++;
		if (pos >= images.size()) {
			pos = 0;
		}
		showPhoto();
		// resetZoom();
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (zooming) {
			return false;
		}
		float velocity = landscape ? velocityY : velocityX;
		if (Math.abs(velocity) > config.getWipeSensitivity()) {
			if (velocityX > 0) {
				pos--;
				if (pos < 0) {
					pos = images.size() - 1;
				}
			} else {
				pos++;
				if (pos >= images.size()) {
					pos = 0;
				}
			}
			showPhoto();
			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		new ShowPhotoDetails(this).setPhotoPath(getAktImage()).show();
		// ignore
	}

	@Override
	protected void onPause() {
		recycleBitmap();
		super.onPause();
	}

	@Override
	protected void onResume() {
		showPhoto();
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		recycleBitmap();
		outState.putInt("POS", pos);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor());
		photoView.setImageMatrix(matrix);
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		zooming = true;
		savedMatrix.set(matrix);
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (zooming) {
			matrix.postTranslate(-1f * distanceX, -1f * distanceY);
			photoView.setImageMatrix(matrix);
			return true;
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// ignore
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent me) {
		return gestureDetector.onTouchEvent(me);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) {
			return true;
		} else if (scaleGestureDetector.onTouchEvent(event)) {
			return true;
		}
		return false;
	}

	private void recycleBitmap() {
		if (bitmapImage != null) {
			bitmapImage.recycle();
		}
	}

	private void resetZoom() {
		matrix.reset();
		matrix.postScale(defaultScale, defaultScale);
		photoView.setImageMatrix(matrix);
		zooming = false;
	}

	private void showPhoto() {
		recycleBitmap();
		String image = getAktImage();
		bitmapImage = BitmapFactory.decodeFile(image, bitmapOptions);

		if (bitmapImage.getWidth() > bitmapImage.getHeight()) {
			Matrix rotate = new Matrix();
			rotate.postRotate(90);
			landscape = true;
			bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), rotate, true);
		} else {
			landscape = false;
		}
		defaultScale = bitmapUtil.getScaleFactor(bitmapImage);
		photoView.setImageBitmap(bitmapImage);
		photoView.setScaleType(ScaleType.MATRIX);
		resetZoom();
	}

}