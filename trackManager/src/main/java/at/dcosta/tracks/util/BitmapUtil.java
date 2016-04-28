package at.dcosta.tracks.util;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.view.Display;

public class BitmapUtil {

	private final int displayWidth, displayHeight;
	private final float density;

	public BitmapUtil(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		displayWidth = display.getWidth();
		displayHeight = display.getHeight();
		density = activity.getResources().getDisplayMetrics().density;
	}

	public float getDisplayDensity() {
		return density;
	}

	public int getDisplayHeight() {
		return displayHeight;
	}

	public int getDisplayWidth() {
		return displayWidth;
	}

	public float getScaleFactor(Bitmap bitmapImage) {
		float h = (float) getDisplayHeight() / bitmapImage.getHeight();
		float w = (float) getDisplayWidth() / bitmapImage.getWidth();
		return h < w ? h : w;
	}

	public int getScaleFactor(String path) throws IOException {
		ExifInterface exif = new ExifInterface(path);
		int l = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
		// int w = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, -1);
		int scaleFactor = Math.max(Math.max(displayHeight, displayWidth) / Math.max(l, l), Math.min(displayHeight, displayWidth) / Math.min(l, l));
		// System.out.println("l: " + l + ", w: " + w + ", scaleFactor=" + scaleFactor);
		return scaleFactor;
	}

}
