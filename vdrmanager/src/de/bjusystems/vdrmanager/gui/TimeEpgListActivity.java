package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EpgSearchTimeValue;
import de.bjusystems.vdrmanager.data.EpgSearchTimeValues;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Preferences;
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
		OnItemClickListener, OnItemSelectedListener, OnTimeSetListener {

	protected Spinner timeSpinner;

	protected View switcher;

	protected View clock;

	ArrayAdapter<EpgSearchTimeValue> timeSpinnerAdapter;

	protected static Date nextForceCache = null;

	private final static ArrayList<Event> CACHE = new ArrayList<Event>();

	private static String cachedTime = null;

	int selectedIndex = 0;

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

		switcher = findViewById(R.id.switch_epg_view);
		switcher.setOnClickListener(this);

		clock = findViewById(R.id.epg_list_times);
		clock.setOnClickListener(this);

		// update gui
		adapter = new TimeEventAdapter(this);
		// searchLabel.setVisibility(View.GONE);
		timeSpinner.setOnItemSelectedListener(this);

		fillTimeSpinnerValues();

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
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	public void onClick(View view) {
		if (view == switcher) {
			final Intent intent = new Intent();
			intent.setClass(this, EventEpgListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		} else if (view == clock) {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.setClass(this, EpgSearchTimesListActivity.class);
			startActivity(intent);
		} else {
			super.onClick(view);
		}
	}

	public void onTimeSet(final TimePicker view, final int hourOfDay,
			final int minute) {
		String tm = String.format("%02d:%02d", hourOfDay, minute);

		// timeSpinnerAdapter.add(time);
		final EpgSearchTimeValues values = new EpgSearchTimeValues(this);
		List<EpgSearchTimeValue> vs = values.getValues();
		final EpgSearchTimeValue time = new EpgSearchTimeValue(3, tm);
		vs.add(vs.size() - 1, time);
		timeSpinnerAdapter.clear();
		int select = -1;
		int counter = 0;
		for (final EpgSearchTimeValue value : vs) {
			timeSpinnerAdapter.add(value);
			if (select == -1 && value.getText().equals(tm)) {
				select = counter;
			}
			counter++;
		}
		timeSpinner.setSelection(select);
		setTitle(resolveWindowTitle());
		// update search
		startEpgQuery(time.getValue(), false);
	}

	private String resolveWindowTitle() {
		if (timeSpinner == null) {
			return getString(R.string.epg_by_time);
		}
		EpgSearchTimeValue v = (EpgSearchTimeValue) timeSpinner
				.getSelectedItem();
		if (v == null) {
			return getString(R.string.epg_by_time);
		}
		return getString(R.string.epg_by_time_args, v.getText());
	}

	public void onItemSelected(final AdapterView<?> parent, final View view,
			final int position, final long id) {

		// get spinner value
		final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner
				.getSelectedItem();

		if (selection.getValue().equals("adhoc")) {
			final Calendar cal = Calendar.getInstance(TimeZone.getDefault());
			// show time selection
			final TimePickerDialog dialog = new TimePickerDialog(this, this,
					cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
					Preferences.get().isUse24hFormat());
			dialog.show();
		} else {
			// update search
			setTitle(getString(R.string.epg_by_time_args, selection.getText()));
			startEpgQuery(selection.getValue(), false);
		}
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

		if (checkInternetConnection() == false) {
			return;
		}

		clearCache();

		epgClient = new EpgClient(time);

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
	protected boolean finishedSuccessImpl() {
		// get spinner value
		final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner
				.getSelectedItem();
		adapter.clear();
		CACHE.clear();
		nextForceCache = FUTURE;
		cachedTime = selection.getValue();
		Date now = new Date();
		sortItemsByChannel(results);
		if (results.isEmpty()) {
			return false;
		}

		adapter.add(new EventListItem(new DateFormatter(results.get(0)
				.getStart()).getDailyHeader()));

		for (Event e : results) {
			CACHE.add(e);
			adapter.add(new EventListItem((Epg) e));
			if (e.getStop().before(nextForceCache) && e.getStop().after(now)) {
				nextForceCache = e.getStop();
			}
		}
		listView.setSelectionAfterHeaderView();
		return CACHE.isEmpty() == false;

	}

	protected void prepareDetailsViewData(final EventListItem item) {
		final VdrManagerApp app = (VdrManagerApp) getApplication();

		// remember event for details view and timer things
		app.setCurrentEvent(item.getEvent());
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
	protected String getWindowTitle() {
		return resolveWindowTitle();
	}

	private void nextEvent() {
		int pos = timeSpinner.getSelectedItemPosition();
		if (pos + 1 >= timeSpinnerAdapter.getCount()) {
			say(R.string.navigae_at_the_end);
			return;
		}
		timeSpinner.setSelection(pos + 1, true);
	}

	private void prevEvent() {
		int pos = timeSpinner.getSelectedItemPosition();
		if (pos <= 0) {
			say(R.string.navigae_at_the_start);
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
