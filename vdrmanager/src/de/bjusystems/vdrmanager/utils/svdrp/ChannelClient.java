package de.bjusystems.vdrmanager.utils.svdrp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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

	private static boolean  inited = false;


	public ChannelClient(boolean useCache){
		super();
		if(useCache == false){
			clearCache();
		}
		addSvdrpListener(this);
	}
	
	private void clearCache(){
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
	public void run() throws SvdrpException {
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

	private synchronized void  initChannels(List<Channel> results) {
		if(inited){
			return;
		}
		ArrayList<Channel> currentChannels = null;
		for (Channel c : results) {
			if (c.isGroupSeparator()) {
				channelGroups.add(c.getName());
				currentChannels = new ArrayList<Channel>();
				groupChannels.put(c.getName(), currentChannels);
			} else {
				channels.add(c);
				currentChannels.add(c);
				String provider = c.getProvider();
				ArrayList<Channel> channels = providerChannels.get(provider);
				if (channels == null) {
					channels = new ArrayList<Channel>();
					providerChannels.put(provider, channels);
				}
				channels.add(c);
			}
		}
		inited = true;
	}

	public void svdrpEvent(SvdrpEvent event, Channel result) {
		if(event != SvdrpEvent.FINISHED_SUCCESS){
			return;
		}
		initChannels(getResults());
		getResults().clear();
	}

}