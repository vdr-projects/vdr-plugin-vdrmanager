package de.bjusystems.vdrmanager.utils.svdrp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Preferences;

/**
 * Class for retrieving informations about the running program
 * 
 * @author bju
 * 
 */
public class ChannelClient extends SvdrpClient<Channel> implements
		SvdrpListener<Channel> {

	private static final ArrayList<String> channelGroups = new ArrayList<String>();

	private static LinkedHashMap<String, ArrayList<Channel>> groupChannels = new LinkedHashMap<String, ArrayList<Channel>>();

	private static TreeMap<String, ArrayList<Channel>> providerChannels = new TreeMap<String, ArrayList<Channel>>();

	private static ArrayList<Channel> channels = new ArrayList<Channel>();

	private static boolean inited = false;

	public ChannelClient(boolean useCache) {
		super();
		if (useCache == false) {
			clearCache();
		}
		addSvdrpListener(this);
	}

	private void clearCache() {
		channelGroups.clear();
		groupChannels.clear();
		providerChannels.clear();
		channels.clear();
		inited = false;
	}

	public static ArrayList<String> getChannelGroups() {
		return channelGroups;
	}

	public static HashMap<String, ArrayList<Channel>> getGroupChannels() {
		return groupChannels;
	}

	public static TreeMap<String, ArrayList<Channel>> getProviderChannels() {
		return providerChannels;
	}

	public static ArrayList<Channel> getChannels() {
		return channels;
	}

	/**
	 * Constructor
	 * 
	 * @param host
	 *            host
	 * @param port
	 *            port
	 * @param ssl
	 *            use ssl
	 */
	public ChannelClient() {
		this(true);

	}

	/**
	 * Starts the EPG request
	 * 
	 * @param parameter
	 *            parameter for lste
	 */
	@Override
	synchronized public void run() throws SvdrpException {
		if (inited == true) {
			informListener(SvdrpEvent.CACHE_HIT, null);
		} else {
			runCommand("channels " + Preferences.getPreferences().getChannels());
		}
	}

	@Override
	public Channel parseAnswer(final String line) {
		return new Channel(line);
	}

	@Override
	public int getProgressTextId() {
		return R.string.progress_channels_loading;
	}

	ArrayList<Channel> currentChannels = new ArrayList<Channel>();

	private void received(Channel c) {
		if (c.isGroupSeparator()) {
			channelGroups.add(c.getName());
			currentChannels = new ArrayList<Channel>();
			groupChannels.put(c.getName(), currentChannels);
		} else {
			if(channelGroups.isEmpty()){//receiver channel with no previous group
				channelGroups.add("");
				groupChannels.put("", currentChannels);
			}
			channels.add(c);
			currentChannels.add(c);
			String provider = c.getProvider();
			ArrayList<Channel> pchannels = providerChannels.get(provider);
			if (pchannels == null) {
				pchannels = new ArrayList<Channel>();
				providerChannels.put(provider, pchannels);
			}
			pchannels.add(c);
		}
	}

	public void svdrpEvent(SvdrpEvent event, Channel c) {
		if (event == SvdrpEvent.RESULT_RECEIVED) {
			received(c);
			return;
		}
		if(event == SvdrpEvent.FINISHED_SUCCESS){
			inited = true;
		}
	}
}
