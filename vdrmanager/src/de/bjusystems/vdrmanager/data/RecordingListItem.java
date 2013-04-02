package de.bjusystems.vdrmanager.data;


public class RecordingListItem extends EventListItem {

	public String folder;

	public RecordingListItem(Recording rec) {
		super(rec);
	}

	public RecordingListItem(String dailyHeader) {
		super(dailyHeader);
	}



	public boolean isFolder() {

		return folder != null;

	}

	@Override
	public String getHeader() {
		if (isFolder()) {
			return folder;
		} else {
			return super.getHeader();
		}
	}

}