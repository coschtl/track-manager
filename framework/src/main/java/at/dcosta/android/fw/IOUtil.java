package at.dcosta.android.fw;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

public class IOUtil {

	public static final void close(BluetoothServerSocket serverSocket) {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				throw new FrameworkException("Cannot close BluetoothServerSocket", e);
			}
		}
	}

	public static final void close(BluetoothSocket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				throw new FrameworkException("Cannot close BluetoothSocket", e);
			}
		}
	}

	public static final void close(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				throw new FrameworkException("Cannot close InputStream", e);
			}
		}
	}

	public static final void close(OutputStream out) {
		close(out, false);
	}

	public static final void close(OutputStream out, boolean suppressException) {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				if (!suppressException) {
					throw new FrameworkException("Cannot close OutputStream", e);
				}
			}
		}
	}

	public static final void close(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				throw new FrameworkException("Cannot close Reader", e);
			}
		}
	}

	public static final void close(ServerSocket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				throw new FrameworkException("Cannot close ServerSocket", e);
			}
		}
	}

	public static final void close(Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				throw new FrameworkException("Cannot close Socket", e);
			}
		}
	}

	public static final void close(Writer writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				throw new FrameworkException("Cannot close Writer", e);
			}
		}
	}

	public static final boolean copy(File from, File to) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(from);
			out = new FileOutputStream(to);
			byte[] buffer = new byte[512];
			int length = in.read(buffer);
			out.write(buffer, 0, length);
			out.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			close(in);
			close(out);
		}
	}

	public static final String getFilenameNoPath(String pathAndFile) {
		int pos = pathAndFile.lastIndexOf(File.separatorChar);
		if (pos == -1) {
			return pathAndFile;
		}
		return pathAndFile.substring(pos + 1);
	}
}
