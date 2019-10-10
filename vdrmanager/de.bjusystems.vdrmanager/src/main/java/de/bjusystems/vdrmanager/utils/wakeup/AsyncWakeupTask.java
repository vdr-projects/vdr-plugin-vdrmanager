package de.bjusystems.vdrmanager.utils.wakeup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.utils.http.HttpHelper;

public class AsyncWakeupTask extends AsyncTask<Object, WakeupProgress, Void> {

	private static final String TAG = "AsyncWakeupTask";
	/** Context */
	private final Context context;
	/** Progress dialog */
	private ProgressDialog progressDialog;

	public AsyncWakeupTask(final Context context) {
		this.context = context;
	}

	Wakeuper getWakeuper() {
		// Preferences
		final Preferences prefs = Preferences.get();

		if (Preferences.get().getWakeupMethod().equals("url")) {
			return new Wakeuper() {
				public void wakeup(Context context) throws  Exception{
					// wakeup by http request
					final HttpHelper httpHelper = new HttpHelper();
						int result = httpHelper.performGet(prefs.getWakeupUrl(),
								prefs.getWakeupUser(), prefs.getWakeupPassword(),
								null);
						if(result == 200){
							throw new Exception("Http Status Code "+result);
						}
				}
			};
		} else {
			return new Wakeuper() {
				public void wakeup(Context context) throws Exception {
					WakeOnLanClient.wake(prefs.getVdrMac());
				}
			};
		}
	}

	@Override
	protected Void doInBackground(final Object... params) {

		// open progress dialog
		publishProgress(new WakeupProgress(WakeupProgressType.WAKEUP_STARTED));

		boolean ok = false;
		String msg = null;
		try {
			getWakeuper().wakeup(context);
			ok = true;
		} catch (final Exception e) {
			Log.w(TAG, e);
			msg = e.getMessage();
		}

		// close progress
		publishProgress(new WakeupProgress(WakeupProgressType.WAKEUP_FINISHED));
		if (ok) {
			publishProgress(new WakeupProgress(WakeupProgressType.WAKEUP_OK));
		} else {
			publishProgress(new WakeupProgress(WakeupProgressType.WAKEUP_ERROR,
					msg));
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(final WakeupProgress... values) {
		super.onProgressUpdate(values);

		WakeupProgress t = values[0];

		switch (t.getState()) {
		case WAKEUP_STARTED:
			final CharSequence message = context
					.getText(R.string.progress_wakeup_sending);
			progressDialog = ProgressDialog.show(context, "", message);
			break;
		case WAKEUP_FINISHED:
			progressDialog.dismiss();
			break;
		case WAKEUP_OK:
			showToast(R.string.progress_wakeup_sent);
			break;
		case WAKEUP_ERROR:
			showToast(R.string.progress_wakeup_error, t.getInfo());
			break;
		}
	}

	private void showToast(final int textId, Object... args) {

		final CharSequence text;
		if (args.length > 1) {
			text = context.getString(textId);
		} else {
			text = context.getString(textId, args);
		}
		final int duration = Toast.LENGTH_SHORT;
		final Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}

}
