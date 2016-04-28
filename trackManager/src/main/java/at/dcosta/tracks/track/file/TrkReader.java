package at.dcosta.tracks.track.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import at.dcosta.android.fw.IOUtil;
import at.dcosta.tracks.track.Point;
import at.dcosta.tracks.track.Waypoint;
import at.dcosta.tracks.validator.DistanceValidator;

public class TrkReader extends TrackReader {

	private enum Status {
		START, CONTENT;
	}

	public static final String EXTENSION = "trk";
	public static final String SUFFIX = "." + EXTENSION;

	private static final String MARKER_BEGIN_POINTS = "--start--";

	private Status status = Status.START;

	public TrkReader(File trackfile, DistanceValidator validator) {
		super(trackfile, validator);
	}

	@Override
	public TrkReader readTrack() throws ParsingException {
		if (listener == null) {
			return this;
		}
		InputStream in = null;
		String line = null;
		try {
			in = new FileInputStream(trackfile);
			BufferedReader trk = new BufferedReader(new InputStreamReader(in), 64);
			Point point;
			while ((line = trk.readLine()) != null && pointsRead++ < readLimit) {
				switch (status) {
					case START:
						if (MARKER_BEGIN_POINTS.equals(line)) {
							status = Status.CONTENT;
						}
						break;
					case CONTENT:
						if (line.length() > 7) {
							int pos = line.indexOf(')');
							if (pos > -1) {
								point = new Point(line.substring(1, pos));
								if (line.length() > pos + 2) {
									point = new Waypoint(point, line.substring(pos + 2));
								}
							} else {
								point = new Point(line);
							}
							updateListener(point);
						}
						break;
				}
			}
			return this;
		} catch (Exception e) {
			throw new ParsingException("Parsing error in file '" + trackfile + "':" + (line == null ? "" : line), e);
		} finally {
			IOUtil.close(in);
		}
	}
}
