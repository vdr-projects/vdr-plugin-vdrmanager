package de.bjusystems.vdrmanager.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Recording;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.tasks.DeleteTimerTask;
import de.bjusystems.vdrmanager.tasks.ToggleTimerTask;
import de.bjusystems.vdrmanager.utils.svdrp.RecordingClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;
import de.bjusystems.vdrmanager.utils.svdrp.TimerClient;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class RecordingListActivity extends BaseActivity implements
		OnItemClickListener, SvdrpAsyncListener<Recording> {

	RecordingClient recordingClient;
	EventAdapter adapter;
	SvdrpProgressDialog progress;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set title
		setTitle(R.string.action_menu_timers);

		// Attach view
		setContentView(getMainLayout());

		// create an adapter
		adapter = new TimeEventAdapter(this);

		// attach adapter to ListView
		final ListView listView = (ListView) findViewById(R.id.recording_list);
		listView.setAdapter(adapter);

		// set click listener
		listView.setOnItemClickListener(this);

		// context menu wanted
		registerForContextMenu(listView);

		// start query
		startRecordingQuery();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	protected void updateWindowTitle(int topic, int subtopic) {
		String title;
		title = getString(topic);
		if (subtopic != -1) {
			title += " > " + getString(subtopic);
		}
		setTitle(title);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (recordingClient != null) {
			recordingClient.abort();
		}
		if (progress != null) {
			progress.dismiss();
			progress = null;
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.recording_list) {
			final MenuInflater inflater = getMenuInflater();
			final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

			// set menu title
			final EventListItem item = adapter.getItem(info.position);
			final EventFormatter formatter = new EventFormatter(item);
			menu.setHeaderTitle(formatter.getTitle());

			inflater.inflate(R.menu.recording_list_item_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final EventListItem event = adapter.getItem(info.position);

		switch (item.getItemId()) {
		case R.id.recording_item_menu_delete: {

			break;
		}
		case R.id.recording_item_menu_stream: {
			break;
		}
		}
		return true;
	}

	public void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {

		// save selected item
		final Timer timer = adapter.getItem(position).getTimer();
		if (timer == null) {
			// header click
			return;
		}

		final VdrManagerApp app = (VdrManagerApp) getApplication();
		app.setCurrentTimer(timer);

		// after timer editing return to the timer list
		app.setNextActivity(RecordingListActivity.class);
		app.clearActivitiesToFinish();

		// show timer details
		final Intent intent = new Intent();
		intent.setClass(this, TimerDetailsActivity.class);
		startActivity(intent);
	}

	private void startRecordingQuery() {

		// get timer client
		recordingClient = new RecordingClient();

		// create backgound task
		final SvdrpAsyncTask<Recording, SvdrpClient<Recording>> task = new SvdrpAsyncTask<Recording, SvdrpClient<Recording>>(
				recordingClient);

		// create progress dialog
		progress = new SvdrpProgressDialog(this, recordingClient);

		// attach listener
		task.addListener(this);

		// start task
		task.run();
	}

	public void svdrpEvent(final SvdrpEvent event, final Recording result) {

		if (progress != null) {
			progress.svdrpEvent(event);
		}

		switch (event) {
		case CONNECTING:
			break;
		case FINISHED_ABNORMALY:
		case CONNECT_ERROR:
			switchNoConnection();// TODO pass arg, what is the problem
		case LOGIN_ERROR:
			progress.dismiss();
			switchNoConnection();
			break;
		case FINISHED_SUCCESS:
			adapter.clear();
			for (final Recording rec : recordingClient.getResults()) {
				adapter.add(new EventListItem(rec));
			}
			// adapter.sortItems();
			if (progress != null) {
				progress.dismiss();
				progress = null;
			}
			if (recordingClient.getResults().isEmpty()) {
				Toast.makeText(RecordingListActivity.this,
						R.string.epg_no_items, Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	public void svdrpException(final SvdrpException exception) {
		if (progress != null) {
			progress.svdrpException(exception);
		}
	}

	private void deleteTimer(final EventListItem item) {

		final DeleteTimerTask task = new DeleteTimerTask(this, item.getTimer()) {
			@Override
			public void finished() {
				// refresh epg list after return
				final VdrManagerApp app = (VdrManagerApp) getApplication();
				app.setReload(true);
			}
		};
		task.start();
	}

	protected void retry() {
		startRecordingQuery();
	}

	protected void refresh() {
		startRecordingQuery();
	}

	@Override
	protected int getMainLayout() {
		return R.layout.recording_list;
	}


}
