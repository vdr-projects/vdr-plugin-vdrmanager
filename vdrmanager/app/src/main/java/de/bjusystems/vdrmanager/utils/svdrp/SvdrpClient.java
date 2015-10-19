package de.bjusystems.vdrmanager.utils.svdrp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import android.sax.StartElementListener;
import android.util.Log;
import de.bjusystems.vdrmanager.app.C;
import de.bjusystems.vdrmanager.data.Preferences;

/**
 * Class for SVDRP communication
 *
 * @author bju
 *
 */
public abstract class SvdrpClient<Result> {

	private final String TAG = getClass().getName();

	/** Socket for connection to SVDRP */
	private Socket socket;
	/** Output stream for sending commands */
	private OutputStream outputStream;
	/** Input stream for reading answer lines */
	private InputStream inputStream;
	/** flag for stopping the current request */
	private boolean abort;
	/** listener for events */
	private final List<SvdrpListener> svdrpListeners = new ArrayList<SvdrpListener>();
	/** Listener for start */
	private final List<SvdrpStartListener> startListeners = new ArrayList<SvdrpStartListener>();
	/** listeners for results */
	private final List<SvdrpResultListener<Result>> svdrpResultListeners = new ArrayList<SvdrpResultListener<Result>>();
	/** listeners for exceptions */
	private final List<SvdrpExceptionListener> svdrpExceptionListeners = new ArrayList<SvdrpExceptionListener>();
	/** listeners for finished job */
	private final List<SvdrpFinishedListener<Result>> svdrpFinishedListeners = new ArrayList<SvdrpFinishedListener<Result>>();
	/** listener for certificate problems set by caller */
	private final CertificateProblemListener certificateProblemListener;

	/** list of results */
	// private final List<Result> results = new ArrayList<Result>();
	/** should the listener be informed about each received result */
	// private boolean resultInfoEnabled = false;
	/**
	 * @return true if the client has result
	 */
	// public boolean hasResults(){
	// return results.isEmpty() == false;
	// }

	private final Timer watchDog = new Timer();

	private String encoding;
	

	// private NativeDES crypt = new NativeDES();

	public boolean isConnected() {
		if (socket == null) {
			return false;
		}
		return socket.isConnected();
	}

	/**
	 * Parse received answer line
	 *
	 * @param line
	 *            line
	 * @return received data object or null if not completed yet
	 */
	protected abstract Result parseAnswer(String line);

	public abstract int getProgressTextId();

	public abstract void run();

	/**
	 * Constructor
	 *
	 * @param prefs
	 *            Preferences
	 */
	protected SvdrpClient(
			final CertificateProblemListener certificateProblemListener) {
		// results.clear();
		this.certificateProblemListener = certificateProblemListener;
		encoding = Preferences.get().getEncoding();
	}

	/**
	 * Remove all listeners
	 */
	public void clearListener() {
		svdrpExceptionListeners.clear();
		svdrpListeners.clear();
		svdrpResultListeners.clear();
	}

	/**
	 * Adds the listener to the list of listeners
	 *
	 * @param listener
	 *            listener
	 */
	public void addSvdrpListener(final SvdrpListener listener) {
		svdrpListeners.add(listener);
	}

	public void addStartListener(final SvdrpStartListener listener) {
		startListeners.add(listener);
	}

	/**
	 * Adds the listener to the list of listeners
	 *
	 * @param listener
	 *            listener
	 */
	public void addSvdrpResultListener(
			final SvdrpResultListener<Result> listener) {
		svdrpResultListeners.add(listener);
	}

	/**
	 * Adds the listener to the list of listeners
	 *
	 * @param listener
	 *            listener
	 */
	public void addSvdrpFinishedListener(
			final SvdrpFinishedListener<Result> listener) {
		svdrpFinishedListeners.add(listener);
	}

	/**
	 * Adds the listener to the list of listeners
	 *
	 * @param listener
	 *            listener
	 */
	public void addSvdrpExceptionListener(final SvdrpExceptionListener listener) {
		svdrpExceptionListeners.add(listener);
	}

	/**
	 * Removes the listener from the list of listeners
	 *
	 * @param listener
	 *            listener
	 */
	public void removeSvdrpListener(final SvdrpListener listener) {
		svdrpListeners.remove(listener);
	}

	public void remoeStartListener(final SvdrpStartListener listener) {
		startListeners.remove(listener);
	}

	public void removeSvdrpResultListener(
			final SvdrpResultListener<Result> listener) {
		svdrpResultListeners.remove(listener);
	}

	public void removeSvdrpExceptionListener(
			final SvdrpExceptionListener listener) {
		svdrpExceptionListeners.remove(listener);
	}

	/**
	 * Cancel the current request
	 */
	public void abort() {
		abort = true;
		try {
			if (isConnected()) {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			}
		} catch (final Exception ex) {
			Log.w(TAG, ex);
		}

	}

	// /**
	// * Gets the list of results
	// *
	// * @return results
	// */
	// public List<Result> getResults() {
	// return results;
	// }

	/**
	 * Connect to SVDRP
	 *
	 * @param host
	 *            host
	 * @param port
	 *            port
	 * @param ssl
	 *            use SSL
	 * @throws IOException
	 *             on errors
	 */
	protected boolean connect() throws IOException {

		final Preferences prefs = Preferences.get();
		try {
			// connect
			informListener(SvdrpEvent.CONNECTING);

			if (Preferences.get().isSecure()) {
				socket = new MySSLSocketFactory(false,
						certificateProblemListener).createSocket();
			} else {
				socket = new Socket();
			}

			socket.connect(
					new InetSocketAddress(prefs.getHost(), prefs
							.getPort()),
					prefs.getConnectionTimeout() * 1000);// 8 secs for connect
			if (abort) {
				informListener(SvdrpEvent.ABORTED);
			}
			//
			socket.setSoTimeout(prefs.getReadTimeout() * 1000);// 15 sec for
			// each read
			final long delay = C.ONE_MINUTE_IN_MILLIS * prefs.getTimeout() * 60; // in
			// 3
			// minutes
			// we
			// abort
			// the
			// communication
			watchDog.schedule(new TimerTask() {
				@Override
				public void run() {
					Log.w(TAG, "Aborted after " + delay + " ms");
					abort = true;
				}
			}, delay);
			informListener(SvdrpEvent.CONNECTED);

			// create streams
			outputStream = socket.getOutputStream();
			inputStream = socket.getInputStream();

		} catch (final SocketTimeoutException sote) {
			Log.w(TAG, sote);
			if (abort) {
				informListener(SvdrpEvent.ABORTED);
			} else {
				informListener(SvdrpEvent.CONNECTION_TIMEOUT);
			}
			return false;
		} catch (final Exception e) {

			Log.w(TAG, e);
			if (abort) {
				informListener(SvdrpEvent.ABORTED);
			} else {
				informListener(SvdrpEvent.CONNECT_ERROR);
			}
			return false;
		}

		// password needed?
		informListener(SvdrpEvent.LOGIN);
		writeLine("passwd " + prefs.getPassword());
		if (!readLine().startsWith("!OK")) {
			informListener(SvdrpEvent.LOGIN_ERROR);
			disconnect();
			return false;
		} else {
			informListener(SvdrpEvent.LOGGED_IN);
		}
		return true;
	}

	/**
	 * Disconnect from SVDRP if connected
	 *
	 * @throws IOException
	 *             on errors
	 */
	protected void disconnect() throws IOException {
		informListener(SvdrpEvent.DISCONNECTING);
		if (socket != null && socket.isConnected()) {
            writeLine("quit");
			socket.close();
			socket = null;
		}
		informListener(SvdrpEvent.DISCONNECTED);
	}

	/**
	 * Sends one line to SVDRP
	 *
	 * @param line
	 *            line of text
	 * @throws IOException
	 *             on errors
	 */
	protected void writeLine(final String line) throws IOException {

		final String command = line + "\r\n";
		// if (false && Preferences.get().isSecure()) {
		// command = crypt.encrypt(command, Preferences.get().getPassword());
		// }
		final byte[] bytes = command.getBytes("utf-8");
		outputStream.write(bytes);
		outputStream.flush();
	}

	/**
	 * Reads one line from SVDRP
	 *
	 * @return line read
	 * @throws IOException
	 *             on errors
	 */
	protected String readLine() throws IOException {

		// handle not gzipped input
		final ByteArrayOutputStream lineBytes = new ByteArrayOutputStream();

		for (;;) {

			// read next char
			final int d = inputStream.read();
			if (d < 0) {
				break;
			}
			final char c = (char) d;

			// skip '\r'
			if (c == '\r') {
				continue;
			}

			// with '\n' the line is completed
			if (c == '\n') {
				break;
			}

			// remember char
			lineBytes.write(c);
		}

		String line = null;
		try {
			line = lineBytes.toString(encoding);
			lineBytes.close();
		} catch (final UnsupportedEncodingException usex) {
			Log.w(TAG, usex);
			line = lineBytes.toString();
		}
		// if (false && Preferences.get().isSecure()) {
		// line = crypt.decrypt(line, Preferences.get().getPassword());
		// }
		return line;
	}

	public void runCommand(final String command) {

		try {

			// reset cancel flag
			abort = false;

			// clear results
			// results.clear();

			// connect
			final boolean connected = connect();
			if (!connected) {
				return;
			}

			// activate compression
			Log.i(TAG, "Activate compression");
			writeLine("compress");

			// send command
			informListener(SvdrpEvent.COMMAND_SENDING);
			writeLine(command);
			informListener(SvdrpEvent.COMMAND_SENT);
			Log.i(TAG, SvdrpEvent.COMMAND_SENT + ":" + command);

			// get the answer for the compress command or
			// the first line of the answer for the command
			String line = readLine();
			if (line.startsWith("!OK")) {
				final String[] words = line.split(" ");
				if (words.length > 1) {
					final String mode = words[1].toUpperCase(Locale.getDefault());
					if (mode.equals("ZLIB")) {
						Log.i(TAG, "ZLIB compression activated");
						inputStream = new InflaterInputStream(inputStream);
					} else if (mode.equals("GZIP")) {
						Log.i(TAG, "GZIP compression activated");
						inputStream = new GZIPInputStream(inputStream);
					} else {
						Log.i(TAG, "NO compression activated");
					}
				}
				line = readLine();
			} else {
				Log.i(TAG, "NO compression activated");
			}

			// correct answer?
			if (!line.startsWith("START")) {
				Log.w(TAG, line);
				throw new IOException("Answer not wellformed: " + line);
			}

			if (line.startsWith("START|")) {
				informStartListener(line.substring(6));
			}

			// read answer lines
			for (; !abort;) {

				// get next line
				line = readLine();
				if (line.length() == 0) {
					break;
				}

				// last line?
				if (line.startsWith("END")) {
					break;
				}

				// error?
				if (line.startsWith("!ERROR")) {
					Log.w(TAG, line);
					String msg;
					if (line.startsWith("!ERROR:")) {
						msg = line.substring(7);
					} else {
						msg = line;
					}
					disconnect();
					informListener(SvdrpEvent.ERROR, new SvdrpException(SvdrpEvent.ERROR,msg));
					break;
				}

				// delegate analysis
				Result result = null;
				try {
					result = parseAnswer(line);

				} catch (final Exception ex) {
					Log.w(TAG, ex);
					disconnect();
					Log.w(TAG, "line: " + line);
					informListener(SvdrpEvent.ERROR, ex);
					return;
				}
				if (result != null) {
					informListener(result);
					// results.add(result);
					// if (resultInfoEnabled) {

					// }
				}

			}

			// disconnect
			disconnect();

			if (abort) {
				informListener(SvdrpEvent.ABORTED);
			} else {
				informListener(SvdrpEvent.FINISHED_SUCCESS);
			}

		} catch (final Exception e) {
			Log.w(TAG, e);
			informListener(SvdrpEvent.FINISHED_ABNORMALY, e);
		}
	}

	// public void setResultInfoEnabled(final boolean resultInfoEnabled) {
	// this.resultInfoEnabled = resultInfoEnabled;
	// }

	protected void informListener(final SvdrpEvent event, final Throwable e) {
		for (final SvdrpExceptionListener listener : svdrpExceptionListeners) {
			listener.svdrpEvent(event, e);
		}
	}

	protected void informListener(final SvdrpEvent event) {
		for (final SvdrpListener listener : svdrpListeners) {
			listener.svdrpEvent(event);
		}
	}

	protected void informListener(final Result result) {
		for (final SvdrpResultListener<Result> listener : svdrpResultListeners) {
			listener.svdrpEvent(result);
		}
	}

	protected void informStartListener(final String result) {
		for (final SvdrpStartListener l : startListeners) {
			l.start(result);
		}
	}
}
