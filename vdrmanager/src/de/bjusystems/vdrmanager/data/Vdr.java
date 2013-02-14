package de.bjusystems.vdrmanager.data;

import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Vdr {

	/**
	 *
	 */
	@DatabaseField(columnName = "_id", generatedId = true)
	private Integer id;

	@DatabaseField(columnName = "name")
	private String name = "-";

	/**
	 * Use secure channel
	 */
	@DatabaseField(defaultValue = "false")
	private boolean secure;

	/** SVDRP host name or ip */
	@DatabaseField
	private String host;

	/** SVDRP port */
	@DatabaseField
	private int port;

	/** Password */
	@DatabaseField
	private String password;

	/** should channels be filtered? */
	@DatabaseField
	private boolean filterChannels = false;

	/** last channel to retrieve */
	@DatabaseField
	private String channelFilter = "";

	/** Enable remote wakeup */
	@DatabaseField
	private boolean wakeupEnabled;

	/** URL of the wakeup script */
	@DatabaseField
	private String wakeupUrl;

	/** User for wakeup */
	@DatabaseField
	private String wakeupUser;

	/** Password for wakeup */
	@DatabaseField
	private String wakeupPassword;

	/**
	 * vdr mac for wol
	 *
	 * @since 0.2
	 */
	@DatabaseField
	private String mac;

	/**
	 * which wakeup method to use
	 *
	 * @since 0.2
	 *
	 */
	@DatabaseField
	private String wakeupMethod;

	/** Check for running VDR is enabled */
	@DatabaseField
	private boolean aliveCheckEnabled;

	/** Intervall for alive test */
	@DatabaseField
	private int aliveCheckInterval;

	/** Buffer before event */
	@DatabaseField
	private int timerPreMargin;

	/** Buffer after event */
	@DatabaseField
	private int timerPostMargin;

	/** Default priority */
	@DatabaseField
	private int timerDefaultPriority;

	/** Default lifetime */
	@DatabaseField
	private int timerDefaultLifetime;

	/** user defined epg search times */
	@DatabaseField
	private String epgSearchTimes;

	/**
	 * Which port to use for streaming
	 *
	 * @since 0.2
	 */
	@DatabaseField
	private int streamPort = 3000;

	/**
	 * Which format to use for streaming
	 *
	 * @since 0.2
	 */
	@DatabaseField
	private String streamFormat = "TS";

	/**
	 * Do not send broadcasts, send directly to the host (router problem)
	 *
	 * @since 0.2
	 */
	@DatabaseField
	private String wolCustomBroadcast = "";

	/**
	 * Use remux ?
	 */
	@DatabaseField
	private boolean enableRemux;

	/**
	 * Remux command
	 */
	@DatabaseField
	private String remuxCommand;

	/**
	 * Remux command Parameter
	 */
	@DatabaseField
	private String remuxParameter;

	@DatabaseField
	private String encoding = "utf-8";

	/**
	 * Connection timeout
	 */
	@DatabaseField
	private int connectionTimeout;

	/**
	 * Read Timeout
	 */
	@DatabaseField
	private int readTimeout;

	/**
	 * Timeout for a whole command run
	 */
	@DatabaseField
	private int timeout;

	@DatabaseField
	private String streamingUsername;

	@DatabaseField
	private String streamingPassword;

	@DatabaseField
	private int livePort;

	@DatabaseField
	private String recStreamMethod;

	@DatabaseField
	private boolean enableRecStreaming = false;

	public String getRecStreamMethod() {
		return recStreamMethod;
	}

	public void setRecStreamMethod(String recStreamMethod) {
		this.recStreamMethod = recStreamMethod;
	}

	public int getLivePort() {
		return livePort;
	}

	public void setLivePort(int livePort) {
		this.livePort = livePort;
	}

	public boolean isEnableRecStreaming() {
		return enableRecStreaming;
	}

	public void setEnableRecStreaming(boolean enableRecStreaming) {
		this.enableRecStreaming = enableRecStreaming;
	}

	public String getStreamingPassword() {
		return streamingPassword;
	}

	public void setStreamingPassword(String streamingPassword) {
		this.streamingPassword = streamingPassword;
	}

	public String getStreamingUsername() {
		return streamingUsername;
	}

	public void setStreamingUsername(String streamingUsername) {
		this.streamingUsername = streamingUsername;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isFilterChannels() {
		return filterChannels;
	}

	public void setFilterChannels(boolean filterChannels) {
		this.filterChannels = filterChannels;
	}

	public String getChannelFilter() {
		return channelFilter;
	}

	public void setChannelFilter(String channelFilter) {
		this.channelFilter = channelFilter;
	}

	public boolean isWakeupEnabled() {
		return wakeupEnabled;
	}

	public void setWakeupEnabled(boolean wakeupEnabled) {
		this.wakeupEnabled = wakeupEnabled;
	}

	public String getWakeupUrl() {
		return wakeupUrl;
	}

	public void setWakeupUrl(String wakeupUrl) {
		this.wakeupUrl = wakeupUrl;
	}

	public String getWakeupUser() {
		return wakeupUser;
	}

	public void setWakeupUser(String wakeupUser) {
		this.wakeupUser = wakeupUser;
	}

	public String getWakeupPassword() {
		return wakeupPassword;
	}

	public void setWakeupPassword(String wakeupPassword) {
		this.wakeupPassword = wakeupPassword;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getWakeupMethod() {
		return wakeupMethod;
	}

	public void setWakeupMethod(String wakeupMethod) {
		this.wakeupMethod = wakeupMethod;
	}

	public boolean isAliveCheckEnabled() {
		return aliveCheckEnabled;
	}

	public void setAliveCheckEnabled(boolean aliveCheckEnabled) {
		this.aliveCheckEnabled = aliveCheckEnabled;
	}

	public int getAliveCheckInterval() {
		return aliveCheckInterval;
	}

	public void setAliveCheckInterval(int aliveCheckInterval) {
		this.aliveCheckInterval = aliveCheckInterval;
	}

	public int getTimerPreMargin() {
		return timerPreMargin;
	}

	public void setTimerPreMargin(int timerPreMargin) {
		this.timerPreMargin = timerPreMargin;
	}

	public int getTimerPostMargin() {
		return timerPostMargin;
	}

	public void setTimerPostMargin(int timerPostMargin) {
		this.timerPostMargin = timerPostMargin;
	}

	public int getTimerDefaultPriority() {
		return timerDefaultPriority;
	}

	public void setTimerDefaultPriority(int timerDefaultPriority) {
		this.timerDefaultPriority = timerDefaultPriority;
	}

	public int getTimerDefaultLifetime() {
		return timerDefaultLifetime;
	}

	public void setTimerDefaultLifetime(int timerDefaultLifetime) {
		this.timerDefaultLifetime = timerDefaultLifetime;
	}

	public String getEpgSearchTimes() {
		return epgSearchTimes;
	}

	public void setEpgSearchTimes(String epgSearchTimes) {
		this.epgSearchTimes = epgSearchTimes;
	}

	public int getStreamPort() {
		return streamPort;
	}

	public void setStreamPort(int streamPort) {
		this.streamPort = streamPort;
	}

	public String getStreamFormat() {
		return streamFormat;
	}

	public void setStreamFormat(String streamFormat) {
		this.streamFormat = streamFormat;
	}

	public String getWolCustomBroadcast() {
		return wolCustomBroadcast;
	}

	public void setWolCustomBroadcast(String wolCustomBroadcast) {
		this.wolCustomBroadcast = wolCustomBroadcast;
	}

	public boolean isEnableRemux() {
		return enableRemux;
	}

	public void setEnableRemux(boolean enableRemux) {
		this.enableRemux = enableRemux;
	}

	public String getRemuxCommand() {
		return remuxCommand;
	}

	public void setRemuxCommand(String remuxCommand) {
		this.remuxCommand = remuxCommand;
	}

	public String getRemuxParameter() {
		return remuxParameter;
	}

	public void setRemuxParameter(String remuxParameter) {
		this.remuxParameter = remuxParameter;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	private static <T> T get(Map<String, Object> map, String key) {
		return get(map, key, null);
	}

	private static <T> T get(Map<String, Object> map, String key, Object def) {
		if(map.containsKey(key)){
			return (T) map.get(key);
		}
		return (T)def;
	}

	private static Integer getInteger(Map<String, Object> map, String key,
			Integer def) {
		if (map.containsKey(key) == false) {
			return def;
		}

		Object obj = get(map, key);
		if (obj instanceof Integer) {
			return (Integer) obj;
		}
		return Integer.valueOf(String.valueOf(obj));
	}

	private static Integer getInteger(Map<String, Object> map, String key) {
		if (map.containsKey(key) == false) {
			return Integer.valueOf(0);
		}

		Object obj = get(map, key);
		if (obj instanceof Integer) {
			return (Integer) obj;
		}
		return Integer.valueOf(String.valueOf(obj));
	}

	private static Boolean getBoolean(Map<String, Object> map, String key) {
		return getBoolean(map, key, false);
	}

	private static Boolean getBoolean(Map<String, Object> map, String key,
			boolean defValue) {
		if (map.containsKey(key) == false) {
			return defValue;
		}
		Object obj = get(map, key);
		if (obj instanceof Boolean) {
			return (Boolean) obj;
		}
		return Boolean.valueOf(String.valueOf(obj));
	}

	public HashMap<String, Object> toMap() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("vdr_name", name);
		map.put("vdr_host", host);
		map.put("vdr_port", port);
		map.put("vdr_password", password);
		map.put("vdr_secure", secure);

		map.put("limit_channels", filterChannels);
		map.put("last_channel", channelFilter);

		map.put("key_wakeup_enabled", wakeupEnabled);
		map.put("key_wakeup_url", wakeupUrl);
		map.put("key_wakeup_user", wakeupUser);
		map.put("key_wakeup_password", wakeupPassword);
		map.put("key_wakeup_method", wakeupMethod);
		map.put("key_wol_custom_broadcast", wolCustomBroadcast);
		map.put("key_wakeup_wol_mac", mac);

		map.put("key_conntimeout_key", connectionTimeout);
		map.put("key_vdr_readtimeout", readTimeout);
		map.put("key_vdr_timeout", timeout);

		map.put("timer_pre_start_buffer", timerPreMargin);
		map.put("timer_post_end_buffer", timerPostMargin);
		map.put("timer_default_priority", timerDefaultPriority);
		map.put("timer_default_lifetime", timerDefaultLifetime);

		map.put("streamingport", streamPort);
		map.put("key_streaming_password", streamingPassword);
		map.put("key_streaming_username", streamingUsername);
		map.put("key_vdr_encoding", encoding);
		map.put("livetv_streamformat", streamFormat);
		map.put("remux_command", remuxCommand);
		map.put("remux_parameter", remuxParameter);
		map.put("remux_enable", enableRemux);
		map.put("key_rec_stream_enable", enableRecStreaming);
		map.put("key_live_port", livePort);
		map.put("key_recstream_method", recStreamMethod);
		return map;
	}

	public void set(Map<String, Object> map) {
		name = get(map, "vdr_name", name);
		host = get(map, "vdr_host", host);
		port = getInteger(map, "vdr_port", port);
		password = get(map, "vdr_password", password);
		secure = getBoolean(map, "vdr_secure", secure);

		filterChannels = getBoolean(map, "limit_channels", filterChannels);
		channelFilter = get(map, "last_channel", channelFilter);

		wakeupEnabled = getBoolean(map, "key_wakeup_enabled", wakeupEnabled);
		wakeupUrl = get(map, "key_wakeup_url", wakeupUrl);
		wakeupUser = get(map, "key_wakeup_user", wakeupUser);
		wakeupPassword = get(map, "key_wakeup_password", wakeupPassword);
		wakeupMethod = get(map, "key_wakeup_method", wakeupMethod);
		wolCustomBroadcast = get(map, "key_wol_custom_broadcast",
				wolCustomBroadcast);
		mac = get(map, "key_wakeup_wol_mac", mac);

		connectionTimeout = getInteger(map, "key_conntimeout_key",
				connectionTimeout);
		readTimeout = getInteger(map, "key_vdr_readtimeout", readTimeout);
		timeout = getInteger(map, "key_vdr_timeout", timeout);

		timerPreMargin = getInteger(map, "timer_pre_start_buffer",
				timerPreMargin);
		timerPostMargin = getInteger(map, "timer_post_end_buffer",
				timerPostMargin);
		timerDefaultPriority = getInteger(map, "timer_default_priority",
				timerDefaultPriority);
		timerDefaultLifetime = getInteger(map, "timer_default_lifetime",
				timerDefaultLifetime);

		streamPort = getInteger(map, "streamingport", streamPort);
		streamingPassword = get(map, "key_streaming_password",
				streamingPassword);
		streamingUsername = get(map, "key_streaming_username",
				streamingUsername);
		encoding = get(map, "key_vdr_encoding", encoding);
		streamFormat = get(map, "livetv_streamformat", streamFormat);
		remuxCommand = get(map, "remux_command", remuxCommand);
		remuxParameter = get(map, "remux_parameter", remuxParameter);
		enableRemux = getBoolean(map, "remux_enable", enableRemux);

		enableRecStreaming = getBoolean(map, "key_rec_stream_enable",
				enableRecStreaming);
		livePort = getInteger(map, "key_live_port", livePort);
		recStreamMethod = get(map, "key_recstream_method", recStreamMethod);

	}

	public void init(Map<String, Object> map) {
		name = get(map, "vdr_name", name);
		host = get(map, "vdr_host");
		port = getInteger(map, "vdr_port");
		password = get(map, "vdr_password");
		secure = getBoolean(map, "vdr_secure");

		filterChannels = getBoolean(map, "limit_channels");
		channelFilter = get(map, "last_channel");

		wakeupEnabled = getBoolean(map, "key_wakeup_enabled");
		wakeupUrl = get(map, "key_wakeup_url", "0.0.0.0");
		wakeupUser = get(map, "key_wakeup_user", "");
		wakeupPassword = get(map, "key_wakeup_password", "");
		wakeupMethod = get(map, "key_wakeup_method", "wol");
		wolCustomBroadcast = get(map, "key_wol_custom_broadcast", "");
		mac = get(map, "key_wakeup_wol_mac", "");

		connectionTimeout = getInteger(map, "key_conntimeout_key");
		readTimeout = getInteger(map, "key_vdr_readtimeout");
		timeout = getInteger(map, "key_vdr_timeout");

		timerPreMargin = getInteger(map, "timer_pre_start_buffer", 5);
		timerPostMargin = getInteger(map, "timer_post_end_buffer", 30);
		timerDefaultPriority = getInteger(map, "timer_default_priority");
		timerDefaultLifetime = getInteger(map, "timer_default_lifetime");

		streamPort = getInteger(map, "streamingport");
		streamingPassword = get(map, "key_streaming_password");
		streamingUsername = get(map, "key_streaming_username");
		encoding = get(map, "key_vdr_encoding");
		streamFormat = get(map, "livetv_streamformat");
		remuxCommand = get(map, "remux_command");
		remuxParameter = get(map, "remux_parameter");
		enableRemux = getBoolean(map, "remux_enable");

		enableRecStreaming = getBoolean(map, "key_rec_stream_enable");
		livePort = getInteger(map, "key_live_port");
		recStreamMethod = get(map, "key_recstream_method");

	}

}
