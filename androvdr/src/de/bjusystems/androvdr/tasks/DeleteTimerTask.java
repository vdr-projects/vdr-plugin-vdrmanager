package de.bjusystems.androvdr.tasks;

import android.app.Activity;
import de.bjusystems.androvdr.data.Timer;
import de.bjusystems.androvdr.utils.svdrp.SetTimerClient;
import de.bjusystems.androvdr.R;

public abstract class DeleteTimerTask extends AsyncProgressTask<Timer> {

	public DeleteTimerTask(final Activity activity, final Timer timer) {
		super(activity, new SetTimerClient(timer, true) {
			@Override
			public int getProgressTextId() {
				return R.string.progress_timer_delete;
			}
		});
	}
}
