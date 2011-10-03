package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EpgSearchTimeValue;
import de.bjusystems.vdrmanager.data.EpgSearchTimeValues;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class TimeEpgListActivity extends BaseEpgListActivity implements
		OnItemClickListener, OnItemSelectedListener, SvdrpAsyncListener<Epg> {

	Spinner timeSpinner;

	ArrayAdapter<EpgSearchTimeValue> timeSpinnerAdapter;

	protected static Date nextForceCache = null;

	private final static ArrayList<Epg> CACHE = new ArrayList<Epg>();

	private static String cachedTime = null;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		setContentView(getMainLayout());

		ListView lv = (ListView) findViewById(R.id.whatson_list);
		lv.setFastScrollEnabled(true);

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
	
	private int currentPostion = 0;

	@Override
	protected void onPause() {
		super.onPause();
		
	}


	
	@Override
	protected void onResume() {
		super.onResume();
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
			for (Epg e : CACHE) {
				adapter.add(new EventListItem(e));
			}
			// adapter.sortItems();
			listView.setSelectionAfterHeaderView();
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
		progress = new SvdrpProgressDialog(this, epgClient);
		// attach listener
		task.addListener(this);

		// start task
		task.run();
	}

	@Override
	protected void finishedSuccess() {
		// get spinner value
		final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner
				.getSelectedItem();
		setTitle(getString(R.string.epg_of_a_channel, selection));
		adapter.clear();
		CACHE.clear();
		nextForceCache = FUTURE;
		cachedTime = selection.getValue();
		Date now = new Date();
		List<Epg> results = epgClient.getResults();
		sortItemsByChannel(results);
		if (results.isEmpty() == false) {
			adapter.add(new EventListItem(new DateFormatter(results.get(0)
					.getStart()).getDailyHeader()));
		}
		for (Epg e : results) {
			CACHE.add(e);
			adapter.add(new EventListItem(e));
			if (e.getStop().before(nextForceCache) && e.getStop().after(now)) {
				nextForceCache = e.getStop();
			}
		}
		listView.setSelectionAfterHeaderView();
		if (progress != null) {
			progress.dismiss();
			progress = null;
		}
	}

	public void svdrpException(final SvdrpException exception) {
		if (progress != null) {
			progress.svdrpException(exception);
		}
	}

	private void sortItemsByChannel(List<Epg> result) {
		final Comparator<Epg> comparator = new Comparator<Epg>() {

			public int compare(final Epg item1, final Epg item2) {
				return Integer.valueOf(item1.getChannelNumber()).compareTo(
						Integer.valueOf(item2.getChannelNumber()));
			}
		};
		Collections.sort(result, comparator);
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

}
