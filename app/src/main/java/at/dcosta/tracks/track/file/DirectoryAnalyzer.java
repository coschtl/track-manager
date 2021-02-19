package at.dcosta.tracks.track.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.dcosta.tracks.track.Track;
import at.dcosta.tracks.track.TrackDescriptionNG;
import at.dcosta.tracks.track.TrackReaderFactory;
import at.dcosta.tracks.track.TrackStatistic;
import at.dcosta.tracks.util.ActivityFactory;
import at.dcosta.tracks.validator.Validators;

public class DirectoryAnalyzer {

	private final List<File> files;
	private final TrackStatistic statistic;
	private final Track track;
	private final PathValidator pathValidator;
	private final ActivityFactory activityFactory;
	private int pos;
	private TrackDescriptionNG description;

	public DirectoryAnalyzer(ActivityFactory activityFactory, String path) {
		this(activityFactory, path, new PathValidator() {

			@Override
			public boolean isValid(String path) {
				return true;
			}
		});
	}

	public DirectoryAnalyzer(ActivityFactory activityFactory, String path, PathValidator pathValidator) {
		files = new ArrayList<File>();
		statistic = new TrackStatistic();
		track = new Track();
		this.pathValidator = pathValidator;
		this.activityFactory = activityFactory;
		init(path);
	}

	public TrackDescriptionNG getDescription() {
		return description;
	}

	public int getPossibleTrackCount() {
		return files.size();
	}

	public Track getTrack() {
		return track;
	}

	private void init(String path) {
		if (path == null) {
			return;
		}
		File dir = new File(path);
		for (File f : dir.listFiles()) {
			if (f.isFile() && pathValidator.isValid(f.getAbsolutePath())) {
				files.add(f);
			}
		}
	}

	public boolean moveToNext() {
		boolean available = false;
		while (pos < files.size() && !available) {
			available = readNextTrack();
		}
		return available;
	}

	private boolean readNextTrack() {
		File trackfile = files.get(pos++);
		try {
			statistic.reset();
			track.clear();
			TrackReader reader = TrackReaderFactory.getTrackReader(trackfile, Validators.DEFAULT);
			reader.setListener(statistic, track);
			reader.readTrack();
			description = new TrackDescriptionNG(reader.getTrackName(), trackfile.getAbsolutePath(), statistic, activityFactory);
			return true;
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error loading track '" + trackfile.getAbsolutePath() + "': " + e.toString());
		}
		return false;
	}

}
