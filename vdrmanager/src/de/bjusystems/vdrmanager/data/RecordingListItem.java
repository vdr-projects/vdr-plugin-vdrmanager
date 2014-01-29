package de.bjusystems.vdrmanager.data;

import de.bjusystems.vdrmanager.gui.RecordingDir;


public class RecordingListItem extends EventListItem {

	public RecordingDir folder;

	//public Integer count = 0;

	public RecordingListItem(Recording rec) {
		super(rec);
	}

	public RecordingListItem(String dailyHeader) {
		super(dailyHeader);
	}



	@Override
	public String getTitle() {
		if(isFolder()){
			return folder.getName();
		}
		return super.getTitle();
	}
	
	public boolean isFolder() {

		return folder != null;

	}

	@Override
	public String getHeader() {
		if (isFolder()) {
			return folder.getName();
		} else {
			return super.getHeader();
		}
	}

}