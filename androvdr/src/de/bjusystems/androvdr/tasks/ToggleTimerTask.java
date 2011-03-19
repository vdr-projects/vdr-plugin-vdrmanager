package de.bjusystems.androvdr.tasks;

import android.app.Activity;
import de.bjusystems.androvdr.data.Timer;
import de.bjusystems.androvdr.utils.svdrp.SetTimerClient;
import de.bjusystems.androvdr.R;

public abstract class ToggleTimerTask extends AsyncProgressTask<Timer> {

	public ToggleTimerTask(final Activity activity, final Timer timer) {
		super(activity, new SetTimerClient(timer, false) {
			boolean enabled = timer.isEnabled();

			@Override
			public int getProgressTextId() {
				if (enabled) {
					return R.string.progress_timer_disable;
				} else {
					return R.string.progress_timer_enable;
				}
			}
		});
		timer.setEnabled(!timer.isEnabled());
	}
}
