package de.bjusystems.vdrmanager.data;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.StringUtils;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.data.db.OrmDatabaseHelper;
import de.bjusystems.vdrmanager.gui.VdrListActivity;

/**
 * Class for all preferences
 * 
 * @author bju, lado
 * 
 */
public class Preferences {

	public static final String DEFAULT_LANGUAGE_VALUE = "DEFAULT";

	public static final String PREFERENCE_FILE_NAME = "VDR-Manager";

	private static Vdr current;

	private static OrmDatabaseHelper db;

	public static OrmDatabaseHelper getDatabaseHelper() {
		return db;
	}

	public static void setCurrentVdr(Context context, Vdr vdr) {
		final SharedPreferences sharedPrefs = getSharedPreferences(context);
		current = vdr;
		sharedPrefs
				.edit()
				.putInt(context.getString(R.string.current_vdr_id_key),
						current.getId()).commit();
	}


	public Vdr getCurrentVdr() {
		return current;
	}

	/** user defined epg search times */
	private String epgSearchTimes;
	/**
	 * format times AM/PM or 24H
	 * 
	 * @since 0.2
	 */
	private boolean use24hFormat;

	/**
	 * Quits the app on back button
	 */
	private boolean quiteOnBackButton = true;

	/**
	 * Show IMDB buttons, where possible (e.g. EPG Details)
	 */
	private boolean showImdbButton = true;

	/**
	 * Show OMDB button in epg details
	 */
	private boolean showOmdbButton = false;

	/**
	 * Show TMDb button in epg details
	 */
	private boolean showTmdbButton = false;

	
	public boolean isShowTmdbButton() {
		return showTmdbButton;
	}

	public boolean isShowOmdbButton() {
		return showOmdbButton;
	}

	/**
	 * On Which imdb site to search?
	 */
	private String imdbUrl = "akas.imdb.com";

	private boolean showChannelNumbers = false;

	public String getEncoding() {
		return getCurrentVdr().getEncoding();
	}

	public int getConnectionTimeout() {
		return getCurrentVdr().getConnectionTimeout();
	}

	public int getReadTimeout() {
		return getCurrentVdr().getReadTimeout();
	}

	public int getTimeout() {
		return getCurrentVdr().getTimeout();
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

	public String getStreamingUsername() {
		return getCurrentVdr().getStreamingUsername();
	}

	public String getStreamingPassword() {
		return getCurrentVdr().getStreamingPassword();
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
		return getCurrentVdr().getWolCustomBroadcast();
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
	public boolean isSecure() {
		return getCurrentVdr().isSecure();
	}

	/**
	 * Retrieves the channel filtering mode
	 * 
	 * @return true, if channels will be filtered
	 */
	public boolean isFilterChannels() {
		return getCurrentVdr().isFilterChannels();
	}

	/**
	 * Last channel to receive
	 * 
	 * @return channel number
	 */
	public String getChannels() {
		return isFilterChannels() ? getCurrentVdr().getChannelFilter() : "";
	}

	/**
	 * Gets the SVDRP host or IP address
	 * 
	 * @return SVDRP host
	 */
	public String getSvdrpHost() {
		return getCurrentVdr().getHost();
	}

	/**
	 * Gets the SVDRP port
	 * 
	 * @return SVDRP port
	 */
	public int getSvdrpPort() {
		return getCurrentVdr().getPort();
	}

	/**
	 * Gets the SVDRP password
	 * 
	 * @return SVDRO password
	 */
	public String getPassword() {
		 String pwd = getCurrentVdr().getPassword();
		 if(pwd == null){
			 return StringUtils.EMPTY_STRING;
		 }
		return pwd;
	}

	/**
	 * Checks for enables remote wakeup
	 * 
	 * @return true, if remote wakeup is enabled
	 */
	public boolean isWakeupEnabled() {
		 return getCurrentVdr().isWakeupEnabled();
	}

	/**
	 * Gets the URL for the wakeup request
	 * 
	 * @return wakeup url
	 */
	public String getWakeupUrl() {
		return getCurrentVdr().getWakeupUrl();
	}

	/**
	 * Gets the user for the wakeup url
	 * 
	 * @return user name
	 */
	public String getWakeupUser() {
		return getCurrentVdr().getWakeupUser();
	}

	/**
	 * Gets the password for the wakeup url
	 * 
	 * @return password
	 */
	public String getWakeupPassword() {
		return getCurrentVdr().getWakeupPassword();
	}

	/**
	 * Checks for enabled alive check
	 * 
	 * @return true, if enabled
	 */
	public boolean isAliveCheckEnabled() {
		return getCurrentVdr().isAliveCheckEnabled();
	}
	
	public boolean isEnableRecStream(){
		return getCurrentVdr().isEnableRecStreaming();
	}

	public int getLivePort(){
		return getCurrentVdr().getLivePort();
	}
	
	/**
	 * Gets the time between alive checks
	 * 
	 * @return time in seconds
	 */
	public int getAliveCheckInterval() {
		return getCurrentVdr().getAliveCheckInterval();
	}

	/**
	 * Gets the buffer before the event start
	 * 
	 * @return pre event buffer
	 */
	public int getTimerPreMargin() {
		return getCurrentVdr().getTimerPreMargin();
	}

	/**
	 * Gets the buffer after the event stop
	 * 
	 * @return post event buffer
	 */
	public int getTimerPostMargin() {
		return getCurrentVdr().getTimerPostMargin();
	}

	/**
	 * Gets the default priority
	 * 
	 * @return default priority
	 */
	public int getTimerDefaultPriority() {
		return getCurrentVdr().getTimerDefaultPriority();
	}

	/**
	 * Gets the default lifetime
	 * 
	 * @return default lifetime
	 */
	public int getTimerDefaultLifetime() {
		return getCurrentVdr().getTimerDefaultLifetime();
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
		return getCurrentVdr().getMac();
	}

	/**
	 * Gets the selection which wakeup method to use
	 * 
	 * @return
	 * @since 0.2
	 */
	public String getWakeupMethod() {
		return getCurrentVdr().getWakeupMethod();
	}

	/**
	 * Getter for streaming port
	 * 
	 * @return
	 * @since 02.
	 */
	public int getStreamPort() {
		return getCurrentVdr().getStreamPort();
	}

	/**
	 * Getter for selected streaming format
	 * 
	 * @return
	 * @since 0.2
	 */
	public String getStreamFormat() {
		return getCurrentVdr().getStreamFormat();
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
		return PREFERENCE_FILE_NAME;
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
		return getCurrentVdr().isEnableRemux();
	}

	/**
	 * getter
	 * 
	 * @return
	 */
	public String getRemuxCommand() {
		return getCurrentVdr().getRemuxCommand();
	}

	/**
	 * getter
	 * 
	 * @return
	 */
	public String getRemuxParameter() {
		return getCurrentVdr().getRemuxParameter();
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

		prefs.epgSearchTimes = getString(context,
				R.string.epg_search_times_key, "");

		 prefs.use24hFormat = getBoolean(context,
		 R.string.gui_enable_24h_format_key, true);

		prefs.showChannelNumbers = getBoolean(context,
				R.string.gui_channels_show_channel_numbers_key, false);

		prefs.quiteOnBackButton = getBoolean(context,
				R.string.qui_quit_on_back_key, true);

		prefs.showImdbButton = getBoolean(context,
				R.string.qui_show_imdb_button_key, true);

		prefs.showOmdbButton = getBoolean(context,
				R.string.qui_show_omdb_button_key, true);


		prefs.showTmdbButton = getBoolean(context,
				R.string.qui_show_tmdb_button_key, true);


		prefs.imdbUrl = getString(context, R.string.qui_imdb_url_key, "akas.imdb.com");
	
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
		if (db == null) {
			db = new OrmDatabaseHelper(context);
		}

		synchronized (Preferences.class) {
			// if (thePrefs != null) {
			// return;
			// }
			initInternal(context);
			setLocale(context);
		}

		// 	if (current != null) {
		// 		return;
		// 	}

		final SharedPreferences sharedPrefs = getSharedPreferences(context);
		int id = sharedPrefs.getInt(
				context.getString(R.string.current_vdr_id_key), -1);

		Vdr vdr = null;
		if (id != -1) {
			vdr = db.getVdrDAO().queryForId(id);
		}

		if (vdr != null) {
			setCurrentVdr(context, vdr);
			return;
		}

		List<Vdr> list = db.getVdrDAO().queryForAll();
		if (list != null && list.isEmpty() == false) {
			vdr = list.get(0);
			setCurrentVdr(context, vdr);
			return;
		}
		if (initFromOldVersion(context) == false) {
			Intent intent = new Intent();
			intent.setClass(context, VdrListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(Intents.EMPTY_CONFIG, Boolean.TRUE);
			context.startActivity(intent);
			Toast.makeText(context, R.string.no_vdr, Toast.LENGTH_SHORT).show();
		}

	}

	private static boolean initFromOldVersion(Context context) {

		Vdr vdr = new Vdr();

		String host = getString(context, R.string.vdr_host_key, null);
		if(host == null){
			return false;
		}
		vdr.setHost(host);
		vdr.setName("Default");
		vdr.setPort(getInt(context, R.string.vdr_port_key, 6420));
		vdr.setPassword(getString(context, R.string.vdr_password_key, ""));
		vdr.setSecure(getBoolean(context, R.string.vdr_ssl_key, false));
		vdr.setStreamPort(getInt(context, R.string.vdr_stream_port, 3000));
		vdr.setStreamFormat(getString(context, R.string.vdr_stream_format, "TS"));

		vdr.setAliveCheckEnabled(getBoolean(context,
				R.string.alive_check_enabled_key, false));
		vdr.setAliveCheckInterval(getInt(context,
				R.string.alive_check_interval_key, 60));

		vdr.setChannelFilter(getString(context,
				R.string.channel_filter_last_key, "").replace(" ", ""));
		vdr.setFilterChannels(getBoolean(context,
				R.string.channel_filter_filter_key, false));
		vdr.setWakeupEnabled(getBoolean(context, R.string.wakeup_enabled_key,
				false));
		vdr.setWakeupUrl(getString(context, R.string.wakeup_url_key, ""));
		vdr.setWakeupUser(getString(context, R.string.wakeup_user_key, ""));
		vdr.setWakeupPassword(getString(context, R.string.wakeup_password_key,
				""));

		vdr.setTimerPreMargin(getInt(context,
				R.string.timer_pre_start_buffer_key, 5));
		vdr.setTimerPostMargin(getInt(context,
				R.string.timer_post_end_buffer_key, 30));

		vdr.setTimerDefaultPriority(getInt(context,
				R.string.timer_default_priority_key, 99));

		vdr.setTimerDefaultLifetime(getInt(context,
				R.string.timer_default_lifetime_key, 99));

		vdr.setEpgSearchTimes(getString(context, R.string.epg_search_times_key,
				""));

		vdr.setMac(getString(context, R.string.wakeup_wol_mac_key, ""));

		vdr.setWakeupMethod(getString(context, R.string.wakeup_method_key,
				"url"));

		vdr.setWolCustomBroadcast(getString(context,
				R.string.wakeup_wol_custom_broadcast_key, ""));

		vdr.setConnectionTimeout(getInt(context, R.string.vdr_conntimeout_key,
				10));
		vdr.setReadTimeout(getInt(context, R.string.vdr_readtimeout_key, 10));
		vdr.setTimeout(getInt(context, R.string.vdr_timeout_key, 120));

		vdr.setStreamingUsername(getString(context,
				R.string.streaming_username_key, ""));

		vdr.setStreamingPassword(getString(context,
				R.string.streaming_password_key, ""));

		vdr.setEncoding(getString(context, R.string.vdr_encoding_key, "utf-8"));

		if (db.getVdrDAO().create(vdr) != 1) {
			return false;
		}

		setCurrentVdr(context, vdr);

		return true;
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
		if(TextUtils.isEmpty(value) || !TextUtils.isDigitsOnly(value)){
			return 0;
		}
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
		String lc = getString(context, R.string.gui_custom_locale_key,
				DEFAULT_LANGUAGE_VALUE);
		Locale locale = null;
		// TODO lado this is very bad.
		if (lc.equals(DEFAULT_LANGUAGE_VALUE)) {
			String lang = Locale.getDefault().toString();
			if (lang.startsWith("de")) {
				locale = Locale.GERMAN;
			} else if (lang.startsWith("it")) {
				locale = Locale.ITALIAN;
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
