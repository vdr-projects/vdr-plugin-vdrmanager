package de.bjusystems.vdrmanager.utils.svdrp;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Recording;

public class RecordingClient extends SvdrpClient<Recording> implements SvdrpListener<Recording> {

	public void svdrpEvent(SvdrpEvent event, Recording result) {
//		if(event == SvdrpEvent.RESULT_RECEIVED){
//			results.add(result);
//		}
	}

	@Override
	protected Recording parseAnswer(String line) {
		return new Recording(line);
	}

	@Override
	public int getProgressTextId() {
		return R.string.progress_recordings_loading;
	}

	@Override
	public void run() throws SvdrpException {
		runCommand("recordings");
	}

}
