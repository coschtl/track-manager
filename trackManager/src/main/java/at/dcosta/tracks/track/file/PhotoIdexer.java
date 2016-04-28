package at.dcosta.tracks.track.file;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.media.ExifInterface;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.TrackDescription;
import at.dcosta.tracks.util.Configuration;
import at.dcosta.tracks.util.PhotoRegistry;

public class PhotoIdexer {

	private static final SimpleDateFormat EXIF_DATE_FORMAT = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	private static final PhotoRegistry photoRegistry = new PhotoRegistry();

	private final PathValidator pathValidator;
	private final TrackDbAdapter trackDbAdapter;
	private final List<String> fileList;
	private int position;
	private final int maxId;
	private final long tolerance;
	private final Set<String> pathRegistry;

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

	private TrackDescription getExactMatch(Date date) {
		TrackDescription entry = trackDbAdapter.findPreviousEntry(date);
		if (entry != null && entry.getEndTime().after(date)) {
			return entry;
		}
		return null;
	}

	public int getPossibleCount() {
		return fileList.size();
	}

	private List<TrackDescription> getTolerantMatches(Date date) {
		List<TrackDescription> l = new ArrayList<TrackDescription>();
		Date dPlusTolerance = new Date(date.getTime() + tolerance);
		Date dMinusTolerance = new Date(date.getTime() - tolerance);
		TrackDescription entry = trackDbAdapter.findPreviousEntry(dPlusTolerance);
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
					TrackDescription exactEntry = getExactMatch(date);
					List<TrackDescription> entries = new ArrayList<TrackDescription>();
					if (exactEntry != null) {
						entries.add(exactEntry);
					}
					entries.addAll(getTolerantMatches(date));
					for (TrackDescription entry : entries) {
						// System.out.println("ENTRY: " + entry.getName() + ": " + entry.getStartTime().toGMTString() + " - " +
						// entry.getEndTime().toGMTString());
						if (!pathRegistry.contains(entry.getPath())) {
							// we get this entry for the first time
							// thererfore we have to remove all photos from the last time
							if (entry.getMultiValueExtra(TrackDescription.EXTRA_PHOTO) != null) {
								entry.getMultiValueExtra(TrackDescription.EXTRA_PHOTO).clear();
							}
							pathRegistry.add(entry.getPath());
						}
						entry.addMultiValueExtra(TrackDescription.EXTRA_PHOTO, path);
						trackDbAdapter.updateEntry(entry);
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// TODO: Wenn sich die Toleranz �ndert, muss die PhotoRegistry gel�scht werden
			photoRegistry.add(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processNext() {
		processFile(fileList.get(position++));
	}
}