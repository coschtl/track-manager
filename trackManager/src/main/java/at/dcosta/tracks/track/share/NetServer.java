package at.dcosta.tracks.track.share;

import java.io.IOException;
import java.net.MulticastSocket;
import java.net.ServerSocket;

public class NetServer {

	public static final int PORT = 8647;
	public static final String MULTICAST_ADDRESS = "228.5.6.7";
	public static final int MULTICAST_PORT = 6789;

	private Thread thread;
	protected ServerSocket socket;
	private MulticastSocket multicastSocket;

	public NetServer() {
		try {
			socket = new ServerSocket(PORT);
			System.out.println("server socket address: " + socket.getLocalPort());
			// multicastSocket = new MulticastSocket(6789);
			// multicastSocket = new MulticastSocket(MULTICAST_PORT);
			// multicastSocket.joinGroup(InetAddress.getByName(MULTICAST_ADDRESS));
			// byte[] buf = new byte[1000];
			// DatagramPacket recv = new DatagramPacket(buf, buf.length);
			// multicastSocket.receive(recv);
		} catch (IOException e) {
			socket = null;
		}
	}

	public void start() {
		new Thread() {

			@Override
			public void run() {
				try {
					socket.accept();
					System.out.println("accepted a new connection");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}.start();

	}

	public void stop() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("error closing server socket");
			e.printStackTrace();
		}
		System.out.println("server ended");

	}

}
