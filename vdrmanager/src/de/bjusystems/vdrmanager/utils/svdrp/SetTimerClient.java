package de.bjusystems.vdrmanager.utils.svdrp;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Timer;

/**
 * Class for retrieving informations about the running program
 * @author bju
 *
 */
public class SetTimerClient extends SvdrpClient<Timer> {

	/** channel names for timer */
	Timer timer;
	/** timer should be deleted */
	boolean deleteTimer;

	/**
	 * Constructor
	 * @param host host
	 * @param port port
	 * @param ssl use ssl
	 */
	public SetTimerClient(final Timer timer, final boolean deleteTimer) {
		super();
		this.timer = timer;
		this.deleteTimer = deleteTimer;
	}

	/**
	 * Starts the request
	 */
	@Override
	public void run() throws SvdrpException {

		final StringBuilder command = new StringBuilder();

		command.append("timer ");
		if (deleteTimer) {
			command.append("-");
		}
		command.append(timer.getNumber());
		command.append(" ");
		command.append(timer.toCommandLine());

		runCommand(command.toString());
	}

	@Override
	public Timer parseAnswer(final String line) {
		return new Timer(line);
	}

	@Override
	public int getProgressTextId() {
		return R.string.progress_timer_save;
	}
}

