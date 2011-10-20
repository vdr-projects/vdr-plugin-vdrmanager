package de.bjusystems.vdrmanager.data;

import java.util.Date;

public interface Event {

	public enum TimerState {
		None,
		Active,
		Inactive,
		Recording,
		Recorded
		;
	}

	String getChannelNumber();
	String getChannelName();
	String getTitle();
	String getDescription();
	String getShortText();
	Date getStart();
	Date getStop();
//	TimerState getTimerState();
}
