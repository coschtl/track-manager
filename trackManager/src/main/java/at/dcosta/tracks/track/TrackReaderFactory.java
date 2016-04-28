package at.dcosta.tracks.track;

import java.io.File;

import at.dcosta.tracks.track.file.GpxSaxReader;
import at.dcosta.tracks.track.file.TmgrReader;
import at.dcosta.tracks.track.file.TrackReader;
import at.dcosta.tracks.track.file.TrkReader;
import at.dcosta.tracks.validator.DistanceValidator;

public class TrackReaderFactory {

	public static TrackReader getTrackReader(File trackfile, DistanceValidator validator) {
		String name = trackfile.getName();
		int pos = name.lastIndexOf('.');
		if (pos < 0) {
			throw new IllegalArgumentException("Can not determine Reader from filename '" + name + "'. Make sure that the filename has an extension!");
		}
		name = name.substring(pos + 1).toLowerCase();
		if (TrkReader.EXTENSION.equals(name)) {
			return new TrkReader(trackfile, validator);
		} else if (GpxSaxReader.EXTENSION.equals(name)) {
			return new GpxSaxReader(trackfile, validator);
			// return new GpxReader(trackfile, validator);
		} else if (TmgrReader.EXTENSION.equals(name)) {
			return new TmgrReader(trackfile, validator);
		}
		throw new IllegalArgumentException("Unknown file type: " + name);
	}

}
