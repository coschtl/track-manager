package at.dcosta.tracks.track.share;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

public class NetClient {
	private Socket socket;

	private void closeSocket() {
		try {
			socket.close();
		} catch (Exception ex) {
			// ignore
		}
	}

	public Socket connect(InetAddress address) {
		socket = new Socket();
		try {
			// socket.connect(new InetSocketAddress("127.0.0.1", 11999));
			socket.connect(new InetSocketAddress(address, NetServer.PORT), 500);
			System.out.println("client connected: " + socket.isConnected());
			return socket;
		} catch (SocketTimeoutException e) {
			System.out.println("timeout while connecting to " + address.getHostAddress() + ":" + NetServer.PORT);
			closeSocket();
		} catch (IOException e) {
			closeSocket();
			e.printStackTrace();
		}
		return null;
	}

	private InetAddress getIPAddress() {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress() && InetAddressUtils.isIPv4Address(addr.getHostAddress())) {
						return addr;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public InetAddress searchForServers() {
		// String msg = "Hello";
		// InetAddress group = InetAddress.getByName("228.5.6.7");
		// MulticastSocket s = new MulticastSocket(6789);
		// s.joinGroup(group);
		// DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),
		// group, 6789);
		// s.send(hi);
		// // get their responses!
		// byte[] buf = new byte[1000];
		// DatagramPacket recv = new DatagramPacket(buf, buf.length);
		// s.receive(recv);
		// ...
		// // OK, I'm done talking - leave the group...
		// s.leaveGroup(group);
		try {
			InetAddress ownAddr = getIPAddress();
			byte[] addrBytes = ownAddr.getAddress();
			byte ownByte = addrBytes[3];
			InetAddress addr;
			Socket socket;
			for (byte b = 50; b < 256; b++) {
				if (b != ownByte) {
					addrBytes[3] = b;
					addr = InetAddress.getByAddress(addrBytes);
					socket = connect(addr);
					if (socket != null) {
						socket.close();
						return addr;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
