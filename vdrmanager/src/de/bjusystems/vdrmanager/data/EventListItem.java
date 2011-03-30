package de.bjusystems.vdrmanager.data;

import java.util.Date;


public class EventListItem implements Event {

	private final Timer timer;
	private final Epg epg;
	private final String header;
	private final Event event;

	public EventListItem(final Timer timer) {
		this.header = null;
		this.timer = timer;
		this.epg = null;
		this.event = timer;
	}

	public EventListItem(final Epg epg) {
		this.header = null;
		this.timer = null;
		this.epg = epg;
		this.event = epg;
	}

	public EventListItem(final String header) {
		this.header = header;
		this.timer = null;
		this.epg = null;
		this.event = null;
	}

	public boolean isHeader() {
		return header != null;
	}

	public boolean isTimer() {
		return timer != null;
	}

	public Date getStart() {
		return event != null ? event.getStart() : null;
	}

	public Date getStop() {
		return event != null ? event.getStop() : null;
	}

	public String getChannelNumber() {
		return event != null ? event.getChannelNumber() : null;
	}

	public String getChannelName() {
		return event != null ? event.getChannelName() : null;
	}

	public String getTitle() {
		return event != null ? event.getTitle() : null;
	}

	public String getDescription() {
		return event != null ? event.getDescription() : null;
	}

	public String getHeader() {
		return header;
	}

	public TimerState getTimerState() {
		return event != null ? event.getTimerState() : TimerState.None;
	}

	public Timer getTimer() {
		return timer;
	}

	public Epg getEpg() {
		return epg;
	}

	public Event getEvent() {
		return event;
	}

	@Override
	public String toString() {
		if (isHeader()) {
			return "Header: " + header;
		}

		final EventFormatter formatter = new EventFormatter(event);
		final StringBuilder text = new StringBuilder();
		text.append(isTimer() ? "Timer: " : "Event: ");
		text.append("Channel: ").append(event.getChannelNumber());
		text.append(" (").append(event.getChannelName()).append("), ");
		text.append("Zeit: ").append(formatter.getDate()).append(" ").append(formatter.getTime());
		return text.toString();
	}
}