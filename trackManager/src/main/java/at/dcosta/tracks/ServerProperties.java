package at.dcosta.tracks;

import java.io.Serializable;

public class ServerProperties implements Serializable {

	private static final long serialVersionUID = 1L;

	private String serverSendAddress, serverCloseAddress;

	private String server, savePath, closePath;
	private String trackingProtocol;

	public String getServerCloseAddress() {
		return serverCloseAddress;
	}

	public String getServerSendAddress() {
		return serverSendAddress;
	}

	public String getTrackingProtocol() {
		return trackingProtocol;
	}

	public boolean initialize() {
		if (isValid()) {
			StringBuilder b = new StringBuilder("http://").append(server);
			if (!server.endsWith("/")) {
				b.append("/");
			}
			server = b.toString();

			b = new StringBuilder(server).append(savePath);
			if (!savePath.endsWith("/")) {
				b.append("/");
			}
			serverSendAddress = b.toString();

			b = new StringBuilder(server).append(closePath);
			if (!closePath.endsWith("/")) {
				b.append("/");
			}
			serverCloseAddress = b.toString();
			return true;
		}
		return false;
	}

	public boolean isValid() {
		return server != null && server.length() > 0 && savePath != null && savePath.length() > 0 && closePath != null && closePath.length() > 0;
	}

	public void setClosePath(String closePath) {
		this.closePath = closePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void setTrackingProtocol(String trackingProtocol) {
		this.trackingProtocol = trackingProtocol;
	}

}
