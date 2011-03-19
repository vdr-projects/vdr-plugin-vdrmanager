package de.bjusystems.androvdr.utils.svdrp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.bjusystems.androvdr.data.Preferences;


/**
 * Class for SVDRP communication
 * @author bju
 *
 */
public abstract class SvdrpClient<Result> {

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
	private final List<Result> results = new ArrayList<Result>();
	/** should the listener be informed about each received result */
	private boolean resultInfoEnabled = false;

	/**
	 * Parse received answer line
	 * @param line line
	 * @return received data object or null if not completed yet
	 */
	protected abstract Result parseAnswer(String line);

	public abstract int getProgressTextId();

	public abstract void run() throws SvdrpException;

	/**
	 * Constructor
	 * @param prefs Preferences
	 */
	protected SvdrpClient() {
		results.clear();
	}

	/**
	 * Remove all listeners
	 */
	public void clearSvdrpListener() {
		listeners.clear();
	}

	/**
	 * Adds the listener to the list of listeners
	 * @param listener listener
	 */
	public void addSvdrpListener(final SvdrpListener<Result> listener) {
		listeners.add(listener);
	}

	/**
	 * Removes the listener from the list of listeners
	 * @param listener listener
	 */
	public void removeSvdrpListener(final SvdrpListener<Result> listener) {
		listeners.remove(listener);
	}

	/**
	 * Cancel the current request
	 */
	public void abort() {
		abort = true;
	}

	/**
	 * Gets the list of results
	 * @return results
	 */
	public List<Result> getResults() {
		return results;
	}

	/**
	 * Connect to SVDRP
	 * @param host host
	 * @param port port
	 * @param ssl use SSL
	 * @throws IOException on errors
	 */
	protected boolean connect() throws IOException {

		final Preferences prefs = Preferences.getPreferences();

		try {
			// connect
			informListener(SvdrpEvent.CONNECTING, null);
			if (prefs.isSSL()) {
				throw new IllegalArgumentException("SSL not implemented yet");
			} else {
				socket = new Socket(prefs.getSvdrpHost(), prefs.getSvdrpPort());
			}
			informListener(SvdrpEvent.CONNECTED, null);
		} catch (final IOException e) {
			informListener(SvdrpEvent.CONNECT_ERROR, null);
			return false;
		}

		// create streams
		outputStream = socket.getOutputStream();
		inputStream = socket.getInputStream();

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
	 * @throws IOException on errors
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
	 * @param line line of text
	 * @throws IOException on errors
	 */
	protected void writeLine(final String line) throws IOException {

		final String command = line + "\r\n";
		final byte[] bytes = command.getBytes();
		outputStream.write(bytes);
		outputStream.flush();
	}

	/**
	 * Reads one line from SVDRP
	 * @return line read
	 * @throws IOException on errors
	 */
	protected String readLine() throws IOException {

		// handle not gzipped input
		final ByteArrayOutputStream lineBytes = new ByteArrayOutputStream();

		for(;;) {

			// read next char
			final int d = inputStream.read();
			if (d < 0) {
				break;
			}
			final char c = (char)d;

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

		return lineBytes.toString();
	}

	public void runCommand(final String command) throws SvdrpException {

		try {

			// reset cancel flag
			abort = false;

			// clear results
			results.clear();

			// connect
			final boolean connected = connect();
			if (!connected) {
				return;
			}

			// send command
			informListener(SvdrpEvent.COMMAND_SENDING, null);
			writeLine(command);
			informListener(SvdrpEvent.COMMAND_SENT, null);

			// read first line
			String line = readLine();
			if (!line.startsWith("START")) {
				throw new IOException("Answer not wellformed");
			}

			// read answer lines
			for(;!abort;) {

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
					informListener(SvdrpEvent.ERROR, null);
					break;
				}

				// delegate analysis
				final Result result = parseAnswer(line);
				if (result != null) {
					results.add(result);
					if (resultInfoEnabled) {
						informListener(SvdrpEvent.RESULT_RECEIVED, result);
					}
				}

			}

			// disconnect
			disconnect();

		} catch (final IOException e) {
			throw new SvdrpException(e);
		} finally {
			informListener(SvdrpEvent.FINISHED, null);
		}
	}

	public void setResultInfoEnabled(final boolean resultInfoEnabled) {
		this.resultInfoEnabled = resultInfoEnabled;
	}

	private void informListener(final SvdrpEvent event, final Result result) {
		for(final SvdrpListener<Result> listener : listeners) {
			listener.svdrpEvent(event, result);
		}
	}
}
