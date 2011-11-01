package de.bjusystems.vdrmanager.gui;

import java.util.Calendar;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Recording;
import de.bjusystems.vdrmanager.tasks.DeleteRecordingTask;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.RecordingClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class RecordingListActivity extends BaseEventListActivity<Recording>
		implements OnItemLongClickListener, SvdrpAsyncListener<Recording> {

	RecordingClient recordingClient;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create an adapter
		adapter = new RecordingAdapter(this);

		// attach adapter to ListView
		listView = (ListView) findViewById(R.id.recording_list);
		listView.setAdapter(adapter);

		// set click listener
		listView.setOnItemLongClickListener(this);
		// register EPG item click
		listView.setOnItemClickListener(this);
		// context menu wanted
		registerForContextMenu(listView);

		// start query
		startRecordingQuery();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void prepareTimer(EventListItem event) {
		getApp().setCurrentEvent(event);
		getApp().setCurrentEpgList(results);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {

		if (v.getId() == R.id.recording_list) {
			final MenuInflater inflater = getMenuInflater();
			final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

			// set menu title
			final EventListItem item = adapter.getItem(info.position);
			final EventFormatter formatter = new EventFormatter(item);
			menu.setHeaderTitle(formatter.getTitle());

			inflater.inflate(R.menu.recording_list_item_menu, menu);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final EventListItem event = adapter.getItem(info.position);
		Recording rec = event.getRecording();
		switch (item.getItemId()) {
		case R.id.recording_item_menu_delete: {
			DeleteRecordingTask drt = new DeleteRecordingTask(this, rec) {
				@Override
				public void finished(SvdrpEvent event) {
					dismiss(progress);
					refresh();
				}
			};
			drt.start();
			break;
		}
		case R.id.recording_item_menu_stream: {
			say("Sorry, not yet. It would be. File -> " + rec.getFileName());
			break;
		}

		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private void startRecordingQuery() {
		

		if (checkInternetConnection() == false) {
			switchNoConnection();
			return;
		}

		// get timer client
		recordingClient = new RecordingClient();

		// create backgound task
		final SvdrpAsyncTask<Recording, SvdrpClient<Recording>> task = new SvdrpAsyncTask<Recording, SvdrpClient<Recording>>(
				recordingClient);

		// create progress dialog
		
		progress = new SvdrpProgressDialog(this, recordingClient);

		
		// attach listener
		task.addListener(progress);
		task.addListener(this);

		// start task
		task.run();
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

	@Override
	protected int getWindowTitle() {
		return R.string.action_menu_recordings;
	}

	@Override
	protected boolean finishedSuccess() {
		adapter.clear();
		Calendar cal = Calendar.getInstance();
		int day = -1;
		for (final Recording rec : recordingClient.getResults()) {
			results.add(rec);
			cal.setTime(rec.getStart());
			int eday = cal.get(Calendar.DAY_OF_YEAR);
			if (eday != day) {
				day = eday;
				adapter.add(new EventListItem(new DateFormatter(cal)
						.getDailyHeader()));
			}
			adapter.add(new EventListItem(rec));
		}
		// adapter.sortItems();
		return results.isEmpty() == false;
	}

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		return false;
	}

}
