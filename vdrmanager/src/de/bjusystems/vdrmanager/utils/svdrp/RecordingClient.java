package de.bjusystems.vdrmanager.utils.svdrp;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Recording;

public class RecordingClient extends SvdrpClient<Recording> {

	@Override
	protected Recording parseAnswer(String line) {
		return new Recording(line);
	}

	@Override
	public int getProgressTextId() {
		return R.string.progress_recordings_loading;
	}

	@Override
	public synchronized void run()   {
		runCommand("recordings");
	}

}
