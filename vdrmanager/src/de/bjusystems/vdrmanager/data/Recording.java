package de.bjusystems.vdrmanager.data;

import java.util.Date;


public class Recording extends BaseEvent{
	
	public Recording(String line)  {
		final String[] words = line.split(":");
		int idx = 0;
		index = Integer.valueOf(words[idx++]);
		start = new Date(Long.parseLong(words[idx++]) * 1000);
		stop = new Date(Long.parseLong(words[idx++]) *  1000);
		channelName = words[idx++];
		title = words[idx++];
		shortText = words[idx++];
		description = words[idx++];
		fileName = words[idx++];
		fileSize = Integer.valueOf(words[idx++]);

	}
	
	private String fileName;

	private int fileSize;
	
	private int index;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

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
	
	public String toCommandLine(){
		return String.valueOf(index);
	}
	

}
