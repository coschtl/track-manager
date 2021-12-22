package at.dcosta.tracks;

import android.content.Context;
import android.os.Build;

import at.dcosta.tracks.track.file.FileLocator;
import at.dcosta.tracks.track.file.LegacyFileLocator;
import at.dcosta.tracks.track.file.SAFFileLocator;

public class CombatFactory {

    public static FileLocator getFileLocator(Context context) {
        if (isLegacy()) {
            return new LegacyFileLocator();
        }
        return new SAFFileLocator(ctx(context));
    }

    private static Context ctx(Context context) {
        return context == null ? TrackManager.context() : context;
    }

    private static boolean isLegacy() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }
}
