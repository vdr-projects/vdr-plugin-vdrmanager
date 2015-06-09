package de.bjusystems.vdrmanager.data;

import de.bjusystems.vdrmanager.gui.Utils;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;

public class EventFormatter {

	private String time;
	private final String stop;
	private final String date;
	private final String longDate;
	private final String title;
	private final String shortText;
	private final String description;

	public EventFormatter(final Event event) {
			this(event,false);
	}
	/**
	 * @param event
	 * @param onlyStartTime  Event Time is rendered as 'start - stop' if false
	 */
	public EventFormatter(final Event event, boolean onlyStartTime) {
		DateFormatter formatter = new DateFormatter(event.getStart());
		this.date = formatter.getDateString();
		this.longDate = formatter.getDailyHeader();
		this.time = formatter.getTimeString();
		formatter = new DateFormatter(event.getStop());
		this.stop = formatter.getTimeString();
		if(onlyStartTime == false){
			this.time += " - " + stop;
		}
		this.title = Utils.mapSpecialChars(event.getTitle());
		this.shortText = Utils.mapSpecialChars(event.getShortText());
		this.description = Utils.mapSpecialChars(event.getDescription());
	}

	public String getShortText() {
		return shortText;
	}

	
	public String getStop(){
		return stop;
	}
	
	public String getTime() {
		return time;
	}

	public String getDate() {
		return date;
	}

	public String getLongDate() {
		return longDate;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}
}
