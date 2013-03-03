package de.bjusystems.vdrmanager.utils.svdrp;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

public class SvdrpAsyncTask<Result, Client extends SvdrpClient<Result>> extends
		AsyncTask<Void, Object, Void> implements SvdrpListener,
		SvdrpExceptionListener, SvdrpResultListener<Result> {

	Client client;

	Throwable ex;

	SvdrpEvent event;

	List<SvdrpListener> eventListeners = new ArrayList<SvdrpListener>();

	List<SvdrpExceptionListener> exceptionListeners = new ArrayList<SvdrpExceptionListener>();

	List<SvdrpFinishedListener<Result>> finishedListeners = new ArrayList<SvdrpFinishedListener<Result>>();

	public SvdrpAsyncTask(final Client client) {
		this.client = client;
		this.client.addSvdrpListener(this);
		this.client.addSvdrpExceptionListener(this);
		this.client.addSvdrpResultListener(this);
	}

	protected  List<Result> results = new ArrayList<Result>();


	public List<Result> getResults() {
		return results;
	}


	/**
	 * Adds the listener to the list of listeners
	 *
	 * @param listener
	 *            listener
	 */
	public void addSvdrpListener(final SvdrpListener listener) {
		// client.addSvdrpListener(listener);
		eventListeners.add(listener);
	}

	/**
	 * Adds the listener to the list of listeners
	 *
	 * @param listener
	 *            listener
	 */
	public void addSvdrpResultListener(
			final SvdrpResultListener<Result> listener) {
		client.addSvdrpResultListener(listener);
	}

	public void addSvdrpFinishedListener(final SvdrpFinishedListener<Result> liste) {
		finishedListeners.add(liste);
	}

	/**
	 * Adds the listener to the list of listeners
	 *
	 * @param listener
	 *            listener
	 */
	public void addSvdrpExceptionListener(final SvdrpExceptionListener listener) {
		// client.addSvdrpExceptionListener(listener);
		exceptionListeners.add(listener);
	}

	public void run() {
		execute();
	}

	@Override
	protected Void doInBackground(final Void... params) {
		client.run();
		return null;
	}

	@Override
	protected void onProgressUpdate(final Object... values) {

		if (values.length == 1) {

			if (List.class.isAssignableFrom(values[0].getClass())) {
				for (final SvdrpFinishedListener<Result> listener : finishedListeners) {
					listener.finished((List<Result>) values[0]);
				}
				return;
			}

			for (final SvdrpListener listener : eventListeners) {
				listener.svdrpEvent((SvdrpEvent) values[0]);
			}

		} else if (values.length == 2) {
			for (final SvdrpExceptionListener listener : exceptionListeners) {
				listener.svdrpEvent((SvdrpEvent) values[0],
						(Throwable) values[1]);
			}
		}

		/*
		 * switch (event) { case CONNECTING: {
		 * setMessage(R.string.progress_connect); progress.show(); break; }
		 *
		 * case LOGGED_IN: { setMessage(R.string.progress_login); break; }
		 *
		 * case COMMAND_SENT: { setMessage(client.getProgressTextId()); break; }
		 *
		 * case DISCONNECTING: { setMessage(R.string.progress_disconnect);
		 * break; }
		 *
		 * case ERROR: case CONNECTION_TIMEOUT: case CONNECT_ERROR: case
		 * FINISHED_ABNORMALY: case CACHE_HIT: case FINISHED_SUCCESS: case
		 * LOGIN_ERROR: { progress.dismiss(); }
		 *
		 * }
		 */
	}

	// @Override
	// protected void onPostExecute(SvdrpException exception) {
	// for (SvdrpExceptionListener l : exceptionListeners) {
	// l.svdrpEvent(exception.getEvent(), ex);
	// }
	// }

	@Override
	public void svdrpEvent(SvdrpEvent event) {
		publishProgress(event);;
		if(event == SvdrpEvent.FINISHED_SUCCESS){
			publishProgress(results);
		}
	}

//	@Override
//	public void finished(ListResult> results) {
//		publishProgress(results);
//	}

	@Override
	public void svdrpEvent(SvdrpEvent event, Throwable t) {
		publishProgress(event, t);
	}


	@Override
	public void svdrpEvent(Result result) {
		results.add(result);
	}
}
