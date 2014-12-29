package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EpgCache;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.tasks.ChannelsTask;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.ChannelClient;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class EventEpgListActivity extends BaseTimerEditActivity<Epg> implements
		OnItemClickListener, OnItemSelectedListener {

	private static final String TAG = EventEpgListActivity.class
			.getSimpleName();

	// protected static Date nextForceCache = null;

	// private static Channel cachedChannel = null;

	Spinner channelSpinner;

	View switcher;

	ArrayAdapter<Channel> channelSpinnerAdapter;

	// protected static ArrayList<Epg> CACHE = new ArrayList<Epg>();

	private TextView audio;

	private View channelInfo;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//
		//
		// create adapter for channel spinner
		channelSpinnerAdapter = new ArrayAdapter<Channel>(this,
				android.R.layout.simple_spinner_item);
		channelSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		channelSpinner = (Spinner) findViewById(R.id.epg_list_channel_spinner);
		channelSpinner.setAdapter(channelSpinnerAdapter);

		switcher = findViewById(R.id.switch_epg_view);
		switcher.setOnClickListener(this);

		// add channel values
		// boolean useChannelNumber = Preferences.get().isShowChannelNumbers();

		// show needed items

		channelInfo = findViewById(R.id.channel_info);

		channelInfo.setOnClickListener(this);

		audio = (TextView) channelInfo.findViewById(R.id.channel_audio);

		adapter = new ChannelEventAdapter(this);

		// if (currentChannel != null) {

		// }
		// startChannelEpgQuery(channel);
		// findViewById(R.id.timer_item_channel).setVisibility(View.GONE);
		// break;

		// Create adapter for EPG list
		listView = (ListView) findViewById(R.id.whatson_list);
		listView.setAdapter(adapter);
		// listView.setFastScrollEnabled(true);
		listView.setTextFilterEnabled(true);
		registerForContextMenu(listView);

		// register EPG item click
		listView.setOnItemClickListener(this);

		if (checkInternetConnection() == false) {
			return;
		}

		startQuery();

	}

	@Override
	protected int getAvailableSortByEntries() {
		return R.array.epg_sort_by_time_alpha;
	}

	@Override
	protected String getViewID() {
		return EventEpgListActivity.class.getSimpleName();
	}

	private void startQuery() {
		new ChannelsTask(this, new ChannelClient(getCertificateProblemDialog())) {
			@Override
			public void finished(final SvdrpEvent event) {
				if (event == SvdrpEvent.CACHE_HIT
						|| event == SvdrpEvent.FINISHED_SUCCESS) {
					final ArrayList<Channel> channels = ChannelClient
							.getChannels();
					currentChannel = getApp().getCurrentChannel();
					boolean found = false;
					int count = 0;
					for (final Channel c : channels) {
						channelSpinnerAdapter.add(c);
						if (currentChannel != null && !found) {
							if (currentChannel.equals(c)) {
								found = true;
							} else {
								count++;
							}
						}
					}
					channelSpinner.setSelection(count);
					channelSpinner
							.setOnItemSelectedListener(EventEpgListActivity.this);
				} else {
					noConnection(event);
				}
			}
		}.start();

	}

	void sort() {
		if (sortBy == BaseEventListActivity.MENU_GROUP_ALPHABET) {
			Collections.sort(getCache(), new TitleComparator());
		} else if (sortBy == BaseEventListActivity.MENU_GROUP_DEFAULT) {
			Collections.sort(getCache(), new TimeComparator(false));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onItemSelected(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		// get spinner value
		final Channel channel = (Channel) channelSpinner.getSelectedItem();
		currentChannel = channel;
		setCurrent(channel);
		// setAsCurrent(channel);
		// update search
		if (channel.getAudio().isEmpty() == false) {
			audio.setText(Utils.formatAudio(this, channel.getAudio()));
		} else {
			audio.setText("");
		}

		startEpgQuery(false);
	}

	@Override
	public void onNothingSelected(final AdapterView<?> arg0) {
		// startTimeEpgQuery(((EpgTimeSpinnerValue)timeSpinner.getAdapter().getItem(0)).getValue());
	}

	@Override
	public void clearCache() {
		getCache().clear();
		EpgCache.CACHE.remove(currentChannel.getId());
		EpgCache.NEXT_REFRESH.remove(currentChannel.getId());
	}

	private boolean useCache() {

		if (currentChannel == null) {
			return false;
		}

		final ArrayList<Epg> cachedChannel = EpgCache.CACHE.get(currentChannel
				.getId());

		if (cachedChannel == null) {
			return false;
		}

		final Date nextForceCache = EpgCache.NEXT_REFRESH.get(currentChannel
				.getId());

		if (nextForceCache == null) {
			return false;
		}
		final Date now = new Date();
		if (nextForceCache.before(now)) {
			return false;
		}
		return true;
	}

	@Override
	public void onClick(final View view) {

		if (view == switcher) {
			final Intent intent = new Intent();
			intent.setClass(this, TimeEpgListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		} else if (view == channelInfo) {
			Utils.stream(this, currentChannel);
		} else {
			super.onClick(view);
		}
	}

	private void startEpgQuery(final boolean force) {
		if (useCache() && !force) {
			fillAdapter();
			return;
		}

		if (checkInternetConnection() == false) {
			return;
		}

		// clearCache();

		final EpgClient epgClient = new EpgClient(currentChannel,
				getCertificateProblemDialog());

		// remove old listeners
		// epgClient.clearSvdrpListener();

		// create background task
		final SvdrpAsyncTask<Epg, SvdrpClient<Epg>> task = new SvdrpAsyncTask<Epg, SvdrpClient<Epg>>(
				epgClient);

		// create progress

		addListener(task);

		// start task
		task.run();
	}

	private static final ArrayList<Epg> EMPTY = new ArrayList<Epg>(0);

	private ArrayList<Epg> getCache() {
		if (currentChannel == null) {
			return EMPTY;
		}
		final ArrayList<Epg> arrayList = EpgCache.CACHE.get(currentChannel
				.getId());
		if (arrayList == null) {
			return EMPTY;
		}
		return arrayList;
	}

	@Override
	protected void fillAdapter() {

		adapter.clear();

		final ArrayList<Epg> cache = getCache();

		if (cache.isEmpty()) {
			return;
		}

		sort();

		final Calendar cal = Calendar.getInstance();
		int day = -1;
		for (final Event e : cache) {
			cal.setTime(e.getStart());
			final int eday = cal.get(Calendar.DAY_OF_YEAR);
			if (eday != day) {
				day = eday;
				adapter.add(new EventListItem(new DateFormatter(cal)
						.getDailyHeader()));
			}
			adapter.add(new EventListItem(e));
		}

		adapter.notifyDataSetChanged();

	}

	/*
	 * (non-Javadoc) TODO this method also should be used in startEpgQuery on
	 * cache hit
	 * 
	 * @see de.bjusystems.vdrmanager.gui.BaseEpgListActivity#finishedSuccess()
	 */
	@Override
	protected boolean finishedSuccessImpl(final List<Epg> results) {
		// adapter.clear();
		// CACHE.clear();

		clearCache();

		if (results.isEmpty()) {
			return false;
		}

		final Date now = new Date();

		EpgCache.NEXT_REFRESH.put(currentChannel.getId(), FUTURE);

		Date nextForceCache = FUTURE;

		// Calendar cal = Calendar.getInstance();
		// int day = -1;
		// sortItemsByTime(results);
		final ArrayList<Epg> cache = new ArrayList<Epg>();
		for (final Epg e : results) {
			cache.add(e);
			// cal.setTime(e.getStart());
			// int eday = cal.get(Calendar.DAY_OF_YEAR);
			// if (eday != day) {
			// day = eday;
			// adapter.add(new EventListItem(new DateFormatter(cal)
			// .getDailyHeader()));
			// }
			// adapter.add(new EventListItem((Epg) e));
			if (e.getStop().before(nextForceCache) && e.getStop().after(now)) {
				nextForceCache = e.getStop();
			}
		}

		EpgCache.NEXT_REFRESH.put(currentChannel.getId(), nextForceCache);
		EpgCache.CACHE.put(currentChannel.getId(), cache);

		fillAdapter();
		listView.setSelectionAfterHeaderView();
		return results.isEmpty() == false;

		// ///////////////

		// // get spinner value
		// final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner
		// .getSelectedItem();
		// nextForceCache = FUTURE;
		// cachedTime = selection.getValue();
		// Date now = new Date();
		//
		// //adapter.add(new EventListItem(new DateFormatter(results.get(0)
		// // .getStart()).getDailyHeader()));
		//
		// for (Event e : results) {
		// CACHE.add(e);
		// if (e.getStop().before(nextForceCache) && e.getStop().after(now)) {
		// nextForceCache = e.getStop();
		// }
		// }
		//

	}

//	@Override
//	protected int prepareDetailsViewData(final EventListItem item, int position) {
//		final VdrManagerApp app = (VdrManagerApp) getApplication();
//		app.setCurrentEvent(item.getEvent());
//		ArrayList<Epg> cache = getCache();
//		app.setCurrentEpgList(cache);
//		for (int i = 0; i < position; ++i) {
//			if (cache.get(i) == item.getEvent()) {
//				return i;
//			}
//		}
//
//		return 0;
//	}

	@Override
	protected int getMainLayout() {
		return R.layout.event_epg_list;
	}

	@Override
	protected void refresh() {
		startEpgQuery(true);
	}

	@Override
	protected void retry() {
		refresh();
	}

	@Override
	protected String getWindowTitle() {
		return getString(R.string.epg_by_channel);
	}

	private void nextEvent() {
		final int pos = channelSpinner.getSelectedItemPosition();
		if (pos + 1 >= channelSpinnerAdapter.getCount()) {
			Toast.makeText(this, R.string.navigae_at_the_end,
					Toast.LENGTH_SHORT).show();
			return;
		}
		channelSpinner.setSelection(pos + 1, true);
	}

	private void prevEvent() {
		final int pos = channelSpinner.getSelectedItemPosition();
		if (pos <= 0) {
			say(R.string.navigae_at_the_start);
			return;
		}
		channelSpinner.setSelection(pos - 1, true);
	}

	@Override
	public void onSwipe(final int direction) {
		switch (direction) {
		case SimpleGestureFilter.SWIPE_RIGHT:
			prevEvent();
			break;
		case SimpleGestureFilter.SWIPE_LEFT:
			nextEvent();
			break;
		}
	}

	@Override
	protected int getListNavigationIndex() {
		return LIST_NAVIGATION_EPG_BY_CHANNEL;
	}

	@Override
	protected List<Epg> getCACHE() {
		return getCache();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.epg_event_list_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == R.id.epg_list_stream) {
			Utils.stream(this, currentChannel);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
