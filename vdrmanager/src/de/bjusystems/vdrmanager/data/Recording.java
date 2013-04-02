package de.bjusystems.vdrmanager.data;

import static de.bjusystems.vdrmanager.gui.Utils.mapSpecialChars;

import java.util.Date;

import de.bjusystems.vdrmanager.StringUtils;
import de.bjusystems.vdrmanager.app.C;

public class Recording extends Event{

	public static String ROOT_FOLDER = "";
	public static final String FOLDERDELIMCHAR = "~";

	public class Folder {

		public String name;

		public Folder parent;

		private String path;

		public boolean isRoot(){
			return parent == null;
		}

		public String getFullPath(){
			if(this.path != null){
				return this.path;
			}
			if(isRoot()){
				this.path = "";
			} else {
				this.path = parent.getFullPath() + "/" + name;
			}

			return path;
		}

		@Override
		public boolean equals(Object o) {
			if(o == this){
				return true;
			}
			return ((Folder)o).name.equals(this.name);
		}
		@Override
		public int hashCode() {
			return name.hashCode();
		}

		@Override
		public String toString() {
			return name + "("+path+")";
		}
	};

	public Recording(String line)  {
		final String[] words = StringUtils.splitPreserveAllTokens(line, C.DATA_SEPARATOR);
		int idx = 0;
		index = Integer.valueOf(words[idx++]);
		start = new Date(Long.parseLong(words[idx++]) * 1000);
		stop = new Date(Long.parseLong(words[idx++]) *  1000);
		channelName = mapSpecialChars(words[idx++]);
		eventTitle = mapSpecialChars(words[idx++]);
		shortText = mapSpecialChars(words[idx++]);
		description = mapSpecialChars(words[idx++]);
		fileName = mapSpecialChars(words[idx++]);
		fileSize = Integer.valueOf(words[idx++]);
		if(idx < words.length){
			channelId = words[idx++];
		}
		if(idx < words.length){
			realDuration = Long.parseLong(words[idx++]) * 1000;
		}

		if(idx < words.length){
			devInode = mapSpecialChars(words[idx++]);
		}

		if(idx  < words.length) { //timer
			String data = words[idx++];
			if(data != null && data.length() > 0){
				timerStopTime = new Date(Long.parseLong(data) * 1000L);
			}
		}

		if(idx  < words.length) { //name
			String titleRaw = words[idx];
			int idxdel = titleRaw.lastIndexOf(FOLDERDELIMCHAR);
			if(idxdel == -1){
				title = titleRaw;
				folder = ROOT_FOLDER;
			} else {
				title = titleRaw.substring(idxdel+1);

				String foldersRaw = titleRaw.substring(0, idxdel);

				folder = foldersRaw;

			}
		}

	}

	private String folder;

	private String fileName;

	private int fileSize;

	private int index;

	private long realDuration = -1;

	private String devInode = null;

	private String eventTitle = null;

	/**
	 * If it is not null, recording is on going or will be on going until this date;
	 */
	private Date timerStopTime = null;

	public String getEventTitle() {
		return eventTitle;
	}

	public void setEventTitle(String eventTitle) {
		this.eventTitle = eventTitle;
	}

	public Date getTimerStopTime() {
		return timerStopTime;
	}

	public void setTimerStopTime(Date timerStopTime) {
		this.timerStopTime = timerStopTime;
	}

	public String getDevInode() {
		return devInode;
	}

	public void setDevInode(String devInode) {
		this.devInode = devInode;
	}

	/**
	 * in millis
	 * @return
	 */
	public long getRealDuration() {
		return realDuration;
	}

	public long getDuration(){
		if(timerStopTime != null){
			return timerStopTime.getTime() - start.getTime();
		}

		if(realDuration != -1){
			return realDuration;
		}
		return super.getDuration();
	}

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

	public String toCommandLine(){
		return String.valueOf(index);
	}

	@Override
	public String toString() {
		return title;
	}


	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}


}