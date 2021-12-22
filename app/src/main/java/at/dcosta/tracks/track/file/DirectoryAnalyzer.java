package at.dcosta.tracks.track.file;

import android.content.Context;
import android.net.Uri;

import java.util.Iterator;

import at.dcosta.tracks.CombatFactory;
import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.track.Track;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.TrackReaderFactory;
import at.dcosta.tracks.track.TrackStatistic;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.validator.Validators;

public class DirectoryAnalyzer {
    private final Context context;
    private final FileLocator fileLocator;
    private final Iterator<Content> trackDirIterator;
    private final TrackStatistic statistic;
    private final Track track;
    private final PathValidator pathValidator;
    private final ActivityFactory activityFactory;
    private final int trackCount;
    private int pos;
    private TrackDescriptionNG description;


    public DirectoryAnalyzer(Context context, ActivityFactory activityFactory, Uri trackFolderUri) {
        this(context, activityFactory, trackFolderUri, new PathValidator() {

            @Override
            public boolean isValid(String path) {
                return true;
            }
        });
    }

    public DirectoryAnalyzer(Context context, ActivityFactory activityFactory, Uri trackFolderUri, PathValidator pathValidator) {
        this(context, activityFactory, CombatFactory.getFileLocator(context), trackFolderUri, pathValidator);
    }

    public DirectoryAnalyzer(Context context, ActivityFactory activityFactory, FileLocator fileLocator, Uri folder, PathValidator pathValidator) {
        statistic = new TrackStatistic();
        track = new Track();
        this.context = context;
        this.pathValidator = pathValidator;
        this.trackDirIterator = fileLocator.list(folder, true).iterator();
        this.activityFactory = activityFactory;
        this.fileLocator = fileLocator;
        this.trackCount = fileLocator.getContentCount(folder);
    }

    public TrackDescriptionNG getDescription() {
        return description;
    }

    public int getPossibleTrackCount() {
        return trackCount;
    }

    public Track getTrack() {
        return track;
    }

    public boolean moveToNext() {
        boolean available = false;
        while (!available && trackDirIterator.hasNext()) {
            available = readNextTrack();
        }
        return available;
    }

    private boolean readNextTrack() {
        Content content = trackDirIterator.next();
        try {
            statistic.reset();
            track.clear();
            TrackReader reader = TrackReaderFactory.getTrackReader(content, Validators.DEFAULT);
            reader.setListener(statistic, track);
            reader.readTrack();
            description = new TrackDescriptionNG(reader.getTrackName(), content.getFullPath(), statistic, activityFactory);
            return true;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading track '" + content.getName() + "': " + e.toString());
        }
        return false;
    }

}
