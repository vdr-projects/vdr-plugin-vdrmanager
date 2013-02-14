package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;

import android.os.Bundle;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Preferences;
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

	public static final int MENU_DATE = 0;
	public static final int MENU_NAME = 1;
	public static final int MENU_CHANNEL = 2;

	public static final int ASC = 0;

	public static final int DESC = 1;

	private int groupBy = MENU_DATE;

	private int ASC_DESC = ASC;

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
		listView.setFastScrollEnabled(true);
		listView.setTextFilterEnabled(true);
		// start query
		startRecordingQuery();
	}


	private String[] getAvailableGroupByEntries() {
		ArrayList<String> entries = new ArrayList<String>(2);
		entries.add(getString(R.string.groupby_date));
		entries.add(getString(R.string.groupby_name));
		entries.add(getString(R.string.groupby_channel));
		return entries.toArray(Utils.EMPTY);
	}

	AlertDialog groupByDialog = null;

	@Override
	public boolean onOptionsItemSelected(final com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_groupby:
			// case MENU_PROVIDER:
			// case MENU_NAME:
			if (groupByDialog == null) {
				groupByDialog = new AlertDialog.Builder(this)
						.setTitle(R.string.menu_groupby)
						.setIcon(android.R.drawable.ic_menu_sort_alphabetically)
						.setSingleChoiceItems(getAvailableGroupByEntries(),
								groupBy, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										if(groupBy == which){
											ASC_DESC =  ASC_DESC == ASC ? DESC : ASC;
										} else {
											groupBy = which;
											ASC_DESC = ASC;
										}
										//fillAdapter();
										groupByDialog.dismiss();
										say("Comming soon...");
									}
								}).create();
			}

			groupByDialog.show();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.bjusystems.vdrmanager.gui.BaseActivity#onCreateOptionsMenu(android
	 * .view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {
		final com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.recording_list_menu, menu);
		return 	super.onCreateOptionsMenu(menu);
	}


	@Override
	protected SvdrpClient<Recording> getClient() {
		return this.recordingClient;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void prepareDetailsViewData(EventListItem event) {
		getApp().setCurrentEvent(event.getEvent());
		getApp().setCurrentEpgList(results);
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final EventListItem item = adapter.getItem(info.position);
		if(item.isHeader()){
			return;
		}

		if (v.getId() == R.id.recording_list) {
			final MenuInflater inflater = getMenuInflater();
			// set menu title
			final EventFormatter formatter = new EventFormatter(item);
			menu.setHeaderTitle(formatter.getTitle());

			inflater.inflate(R.menu.recording_list_item_menu, menu);
			if(Preferences.get().isEnableRecStream() == false){
				menu.removeItem(R.id.recording_item_menu_stream);
			}

		}

		super.onCreateContextMenu(menu, v, menuInfo);
		//
//		http://projects.vdr-developer.org/issues/863
		if(Utils.isLive(item)){
			menu.removeItem(R.id.epg_item_menu_live_tv);
		}
	}


	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final EventListItem event = adapter.getItem(info.position);
		Recording rec = (Recording) event.getEvent();
		switch (item.getItemId()) {
		case R.id.recording_item_menu_delete: {
			DeleteRecordingTask drt = new DeleteRecordingTask(this, rec) {
				@Override
				public void finished(SvdrpEvent event) {
					backupViewSelection();
					refresh();
				}
			};
			drt.start();
			break;
		}
		case R.id.recording_item_menu_stream: {
			Utils.streamRecording(this, rec);
			//say("Sorry, not yet. It would be. File -> " + rec.getFileName());
			break;
		}

		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private void startRecordingQuery() {


		if (checkInternetConnection() == false) {
			return;
		}

		// get timer client
		recordingClient = new RecordingClient();

		// create backgound task
		final SvdrpAsyncTask<Recording, SvdrpClient<Recording>> task = new SvdrpAsyncTask<Recording, SvdrpClient<Recording>>(
				recordingClient);

		// create progress dialog

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
	protected String getWindowTitle() {
		return getString(R.string.action_menu_recordings);
	}

	@Override
	protected boolean finishedSuccessImpl() {
		adapter.clear();
		Calendar cal = Calendar.getInstance();
		int day = -1;
		sortItemsByTime(results, true);
		for (final Event rec : results) {
			cal.setTime(rec.getStart());
			int eday = cal.get(Calendar.DAY_OF_YEAR);
			if (eday != day) {
				day = eday;
				adapter.add(new EventListItem(new DateFormatter(cal)
						.getDailyHeader()));
			}
			adapter.add(new EventListItem((Recording)rec));
		}
		// adapter.sortItems();
		return results.isEmpty() == false;
	}

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {

		return false;
	}

}
