package de.bjusystems.vdrmanager.utils.svdrp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
		SvdrpListener, SvdrpResultListener<Channel> {

	private static final ArrayList<String> channelGroups = new ArrayList<String>();

	private static final ArrayList<String> channelSources = new ArrayList<String>();

	private static LinkedHashMap<String, ArrayList<Channel>> groupChannels = new LinkedHashMap<String, ArrayList<Channel>>();

	private static TreeMap<String, ArrayList<Channel>> providerChannels = new TreeMap<String, ArrayList<Channel>>();

	private static TreeMap<String, ArrayList<Channel>> sourceChannels = new TreeMap<String, ArrayList<Channel>>();

	private static ArrayList<Channel> channels = new ArrayList<Channel>();

	private static Map<String, Channel> idChannels = new HashMap<String, Channel>();

	public static Map<String, Channel> getIdChannels() {
		return idChannels;
	}

	private static boolean inited = false;

	public ChannelClient() {
		super();
		// if (useCache == false) {
		// clearCache();
		// }
		addSvdrpListener(this);
		addSvdrpResultListener(this);
	}

	public static void clearCache() {
		channelSources.clear();
		sourceChannels.clear();
		channelGroups.clear();
		groupChannels.clear();
		providerChannels.clear();
		channels.clear();
		idChannels.clear();
		inited = false;
	}

	public static ArrayList<String> getChannelGroups() {
		return channelGroups;
	}

	public static ArrayList<String> getChannelSources() {
		return channelSources;
	}


	public static HashMap<String, ArrayList<Channel>> getGroupChannels() {
		return groupChannels;
	}

	public static TreeMap<String, ArrayList<Channel>> getProviderChannels() {
		return providerChannels;
	}

	public static TreeMap<String, ArrayList<Channel>> getSourceChannels() {
		return sourceChannels;
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
	// public ChannelClient() {
	// this(true);
	//
	// }

	/**
	 * Starts the EPG request
	 *
	 * @param parameter
	 *            parameter for lste
	 */
	@Override
	synchronized public void run() {
		if (inited == true) {
			informListener(SvdrpEvent.CACHE_HIT);
		} else {
			runCommand("channels " + Preferences.get().getChannels());
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

	String currentGroup;

	private void addSourceChannel(Channel c){
		ArrayList<Channel> channels = sourceChannels.get(c.getSource());

		if(channels == null){
			channels = new ArrayList<Channel>();
			sourceChannels.put(c.getSource(), channels);
			channelSources.add(c.getSource());
		}
		channels.add(c);
	}

	private void received(Channel c) {




		if (c.isGroupSeparator()) {
			currentGroup = c.getName();
			channelGroups.add(currentGroup);
			currentChannels = new ArrayList<Channel>();
			groupChannels.put(c.getName(), currentChannels);
		} else {

			addSourceChannel(c);

			if (channelGroups.isEmpty()) {// receiver channel with no previous
											// group
				channelGroups.add("");
				groupChannels.put("", currentChannels);
			}

			c.setGroup(currentGroup);
			channels.add(c);
			idChannels.put(c.getId(), c);
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

	@Override
	public void svdrpEvent(Channel c) {
		received(c);
	}

	@Override
	public void svdrpEvent(SvdrpEvent event) {
		if (event == SvdrpEvent.FINISHED_SUCCESS) {
			inited = true;
		}
	}
}
