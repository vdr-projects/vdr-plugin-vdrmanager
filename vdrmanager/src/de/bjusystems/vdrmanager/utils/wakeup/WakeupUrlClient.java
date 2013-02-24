package de.bjusystems.vdrmanager.utils.wakeup;

import de.bjusystems.vdrmanager.data.WakeupState;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

/**
 * Class for retrieving informations about the running program
 * @author bju
 *
 */
public class WakeupUrlClient extends SvdrpClient<WakeupState> {

	private WakeupState state;

	/**
	 * Constructor
	 */
	public WakeupUrlClient() {
		super();
	}

	/**
	 * Starts the wakeup request
	 */
	@Override
	public void run() {
		runCommand("wake");
	}

	@Override
	public WakeupState parseAnswer(final String line) {

		if (line.startsWith("200")) {
			state = WakeupState.OK;
		} else if (line.startsWith("400")) {
			state = WakeupState.FAILED;
		} else {
			state = WakeupState.ERROR;
		}
		return state;
	}

	@Override
	public int getProgressTextId() {
		return 0;
	}

	public WakeupState getState() {
		return state;
	}

}