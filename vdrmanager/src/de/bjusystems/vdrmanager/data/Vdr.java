package de.bjusystems.vdrmanager.data;

public class Vdr {
	
	/**
	 * Use secure channel 
	 */
	private boolean secure;
	
	/** SVDRP host name or ip */
	private String host;
	
	/** SVDRP port */
	private int port;
	
	/** Password */
	private String password;
	
	/** should channels be filtered? */
	private boolean filterChannels;
	
	/** last channel to retrieve */
	private String channelFilter;
	
	/** Enable remote wakeup */
	private boolean wakeupEnabled;
	
	/** URL of the wakeup script */
	private String wakeupUrl;
	
	/** User for wakeup */
	private String wakeupUser;
	
	/** Password for wakeup */
	private String wakeupPassword;
	
	/**
	 * vdr mac for wol
	 * 
	 * @since 0.2
	 */
	private String mac;
	
	/**
	 * which wakeup method to use
	 * 
	 * @since 0.2
	 * 
	 */
	private String wakeupMethod;
	
	/** Check for running VDR is enabled */
	private boolean aliveCheckEnabled;
	
	/** Intervall for alive test */
	private int aliveCheckInterval;
	
	/** Buffer before event */
	private int timerPreMargin;
	
	/** Buffer after event */
	private int timerPostMargin;
	
	/** Default priority */
	private int timerDefaultPriority;
	
	/** Default lifetime */
	private int timerDefaultLifetime;
	
	/** user defined epg search times */
	private String epgSearchTimes;

	/**
	 * Which port to use for streaming
	 * 
	 * @since 0.2
	 */
	private int streamPort = 3000;

	/**
	 * Which format to use for streaming
	 * 
	 * @since 0.2
	 */
	private String streamFormat = "TS";

	/**
	 * Do not send broadcasts, send directly to the host (router problem)
	 * 
	 * @since 0.2
	 */
	private String wolCustomBroadcast = "";


	/**
	 * Use remux ?
	 */
	private boolean enableRemux;

	/**
	 * Remux command
	 */
	private String remuxCommand;

	/**
	 * Remux command Parameter
	 */
	private String remuxParameter;

}
