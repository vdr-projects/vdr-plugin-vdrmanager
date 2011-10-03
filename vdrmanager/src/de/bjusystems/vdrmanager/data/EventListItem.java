package de.bjusystems.vdrmanager.data;

import java.util.Date;

import de.bjusystems.vdrmanager.gui.Utils;


public class EventListItem extends BaseEvent {

	private final Recording rec;
	private final Timer timer;
	private final Epg epg;
	private final String header;
	


	public EventListItem(final Recording rec) {
		super(rec);
		this.header = null;
		this.rec = rec;
		this.epg = null;
		this.timer = null;
	}

	public EventListItem(final Timer timer) {
		super(timer);
		this.header = null;
		this.timer = timer;
		this.epg = null;
		this.rec = null;
	}

	public EventListItem(final Epg epg) {
		super(epg);
		this.header = null;
		this.timer = null;
		this.epg = epg;
		this.rec = null;
	}

	public EventListItem(final String header) {
		super(null);
		this.header = header;
		this.timer = null;
		this.epg = null;
		this.rec = null;
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
	
	public boolean isLive(){
		return Utils.isLive(event);
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

	public String getShortText() {
		return event != null ? event.getShortText() : null;
	}
}