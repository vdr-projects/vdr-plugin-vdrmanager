package de.bjusystems.androvdr.data;

import de.bjusystems.androvdr.utils.date.DateFormatter;

public class EventFormatter {

	private String time;
	private final String date;
	private final String longDate;
	private final String title;
	private final String description;

	public EventFormatter(final Event event) {
		DateFormatter formatter = new DateFormatter(event.getStart());
		this.date = formatter.getDateString();
		this.longDate = formatter.getDailyHeader();
		this.time = formatter.getTimeString();
		formatter = new DateFormatter(event.getStop());
		this.time += " - " + formatter.getTimeString();
		this.title = event.getTitle().replace("|##", ":").replace("||#", "\n");
		this.description = event.getDescription().replace("|##", ":").replace("||#", "\n");
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
