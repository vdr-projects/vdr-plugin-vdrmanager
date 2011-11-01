package de.bjusystems.vdrmanager.data;

import java.util.Date;


import de.bjusystems.vdrmanager.StringUtils;
import de.bjusystems.vdrmanager.app.C;

import static de.bjusystems.vdrmanager.gui.Utils.mapSpecialChars;

public class Recording extends Event{
	
	public Recording(String line)  {
		final String[] words = StringUtils.splitPreserveAllTokens(line, C.DATA_SEPARATOR);
		int idx = 0;
		index = Integer.valueOf(words[idx++]);
		start = new Date(Long.parseLong(words[idx++]) * 1000);
		stop = new Date(Long.parseLong(words[idx++]) *  1000);
		channelName = mapSpecialChars(words[idx++]);
		title = mapSpecialChars(words[idx++]);
		shortText = mapSpecialChars(words[idx++]);
		description = mapSpecialChars(words[idx++]);
		fileName = mapSpecialChars(words[idx++]);
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
