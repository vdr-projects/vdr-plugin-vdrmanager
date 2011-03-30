package de.bjusystems.vdrmanager.utils.svdrp;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Preferences;

/**
 * Class for retrieving informations about the running program
 * @author bju
 *
 */
public class ChannelClient extends SvdrpClient<Channel> {

	/**
	 * Constructor
	 * @param host host
	 * @param port port
	 * @param ssl use ssl
	 */
	public ChannelClient() {
		super();
	}

	/**
	 * Starts the EPG request
	 * @param parameter parameter for lste
	 */
	@Override
	public void run() throws SvdrpException {
		runCommand("channels " + Preferences.getPreferences().getChannels());
	}

	@Override
	public Channel parseAnswer(final String line) {
		return new Channel(line);
	}

	@Override
	public int getProgressTextId() {
		return R.string.progress_channels_loading;
	}


}