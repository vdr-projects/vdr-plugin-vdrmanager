package de.bjusystems.androvdr.data;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Class for timer data
 * @author bju
 */
public class Timer implements Event {

	private static final int ENABLED = 1;
	private static final int INSTANT = 2;
	private static final int VPS = 4;
	private static final int RECORDING = 8;

	private final int number;
	private int flags;
	private String title;
	private String description;
	private Date start;
	private Date stop;
	private final String channelNumber;
	private final String channelName;
	private final int priority;
	private final int lifetime;


	/**
	 * Constructs a timer from SvdrpHelper result line
	 * @param timerData result line
	 * @param channels list of channels
	 */
	public Timer(final String timerData) {

		final String[] values = timerData.split(":");

		// number
		this.number = Integer.valueOf(values[0].substring(1));

		// flags, channel number and channel name
		this.flags = Integer.valueOf(values[1]);
		this.channelNumber = values[2];
		this.channelName = values[3];

		// get start and stop
		this.start = new Date(Long.parseLong(values[4]) * 1000);
		this.stop = new Date(Long.parseLong(values[5]) * 1000);

		// priority and lifetime
		this.priority = Integer.valueOf(values[6]);
		this.lifetime = Integer.valueOf(values[7]);

		// title and description
		this.title = values[8];
		this.description = values.length > 9 ? values[9] : "";
	}

	public Timer(final Epg event) {

		final Preferences prefs = Preferences.getPreferences();

		this.number = 0;
		this.flags = ENABLED;
		this.channelNumber = event.getChannelNumber();
		this.channelName = event.getChannelName();
		this.priority = prefs.getTimerDefaultPriority();
		this.lifetime = prefs.getTimerDefaultLifetime();

		this.start = new Date(event.getStart().getTime() - prefs.getTimerPreMargin() * 60000);
		this.stop = new Date(event.getStop().getTime() + prefs.getTimerPostMargin() * 60000);

		this.title = event.getTitle();
		this.description = event.getDescription();
	}

	public String toCommandLine() {

		final StringBuilder line = new StringBuilder();

		line.append(number).append(":");
		line.append(flags).append(":");
		line.append(channelNumber).append(":");

		final Calendar cal = new GregorianCalendar();
		cal.setTime(start);
		line.append(String.format("%04d-%02d-%02d:", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH)));
		line.append(String.format("%02d%02d:", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));

		cal.setTime(stop);
		line.append(String.format("%02d%02d:", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));

		line.append(priority).append(":");
		line.append(lifetime).append(":");
		line.append(title).append(":");
		line.append(description);

		return line.toString();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public int getNumber() {
		return number;
	}

	public Date getStart() {
		return start;
	}

	public Date getStop() {
		return stop;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getChannelNumber() {
		return channelNumber;
	}

	public String getChannelName() {
		return channelName;
	}

	public int getPriority() {
		return priority;
	}

	public int getLifetime() {
		return lifetime;
	}

	public TimerState getTimerState() {
		if (isEnabled()) {
			if (isRecording()) {
				return TimerState.Recording;
			}
			return TimerState.Active;
		}
		return TimerState.Inactive;
	}

	public boolean isEnabled() {
		return (flags & ENABLED) == ENABLED;
	}

	public boolean isInstant() {
		return (flags & INSTANT) == INSTANT;
	}

	public boolean isVps() {
		return (flags & VPS) == VPS;
	}
	public boolean isRecording() {
		return (flags & RECORDING) == RECORDING;
	}

	public void setStart(final Date start) {
		this.start = start;
	}

	public void setStop(final Date stop) {
		this.stop = stop;
	}

	public void setEnabled(final boolean enabled) {
		if (enabled) {
			flags = flags | ENABLED;
		} else {
			flags = flags & ~ENABLED;
		}
	}
}
