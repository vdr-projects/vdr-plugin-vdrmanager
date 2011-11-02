package de.bjusystems.vdrmanager.gui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

public class SvdrpProgressDialog<T> extends ProgressDialog implements
		SvdrpAsyncListener<T>, DialogInterface.OnCancelListener {
	ProgressDialog progress;
	Activity activity;
	SvdrpClient<? extends Object> client;

	public SvdrpProgressDialog(final Activity activity,
			final SvdrpClient<T> client) {
		super(activity);
		this.activity = activity;
		this.client = client;
		progress = new ProgressDialog(activity);
		progress.setOnCancelListener(this);
	}

	public void svdrpEvent(final SvdrpEvent event) {
		svdrpEvent(event, null);
	}

	public void svdrpEvent(final SvdrpEvent sevent, T e) {
		switch (sevent) {
		case ABORTED:
		case CONNECT_ERROR:
		case ERROR:
		case LOGIN_ERROR:
		case FINISHED_ABNORMALY:
		case FINISHED_SUCCESS:
		case CACHE_HIT:
			progress.dismiss();
			break;
		case CONNECTING:
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			setMessage(R.string.progress_connect);
			progress.show();
			break;
		case LOGGED_IN:
			setMessage(R.string.progress_login);
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
		}
	}

	public void svdrpException(final SvdrpException exception) {
		progress.dismiss();
	}

	private void setMessage(final int resId, Object... args) {
		if (args.length == 0) {
			progress.setMessage(activity.getText(resId));
		} else {
			progress.setMessage(activity.getString(resId, args));
		}
	}

	private void abort() {
		client.abort();
		dismiss();
	}

	public void dismiss() {
		progress.dismiss();
	}

	public void onCancel(DialogInterface dialog) {
		abort();
	}
}
