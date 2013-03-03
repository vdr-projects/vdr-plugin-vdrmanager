package de.bjusystems.vdrmanager.utils.svdrp;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EpgSearchParams;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Timer;

/**
 * Class for retrieving informations about the running program
 * @author bju
 *
 */
public class EpgClient extends SvdrpClient<Epg>  {

	/** Time to retrieve EPG for */
	private String time;
	/** Channel to retrieve EPG for */
	private Channel channel;
	/** Search parameters to retrieve EPG for */
	private EpgSearchParams search;
	/** Last read EPG */
	private Epg lastEpg;

	/**
	 * Constructor
	 */
	private EpgClient() {
		super();
		this.time = null;
		this.channel = null;
		this.search = null;
	}

	/**
	 * Constructor
	 * @param time time to search for epg events
	 */
	public EpgClient(final String time) {
		this();
		this.time = time;
	}

	/**
	 * Constructor
	 * @param channel channel to search for epg events
	 */
	public EpgClient(final Channel channel) {
		this();
		this.channel = channel;
	}

	public EpgClient(final EpgSearchParams search) {
		this();
		this.search = search;
	}

	/**
	 * Starts the EPG request
	 * @param parameter parameter for lste
	 */
	@Override
	public void run()   {
		if (time != null) {
			runCommand(String.format("tevents %s %s", time, Preferences.getPreferences().getChannels()));
		} else if (channel != null) {
			runCommand(String.format("cevents %s", channel.getNumber()));
		} else if (search != null) {
			runCommand(String.format("search %s:%s", Preferences.get().getChannels(), search.toCommandLine()));
		}
	}

	@Override
	public Epg parseAnswer(final String line) {

		if (line.startsWith("E")) {
			lastEpg = new Epg(line);
			return lastEpg;
		} else if (line.startsWith("T")) {
			lastEpg.setTimer(new Timer(line));
		}
		return null;
	}

	@Override
	public int getProgressTextId() {
		return R.string.progress_whatson_loading;
	}

}

