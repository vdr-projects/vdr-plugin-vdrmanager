package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Date;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EpgSearchTimeValue;
import de.bjusystems.vdrmanager.data.EpgSearchTimeValues;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class TimeEpgListActivity extends BaseTimerEditActivity<Epg> implements
		OnItemClickListener, OnItemSelectedListener {

	Spinner timeSpinner;

	ArrayAdapter<EpgSearchTimeValue> timeSpinnerAdapter;

	protected static Date nextForceCache = null;

	private final static ArrayList<Event> CACHE = new ArrayList<Event>();

	private static String cachedTime = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create adapter for time spinner
		timeSpinnerAdapter = new ArrayAdapter<EpgSearchTimeValue>(this,
				android.R.layout.simple_spinner_item);
		timeSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timeSpinner = (Spinner) findViewById(R.id.epg_list_time_spinner);
		timeSpinner.setAdapter(timeSpinnerAdapter);

		fillTimeSpinnerValues();

		// update gui
		adapter = new TimeEventAdapter(this);
		// searchLabel.setVisibility(View.GONE);
		timeSpinner.setOnItemSelectedListener(this);
		timeSpinner.setSelection(0);

		// Create adapter for EPG list
		listView = (ListView) findViewById(R.id.whatson_list);
		listView.setFastScrollEnabled(true);
		listView.setTextFilterEnabled(true);

		listView.setAdapter(adapter);
		registerForContextMenu(listView);

		// register EPG item click
		listView.setOnItemClickListener(this);

	}



	private void fillTimeSpinnerValues() {
		final EpgSearchTimeValues values = new EpgSearchTimeValues(this);
		timeSpinnerAdapter.clear();
		for (final EpgSearchTimeValue value : values.getValues()) {
			timeSpinnerAdapter.add(value);
		}
	}


	@Override
	protected void onPause() {
		super.onPause();

	}


	public void onItemSelected(final AdapterView<?> parent, final View view,
			final int position, final long id) {

		// get spinner value
		final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner
				.getSelectedItem();
		// update search
		startEpgQuery(selection.getValue(), false);

	}

	public void onNothingSelected(final AdapterView<?> arg0) {
		// startTimeEpgQuery(((EpgTimeSpinnerValue)timeSpinner.getAdapter().getItem(0)).getValue());
	}

	private void clearCache() {
		cachedTime = null;
	}

	private boolean useCache(String time) {

		if (cachedTime == null) {
			return false;
		}

		if (cachedTime.equals(time) == false) {
			return false;
		}

		if (nextForceCache == null) {
			return false;
		}
		Date now = new Date();
		if (nextForceCache.before(now)) {
			return false;
		}
		return true;
	}

	private void startEpgQuery(String time, boolean force) {

		if (useCache(time) && !force) {
			// TODO unschön, refactor to have one code for filling adapter.
			adapter.clear();
			if (CACHE.isEmpty() == false) {
				adapter.add(new EventListItem(new DateFormatter(CACHE.get(0)
						.getStart()).getDailyHeader()));
			}
			for (Event e : CACHE) {
				adapter.add(new EventListItem((Epg) e));
			}
			// adapter.sortItems();
			listView.setSelectionAfterHeaderView();
			return;
		}

		clearCache();

		epgClient = new EpgClient(time);
		epgClient.setResultInfoEnabled(true);

		// remove old listeners
		// epgClient.clearSvdrpListener();

		// create background task
		final SvdrpAsyncTask<Epg, SvdrpClient<Epg>> task = new SvdrpAsyncTask<Epg, SvdrpClient<Epg>>(
				epgClient);

		// create progress
		progress = new SvdrpProgressDialog<Epg>(this, epgClient);
		// attach listener
		task.addListener(progress);
		task.addListener(this);

		// start task
		task.run();
	}

	@Override
	protected boolean finishedSuccess() {
		// get spinner value
		final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner
				.getSelectedItem();
		adapter.clear();
		CACHE.clear();
		nextForceCache = FUTURE;
		cachedTime = selection.getValue();
		Date now = new Date();
		sortItemsByChannel(results);
		if (results.isEmpty() == false) {
			adapter.add(new EventListItem(new DateFormatter(results.get(0)
					.getStart()).getDailyHeader()));
		}
		for (Event e : results) {
			CACHE.add(e);
			adapter.add(new EventListItem((Epg) e));
			if (e.getStop().before(nextForceCache) && e.getStop().after(now)) {
				nextForceCache = e.getStop();
			}
		}
		listView.setSelectionAfterHeaderView();
		dismiss(progress);
		return CACHE.isEmpty() == false;

	}

	protected void prepareTimer(final EventListItem item) {
		final VdrManagerApp app = (VdrManagerApp) getApplication();

		// remember event for details view and timer things
		app.setCurrentEvent(item.getEpg());
		app.setCurrentEpgList(CACHE);
	}

	@Override
	protected int getMainLayout() {
		return R.layout.time_epg_list;
	}

	@Override
	protected void refresh() {
		// get spi
		final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner
				.getSelectedItem();
		// update search
		startEpgQuery(selection.getValue(), true);
	}

	@Override
	protected void retry() {
		refresh();
	}

	@Override
	protected int getWindowTitle() {
		return R.string.epg_by_time;
	}

	private void nextEvent() {
		int pos = timeSpinner.getSelectedItemPosition();
		if (pos + 1 >= timeSpinnerAdapter.getCount()) {
			Toast.makeText(this, R.string.navigae_at_the_end,
					Toast.LENGTH_SHORT).show();
			return;
		}
		timeSpinner.setSelection(pos + 1, true);
	}

	private void prevEvent() {
		int pos = timeSpinner.getSelectedItemPosition();
		if (pos <= 0) {
			Toast.makeText(this, R.string.navigae_at_the_start,
					Toast.LENGTH_SHORT).show();
			return;
		}
		timeSpinner.setSelection(pos - 1, true);
	}

	@Override
	public void onSwipe(int direction) {
		switch (direction) {
		case SimpleGestureFilter.SWIPE_RIGHT:
			prevEvent();
			break;
		case SimpleGestureFilter.SWIPE_LEFT:
			nextEvent();
			break;
		}
	}
}
