package de.bjusystems.androvdr.data;

import android.content.Context;
import android.content.SharedPreferences;
import de.bjusystems.androvdr.R;

/**
 * Class for all preferences
 * @author bju
 */
public class Preferences {

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
	/** Wakeup by HTTP request */
	private boolean wakeupHttp;
	/** URL of the wakeup script */
	private String wakeupUrl;
	/** User for wakeup */
	private String wakeupUser;
	/** Password for wakeup */
	private String wakeupPassword;
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

	/** Properties singleton */
	private static Preferences thePrefs;

	/**
	 * Checks for connect using SSL
	 * @return true, if use SSL connections
	 */
	public boolean isSSL() {
		return ssl;
	}

	/**
	 * Retrieves the channel filtering mode
	 * @return true, if channels will be filtered
	 */
	public boolean isFilterChannels() {
		return filterChannels;
	}

	/**
	 * Last channel to receive
	 * @return channel number
	 */
	public String getChannels() {
		return filterChannels ? channels : "";
	}

	/**
	 * Gets the SVDRP host or IP address
	 * @return SVDRP host
	 */
	public String getSvdrpHost() {
		return svdrpHost;
	}

	/**
	 * Gets the SVDRP port
	 * @return SVDRP port
	 */
	public int getSvdrpPort() {
		return svdrpPort;
	}

	/**
	 * Gets the SVDRP password
	 * @return SVDRO password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Checks for enables remote wakeup
	 * @return true, if remote wakeup is enabled
	 */
	public boolean isWakeupEnabled() {
		return wakeupEnabled;
	}

	/**
	 * Gets the wakeup method
	 * @return true, wenn wakeup by http request
	 */
	public boolean isWakeupHttp() {
		return wakeupHttp;
	}

	/**
	 * Gets the URL for the wakeup request
	 * @return wakeup url
	 */
	public String getWakeupUrl() {
		return wakeupUrl;
	}

	/**
	 * Gets the user for the wakeup url
	 * @return user name
	 */
	public String getWakeupUser() {
		return wakeupUser;
	}

	/**
	 * Gets the password for the wakeup url
	 * @return password
	 */
	public String getWakeupPassword() {
		return wakeupPassword;
	}

	/**
	 * Checks for enabled alive check
	 * @return true, if enabled
	 */
	public boolean isAliveCheckEnabled() {
		return aliveCheckEnabled;
	}

	/**
	 * Gets the time between alive checks
	 * @return time in seconds
	 */
	public int getAliveCheckInterval() {
		return aliveCheckInterval;
	}

	/**
	 * Gets the buffer before the event start
	 * @return pre event buffer
	 */
	public int getTimerPreMargin() {
		return timerPreMargin;
	}

	/**
	 * Gets the buffer after the event stop
	 * @return post event buffer
	 */
	public int getTimerPostMargin() {
		return timerPostMargin;
	}

	/**
	 * Gets the default priority
	 * @return default priority
	 */
	public int getTimerDefaultPriority() {
		return timerDefaultPriority;
	}

	/**
	 * Gets the default lifetime
	 * @return default lifetime
	 */
	public int getTimerDefaultLifetime() {
		return timerDefaultLifetime;
	}

	/**
	 * Gets the time values for the epg search
	 * @return
	 */
	public String getEpgSearchTimes() {
			return epgSearchTimes;
	}

	/**
	 * Sets the time values for the epg search
	 * @param epgSearchTimes new time values
	 */
	public void setEpgSearchTimes(final Context context, final String epgSearchTimes) {

		final SharedPreferences prefs = getSharedPreferences(context);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString(context.getString(R.string.epg_search_times_key), epgSearchTimes);
		editor.commit();

		// reload
		loadPreferences(context);
	}

	/**
	 * Gets the name for the file which preferences will be saved into
	 * @param context Context
	 * @return filename
	 */
	public static String getPreferenceFile(final Context context) {
		return context.getString(R.string.app_name);
	}

	/**
	 * Gets the previous loaded preferences
	 * @return preferences
	 */
	public static Preferences getPreferences() {
		return thePrefs;
	}

	/**
	 * Loads all preferences
	 * @param context Context
	 * @return Preferences
	 */
	public static void loadPreferences(final Context context) {

		final SharedPreferences sharedPrefs = getSharedPreferences(context);

		final Preferences prefs = new Preferences();

		prefs.svdrpHost = getString(context, sharedPrefs, R.string.vdr_host_key, "10.0.2.2");
		prefs.svdrpPort = getInt(context, sharedPrefs, R.string.vdr_port_key, 6419);
		prefs.password = getString(context, sharedPrefs, R.string.vdr_password_key, "");
		prefs.ssl = getBoolean(context, sharedPrefs, R.string.vdr_ssl_key, false);

		prefs.aliveCheckEnabled = getBoolean(context, sharedPrefs, R.string.alive_check_enabled_key, false);
		prefs.aliveCheckInterval = getInt(context, sharedPrefs, R.string.alive_check_interval_key, 60);

		prefs.channels = getString(context, sharedPrefs, R.string.channel_filter_last_key, "").replace(" ", "");
		prefs.filterChannels = getBoolean(context, sharedPrefs, R.string.channel_filter_filter_key, false);

		prefs.wakeupEnabled = getBoolean(context, sharedPrefs, R.string.wakeup_enabled_key, false);
		prefs.wakeupHttp = getBoolean(context, sharedPrefs, R.string.wakeup_svdrphelper_key, false);
		prefs.wakeupUrl = getString(context, sharedPrefs, R.string.wakeup_url_key, "");
		prefs.wakeupUser = getString(context, sharedPrefs, R.string.wakeup_user_key, "");
		prefs.wakeupPassword = getString(context, sharedPrefs, R.string.wakeup_password_key, "");

		prefs.timerPreMargin = getInt(context, sharedPrefs, R.string.timer_pre_start_buffer_key, 5);
		prefs.timerPostMargin = getInt(context, sharedPrefs, R.string.timer_post_end_buffer_key, 30);
		prefs.timerDefaultPriority = getInt(context, sharedPrefs, R.string.timer_default_priority_key, 99);
		prefs.timerDefaultLifetime = getInt(context, sharedPrefs, R.string.timer_default_lifetime_key, 99);

		prefs.epgSearchTimes = getString(context, sharedPrefs, R.string.epg_search_times_key, "");

		thePrefs = prefs;
	}

	/**
	 * Gets the persistent preferences
	 * @param context Context
	 * @return preferences
	 */
	private static SharedPreferences getSharedPreferences(final Context context) {

		return context.getSharedPreferences(getPreferenceFile(context), Context.MODE_PRIVATE);
	}

	/**
	 * Helper for retrieving integer values from preferences
	 * @param context Context
	 * @param sharedPrefs Object for the preference file
	 * @param resId ressource id of the preferences name
	 * @param defValue default value
	 * @return value or the default value if not defined
	 */
	private static int getInt(final Context context, final SharedPreferences sharedPrefs, final int resId, final int defValue) {
		final String value = getString(context, sharedPrefs, resId, String.valueOf(defValue));
		return Integer.parseInt(value);
	}

	/**
	 * Helper for retrieving boolean values from preferences
	 * @param context Context
	 * @param sharedPrefs Object for the preference file
	 * @param resId ressource id of the preferences name
	 * @param defValue default value
	 * @return value or the default value if not defined
	 */
	private static boolean getBoolean(final Context context, final SharedPreferences sharedPrefs, final int resId, final boolean defValue) {
		return sharedPrefs.getBoolean(context.getString(resId), defValue);
	}

	/**
	 * Helper for retrieving string values from preferences
	 * @param context Context
	 * @param sharedPrefs Object for the preference file
	 * @param resId ressource id of the preferences name
	 * @param defValue default value
	 * @return value or the default value if not defined
	 */
	private static String getString(final Context context, final SharedPreferences sharedPrefs, final int resId, final String defValue) {
		return sharedPrefs.getString(context.getString(resId), defValue);
	}
}
