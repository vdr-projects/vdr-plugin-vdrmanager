package de.bjusystems.vdrmanager.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

public class CACHE {

	public static WeakHashMap<String, ArrayList<Epg>> CACHE = new WeakHashMap<String, ArrayList<Epg>>();

	public static WeakHashMap<String, Date> NEXT_REFRESH = new WeakHashMap<String, Date>();


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

	private static boolean channels_inited = false;

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
}
