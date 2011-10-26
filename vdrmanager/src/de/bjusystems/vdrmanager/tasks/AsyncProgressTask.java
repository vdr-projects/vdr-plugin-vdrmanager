package de.bjusystems.vdrmanager.tasks;

import android.app.Activity;
import de.bjusystems.vdrmanager.gui.SvdrpProgressDialog;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;

public abstract class AsyncProgressTask<Result> {

	class AsyncProgress extends SvdrpProgressDialog<Result> implements SvdrpAsyncListener<Result> {

		public AsyncProgress(final Activity activity, final SvdrpClient<Result> client) {
			super(activity, client);
		}

		public void svdrpEvent(final SvdrpEvent event, final Result result) {
			super.svdrpEvent(event, result);
			switch (event) {
			case FINISHED_ABNORMALY:
			case FINISHED_SUCCESS:
			case ABORTED:
				AsyncProgressTask.this.finished();
				break;
			}
		}
	}

	Activity activity;
	SvdrpClient<Result> client;

	public AsyncProgressTask(final Activity activity, final SvdrpClient<Result> client) {
		this.activity = activity;
		this.client = client;
	}

	public void start() {

		// delete timer
/*
		final SetTimerClient client = new SetTimerClient(timer, true) {
			@Override
			public int getProgressTextId() {
				return R.string.progress_timer_delete;
			}
		};
*/
		final SvdrpAsyncTask<Result, SvdrpClient<Result>> task = new SvdrpAsyncTask<Result, SvdrpClient<Result>>(client);
		final AsyncProgress progress = new AsyncProgress(activity, client);
		task.addListener(progress);
		task.run();
	}

	public abstract void finished();
}
