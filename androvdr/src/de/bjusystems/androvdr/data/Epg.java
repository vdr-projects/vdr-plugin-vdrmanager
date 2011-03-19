package de.bjusystems.androvdr.data;

import java.util.Date;

/**
 * Class for EPG events
 * @author bju
 */
public class Epg implements Event {

	private final String channelNumber;
	private final String channelName;
	private final String title;
	private final String description;
	private final Date start;
	private final Date stop;
	private Timer timer;

	public Epg(final String line) {

		final String[] words = line.split(":");

		channelNumber = words[0].substring(1);
		channelName = words[1];
		start = new Date(Long.parseLong(words[2])*1000);
		stop = new Date(Long.parseLong(words[3])*1000);
		title = words[4];
		description = words.length > 5 ? words[5] : "";
	}

	public String getChannelNumber() {
		return channelNumber;
	}
	public String getChannelName() {
		return channelName;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public Date getStart() {
		return start;
	}
	public Date getStop() {
		return stop;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(final Timer timer) {
		this.timer = timer;
	}

	public TimerState getTimerState() {
		if (timer == null) {
			return TimerState.None;
		} else {
			return timer.getTimerState();
		}
	}
}
