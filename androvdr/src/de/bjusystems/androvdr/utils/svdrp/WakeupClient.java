package de.bjusystems.androvdr.utils.svdrp;

import de.bjusystems.androvdr.data.WakeupState;

/**
 * Class for retrieving informations about the running program
 * @author bju
 *
 */
public class WakeupClient extends SvdrpClient<WakeupState> {

	private WakeupState state;

	/**
	 * Constructor
	 */
	public WakeupClient() {
		super();
	}

	/**
	 * Starts the wakeup request
	 */
	@Override
	public void run() throws SvdrpException {
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