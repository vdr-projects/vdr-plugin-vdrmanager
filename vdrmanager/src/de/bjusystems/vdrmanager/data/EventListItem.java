package de.bjusystems.vdrmanager.data;

import de.bjusystems.vdrmanager.gui.Utils;

/**
 * @author lado
 * 
 *         TODO auf Event Interface umstellen und die Aufrufen an event
 *         delegieren. Das hier ist nicht gut.
 */
public class EventListItem extends Event {

	Event event;

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	//private final Recording rec;
	//private final Timer timer;
	//private final Epg epg;
	private final String header;

	//
	// public EventListItem(final Event event){
	// if(event instanceof Recording){
	// this((Recording)event);
	// } else if (event instanceof Timer){
	// this((Timer)event);
	// } else {
	// this((Epg)event);
	// }
	// throw new IllegalArgumentException("Uknown event type " + event);
	// }


	public EventListItem(final Event rec) {
		super(rec);
		event = rec;
		this.header = null;
//		this.rec = rec;
	//	this.epg = null;
		//this.timer = null;
	}
//	
//	public EventListItem(final Recording rec) {
//		super(rec);
//		event = rec;
//		this.header = null;
//		this.rec = rec;
//		this.epg = null;
//		this.timer = null;
//	}
//
//	public EventListItem(final Timer timer) {
//		super(timer);
//		event = timer;
//		this.header = null;
//		this.timer = timer;
//		this.epg = null;
//		this.rec = null;
//	}
//
//	public EventListItem(final Epg epg) {
//		super(epg);
//		event = epg;
//		this.header = null;
//		this.timer = null;
//		this.epg = epg;
//		this.rec = null;
//	}
//
//	@Override
//	public TimerState getTimerState() {
//		return event.getTimerState();
//	}

	public EventListItem(final String header) {
		this.header = header;
	}

	public boolean isHeader() {
		return header != null;
	}

//	public boolean isTimer() {
//		return event instanceof Timer;
//	}

	public String getHeader() {
		return header;
	}

//	public Timer getTimer() {
//		return timer;
//	}
//
//	public Epg getEpg() {
//		return epg;
//	}
//
//	public Recording getRecording() {
//		return rec;
//	}

	// public Event getEvent() {
	// return event;
	// }
	//
	public boolean isLive() {
		return Utils.isLive(this);
	}

	@Override
	public String toString() {
		if (isHeader()) {
			return "Header: " + header;
		}

		final EventFormatter formatter = new EventFormatter(this);
		final StringBuilder text = new StringBuilder();
		text.append("Timer / Event: ");
		text.append("Channel: ").append(getChannelNumber());
		text.append(" (").append(getChannelName()).append("), ");
		text.append("Zeit: ").append(formatter.getDate()).append(" ")
				.append(formatter.getTime());
		return text.toString();
	}
	
	
	@Override
	public long getDuration() {
		if(event != null){
			return event.getDuration();
		}
		return super.getDuration();
	}
	
	@Override
	public String getChannelId() {
		if(event != null){
			return event.getChannelId();
		}
		return null;
	}
	
	@Override
	public String getStreamId() {
		if(event == null){
			return null;
		}
		return event.getStreamId();
	}
//
//	@Override
//	public Timer getTimer() {
//		return event.getTimer();
//	}

}