package at.dcosta.tracks;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import at.dcosta.android.fw.props.Property;
import at.dcosta.android.fw.props.PropertyDbAdapter;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.TrackDescription;
import at.dcosta.tracks.track.file.DirectoryAnalyzer;
import at.dcosta.tracks.track.file.PathValidator;
import at.dcosta.tracks.track.file.PhotoIdexer;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.PhotoRegistry;
import at.dcosta.tracks.util.TrackActivity;

public class Loader extends Activity {

	private static class LoadPhotosAndTracksThread extends LoadThread {

		LoadPhotosAndTracksThread(Handler handler, Activity parent, ProgressDialog progressDialog, boolean fullReload) {
			super(handler, parent, progressDialog, fullReload);
		}

		@Override
		protected void doLoad() {
			new LoadTracksThread(handler, parent, progressDialog, fullReload).doLoad();
			new LoadPhotosThread(handler, parent, progressDialog, fullReload).doLoad();
		}

	}

	private static class LoadPhotosThread extends LoadThread {

		LoadPhotosThread(Handler handler, Activity parent, ProgressDialog progressDialog, boolean fullReload) {
			super(handler, parent, progressDialog, fullReload);
			if (fullReload) {
				new PhotoRegistry().clear().persist();
				trackDbAdapter.deleteAllExtras(TrackDescription.EXTRA_PHOTO);
			}
			setProgressBarTitle(R.string.loader_loading_photos);
		}

		@Override
		protected void doLoad() {
			Iterator<Property> photoDirs = propertyDbAdapter.fetchAllProperties(Configuration.PROPERTY_PHOTO_FOLDER);
			while (photoDirs.hasNext()) {
				String path = photoDirs.next().getValue();
				PhotoIdexer photoIdexer = new PhotoIdexer(trackDbAdapter, path);
				Message msg = handler.obtainMessage();
				msg.arg1 = MSG_INIT;
				msg.arg2 = photoIdexer.getPossibleCount();
				msg.obj = "Reading photos form '" + path + "'...";
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
		Set<String> completeList;
		final Handler handler;
		final Activity parent;
		final ProgressDialog progressDialog;
		final PropertyDbAdapter propertyDbAdapter;
		final TrackDbAdapter trackDbAdapter;
		final boolean fullReload;

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

		private static final class NameAndIcon {
			String name, icon;

			public NameAndIcon(String name, String icon) {
				this.name = name;
				this.icon = icon;
			}
		}

		private final Map<String, NameAndIcon> cache = new HashMap<String, NameAndIcon>();

		LoadTracksThread(Handler handler, Activity parent, ProgressDialog progressDialog, boolean fullReload) {
			super(handler, parent, progressDialog, fullReload);
			setProgressBarTitle(R.string.loader_loading_tracks);
			if (fullReload) {
				cacheEntries();
				trackDbAdapter.clear();
				Configuration.getInstance().getTrackCache().clear();
				new PhotoRegistry().clear().persist();
			}
		}

		private void analyzeDirectory(ActivityFactory activityFactory, String path) {
			DirectoryAnalyzer analyzer = new DirectoryAnalyzer(activityFactory, path, this);

			Message msg = handler.obtainMessage();
			msg.arg1 = MSG_INIT;
			msg.arg2 = analyzer.getPossibleTrackCount();
			msg.obj = "Reading tracks form '" + path + "'...";
			handler.sendMessage(msg);

			while (analyzer.moveToNext()) {
				TrackDescription track = analyzer.getDescription();
				TrackDescription entry = trackDbAdapter.findEntryLikePath(track.getPathNoHash() + "%");
				boolean isNewEntry = entry == null;
				if (fullReload || isNewEntry) {
					long rowId = trackDbAdapter.createEntry(track);
					if (rowId >= 0) {
						Configuration.getInstance().getTrackCache().save(rowId, analyzer.getTrack().getPoints());
						TrackDescription dbEntry = trackDbAdapter.fetchEntry(rowId);
						NameAndIcon nameAndIcon = cache.get(dbEntry.getPathNoHash());
						if (nameAndIcon != null) {
							dbEntry.setName(nameAndIcon.name);
							if (nameAndIcon.icon != null) {
								TrackEdit.updateTrack(dbEntry, nameAndIcon.icon, trackDbAdapter.getActivityFactory());
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
			Iterator<TrackDescription> all = trackDbAdapter.fetchAllEntries(true);
			cache.clear();
			while (all.hasNext()) {
				TrackDescription descr = all.next();
				TrackActivity activity = descr.getActivity();
				cache.put(descr.getPathNoHash(), new NameAndIcon(descr.getName(), activity == null ? null : activity.getIcon()));
			}
		}

		@Override
		protected void doLoad() {
			completeList = trackDbAdapter.getAllTrackPaths(true);
			ActivityFactory activityFactory = new ActivityFactory(parent);
			Iterator<Property> propIter = propertyDbAdapter.fetchAllProperties(Configuration.PROPERTY_TRACK_FOLDER);
			while (propIter.hasNext()) {
				Property property = propIter.next();
				String path = property.getValue();
				analyzeDirectory(activityFactory, path);
			}
			analyzeDirectory(activityFactory, Configuration.getInstance().getRecordedTracksDir().getAbsolutePath());
			analyzeDirectory(activityFactory, Configuration.getInstance().getCopiedTracksDir().getAbsolutePath());
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

	public static final String KEY_ACTION = "action";
	public static final String ACTION_LOAD_ALL_TRACKS = "loadAll";
	public static final String ACTION_LOAD_NEW_TRACKS_AND_PHOTOS = "loadNewTracksAndPhotos";
	public static final String ACTION_RESCAN_PHOTOS = "rescanPhotos";

	private static final int MSG_INIT = 0;
	private static final int MSG_INCREMENT = 1;
	private static final int MSG_FINISHED = 3;
	private static final int PROGRESS_DIALOG_ID = 1;

	private LoadThread loadThread;
	private ProgressDialog progressDialog;
	private String action;

	// Define the Handler that receives messages from the thread and update the progress
	final Handler handler = new MyHandler(this);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		action = extras.getString(KEY_ACTION);

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
		switch (id) {
			case PROGRESS_DIALOG_ID:
				if (progressDialog.getMax() > 0) {
					progressDialog.setProgress(0);
				}
				if (ACTION_LOAD_ALL_TRACKS.equals(action)) {
					loadThread = new LoadPhotosAndTracksThread(handler, this, progressDialog, true);
				} else if (ACTION_LOAD_NEW_TRACKS_AND_PHOTOS.equals(action)) {
					loadThread = new LoadPhotosAndTracksThread(handler, this, progressDialog, false);
				} else if (ACTION_RESCAN_PHOTOS.equals(action)) {
					loadThread = new LoadPhotosThread(handler, this, progressDialog, true);
				}
				loadThread.start();
		}
	}

}
