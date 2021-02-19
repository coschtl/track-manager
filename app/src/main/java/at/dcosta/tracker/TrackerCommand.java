package at.dcosta.tracker;

import android.location.GpsStatus.Listener;
import android.location.LocationListener;

import java.io.Serializable;

public interface TrackerCommand extends Serializable {

	TrackerCommand STOP_TRACK = new SimpleCommand(Command.STOP_TRACK);
	TrackerCommand SHUTDOWN = new SimpleCommand(Command.SHUTDOWN);
	TrackerCommand PAUSE_TRACK = new SimpleCommand(Command.PAUSE_TRACK);
	TrackerCommand CONNECT = new SimpleCommand(Command.CONNECT);

	Command getCommand();

	enum Command {
		CONNECT, SEND, STOP_SENDING, START_TRACK, STOP_TRACK, PAUSE_TRACK, RESUME_TRACK, SET_LISTENER, SHUTDOWN
	}

	class FilenameCommand extends SimpleCommand {

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

	class ResumeTrack extends FilenameCommand {

		private static final long serialVersionUID = -5165398749538530480L;

		public ResumeTrack(String filename) {
			super(Command.START_TRACK, filename);
		}
	}

	class SendTrack extends ServerCommComand {

		private static final long serialVersionUID = 0L;

		public SendTrack(String trackName, String address) {
			super(Command.SEND, trackName, address);
		}

	}

	abstract class ServerCommComand extends SimpleCommand {

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

	class SetListener extends SimpleCommand {

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

	class SimpleCommand implements TrackerCommand {
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

	class StartTrack extends FilenameCommand {

		private static final long serialVersionUID = 0L;

		public StartTrack(String filename) {
			super(Command.START_TRACK, filename);
		}

	}

	class StopSending extends ServerCommComand {

		private static final long serialVersionUID = 0L;

		public StopSending(String trackName, String address) {
			super(Command.STOP_SENDING, trackName, address);
		}
	}

}
