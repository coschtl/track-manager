package at.dcosta.tracks.track.file;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.tracks.TrackManager;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.Photo;

public class PhotoFinder {
    private static final String[] COLS = new String[]{
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.ImageColumns.ORIENTATION,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.WIDTH
    };

    private static final String WHERE = MediaStore.Images.Media.MIME_TYPE + "=? AND "
            + MediaStore.Images.Media.DATE_TAKEN + ">=? AND "
            + MediaStore.Images.Media.DATE_TAKEN + "<=?";

    public static List<Photo> findPhotos(Context context, Date from, Date to) {
        long tolerance = 60000L * Configuration.getInstance().getTrackFotoTolerance();
        String fromEpoch = Long.toString(from.getTime() + TimeZone.getDefault().getOffset(from.getTime()) - tolerance);
        String toEpoch = Long.toString(to.getTime() + TimeZone.getDefault().getOffset(to.getTime()) + tolerance);
        List<Photo> photos = new ArrayList<>();
        addPhotos(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fromEpoch, toEpoch, photos);
        addPhotos(MediaStore.Images.Media.INTERNAL_CONTENT_URI, fromEpoch, toEpoch, photos);
        Collections.sort(photos, new Comparator<Photo>() {
            @Override
            public int compare(Photo p1, Photo p2) {
                return (int) (p1.getCreatedOn() - p2.getCreatedOn());
            }
        });
        return photos;
    }

    private static void addPhotos(Uri contentUri, String fromEpoch, String toEpoch, List<Photo> photos) {
        Cursor cur = null;
        try {
            cur = TrackManager.context().getContentResolver().query(contentUri, COLS,
                    WHERE,
                    new String[]{"image/jpeg", fromEpoch, toEpoch},
                    MediaStore.Images.Media.DATE_TAKEN + " DESC"
            );
            if (cur.moveToFirst()) {
                int dateCol = cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                int dataCol = cur.getColumnIndex(MediaStore.Images.Media.DATA);
                int orCol = cur.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION);
                int hCol = cur.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT);
                int wCol = cur.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH);
                do {
                    Photo photo = new Photo();
                    photo.setPath(cur.getString(dataCol));
                    photo.setCreatedOn(cur.getLong(dateCol));
                    photo.setWidth(cur.getInt(wCol));
                    photo.setHeight(cur.getInt(hCol));
                    photo.setOrientation(cur.getInt(orCol));
                    photos.add(photo);
                } while (cur.moveToNext());
            }
        } finally {
            IOUtil.close(cur, true);
        }
    }
}
