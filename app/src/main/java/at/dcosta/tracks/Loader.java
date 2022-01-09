package at.dcosta.tracks;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import at.dcosta.android.fw.props.PropertyDbAdapter;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.file.LegacyFileLocator;
import at.dcosta.tracks.track.file.PathValidator;
import at.dcosta.tracks.track.file.TrackDirectoryAnalyzer;
import at.dcosta.tracks.track.file.legacy.PhotoIdexer;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.PhotoRegistry;
import at.dcosta.tracks.util.TrackActivity;

public class Loader extends Activity {

    public static final String KEY_ACTION = "action";
    public static final String ACTION_LOAD_ALL_TRACKS = "loadAll";
    public static final String ACTION_LOAD_NEW_TRACKS_AND_PHOTOS = "loadNewTracksAndPhotos";
    public static final String ACTION_RESCAN_PHOTOS = "rescanPhotos";
    private static final int MSG_INIT = 0;
    public static final int MSG_INCREMENT = 1;
    private static final int MSG_FINISHED = 3;
    private static final int PROGRESS_DIALOG_ID = 1;
    // Define the Handler that receives messages from the thread and update the progress
    final Handler handler = new MyHandler(this);
    private LoadThread loadThread;
    private ProgressDialog progressDialog;
    private String action;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        action = getIntent().getStringExtra(KEY_ACTION);

        showDialog(PROGRESS_DIALOG_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == PROGRESS_DIALOG_ID) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage(" Loading. Please wait ... ");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            return progressDialog;
        }
        return null;
    }

    @Override
    protected void onPause() {
        if (loadThread != null) {
            loadThread.closeDbs();
        }
        super.onPause();
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == PROGRESS_DIALOG_ID) {
            if (progressDialog.getMax() > 0) {
                progressDialog.setProgress(0);
            }
            if (ACTION_LOAD_ALL_TRACKS.equals(action)) {
                loadThread = new LoadPhotosAndTracksThread(handler, this, progressDialog, true);
            } else if (ACTION_LOAD_NEW_TRACKS_AND_PHOTOS.equals(action)) {
                loadThread = new LoadPhotosAndTracksThread(handler, this, progressDialog, false);
            } else if (ACTION_RESCAN_PHOTOS.equals(action)) {
                loadThread = new LegacyLoadPhotosThread(handler, this, progressDialog, true);
            }
            loadThread.start();
        }
    }

    private static class LoadPhotosAndTracksThread extends LoadThread {

        LoadPhotosAndTracksThread(Handler handler, Activity parent, ProgressDialog progressDialog, boolean fullReload) {
            super(handler, parent, progressDialog, fullReload);
        }

        @Override
        protected void doLoad() {
            new LoadTracksThread(handler, parent, progressDialog, fullReload).doLoad();
            if (CombatFactory.isLegacy()) {
                new LegacyLoadPhotosThread(handler, parent, progressDialog, fullReload).doLoad();
            }
        }

    }

    private static class LegacyLoadPhotosThread extends LoadThread {

        LegacyLoadPhotosThread(Handler handler, Activity parent, ProgressDialog progressDialog, boolean fullReload) {
            super(handler, parent, progressDialog, fullReload);
            if (fullReload) {
                PhotoRegistry.clearRegistry();
                trackDbAdapter.deleteAllExtras(TrackDescriptionNG.EXTRA_PHOTO);
            }
            setProgressBarTitle(R.string.loader_loading_photos);
        }

        @Override
        protected void doLoad() {
            for (Uri photoFolder : Configuration.getInstance().getPhotoFolders()) {
                PhotoIdexer photoIdexer = new PhotoIdexer(parent, trackDbAdapter, photoFolder, isFullReload());
                Message msg = handler.obtainMessage();
                msg.arg1 = MSG_INIT;
                msg.arg2 = photoIdexer.getPossibleCount();
                msg.obj = "Reading photos form '" + photoFolder + "'...";
                handler.sendMessage(msg);

                while (photoIdexer.hasNext()) {
                    photoIdexer.processNext();
                    msg = handler.obtainMessage();
                    msg.arg1 = MSG_INCREMENT;
                    handler.sendMessage(msg);
                }
            }
        }
    }

    private static abstract class LoadThread extends Thread implements PathValidator {
        final Handler handler;
        final Activity parent;
        final ProgressDialog progressDialog;
        final PropertyDbAdapter propertyDbAdapter;
        final TrackDbAdapter trackDbAdapter;
        final boolean fullReload;
        Set<String> completeList;

        LoadThread(Handler handler, Activity parent, ProgressDialog progressDialog, boolean fullReload) {
            this.handler = handler;
            this.parent = parent;
            this.progressDialog = progressDialog;
            this.fullReload = fullReload;
            propertyDbAdapter = Configuration.getInstance().getPropertyDbAdapter();
            trackDbAdapter = new TrackDbAdapter(Configuration.getInstance().getDatabaseHelper(), parent);
        }

        public void closeDbs() {
            trackDbAdapter.close();
            propertyDbAdapter.close();
        }

        protected abstract void doLoad();

        @Override
        public boolean isValid(String path) {
            if (fullReload) {
                return true;
            }
            return completeList != null && !completeList.contains(path);
        }

        public boolean isFullReload() {
            return fullReload;
        }

        @Override
        public void run() {
            completeList = null;
            doLoad();
            closeDbs();
            Message msg = handler.obtainMessage();
            msg.arg1 = MSG_FINISHED;
            handler.sendMessage(msg);
        }

        protected void setProgressBarTitle(final int id) {
            parent.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.setTitle(id);
                }
            });
        }
    }

    private static class LoadTracksThread extends LoadThread {

        private final Map<String, NameAndIcon> cache = new HashMap<String, NameAndIcon>();

        LoadTracksThread(Handler handler, Activity parent, ProgressDialog progressDialog, boolean fullReload) {
            super(handler, parent, progressDialog, fullReload);
            setProgressBarTitle(R.string.loader_loading_tracks);
            if (fullReload) {
                cacheEntries();
                trackDbAdapter.clear();
                Configuration.getInstance().getTrackCache().clear();
                PhotoRegistry.clearRegistry();
            }
        }

        private void analyzeDirectory(ActivityFactory activityFactory, Uri trackFolder) {
            TrackDirectoryAnalyzer trackDirectoryAnalyzer = new TrackDirectoryAnalyzer(parent, trackDbAdapter, activityFactory, trackFolder, this);
            analyzeDirectory( trackDirectoryAnalyzer, trackFolder.toString());
        }

        private void analyzeInternalDirectory(ActivityFactory activityFactory, File dir) {
            analyzeDirectory( new TrackDirectoryAnalyzer(parent, trackDbAdapter, activityFactory, new LegacyFileLocator(), Uri.parse(dir.getAbsolutePath()), this), dir.getAbsolutePath());
        }

        private void analyzeDirectory(TrackDirectoryAnalyzer analyzer, String trackLocation) {
            Message msg = handler.obtainMessage();
            msg.arg1 = MSG_INIT;
            msg.arg2 = analyzer.getPossibleTrackCount();
            msg.obj = "Reading tracks form '" + trackLocation + "'...";
            handler.sendMessage(msg);
            analyzer.setHandler(handler);

            while (analyzer.moveToNext()) {
                TrackDescriptionNG track = analyzer.getDescription();
                TrackDescriptionNG entry = trackDbAdapter.findEntryLikePath(track.getPathNoHash() + "%");
                boolean isNewEntry = (entry == null);
                if (fullReload || isNewEntry) {
                    long rowId = trackDbAdapter.createEntry(track);
                    if (rowId >= 0) {
                        Configuration.getInstance().getTrackCache().save(rowId, analyzer.getTrack().getPoints());
                        TrackDescriptionNG dbEntry = trackDbAdapter.fetchEntry(rowId);
                        NameAndIcon nameAndIcon = cache.get(dbEntry.getPathNoHash());
                        if (nameAndIcon != null) {
                            dbEntry.setName(nameAndIcon.name);
                            if (nameAndIcon.icon != null) {
                                TrackEdit.updateTrack(TrackManager.context(), dbEntry, nameAndIcon.icon, trackDbAdapter.getActivityFactory());
                            }
                            trackDbAdapter.updateEntry(dbEntry);
                        }
                    }
                } else if (!isNewEntry) {
                    if (!entry.getPath().equals(track.getPath())) {
                        entry.setPath(track.getPath());
                        trackDbAdapter.updateEntry(entry);
                    }
                }
                msg = handler.obtainMessage();
                msg.arg1 = MSG_INCREMENT;
                handler.sendMessage(msg);
            }
        }

        private void cacheEntries() {
            Iterator<TrackDescriptionNG> all = trackDbAdapter.fetchAllEntries(true);
            cache.clear();
            while (all.hasNext()) {
                TrackDescriptionNG descr = all.next();
                TrackActivity activity = descr.getActivity();
                cache.put(descr.getPathNoHash(), new NameAndIcon(descr.getName(), activity == null ? null : activity.getIcon()));
            }
        }

        @Override
        protected void doLoad() {
            completeList = trackDbAdapter.getAllTrackPaths(true);
            ActivityFactory activityFactory = new ActivityFactory(parent);
            Configuration config = Configuration.getInstance();
            for (Uri trackFolder : config.getTrackFolders()) {
                analyzeDirectory(activityFactory, trackFolder);
            }
            analyzeInternalDirectory(activityFactory, config.getRecordedTracksDir());
            analyzeInternalDirectory(activityFactory, config.getCopiedTracksDir());
        }

        private static final class NameAndIcon {
            String name, icon;

            public NameAndIcon(String name, String icon) {
                this.name = name;
                this.icon = icon;
            }
        }
    }

    private static class MyHandler extends Handler {

        WeakReference<Loader> loaderRef;

        MyHandler(Loader loader) {
            loaderRef = new WeakReference<Loader>(loader);
        }

        @Override
        public void handleMessage(Message msg) {

            Loader loader = loaderRef.get();
            switch (msg.arg1) {
                case MSG_INIT:
                    loader.progressDialog.setMax(msg.arg2);
                    if (loader.progressDialog.getMax() > 0) {
                        loader.progressDialog.setProgress(0);
                    }
                    loader.progressDialog.setMessage((String) msg.obj);
                    break;
                case MSG_INCREMENT:
                    loader.progressDialog.incrementProgressBy(1);
                    break;
                case MSG_FINISHED:
                    loader.dismissDialog(PROGRESS_DIALOG_ID);
                    loader.finish();
                    break;
            }
        }

    }

}
