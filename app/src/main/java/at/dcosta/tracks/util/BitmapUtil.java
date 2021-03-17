package at.dcosta.tracks.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.view.Display;

import java.io.IOException;

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
        return Math.min(h, w);
    }

    public int getScaleFactor(String path) throws IOException {
        ExifInterface exif = new ExifInterface(path);
        int l = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, -1);
        return Math.max(Math.max(displayHeight, displayWidth) / l, Math.min(displayHeight, displayWidth) / l);
    }

}
