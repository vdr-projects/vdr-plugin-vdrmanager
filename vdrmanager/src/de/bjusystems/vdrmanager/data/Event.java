package de.bjusystems.vdrmanager.data;

import java.util.Date;

public interface Event {

	public enum TimerState {
		None,
		Active,
		Inactive,
		Recording
	}

	String getChannelNumber();
	String getChannelName();
	String getTitle();
	String getDescription();
	Date getStart();
	Date getStop();
	TimerState getTimerState();
}
