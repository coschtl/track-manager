package at.dcosta.tracks.track.file;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import java.util.Iterator;

import at.dcosta.tracks.CombatFactory;
import at.dcosta.tracks.Loader;
import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.Track;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.TrackReaderFactory;
import at.dcosta.tracks.track.TrackStatistic;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.validator.Validators;

public class TrackDirectoryAnalyzer {
    private final Context context;
    private final FileLocator fileLocator;
    private final Iterator<Content> trackDirIterator;
    private final TrackStatistic statistic;
    private final Track track;
    private final PathValidator pathValidator;
    private final TrackDbAdapter trackDbAdapter;
    private final ActivityFactory activityFactory;
    private final int trackCount;
    private int pos;
    private TrackDescriptionNG description;
    private Handler handler;

    public TrackDirectoryAnalyzer(Context context, TrackDbAdapter trackDbAdapter, ActivityFactory activityFactory, Uri trackFolderUri, PathValidator pathValidator) {
        this(context, trackDbAdapter, activityFactory, CombatFactory.getFileLocator(context), trackFolderUri, pathValidator);
    }

    public TrackDirectoryAnalyzer(Context context, TrackDbAdapter trackDbAdapter, ActivityFactory activityFactory, FileLocator fileLocator, Uri folder, PathValidator pathValidator) {
        long latestEndTimeEpochMillis = trackDbAdapter.findLatestEndTimeEpochMillis();
        this.statistic = new TrackStatistic();
        this.track = new Track();
        this.context = context;
        this.pathValidator = pathValidator;
        this.trackDirIterator = fileLocator.list(folder, latestEndTimeEpochMillis, true).filter(content -> TrackReaderFactory.canRead(content)).iterator();
        this.trackDbAdapter = trackDbAdapter;
        this.activityFactory = activityFactory;
        this.fileLocator = fileLocator;
        this.trackCount = fileLocator.getContentCount(folder, latestEndTimeEpochMillis);
    }

    public TrackDescriptionNG getDescription() {
        return description;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
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
            if (!available && handler != null) {
                Message msg = handler.obtainMessage();
                msg.arg1 = Loader.MSG_INCREMENT;
                handler.sendMessage(msg);
            }
        }
        return available;
    }

    private boolean readNextTrack() {
        Content content = trackDirIterator.next();
        if (trackDbAdapter.findEntryByPath(content.getFullPath()) != null) {
            return false;
        }
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
