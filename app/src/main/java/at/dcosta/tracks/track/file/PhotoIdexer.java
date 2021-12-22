package at.dcosta.tracks.track.file;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import at.dcosta.tracks.CombatFactory;
import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.PhotoRegistry;

public class PhotoIdexer {

    private static final Logger LOGGER = Logger.getLogger(PhotoIdexer.class.getName());
    private static final SimpleDateFormat EXIF_DATE_FORMAT = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private static final PhotoRegistry PHOTO_REGISTRY = new PhotoRegistry();

    private final PathValidator pathValidator;
    private final TrackDbAdapter trackDbAdapter;
    private final List<Content> fileList;
    private final int maxId;
    private final long tolerance;
    private final Set<String> pathRegistry;
    private int position;

    public PhotoIdexer(Context context, TrackDbAdapter trackDbAdapter, Uri photoPath) {
        this(context, trackDbAdapter, photoPath, new PathValidator() {

            @Override
            public boolean isValid(String path) {
                return !PHOTO_REGISTRY.contains(path);
            }
        });
    }

    public static void clear() {
        PHOTO_REGISTRY.clear();
    }

    public PhotoIdexer(Context context, TrackDbAdapter trackDbAdapter, Uri photoPath, PathValidator pathValidator) {
        this.trackDbAdapter = trackDbAdapter;
        this.pathValidator = pathValidator;
        fileList = new ArrayList<>();
        if (photoPath != null) {
            addFilePaths(context, photoPath, fileList);
        }
        maxId = fileList.size() - 1;
        tolerance = 60000L * Configuration.getInstance().getTrack_foto_tolerance();
        pathRegistry = new TreeSet<String>();
    }

    private void addFilePaths(Context context, Uri photoPath, List<Content> filelist) {
        filelist.addAll(CombatFactory.getFileLocator(context).list(photoPath).collect(Collectors.toList()));
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
        PHOTO_REGISTRY.persist();
        return false;
    }

    private void processFile(Content image) {
        try (InputStream in = image.getInputStream()) {
            LOGGER.info("processing image " + image.getName());
            ExifInterface exif = new ExifInterface(in);
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
                        entry.addMultiValueExtra(TrackDescriptionNG.EXTRA_PHOTO, image.getFullPath());
                        trackDbAdapter.updateEntry(entry);
                    }
                } catch (ParseException e) {
                    LOGGER.severe("ERROR processing image " + image.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException("error while processing path " + image.getName(), e);
                }
            }
            PHOTO_REGISTRY.add(image.getFullPath());
        } catch (Exception e) {
            LOGGER.severe("ERROR processing image " + image.getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("error while processing path " + image.getName(), e);
        }
    }

    public void processNext() {
        processFile(fileList.get(position++));
    }
}