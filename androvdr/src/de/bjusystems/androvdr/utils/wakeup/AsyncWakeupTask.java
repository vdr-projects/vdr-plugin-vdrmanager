package de.bjusystems.androvdr.utils.wakeup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import de.bjusystems.androvdr.data.Preferences;
import de.bjusystems.androvdr.data.WakeupState;
import de.bjusystems.androvdr.utils.http.HttpHelper;
import de.bjusystems.androvdr.utils.svdrp.WakeupClient;
import de.bjusystems.androvdr.R;

public class AsyncWakeupTask extends AsyncTask<Object, WakeupProgress, Void> {

	/** Context */
	private final Context context;
	/** Progress dialog */
	private ProgressDialog progressDialog;

	public AsyncWakeupTask(final Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(final Object... params) {

		// open progress dialog
		publishProgress(WakeupProgress.WAKEUP_STARTED);

		// Preferences
		final Preferences prefs = Preferences.getPreferences();

		boolean ok = false;
		try {

			// wakeup by http request
			final HttpHelper httpHelper = new HttpHelper();
			httpHelper.performGet(prefs.getWakeupUrl(), prefs.getWakeupUser(), prefs.getWakeupPassword(), null);
			// request sent
			ok = true;

		} catch (final Exception e) {
		}

		// close progress
		publishProgress(WakeupProgress.WAKEUP_FINISHED);

		if (ok) {
			publishProgress(WakeupProgress.WAKEUP_OK);
		} else {
			publishProgress(WakeupProgress.WAKEUP_ERROR);
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(final WakeupProgress... values) {
		super.onProgressUpdate(values);

		switch (values[0]) {
		case WAKEUP_STARTED:
			final CharSequence message = context.getText(R.string.progress_wakeup_sending);
			progressDialog = ProgressDialog.show(context, "", message);
			break;
		case WAKEUP_FINISHED:
			progressDialog.dismiss();
			break;
		case WAKEUP_OK:
			showToast(R.string.progress_wakeup_sent);
			break;
		case WAKEUP_ERROR:
			showToast(R.string.progress_wakeup_error);
			break;
		}
	}

	private void showToast(final int textId) {

		final CharSequence text = context.getText(textId);
		final int duration = Toast.LENGTH_SHORT;
		final Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

}
