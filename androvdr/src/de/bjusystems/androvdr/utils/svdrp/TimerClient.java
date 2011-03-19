package de.bjusystems.androvdr.utils.svdrp;

import java.util.HashMap;
import java.util.Map;

import de.bjusystems.androvdr.data.Channel;
import de.bjusystems.androvdr.data.Timer;
import de.bjusystems.androvdr.R;

/**
 * Class for retrieving informations about the running program
 * @author bju
 *
 */
public class TimerClient extends SvdrpClient<Timer> {

	/** channel names for timer */
	Map<String, Channel> channels;

	/**
	 * Constructor
	 * @param host host
	 * @param port port
	 * @param ssl use ssl
	 */
	public TimerClient() {
		super();
		this.channels = new HashMap<String, Channel>();
	}

	/**
	 * Starts the EPG request
	 * @param parameter parameter for lste
	 */
	@Override
	public void run() throws SvdrpException {
		runCommand("timers");
	}

	@Override
	public Timer parseAnswer(final String line) {
		return new Timer(line);
	}

	@Override
	public int getProgressTextId() {
		return R.string.progress_timers_loading;
	}
}

