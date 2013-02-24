package de.bjusystems.vdrmanager.gui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpExceptionListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpListener;

public class SvdrpProgressDialog<T> extends ProgressDialog implements
		SvdrpExceptionListener, SvdrpListener, DialogInterface.OnCancelListener {

	ProgressDialog progress;

	SvdrpClient<? extends Object> client;

	public SvdrpProgressDialog(final Context context,
			final SvdrpClient<T> client) {
		super(context);

		this.client = client;
		progress = new ProgressDialog(context);
		progress.setOnCancelListener(this);
	}

	public void svdrpEvent(final SvdrpEvent sevent) {
		switch (sevent) {
		case ABORTED:
		case CONNECTION_TIMEOUT:
		case CONNECT_ERROR:
		case ERROR:
		case LOGIN_ERROR:
		case FINISHED_ABNORMALY:
		case FINISHED_SUCCESS:
		case CACHE_HIT:
			progress.dismiss();
			break;
		case DISCONNECTED:
			break;
		}
	}

	public void svdrpException(final SvdrpException exception) {

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

	@Override
	public void svdrpEvent(SvdrpEvent event, Throwable t) {
		progress.dismiss();
		Utils.say(getContext(), t.getLocalizedMessage());
	}
}
