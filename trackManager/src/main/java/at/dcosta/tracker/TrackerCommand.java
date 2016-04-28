package at.dcosta.tracker;

import java.io.Serializable;

import android.location.GpsStatus.Listener;
import android.location.LocationListener;

public interface TrackerCommand extends Serializable {

	public enum Command {
		CONNECT, SEND, STOP_SENDING, START_TRACK, STOP_TRACK, PAUSE_TRACK, RESUME_TRACK, SET_LISTENER, SHUTDOWN;
	}

	static class FilenameCommand extends SimpleCommand {

		private static final long serialVersionUID = 3050262369028328423L;
		private final String filename;

		public FilenameCommand(Command command, String filename) {
			super(command);
			this.filename = filename;
		}

		public String getFilename() {
			return filename;
		}

	}

	public static class ResumeTrack extends FilenameCommand {

		private static final long serialVersionUID = -5165398749538530480L;

		public ResumeTrack(String filename) {
			super(Command.START_TRACK, filename);
		}
	}

	public static class SendTrack extends ServerCommComand {

		private static final long serialVersionUID = 0L;

		public SendTrack(String trackName, String address) {
			super(Command.SEND, trackName, address);
		}

	}

	public abstract class ServerCommComand extends SimpleCommand {

		private static final long serialVersionUID = 0L;
		private final String trackName, address;

		public ServerCommComand(Command command, String trackName, String address) {
			super(command);
			this.trackName = trackName;
			this.address = address;
		}

		public String getAddress() {
			return address;
		}

		public String getTrackName() {
			return trackName;
		}
	}

	public static class SetListener extends SimpleCommand {

		private static final long serialVersionUID = 8978480326406912460L;
		private final LocationListener locationListener;
		private final Listener listener;

		public SetListener(LocationListener locationListener, Listener listener) {
			super(Command.SET_LISTENER);
			this.locationListener = locationListener;
			this.listener = listener;
		}

		public Listener getListener() {
			return listener;
		}

		public LocationListener getLocationListener() {
			return locationListener;
		}

	}

	static class SimpleCommand implements TrackerCommand {
		private static final long serialVersionUID = 8311517319831469738L;
		private final Command command;

		public SimpleCommand(Command command) {
			this.command = command;
		}

		@Override
		public at.dcosta.tracker.TrackerCommand.Command getCommand() {
			return command;
		}

	}

	public static class StartTrack extends FilenameCommand {

		private static final long serialVersionUID = 0L;

		public StartTrack(String filename) {
			super(Command.START_TRACK, filename);
		}

	}

	public static class StopSending extends ServerCommComand {

		private static final long serialVersionUID = 0L;

		public StopSending(String trackName, String address) {
			super(Command.STOP_SENDING, trackName, address);
		}
	}

	public static TrackerCommand STOP_TRACK = new SimpleCommand(Command.STOP_TRACK);
	public static TrackerCommand SHUTDOWN = new SimpleCommand(Command.SHUTDOWN);
	public static TrackerCommand PAUSE_TRACK = new SimpleCommand(Command.PAUSE_TRACK);
	public static TrackerCommand CONNECT = new SimpleCommand(Command.CONNECT);

	public Command getCommand();

}
