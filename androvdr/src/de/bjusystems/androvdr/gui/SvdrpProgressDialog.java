package de.bjusystems.androvdr.gui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;
import de.bjusystems.androvdr.utils.svdrp.SvdrpClient;
import de.bjusystems.androvdr.utils.svdrp.SvdrpEvent;
import de.bjusystems.androvdr.utils.svdrp.SvdrpException;
import de.bjusystems.androvdr.R;

public class SvdrpProgressDialog extends ProgressDialog {

	ProgressDialog progress;
	Activity activity;
	SvdrpClient<? extends Object> client;

	public SvdrpProgressDialog(final Activity activity, final SvdrpClient<? extends Object> client) {
		super(activity);
		this.activity = activity;
		this.client = client;
	}

	public void svdrpEvent(final SvdrpEvent event) {

		switch (event) {
		case CONNECTING:
			progress = new ProgressDialog(activity);
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			setMessage(R.string.progress_connect);
			progress.show();
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
		case FINISHED:
			progress.dismiss();
			break;
		}
	}

	public void svdrpException(final SvdrpException exception) {
		showToast(R.string.vdr_error_text);
	}

	private void setMessage(final int resId) {
		progress.setMessage(activity.getText(resId));
	}

	private void showToast(final int resId) {
		progress.dismiss();

		final CharSequence text = activity.getText(resId);
		final int duration = Toast.LENGTH_LONG;
		final Toast toast = Toast.makeText(activity, text, duration);
		toast.show();
	}
}

