package de.bjusystems.androvdr.utils.svdrp;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

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
			publishProgress(null, null, e);
		}
		return null;
	}

	public void svdrpEvent(final SvdrpEvent event, final Result result) {
		publishProgress(event, result, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onProgressUpdate(final Object... values) {
		super.onProgressUpdate(values);

		if (values[2] == null) {
			for(final SvdrpAsyncListener<Result> listener : listeners) {
				listener.svdrpEvent((SvdrpEvent)values[0], (Result)values[1]);
			}
		} else {
			for(final SvdrpAsyncListener<Result> listener : listeners) {
				listener.svdrpException((SvdrpException)values[2]);
			}
		}
	}
}
