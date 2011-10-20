package de.bjusystems.vdrmanager.data;

import java.util.Date;


public class Recording extends BaseEvent{
	
	public Recording(String line)  {
		final String[] words = line.split(":");
		start = new Date(Long.parseLong(words[0])*1000);
		stop = new Date(Long.parseLong(words[1]) * 1000);
		fileSize = Integer.valueOf(words[2]);
		channelName = words[3];
		title = words[4];
		shortText = words[5];
		description = words[6];
		fileName = words[7];
	}
	
	private String fileName;

	private int fileSize;

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

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
