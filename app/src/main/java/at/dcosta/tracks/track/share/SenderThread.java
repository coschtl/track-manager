package at.dcosta.tracks.track.share;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SenderThread extends Thread {

    protected final TrackToShare[] tracks;
    protected final TrackSharingListener listener;

    public SenderThread(TrackSharingListener listener, TrackToShare... tracks) {
        this.listener = listener;
        this.tracks = tracks;
    }

    protected TransferStatus readFromStream(ObjectInputStream in) {
        try {
            return (TransferStatus) in.readObject();
        } catch (Exception e) {
            System.err.println("Unable to read transfer status: " + e.toString());
        }
        return TransferStatus.UNKNOWN;
    }

    protected void writeToStream(ObjectOutputStream out, TrackToShare track) throws IOException {
        out.writeObject(track);
        out.flush();
    }

}
