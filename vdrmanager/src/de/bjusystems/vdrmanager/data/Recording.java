package de.bjusystems.vdrmanager.data;

import static de.bjusystems.vdrmanager.gui.Utils.mapSpecialChars;

import java.util.Date;

import de.bjusystems.vdrmanager.StringUtils;
import de.bjusystems.vdrmanager.app.C;
import de.bjusystems.vdrmanager.gui.Utils;

public class Recording extends Event {

	public static String ROOT_FOLDER = "";

	public static final String FOLDERDELIMCHAR = "~";

	public Recording(String line) {
		String[] words = StringUtils.splitPreserveAllTokens(line,
				C.DATA_SEPARATOR);
		int idx = 0;
		index = Integer.valueOf(words[idx++]);
		start = new Date(Long.parseLong(words[idx++]) * 1000);
		stop = new Date(Long.parseLong(words[idx++]) * 1000);
		channelName = mapSpecialChars(words[idx++]);
		title = mapSpecialChars(words[idx++]);
		shortText = mapSpecialChars(words[idx++]);
		description = mapSpecialChars(words[idx++]);
		fileName = mapSpecialChars(words[idx++]);
		try {
			fileSize = Integer.valueOf(words[idx++]);
		} catch (NumberFormatException ex) {

			/************** TEMPORARY TO FIX THE BUG UNTIL Server's 0.13 */

			int offset = 0;
			int count = 0;
			while(offset != -1){
				offset = line.indexOf(":", offset + 1);
				count++;
				if(count == 5){
					 words = StringUtils.splitPreserveAllTokens(line.substring(0, offset) + Utils.unMapSpecialChars(":") + line.substring(offset+1), 
							C.DATA_SEPARATOR);  
					 break;
				}
			}
			
			idx = 0;
			index = Integer.valueOf(words[idx++]);
			start = new Date(Long.parseLong(words[idx++]) * 1000);
			stop = new Date(Long.parseLong(words[idx++]) * 1000);
			channelName = mapSpecialChars(words[idx++]);
			title = mapSpecialChars(words[idx++]);
			shortText = mapSpecialChars(words[idx++]);
			description = mapSpecialChars(words[idx++]);
			fileName = mapSpecialChars(words[idx++]);
			fileSize = Integer.valueOf(words[idx++]);

			if (idx < words.length) {
				channelId = words[idx++];
			}
			if (idx < words.length) {
				realDuration = Long.parseLong(words[idx++]) * 1000;
			}

			if (idx < words.length) {
				devInode = mapSpecialChars(words[idx++]);
			}

			if (idx < words.length) { // timer
				String data = words[idx++];
				if (data != null && data.length() > 0) {
					timerStopTime = new Date(Long.parseLong(data) * 1000L);
				}
			}

			if (idx < words.length) { // name
				String titleRaw = mapSpecialChars(words[idx++]);
				int idxdel = titleRaw.lastIndexOf(FOLDERDELIMCHAR);
				if (idxdel == -1) {
					title = titleRaw;
					folder = ROOT_FOLDER;
				} else {
					title = titleRaw.substring(idxdel + 1);
					String foldersRaw = titleRaw.substring(0, idxdel);
					folder = foldersRaw;

				}
			} else {
				folder = ROOT_FOLDER;
			}

			if (idx < words.length) {
				if (words[idx++].equals("1")) {
					neww = true;
				}
			}
			if (title.charAt(0) == '%') {
				cut = true;
				title = title.substring(1);
			}
			return;
		}

		if (idx < words.length) {
			channelId = words[idx++];
		}
		if (idx < words.length) {
			realDuration = Long.parseLong(words[idx++]) * 1000;
		}

		if (idx < words.length) {
			devInode = mapSpecialChars(words[idx++]);
		}

		if (idx < words.length) { // timer
			String data = words[idx++];
			if (data != null && data.length() > 0) {
				timerStopTime = new Date(Long.parseLong(data) * 1000L);
			}
		}

		if (idx < words.length) { // name
			String titleRaw = mapSpecialChars(words[idx++]);
			int idxdel = titleRaw.lastIndexOf(FOLDERDELIMCHAR);
			if (idxdel == -1) {
				title = titleRaw;
				folder = ROOT_FOLDER;
			} else {
				title = titleRaw.substring(idxdel + 1);
				String foldersRaw = titleRaw.substring(0, idxdel);
				folder = foldersRaw;

			}
		} else {
			folder = ROOT_FOLDER;
		}

		if (idx < words.length) {
			if (words[idx++].equals("1")) {
				neww = true;
			}
		}
		if (title.charAt(0) == '%') {
			cut = true;
			title = title.substring(1);
		}

	}

	private String folder;

	private String fileName;

	private int fileSize;

	private int index;

	private long realDuration = -1;

	private String devInode = null;

	private boolean cut = false;

	private boolean neww = false;

	/**
	 * If it is not null, recording is on going or will be on going until this
	 * date;
	 */
	private Date timerStopTime = null;

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
	 * 
	 * @return
	 */
	public long getRealDuration() {
		return realDuration;
	}

	public long getDuration() {
		if (timerStopTime != null) {
			return timerStopTime.getTime() - start.getTime();
		}

		if (realDuration != -1) {
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

	public String toCommandLine() {
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

	public boolean isCut() {
		return cut;
	}

	public boolean isNeww() {
		return neww;
	}

}