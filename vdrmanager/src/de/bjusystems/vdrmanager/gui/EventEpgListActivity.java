package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.content.Intent;
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
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpListener;

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

	View switcher;

	ArrayAdapter<Channel> channelSpinnerAdapter;

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
	}

	// private void ensureChannelList() {

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		backupViewSelection();
		startEpgQuery();
		channelSpinner.setOnItemSelectedListener(this);
	}

	public void onItemSelected(final AdapterView<?> parent, final View view,
			final int position, final long id) {

		// get spinner value
		final Channel channel = (Channel) channelSpinner.getSelectedItem();
		currentChannel = channel;
		getApp().setCurrentChannel(currentChannel);
		// setAsCurrent(channel);
		// update search
		startEpgQuery();

	}

	// class LoadChannelsTask extends AsyncProgressTask<Channel> {
	//
	// boolean f;
	// Channel c;
	//
	// public LoadChannelsTask(Channel channel, boolean force,
	// Activity activity, ChannelClient client) {
	// super(activity, client);
	// this.f = force;
	// this.c = channel;
	// }
	//
	// @Override
	// public void finished(SvdrpEvent event) {
	// if (noConnection(event)) {
	// return;
	// }
	// ArrayList<Channel> channels = ChannelClient.getChannels();
	// for (final Channel c : channels) {
	// channelSpinnerAdapter.add(c);
	// }
	// }
	// }

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

	@Override
	public void onClick(View view) {
		if (view == switcher) {
			final Intent intent = new Intent();
			intent.setClass(this, TimeEpgListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		} else {
			super.onClick(view);
		}
	}

	private void startEpgQuery(Channel channel, boolean force) {
		try {
			ChannelClient client = new ChannelClient();
			client.addSvdrpListener(new SvdrpListener<Channel>() {
				public void svdrpEvent(SvdrpEvent event, Channel result) {
					if (event == SvdrpEvent.CACHE_HIT
							|| event == SvdrpEvent.FINISHED_SUCCESS) {
						ArrayList<Channel> channels = ChannelClient
								.getChannels();
						for (final Channel c : channels) {
							channelSpinnerAdapter.add(c);
						}
					}
					noConnection(event);
				}
			});
			client.run();
		} catch (SvdrpException ex) {
			svdrpException(ex);
		}
		startEpgQueryImpl(channel, force);
	}

	private void startEpgQueryImpl(Channel channel, boolean force) {

		if (useCache(channel) && !force) {
			Calendar cal = Calendar.getInstance();
			int day = -1;
			for (Event i : CACHE) {
				Epg e = (Epg) i;
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

		if (checkInternetConnection() == false) {
			switchNoConnection();
			return;
		}

		clearCache();

		epgClient = new EpgClient(channel);
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
		sortItemsByTime(results);
		for (Event e : results) {
			CACHE.add(e);
			cal.setTime(e.getStart());
			int eday = cal.get(Calendar.DAY_OF_YEAR);
			if (eday != day) {
				day = eday;
				adapter.add(new EventListItem(new DateFormatter(cal)
						.getDailyHeader()));
			}
			adapter.add(new EventListItem((Epg) e));
			if (e.getStop().before(nextForceCache) && e.getStop().after(now)) {
				nextForceCache = e.getStop();
			}
		}
		cachedChannel = currentChannel;
		// listView.setSelectionAfterHeaderView();
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
		refresh();
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
			say(R.string.navigae_at_the_start);
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

	protected void sortItemsByChannel(List<Event> result) {
		final Comparator<Event> comparator = new Comparator<Event>() {

			public int compare(final Event item1, final Event item2) {
				return Integer.valueOf(item1.getChannelNumber()).compareTo(
						Integer.valueOf(item2.getChannelNumber()));
			}
		};
		Collections.sort(result, comparator);
	}

	@Override
	protected void timerModified() {
		cachedChannel = null;
	}

}
