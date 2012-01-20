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
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import de.bjusystems.vdrmanager.app.C;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.utils.crypt.NativeDES;

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
	/** listener */
	private final List<SvdrpListener<Result>> listeners = new ArrayList<SvdrpListener<Result>>();
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

	private Timer watchDog = new Timer();
	
	private NativeDES crypt  = new NativeDES();

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

	public abstract void run() throws SvdrpException;

	/**
	 * Constructor
	 * 
	 * @param prefs
	 *            Preferences
	 */
	protected SvdrpClient() {
		// results.clear();
	}

	/**
	 * Remove all listeners
	 */
	public void clearSvdrpListener() {
		listeners.clear();
	}

	/**
	 * Adds the listener to the list of listeners
	 * 
	 * @param listener
	 *            listener
	 */
	public void addSvdrpListener(final SvdrpListener<Result> listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the listener from the list of listeners
	 * 
	 * @param listener
	 *            listener
	 */
	public void removeSvdrpListener(final SvdrpListener<Result> listener) {
		listeners.remove(listener);
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
		} catch (Exception ex) {
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
			informListener(SvdrpEvent.CONNECTING, null);
			socket = new Socket();
			socket.connect(
					new InetSocketAddress(prefs.getSvdrpHost(), prefs
							.getSvdrpPort()),
					prefs.getConnectionTimeout() * 1000);// 8 secs for connect
			if (abort) {
				informListener(SvdrpEvent.ABORTED, null);
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
			informListener(SvdrpEvent.CONNECTED, null);
		} catch (final SocketTimeoutException sote) {
			Log.w(TAG, sote);
			if (abort) {
				informListener(SvdrpEvent.ABORTED, null);
			} else {
				informListener(SvdrpEvent.CONNECTION_TIMEOUT, null);
			}
			return false;
		} catch (final Exception e) {

			Log.w(TAG, e);
			if (abort) {
				informListener(SvdrpEvent.ABORTED, null);
			} else {
				informListener(SvdrpEvent.CONNECT_ERROR, null);
			}
			return false;
		}

		// create streams
		outputStream = socket.getOutputStream();
		inputStream = socket.getInputStream();
		//TODO http://projects.vdr-developer.org/issues/790
		//inputStream = new InflaterInputStream(socket.getInputStream())

		// password needed?
		informListener(SvdrpEvent.LOGIN, null);
		writeLine("passwd " + prefs.getPassword());
		if (!readLine().startsWith("!OK")) {
			informListener(SvdrpEvent.LOGIN_ERROR, null);
			disconnect();
			return false;
		} else {
			informListener(SvdrpEvent.LOGGED_IN, null);
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
		informListener(SvdrpEvent.DISCONNECTING, null);
		if (socket != null && socket.isConnected()) {
			socket.close();
			socket = null;
		}
		informListener(SvdrpEvent.DISCONNECTED, null);
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

		String command = line + "\r\n";
		if(false && Preferences.get().isSecure()){
			command = crypt.encrypt(command, Preferences.get().getPassword());
		}
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
		try{
			line = lineBytes.toString(Preferences.get().getEncoding());
		} catch(UnsupportedEncodingException usex){
			Log.w(TAG, usex);
			line = lineBytes.toString();
		}
		if(false && Preferences.get().isSecure()){
			line = crypt.decrypt(line, Preferences.get().getPassword());
		}
		return line;
	}

	public void runCommand(final String command) throws SvdrpException {

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

			// send command
			informListener(SvdrpEvent.COMMAND_SENDING, null);
			writeLine(command);
			informListener(SvdrpEvent.COMMAND_SENT, null);
			Log.i(TAG, SvdrpEvent.COMMAND_SENT + ":" + command);

			// read first line
			String line = readLine();
			if (!line.startsWith("START")) {
				Log.w(TAG, line);
				throw new IOException("Answer not wellformed: " + line);
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
					informListener(SvdrpEvent.ERROR, null);
					break;
				}

				// delegate analysis
				Result result = null;
				try {
					result = parseAnswer(line);
				} catch (Exception ex) {
					Log.w(TAG, ex);
					Log.w(TAG, "line: " + line);
					informListener(SvdrpEvent.ERROR, null);
					disconnect();
					break;
				}
				if (result != null) {
					// results.add(result);
					// if (resultInfoEnabled) {
					informListener(SvdrpEvent.RESULT_RECEIVED, result);
					// }
				}

			}

			// disconnect
			disconnect();

			if (abort) {
				informListener(SvdrpEvent.ABORTED, null);
			} else {
				informListener(SvdrpEvent.FINISHED_SUCCESS, null);
			}

		} catch (final Exception e) {
			// throw new SvdrpException(e);
			Log.w(TAG, e);
			informListener(SvdrpEvent.FINISHED_ABNORMALY, null);
		}
	}

	// public void setResultInfoEnabled(final boolean resultInfoEnabled) {
	// this.resultInfoEnabled = resultInfoEnabled;
	// }

	protected void informListenerError(final SvdrpEvent event,
			final Throwable result) {

	}

	protected void informListener(final SvdrpEvent event, final Result result) {
		for (final SvdrpListener<Result> listener : listeners) {
			listener.svdrpEvent(event, result);
		}
	}
}
