package de.bjusystems.vdrmanager.gui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

public class SvdrpProgressDialog extends ProgressDialog {

	private static final String TAG = SvdrpProgressDialog.class.getName();
	ProgressDialog progress;
	Activity activity;
	SvdrpClient<? extends Object> client;

	public SvdrpProgressDialog(final Activity activity,
			final SvdrpClient<? extends Object> client) {
		super(activity);
		this.activity = activity;
		this.client = client;
		progress = new ProgressDialog(activity);
	}

	public void svdrpEvent(final SvdrpEvent event) {
		svdrpEvent(event, null);
	}

	public void svdrpEvent(final SvdrpEvent event, Throwable error) {
		switch (event) {
		case CONNECTING:
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			setMessage(R.string.progress_connect);
			progress.show();
			break;
		case CONNECT_ERROR:
			progress.dismiss();
			showToast(R.string.progress_connect_error);
			break;
		case LOGGED_IN:
			setMessage(R.string.progress_login);
			break;
		case LOGIN_ERROR:
			progress.dismiss();
			showToast(R.string.progress_login_error);
			break;
		case COMMAND_SENT:
			setMessage(client.getProgressTextId());
			break;
		case RESULT_RECEIVED:
			break;
		case DISCONNECTING:
			setMessage(R.string.progress_disconnect);
			break;
		case DISCONNECTED:
			break;
		case FINISHED_ABNORMALY:
			progress.dismiss();
			if (error == null) {
				showToast(R.string.progress_connect_finished_abnormal);
			} else {
				showToast(R.string.progress_connect_finished_abnormal_arg,
						error.getMessage());
			}
		case FINISHED_SUCCESS:
			progress.dismiss();
			break;
		case CACHE_HIT:
			progress.dismiss();
			setMessage(R.string.progress_cache_hit);
			break;
		}
	}

	public void svdrpException(final SvdrpException exception) {
		Log.w(TAG, String.valueOf(activity), exception);
		showToast(R.string.vdr_error_text, exception.getMessage());
	}

	private void setMessage(final int resId, Object... args) {
		if (args.length == 0) {
			progress.setMessage(activity.getText(resId));
		} else {
			progress.setMessage(activity.getString(resId, args));
		}
	}

	private void showToast(final int resId, Object... args) {
		progress.dismiss();
		final CharSequence text = args.length == 0 ? activity.getString(resId)
				: activity.getString(resId, args);
		final int duration = Toast.LENGTH_SHORT;
		final Toast toast = Toast.makeText(activity, text, duration);
		toast.show();
	}
	
	public void dismiss(){
		progress.dismiss();
	}
}
