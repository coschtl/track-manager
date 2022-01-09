package at.dcosta.tracks.track;

import at.dcosta.tracks.combat.Content;
import at.dcosta.tracks.track.file.GpxSaxReader;
import at.dcosta.tracks.track.file.TmgrReader;
import at.dcosta.tracks.track.file.TrackReader;
import at.dcosta.tracks.track.file.TrkReader;
import at.dcosta.tracks.validator.DistanceValidator;

public class TrackReaderFactory {

    public static boolean canRead(Content trackContent) {
        String suffix = getLowercaseSuffix(trackContent);
        if (suffix == null) {
            return false;
        }
        return TrkReader.EXTENSION.equals(suffix) || GpxSaxReader.EXTENSION.equals(suffix) || TmgrReader.EXTENSION.equals(suffix);
    }

    private static String getLowercaseSuffix(Content trackContent) {
        String name = trackContent.getName();
        int pos = name.lastIndexOf('.');
        if (pos < 0) {
            return null;
        }
        return name.substring(pos + 1).toLowerCase();
    }

    public static TrackReader getTrackReader(Content trackContent, DistanceValidator validator) {
        String suffix = getLowercaseSuffix(trackContent);
        if (suffix == null) {
            throw new IllegalArgumentException("Can not determine Reader from filename '" + trackContent.getName() + "'. Make sure that the filename has an extension!");
        }
        if (TrkReader.EXTENSION.equals(suffix)) {
            return new TrkReader(trackContent, validator);
        } else if (GpxSaxReader.EXTENSION.equals(suffix)) {
            return new GpxSaxReader(trackContent, validator);
            // return new GpxReader(trackfile, validator);
        } else if (TmgrReader.EXTENSION.equals(suffix)) {
            return new TmgrReader(trackContent, validator);
        }
        throw new IllegalArgumentException("Unknown file type: " + suffix);
    }

}
