package de.bjusystems.vdrmanager.data;

import java.util.Date;

import android.text.TextUtils;

public abstract class BaseEvent implements Event {

	protected Event event;
	protected String channelNumber;
	protected String channelName;
	protected String title;
	protected String shortText;
	protected String description;
	protected Date start;
	protected Date stop;

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public void setChannelNumber(String channelNumber) {
		this.channelNumber = channelNumber;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setShortText(String shortText) {
		this.shortText = shortText;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public void setStop(Date stop) {
		this.stop = stop;
	}

	public BaseEvent(Event event) {
		this.event = event;
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

	public String getShortText() {
		if (TextUtils.isEmpty(shortText) == false) {
			return shortText;
		}
		if (TextUtils.isEmpty(description) == false) {
			if (description.length() < 20) {
				return description;
			}
			return TextUtils.substring(description, 0, 20) + "…";
		}
		return shortText;
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

}
