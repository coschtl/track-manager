package at.dcosta.tracks.track.file;

import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.PhotoRegistry;

public class PhotoIdexer {

    private static final SimpleDateFormat EXIF_DATE_FORMAT = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private static final PhotoRegistry photoRegistry = new PhotoRegistry();

    private final PathValidator pathValidator;
    private final TrackDbAdapter trackDbAdapter;
    private final List<String> fileList;
    private final int maxId;
    private final long tolerance;
    private final Set<String> pathRegistry;
    private int position;

    public PhotoIdexer(TrackDbAdapter trackDbAdapter, String path) {
        this(trackDbAdapter, path, new PathValidator() {

            @Override
            public boolean isValid(String path) {
                return !photoRegistry.contains(path);
            }
        });
    }

    public PhotoIdexer(TrackDbAdapter trackDbAdapter, String path, PathValidator pathValidator) {
        this.trackDbAdapter = trackDbAdapter;
        this.pathValidator = pathValidator;
        fileList = new ArrayList<String>();
        if (path != null) {
            addFilePaths(new File(path), fileList);
        }
        maxId = fileList.size() - 1;
        tolerance = 60000l * Configuration.getInstance().getTrack_foto_tolerance();
        pathRegistry = new TreeSet<String>();
    }

    private void addFilePaths(File dir, List<String> filelist) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                addFilePaths(f, filelist);
            } else {
                String path = f.getAbsolutePath();
                if (pathValidator.isValid(path)) {
                    filelist.add(path);
                }
            }
        }
    }

    private TrackDescriptionNG getExactMatch(Date date) {
        TrackDescriptionNG entry = trackDbAdapter.findPreviousEntry(date);
        if (entry != null && entry.getEndTime().after(date)) {
            return entry;
        }
        return null;
    }

    public int getPossibleCount() {
        return fileList.size();
    }

    private List<TrackDescriptionNG> getTolerantMatches(Date date) {
        List<TrackDescriptionNG> l = new ArrayList<TrackDescriptionNG>();
        Date dPlusTolerance = new Date(date.getTime() + tolerance);
        Date dMinusTolerance = new Date(date.getTime() - tolerance);
        TrackDescriptionNG entry = trackDbAdapter.findPreviousEntry(dPlusTolerance);
        while (entry != null && entry.getEndTime().after(dMinusTolerance)) {
            l.add(entry);
            entry = trackDbAdapter.findPreviousEntry(entry.getStartTime());
        }
        return l;
    }

    public boolean hasNext() {
        if (position < maxId) {
            return true;
        }
        photoRegistry.persist();
        return false;
    }

    private void processFile(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
            if (dateTime != null) {
                try {
                    Date date = EXIF_DATE_FORMAT.parse(dateTime);
                    TrackDescriptionNG exactEntry = getExactMatch(date);
                    List<TrackDescriptionNG> entries = new ArrayList<TrackDescriptionNG>();
                    if (exactEntry != null) {
                        entries.add(exactEntry);
                    }
                    entries.addAll(getTolerantMatches(date));
                    for (TrackDescriptionNG entry : entries) {
                        // System.out.println("ENTRY: " + entry.getName() + ": " + entry.getStartTime().toGMTString() + " - " +
                        // entry.getEndTime().toGMTString());
                        if (!pathRegistry.contains(entry.getPath())) {
                            // we get this entry for the first time
                            // thererfore we have to remove all photos from the last time
                            if (entry.getMultiValueExtra(TrackDescriptionNG.EXTRA_PHOTO) != null) {
                                entry.getMultiValueExtra(TrackDescriptionNG.EXTRA_PHOTO).clear();
                            }
                            pathRegistry.add(entry.getPath());
                        }
                        entry.addMultiValueExtra(TrackDescriptionNG.EXTRA_PHOTO, path);
                        trackDbAdapter.updateEntry(entry);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                   throw new RuntimeException("error while processing path "+ path, e);
                }
            }
            photoRegistry.add(path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("error while processing path "+ path, e);
        }
    }

    public void processNext() {
        processFile(fileList.get(position++));
    }
}