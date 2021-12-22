package at.dcosta.tracks.track.share;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import at.dcosta.tracks.TrackCopy;
import at.dcosta.tracks.TrackManager;
import at.dcosta.tracks.db.TrackDbAdapter;
import at.dcosta.tracks.track.TrackDescriptionNG;

public abstract class ReceiverThread extends Thread {

    protected final TrackDbAdapter trackDbAdapter;
    protected final TrackSharingListener listener;

    public ReceiverThread(TrackSharingListener listener, TrackDbAdapter trackDbAdapter) {
        this.listener = listener;
        this.trackDbAdapter = trackDbAdapter;
    }

    protected boolean persist(TrackToShare track) {
        TrackDescriptionNG origTrack = track.getDescription();
        origTrack.setActivityFactory(trackDbAdapter.getActivityFactory());
        //try {
        TrackCopy.copyTrack(TrackManager.context(), origTrack, track.getTrack(), origTrack.getName(), 0, trackDbAdapter);
        //} catch (ParsingException e) {
        //	System.err.println("Can not save received track: " + e.toString());
        //	return false;
        //}
        return true;
    }

    protected TrackToShare readFromStream(ObjectInputStream in) {
        try {
            return (TrackToShare) in.readObject();
        } catch (Exception e) {
            System.err.println("Unable to read track: " + e.toString());
        }
        return null;
    }

    protected void writeToStream(ObjectOutputStream out, TransferStatus status) throws IOException {
        out.writeObject(status);
        out.flush();
    }

}
