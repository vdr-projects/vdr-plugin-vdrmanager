package de.bjusystems.vdrmanager.data;

import java.util.Date;


public class Recording extends BaseEvent{
	
	public Recording(String line)  {
		super(null);
		final String[] words = line.split(":");
		start = new Date(Long.parseLong(words[0])*1000);
		stop = new Date(Long.parseLong(words[0] + 1000 * 60 * 60 * 2)*1000);
		channelName = words[2];
		title = words[3];
		shortText = words[4];
		description = words[5];
		fileName = words[6];
	}
	
	private String fileName;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public TimerState getTimerState() {
		return TimerState.Recorded;
	}

}
