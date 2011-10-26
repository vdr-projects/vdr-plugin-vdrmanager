package de.bjusystems.vdrmanager.utils.svdrp;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

public class SvdrpAsyncTask<Result, Client extends SvdrpClient<Result>>
										extends AsyncTask<Void, Object, Void>
										implements SvdrpListener<Result> {

	Client client;
	List<SvdrpAsyncListener<Result>> listeners = new ArrayList<SvdrpAsyncListener<Result>>();

	public SvdrpAsyncTask(final Client client) {
		this.client = client;
		client.addSvdrpListener(this);
	}

	public void addListener(final SvdrpAsyncListener<Result> listener) {
		listeners.add(listener);
	}

	public void removeListener(final SvdrpAsyncListener<Result> listener) {
		listeners.remove(listener);
	}

	public void run() {
		execute();
	}

	@Override
	protected Void doInBackground(final Void... params) {
		try {
			client.run();
		} catch (final SvdrpException e) {
			publishProgress(e);
		}
		return null;
	}

	public void svdrpEvent(final SvdrpEvent event, final Result result) {
		publishProgress(event, result);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onProgressUpdate(final Object... values) {
		super.onProgressUpdate(values);

		if (values.length == 2) {
			for(final SvdrpAsyncListener<Result> listener : listeners) {
				listener.svdrpEvent((SvdrpEvent)values[0], (Result)values[1]);
			}
		} else if(values.length == 1) {
			for(final SvdrpAsyncListener<Result> listener : listeners) {
				listener.svdrpException((SvdrpException)values[0]);
			}
		} else {
			Log.w(toString(), "Unknonw count of argument in onProgressUpdate");
		}
	}
}
