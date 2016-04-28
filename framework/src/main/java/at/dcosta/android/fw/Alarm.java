package at.dcosta.android.fw;

public class Alarm {

	public interface AlarmReceiver {
		void alarm();
	}

	private final AlarmReceiver receiver;
	private Thread thread;

	public Alarm(AlarmReceiver receiver) {
		this.receiver = receiver;
	}

	public void start(final long timeout) {
		thread = new Thread() {
			@Override
			public void run() {
				try {
					sleep(timeout);
					receiver.alarm();
				} catch (InterruptedException e) {
					// shut down because stop() has been called
				}
			}
		};
		thread.start();
	}

	public void stop() {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
	}
}
