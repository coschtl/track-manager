package at.dcosta.tracks.util;

import android.content.Context;
import android.widget.Toast;

public class PermissionUtil {
    public static void showLocationPermissionMissingWarning(Context context) {
        showPermissionMissingWarning(context, "reading location");
    }

    public static void showPermissionMissingWarning(Context context, String description) {
        Toast.makeText(context, "Permission for " + description + " is missing!", Toast.LENGTH_LONG).show();
    }
}
