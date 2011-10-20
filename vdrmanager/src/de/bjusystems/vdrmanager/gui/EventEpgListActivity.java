package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Calendar;
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
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.ChannelClient;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class EventEpgListActivity extends BaseTimerEditActivity<Epg> implements
		OnItemClickListener, OnItemSelectedListener {

	private final static ArrayList<Event> CACHE = new ArrayList<Event>();

	protected static Date nextForceCache = null;

	private static Channel cachedChannel = null;

	Spinner channelSpinner;

	ArrayAdapter<Channel> channelSpinnerAdapter;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get state
		final List<Channel> channels = ChannelClient.getChannels();

		// create adapter for channel spinner
		channelSpinnerAdapter = new ArrayAdapter<Channel>(this,
				android.R.layout.simple_spinner_item);
		channelSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		channelSpinner = (Spinner) findViewById(R.id.epg_list_channel_spinner);
		channelSpinner.setAdapter(channelSpinnerAdapter);

		// add channel values
		// boolean useChannelNumber = Preferences.get().isShowChannelNumbers();
		for (final Channel c : channels) {
			channelSpinnerAdapter.add(c);
		}
		// show needed items

		adapter = new ChannelEventAdapter(this);

		channelSpinner.setOnItemSelectedListener(this);
		channelSpinner.setSelection(currentChannel.getNumber() - 1);
		// startChannelEpgQuery(channel);
		// findViewById(R.id.timer_item_channel).setVisibility(View.GONE);
		// break;

		// Create adapter for EPG list
		listView = (ListView) findViewById(R.id.whatson_list);
		listView.setAdapter(adapter);
		listView.setFastScrollEnabled(true);
		listView.setTextFilterEnabled(true);
		registerForContextMenu(listView);

		// register EPG item click
		listView.setOnItemClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		// adapter.notifyDataSetChanged();
		// startEpgQuery();
	}

	public void onItemSelected(final AdapterView<?> parent, final View view,
			final int position, final long id) {

		// get spinner value
		final Channel channel = (Channel) channelSpinner.getSelectedItem();
		currentChannel = channel;
		// setAsCurrent(channel);
		// update search
		startEpgQuery();

	}

	public void onNothingSelected(final AdapterView<?> arg0) {
		// startTimeEpgQuery(((EpgTimeSpinnerValue)timeSpinner.getAdapter().getItem(0)).getValue());
	}

	private void clearCache() {
		cachedChannel = null;
	}

	private boolean useCache(Channel channel) {
		if (cachedChannel == null) {
			return false;
		}
		if (channel.getNumber() != cachedChannel.getNumber()) {
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

	//

	private void startEpgQuery(boolean force) {
		startEpgQuery(currentChannel, force);
	}

	private void startEpgQuery() {
		startEpgQuery(false);
	}

	private void startEpgQuery(Channel channel, boolean force) {

		if (useCache(channel) && !force) {
			Calendar cal = Calendar.getInstance();
			int day = -1;
			for (Event i : CACHE) {
				Epg e  = (Epg)i;
				cal.setTime(e.getStart());
				int eday = cal.get(Calendar.DAY_OF_YEAR);
				if (eday != day) {
					day = eday;
					adapter.add(new EventListItem(new DateFormatter(cal)
							.getDailyHeader()));
				}
				adapter.add(new EventListItem(e));
			}
			return;
		}

		clearCache();

		epgClient = new EpgClient(channel);

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

	/*
	 * (non-Javadoc) TODO this method also should be used in startEpgQuery on
	 * cache hit
	 * 
	 * @see de.bjusystems.vdrmanager.gui.BaseEpgListActivity#finishedSuccess()
	 */
	@Override
	protected boolean finishedSuccess() {
		adapter.clear();
		CACHE.clear();
		Date now = new Date();
		nextForceCache = FUTURE;
		Calendar cal = Calendar.getInstance();
		int day = -1;
		for (Epg e : epgClient.getResults()) {
			CACHE.add(e);
			cal.setTime(e.getStart());
			int eday = cal.get(Calendar.DAY_OF_YEAR);
			if (eday != day) {
				day = eday;
				adapter.add(new EventListItem(new DateFormatter(cal)
						.getDailyHeader()));
			}
			adapter.add(new EventListItem(e));
			if (e.getStop().before(nextForceCache) && e.getStop().after(now)) {
				nextForceCache = e.getStop();
			}
		}
		cachedChannel = currentChannel;
		listView.setSelectionAfterHeaderView();
		dismiss(progress);
		return CACHE.isEmpty() == false;
	}

	protected void prepareTimer(final EventListItem item) {
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		app.setCurrentEvent(item.getEpg());
		app.setCurrentEpgList(CACHE);
	}

	@Override
	protected int getMainLayout() {
		return R.layout.event_epg_list;
	}

	@Override
	protected void refresh() {
		startEpgQuery(currentChannel, true);
	}

	@Override
	protected void retry() {
		startEpgQuery(currentChannel, true);
	}

	@Override
	protected int getWindowTitle() {
		return R.string.epg_by_channel;
	}

	private void nextEvent() {
		int pos = channelSpinner.getSelectedItemPosition();
		if (pos + 1 >= channelSpinnerAdapter.getCount()) {
			Toast.makeText(this, R.string.navigae_at_the_end,
					Toast.LENGTH_SHORT).show();
			return;
		}
		channelSpinner.setSelection(pos + 1, true);
	}

	private void prevEvent() {
		int pos = channelSpinner.getSelectedItemPosition();
		if (pos <= 0) {
			Toast.makeText(this, R.string.navigae_at_the_start,
					Toast.LENGTH_SHORT).show();
			return;
		}
		channelSpinner.setSelection(pos - 1, true);
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
