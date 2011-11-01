package de.bjusystems.vdrmanager.data;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.text.TextUtils;
import de.bjusystems.vdrmanager.R;

/**
 * Class for all preferences
 * 
 * @author bju, lado
 * 
 */
public class Preferences {

	public static final String DEFAULT_LANGUAGE_VALUE = "DEFAULT";

	private boolean ssl;
	/** SVDRP host name or ip */
	private String svdrpHost;
	/** SVDRP port */
	private int svdrpPort;
	/** Password */
	private String password;
	/** should channels be filtered? */
	private boolean filterChannels;
	/** last channel to retrieve */
	private String channels;
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
	private String vdrMac;
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
	 * format times AM/PM or 24H
	 * 
	 * @since 0.2
	 */
	private boolean use24hFormat;

	/**
	 * Do not send broadcasts, send directly to the host (router problem)
	 * 
	 * @since 0.2
	 */
	private String wolCustomBroadcast = "";

	/**
	 * Whether to show channel numbers in the overviews
	 * 
	 * @since 0.2
	 */
	private boolean showChannelNumbers = false;

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

	/**
	 * Quits the app on back button
	 */
	private boolean quiteOnBackButton = true;

	/**
	 * Show IMDB buttons, where possible (e.g. EPG Details)
	 */
	private boolean showImdbButton = true;

	/**
	 * On Which imdb site to search?
	 */
	private String imdbUrl = "akas.imdb.com";

	/**
	 * Connection timeout
	 */
	private int connectionTimeout;

	/**
	 * Read Timeout
	 */
	private int readTimeout;

	/**
	 * Timeout for a whole command run
	 */
	private int timeout;

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getImdbUrl() {
		return imdbUrl;
	}

	public void setImdbUrl(String imdbUrl) {
		this.imdbUrl = imdbUrl;
	}

	/**
	 * @return whether to shwo imdb button
	 */
	public boolean isShowImdbButton() {
		return showImdbButton;
	}

	/** Properties singleton */
	private static Preferences thePrefs;

	/**
	 * Whether to send Packets to the custom broadcast address. It is used, if
	 * the address ist not empty
	 * 
	 * @return
	 * @since 0.2
	 */
	public String getWolCustomBroadcast() {
		return wolCustomBroadcast;
	}

	/**
	 * Getter for use24hFormat
	 * 
	 * @since 0.2
	 * @return
	 */
	public boolean isUse24hFormat() {
		return use24hFormat;
	}

	/**
	 * Checks for connect using SSL
	 * 
	 * @return true, if use SSL connections
	 */
	public boolean isSSL() {
		return ssl;
	}

	/**
	 * Retrieves the channel filtering mode
	 * 
	 * @return true, if channels will be filtered
	 */
	public boolean isFilterChannels() {
		return filterChannels;
	}

	/**
	 * Last channel to receive
	 * 
	 * @return channel number
	 */
	public String getChannels() {
		return filterChannels ? channels : "";
	}

	/**
	 * Gets the SVDRP host or IP address
	 * 
	 * @return SVDRP host
	 */
	public String getSvdrpHost() {
		return svdrpHost;
	}

	/**
	 * Gets the SVDRP port
	 * 
	 * @return SVDRP port
	 */
	public int getSvdrpPort() {
		return svdrpPort;
	}

	/**
	 * Gets the SVDRP password
	 * 
	 * @return SVDRO password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Checks for enables remote wakeup
	 * 
	 * @return true, if remote wakeup is enabled
	 */
	public boolean isWakeupEnabled() {
		return wakeupEnabled;
	}

	/**
	 * Gets the URL for the wakeup request
	 * 
	 * @return wakeup url
	 */
	public String getWakeupUrl() {
		return wakeupUrl;
	}

	/**
	 * Gets the user for the wakeup url
	 * 
	 * @return user name
	 */
	public String getWakeupUser() {
		return wakeupUser;
	}

	/**
	 * Gets the password for the wakeup url
	 * 
	 * @return password
	 */
	public String getWakeupPassword() {
		return wakeupPassword;
	}

	/**
	 * Checks for enabled alive check
	 * 
	 * @return true, if enabled
	 */
	public boolean isAliveCheckEnabled() {
		return aliveCheckEnabled;
	}

	/**
	 * Gets the time between alive checks
	 * 
	 * @return time in seconds
	 */
	public int getAliveCheckInterval() {
		return aliveCheckInterval;
	}

	/**
	 * Gets the buffer before the event start
	 * 
	 * @return pre event buffer
	 */
	public int getTimerPreMargin() {
		return timerPreMargin;
	}

	/**
	 * Gets the buffer after the event stop
	 * 
	 * @return post event buffer
	 */
	public int getTimerPostMargin() {
		return timerPostMargin;
	}

	/**
	 * Gets the default priority
	 * 
	 * @return default priority
	 */
	public int getTimerDefaultPriority() {
		return timerDefaultPriority;
	}

	/**
	 * Gets the default lifetime
	 * 
	 * @return default lifetime
	 */
	public int getTimerDefaultLifetime() {
		return timerDefaultLifetime;
	}

	/**
	 * Gets the time values for the epg search
	 * 
	 * @return
	 */
	public String getEpgSearchTimes() {
		return epgSearchTimes;
	}

	/**
	 * gets the MAC Address of the vdr host
	 * 
	 * @return
	 * @since 0.2
	 */
	public String getVdrMac() {
		return vdrMac;
	}

	/**
	 * Gets the selection which wakeup method to use
	 * 
	 * @return
	 * @since 0.2
	 */
	public String getWakeupMethod() {
		return wakeupMethod;
	}

	/**
	 * Getter for streaming port
	 * 
	 * @return
	 * @since 02.
	 */
	public int getStreamPort() {
		return streamPort;
	}

	/**
	 * Getter for selected streaming format
	 * 
	 * @return
	 * @since 0.2
	 */
	public String getStreamFormat() {
		return streamFormat;
	}

	/**
	 * Sets the time values for the epg search
	 * 
	 * @param epgSearchTimes
	 *            new time values
	 */
	public void setEpgSearchTimes(final Context context,
			final String epgSearchTimes) {

		final SharedPreferences prefs = getSharedPreferences(context);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString(context.getString(R.string.epg_search_times_key),
				epgSearchTimes);
		editor.commit();

		// reload
		init(context);
	}

	/**
	 * Gets the name for the file which preferences will be saved into
	 * 
	 * @param context
	 *            Context
	 * @return filename
	 */
	public static String getPreferenceFile(final Context context) {
		return context.getString(R.string.app_name);
	}

	/**
	 * Show Channel Numbers in the overviews
	 * 
	 * @since 0.2
	 * @return
	 */
	public boolean isShowChannelNumbers() {
		return showChannelNumbers;
	}

	/**
	 * getter
	 * 
	 * @return
	 */
	public boolean isEnableRemux() {
		return enableRemux;
	}

	/**
	 * getter
	 * 
	 * @return
	 */
	public String getRemuxCommand() {
		return remuxCommand;
	}

	/**
	 * getter
	 * 
	 * @return
	 */
	public String getRemuxParameter() {
		return remuxParameter;
	}

	/**
	 * getter
	 * 
	 * @return
	 */
	public boolean isQuiteOnBackButton() {
		return quiteOnBackButton;
	}

	/**
	 * Gets the previous loaded preferences
	 * 
	 * @return preferences
	 */
	public static Preferences getPreferences() {
		return thePrefs;
	}

	/**
	 * 
	 * Gets the previous loaded preferences, same as getPreferences();
	 * 
	 * @return
	 */
	public static Preferences get() {
		return thePrefs;
	}

	private static void initInternal(final Context context) {

		final Preferences prefs = new Preferences();

		prefs.svdrpHost = getString(context, R.string.vdr_host_key, "0.0.0.0");
		prefs.svdrpPort = getInt(context, R.string.vdr_port_key, 6420);
		prefs.password = getString(context, R.string.vdr_password_key, "");
		prefs.ssl = getBoolean(context, R.string.vdr_ssl_key, false);
		prefs.streamPort = getInt(context, R.string.vdr_stream_port, 3000);
		prefs.streamFormat = getString(context, R.string.vdr_stream_format,
				"TS");

		prefs.aliveCheckEnabled = getBoolean(context,
				R.string.alive_check_enabled_key, false);
		prefs.aliveCheckInterval = getInt(context,
				R.string.alive_check_interval_key, 60);

		prefs.channels = getString(context, R.string.channel_filter_last_key,
				"").replace(" ", "");
		prefs.filterChannels = getBoolean(context,
				R.string.channel_filter_filter_key, false);

		prefs.wakeupEnabled = getBoolean(context, R.string.wakeup_enabled_key,
				false);
		prefs.wakeupUrl = getString(context, R.string.wakeup_url_key, "");
		prefs.wakeupUser = getString(context, R.string.wakeup_user_key, "");
		prefs.wakeupPassword = getString(context, R.string.wakeup_password_key,
				"");

		prefs.timerPreMargin = getInt(context,
				R.string.timer_pre_start_buffer_key, 5);
		prefs.timerPostMargin = getInt(context,
				R.string.timer_post_end_buffer_key, 30);
		prefs.timerDefaultPriority = getInt(context,
				R.string.timer_default_priority_key, 99);
		prefs.timerDefaultLifetime = getInt(context,
				R.string.timer_default_lifetime_key, 99);

		prefs.epgSearchTimes = getString(context,
				R.string.epg_search_times_key, "");

		prefs.vdrMac = getString(context, R.string.wakeup_wol_mac_key, "");
		prefs.wakeupMethod = getString(context, R.string.wakeup_method_key,
				"url");

		prefs.use24hFormat = getBoolean(context,
				R.string.gui_enable_24h_format_key, true);

		prefs.wolCustomBroadcast = getString(context,
				R.string.wakeup_wol_custom_broadcast_key, "");

		prefs.showChannelNumbers = getBoolean(context,
				R.string.gui_channels_show_channel_numbers_key, false);

		prefs.enableRemux = getBoolean(context, R.string.key_remux_enable,
				false);

		prefs.remuxCommand = getString(context, R.string.key_remux_command,
				"EXT");

		prefs.remuxParameter = getString(context, R.string.key_remux_parameter,
				"");

		prefs.quiteOnBackButton = getBoolean(context,
				R.string.qui_quit_on_back_key, true);

		prefs.showImdbButton = getBoolean(context,
				R.string.qui_show_imdb_button_key, true);

		prefs.imdbUrl = getString(context, R.string.qui_imdb_url_key, "imdb.de");

		prefs.connectionTimeout = getInt(context, R.string.vdr_conntimeout_key,
				10);
		prefs.readTimeout = getInt(context, R.string.vdr_readtimeout_key, 10);
		prefs.timeout = getInt(context, R.string.vdr_timeout_key, 120);

		thePrefs = prefs;
	}

	public static void reset() {
		thePrefs = null;
	}

	/**
	 * Loads all preferences
	 * 
	 * @param context
	 *            Context
	 * @return Preferences
	 */
	public static void init(final Context context) {

		// if (thePrefs != null) {
		// return;
		// }

		synchronized (Preferences.class) {
			// if (thePrefs != null) {
			// return;
			// }
			initInternal(context);
			setLocale(context);
		}

	}

	/**
	 * Gets the persistent preferences
	 * 
	 * @param context
	 *            Context
	 * @return preferences
	 */
	public static SharedPreferences getSharedPreferences(final Context context) {
		return context.getSharedPreferences(getPreferenceFile(context),
				Context.MODE_PRIVATE);
	}

	/**
	 * Helper for retrieving integer values from preferences
	 * 
	 * @param context
	 *            Context
	 * @param resId
	 *            ressource id of the preferences name
	 * @param defValue
	 *            default value
	 * @return value or the default value if not defined
	 */
	private static int getInt(final Context context, final int resId,
			final int defValue) {
		final String value = getString(context, resId, String.valueOf(defValue));
		return Integer.parseInt(value);
	}

	/**
	 * Helper for retrieving boolean values from preferences
	 * 
	 * @param context
	 *            Context
	 * @param resId
	 *            ressource id of the preferences name
	 * @param defValue
	 *            default value
	 * @return value or the default value if not defined
	 */
	private static boolean getBoolean(final Context context, final int resId,
			final boolean defValue) {
		final SharedPreferences sharedPrefs = getSharedPreferences(context);
		return sharedPrefs.getBoolean(context.getString(resId), defValue);
	}

	/**
	 * Helper for retrieving string values from preferences
	 * 
	 * @param context
	 *            Context
	 * @param resId
	 *            ressource id of the preferences name
	 * @param defValue
	 *            default value
	 * @return value or the default value if not defined
	 */
	private static String getString(final Context context, final int resId,
			final String defValue) {
		final SharedPreferences sharedPrefs = getSharedPreferences(context);
		return sharedPrefs.getString(context.getString(resId), defValue);
	}

	public String getTimeFormat() {
		if (isUse24hFormat()) {
			return "HH:mm";
		}
		return "h:mm a";
	}

	/**
	 * Set locale read from preferences to context.
	 * 
	 * @param context
	 *            {@link Context}
	 */
	public static void setLocale(final Context context) {
		String lc = getString(context, R.string.gui_custom_locale_key, DEFAULT_LANGUAGE_VALUE);
		Locale locale = null;
		if (lc.equals(DEFAULT_LANGUAGE_VALUE)) {
			String lang = Locale.getDefault().toString();
			if (lang.startsWith("de")) {
				locale = Locale.GERMAN;
			} else {
				locale = Locale.ENGLISH;
			}
		} else {
			locale = new Locale(lc);
		}
		final Configuration config = new Configuration();
		config.locale = locale;
		context.getResources().updateConfiguration(config, null);
	}
}
