package de.bjusystems.androvdr.gui;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import de.bjusystems.androvdr.app.AndroVdrApp;
import de.bjusystems.androvdr.data.Channel;
import de.bjusystems.androvdr.data.Epg;
import de.bjusystems.androvdr.data.EpgSearchParams;
import de.bjusystems.androvdr.data.EpgSearchTimeValue;
import de.bjusystems.androvdr.data.EpgSearchTimeValues;
import de.bjusystems.androvdr.data.EventFormatter;
import de.bjusystems.androvdr.data.EventListItem;
import de.bjusystems.androvdr.data.Preferences;
import de.bjusystems.androvdr.tasks.DeleteTimerTask;
import de.bjusystems.androvdr.tasks.ToggleTimerTask;
import de.bjusystems.androvdr.utils.svdrp.EpgClient;
import de.bjusystems.androvdr.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.androvdr.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.androvdr.utils.svdrp.SvdrpClient;
import de.bjusystems.androvdr.utils.svdrp.SvdrpEvent;
import de.bjusystems.androvdr.utils.svdrp.SvdrpException;
import de.bjusystems.androvdr.R;

/**
 * This class is used for showing what's
 * current running on all channels
 * @author bju
 */
public class EpgListActivity extends Activity
				implements OnItemClickListener, OnItemSelectedListener, SvdrpAsyncListener<Epg> {

	EpgClient epgClient;
	EventAdapter adapter;
	Preferences prefs;
	Spinner timeSpinner;
	Spinner channelSpinner;
	ArrayAdapter<EpgSearchTimeValue> timeSpinnerAdapter;
	ListView listView;
	SvdrpProgressDialog progress;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// application object
		final AndroVdrApp app = (AndroVdrApp)getApplication();
		// get state
		final Channel channel = ((AndroVdrApp) getApplication()).getCurrentChannel();
		final List<Channel> channels = ((AndroVdrApp) getApplication()).getChannels();

		// Attach view
		setContentView(R.layout.epg_list);

		// create adapter for time spinner
		timeSpinnerAdapter = new ArrayAdapter<EpgSearchTimeValue>(this, android.R.layout.simple_spinner_item);
		timeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timeSpinner = (Spinner) findViewById(R.id.epg_list_time_spinner);
		timeSpinner.setAdapter(timeSpinnerAdapter);

		// create adapter for channel spinner
		final ArrayAdapter<Channel> channelSpinnerAdapter = new ArrayAdapter<Channel>(this, android.R.layout.simple_spinner_item);
		channelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		channelSpinner = (Spinner) findViewById(R.id.epg_list_channel_spinner);
		channelSpinner.setAdapter(channelSpinnerAdapter);

		// get search label
		final TextView searchLabel = (TextView) findViewById(R.id.epg_list_search_label);

		// fill spinners
		if (channels != null) {
			// add dummy timer
			timeSpinnerAdapter.add(new EpgSearchTimeValue());
			// add channel values
			for(final Channel c : channels) {
				channelSpinnerAdapter.add(c);
			}
		} else {
			// add dummy channel
			channelSpinnerAdapter.add(new Channel());
			// add time values
			fillTimeSpinnerValues();
		}

    // show needed items
    final LinearLayout timeLayout = (LinearLayout) findViewById(R.id.whatson_time);

    // update gui
    switch (app.getEpgListState()) {
    case EPG_TIME:
  		adapter = new EventAdapter(this, false);
			timeLayout.setVisibility(View.VISIBLE);
			channelSpinner.setVisibility(View.GONE);
			searchLabel.setVisibility(View.GONE);
			timeSpinner.setOnItemSelectedListener(this);
			timeSpinner.setSelection(0);
			break;
    case EPG_CHANNEL:
  		adapter = new EventAdapter(this, true);
			timeLayout.setVisibility(View.GONE);
			channelSpinner.setVisibility(View.VISIBLE);
			searchLabel.setVisibility(View.GONE);
    	channelSpinner.setOnItemSelectedListener(this);
    	channelSpinner.setSelection(channel.getNumber() - 1);
			break;
    case EPG_SEARCH:
  		adapter = new EventAdapter(this, true);
			timeLayout.setVisibility(View.GONE);
			channelSpinner.setVisibility(View.GONE);
			searchLabel.setVisibility(View.VISIBLE);
			startSearchEpgQuery(app.getCurrentSearch());
			break;
    }

		// Create adapter for EPG list
		listView = (ListView) findViewById(R.id.whatson_list);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);

    // register EPG item click
    listView.setOnItemClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		fillTimeSpinnerValues();

		reloadIfNeeded();
	}

	private void reloadIfNeeded() {

		final AndroVdrApp app = (AndroVdrApp) getApplication();
		if (app.isReload()) {
			app.setReload(false);
			startEpgQuery(epgClient);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (epgClient != null) {
			epgClient.abort();
		}
		if (progress != null) {
			progress.dismiss();
			progress = null;
		}
	}

	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {

		// find and remember item
		final EventListItem item =  adapter.getItem(position);

		// prepare timer if we want to program
		prepareTimer(item);

		// show details
		final Intent intent = new Intent();
		intent.setClass(this, EpgDetailsActivity.class);
		startActivity(intent);
	}

	public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {

		if (parent == timeSpinner) {
			// get spinner value
			final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner.getSelectedItem();
			// update search
			startTimeEpgQuery(selection.getValue());
		} else {
			// get spinner value
			final Channel channel = (Channel) channelSpinner.getSelectedItem();
			// update search
			startChannelEpgQuery(channel);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.epg_list_menu, menu);
    return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		Intent intent;

		switch (item.getItemId())
		{
		case R.id.epg_menu_search:
			intent = new Intent();
			intent.setClass(this, EpgSearchActivity.class);
			startActivity(intent);
			break;
		case R.id.epg_menu_times:
			intent = new Intent();
			intent.setClass(this, EpgSearchTimesListActivity.class);
			startActivity(intent);
			break;
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.whatson_list) {
	    final MenuInflater inflater = getMenuInflater();
	    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

	    // set menu title
	    final EventListItem item = adapter.getItem(info.position);
	    final EventFormatter formatter = new EventFormatter(item);
	    menu.setHeaderTitle(formatter.getTitle());

	    inflater.inflate(R.menu.epg_list_item_menu, menu);

	    // remove unneeded menu items
	    if (item.getEpg().getTimer() != null) {
	    	menu.findItem(R.id.epg_item_menu_timer_add).setVisible(false);
	    	final MenuItem enableMenuItem = menu.findItem(R.id.epg_item_menu_timer_toggle);
	    	enableMenuItem.setTitle(item.getEpg().getTimer().isEnabled() ? R.string.epg_item_menu_timer_disable : R.string.epg_item_menu_timer_enable);
	    } else {
	    	menu.findItem(R.id.epg_item_menu_timer_modify).setVisible(false);
	    	menu.findItem(R.id.epg_item_menu_timer_delete).setVisible(false);
	    }
		}
	}



	@Override
	public boolean onContextItemSelected(final MenuItem item) {

    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    final EventListItem event = adapter.getItem(info.position);

    switch (item.getItemId()) {
    case R.id.epg_item_menu_timer_add:
    case R.id.epg_item_menu_timer_modify:
    {
    	prepareTimer(event);

    	final Intent intent = new Intent();
    	intent.setClass(this, TimerDetailsActivity.class);
    	startActivity(intent);
    	break;
    }
    case R.id.epg_item_menu_timer_delete:
    {
    	deleteTimer(event);
    	break;
    }
    case R.id.epg_item_menu_timer_toggle:
    {
    	toggleTimer(event);
    }
		}

		return true;
	}

	public void onNothingSelected(final AdapterView<?> arg0) {
		//startTimeEpgQuery(((EpgTimeSpinnerValue)timeSpinner.getAdapter().getItem(0)).getValue());
	}

	private void startSearchEpgQuery(final EpgSearchParams search) {

		epgClient = new EpgClient(search);
		startEpgQuery(epgClient);
	}

	private void startTimeEpgQuery(final String time) {

		epgClient = new EpgClient(time);
		startEpgQuery(epgClient);
	}

	private void startChannelEpgQuery(final Channel channel) {

		epgClient = new EpgClient(channel);
		startEpgQuery(epgClient);
	}

	private void startEpgQuery(final EpgClient epgClient) {

		// remove old listeners
		epgClient.clearSvdrpListener();

		// create background task
		final SvdrpAsyncTask<Epg, SvdrpClient<Epg>> task = new SvdrpAsyncTask<Epg, SvdrpClient<Epg>>(epgClient);

		// create progress
		progress = new SvdrpProgressDialog(this, epgClient);

		// attach listener
		task.addListener(this);

		// start task
		task.run();
	}

	public void svdrpEvent(final SvdrpEvent event, final Epg result) {

		if (progress != null) {
			progress.svdrpEvent(event);
		}

		switch (event) {
		case CONNECTING:
			adapter.clearItems();
			break;
		case LOGIN_ERROR:
			this.finish();
			break;
		case FINISHED:
			epgClient.clearSvdrpListener();
			for(final Epg epg : epgClient.getResults()) {
				adapter.addItem(new EventListItem(epg));
			}
			adapter.sortItems();
			listView.setSelectionAfterHeaderView();
			progress = null;
			break;
		}
	}

	public void svdrpException(final SvdrpException exception) {
		if (progress != null) {
			progress.svdrpException(exception);
		}
	}

	private void prepareTimer(final EventListItem item) {

		final AndroVdrApp app = (AndroVdrApp) getApplication();

		// remember event for details view and timer things
		app.setCurrentEvent(item.getEpg());

		// if we create or modify the attached timer we will return to a new epg list
		app.clearActivitiesToFinish();
		app.addActivityToFinish(this);
		app.setNextActivity(EpgListActivity.class);
	}

	private void deleteTimer(final EventListItem item) {

		final DeleteTimerTask task = new DeleteTimerTask(this, item.getEpg().getTimer()) {
			@Override
			public void finished() {
				// refresh epg list after return
				final AndroVdrApp app = (AndroVdrApp) getApplication();
				app.setReload(true);
				reloadIfNeeded();
			}
		};
		task.start();
	}

	private void toggleTimer(final EventListItem item) {

		final ToggleTimerTask task = new ToggleTimerTask(this, item.getEpg().getTimer()) {
			@Override
			public void finished() {
				// refresh epg list after return
				final AndroVdrApp app = (AndroVdrApp) getApplication();
				app.setReload(true);
				reloadIfNeeded();
			}
		};
		task.start();
	}

	private void fillTimeSpinnerValues() {
		final EpgSearchTimeValues values = new EpgSearchTimeValues(this);
		timeSpinnerAdapter.clear();
		for(final EpgSearchTimeValue value : values.getValues()) {
			timeSpinnerAdapter.add(value);
		}
	}
}
