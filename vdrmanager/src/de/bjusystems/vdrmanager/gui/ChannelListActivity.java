package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.utils.svdrp.ChannelClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class ChannelListActivity extends BaseActivity<ExpandableListView> implements
		OnChildClickListener, OnGroupClickListener, SvdrpAsyncListener<Channel> {

	private static final String TAG = ChannelListActivity.class.getName();
	ChannelClient channelClient;
	
	ChannelAdapter adapter;
	
	Preferences prefs;
	
	public static final int MENU_GROUP = 0;
	public static final int MENU_PROVIDER = 1;
	public static final int MENU_NAME = 2;

	private int groupBy = MENU_GROUP;

	final static ArrayList<String> ALL_CHANNELS_GROUP = new ArrayList<String>(
			1);

	//ExpandableListView listView;

	// @Override
	// public boolean onKeyLongPress(int keyCode, KeyEvent event) {
	// // TODO Auto-generated method stub
	// return super.onKeyLongPress(keyCode, event);
	// }
	//
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// // TODO Auto-generated method stub
	// return super.onKeyDown(keyCode, event);
	// }

	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// // TODO Auto-generated method stub
	// return super.onKeyUp(keyCode, event);
	// }
	//
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		// Attach view
		setContentView(getMainLayout());
		setTitle(R.string.action_menu_channels);
		adapter = new ChannelAdapter(this);

		// Create adapter for ListView

		listView = (ExpandableListView) findViewById(R.id.channel_list);
		// listView.setOnItemClickListener(this);
		listView.setOnChildClickListener(this);
		listView.setTextFilterEnabled(true);
		listView.setFastScrollEnabled(true);
		listView.setAdapter(adapter);
		// register context menu
		registerForContextMenu(listView);
		startChannelQuery();
	}

	//

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void startChannelQuery() {
		backupViewSelection();
		startChannelQuery(true);
	}

	private void startChannelQuery(boolean useCache) {

		// get channel task
		channelClient = new ChannelClient(useCache);

		// create background task
		final SvdrpAsyncTask<Channel, SvdrpClient<Channel>> task = new SvdrpAsyncTask<Channel, SvdrpClient<Channel>>(
				channelClient);

		// create progress
		progress = new SvdrpProgressDialog<Channel>(this, channelClient);

		// attach listener
		task.addListener(this);
		task.addListener(progress);

		// start task
		task.run();
	}

	private void fillAdapter() {
		switch (groupBy) {
		case MENU_GROUP:
			ArrayList<String> cgs = channelClient.getChannelGroups();
			adapter.fill(cgs, channelClient.getGroupChannels(), groupBy);
			if(cgs.size() == 1){
			listView.expandGroup(0);
			}
			updateWindowTitle(
					getString(R.string.action_menu_channels),
					getString(R.string.groupby_window_title_templte,
							getString(R.string.groupby_group)));
			break;
		case MENU_PROVIDER:
			ArrayList<String> gs = new ArrayList<String>(channelClient
					.getProviderChannels().keySet());
			adapter.fill(gs, channelClient
					.getProviderChannels(), groupBy);
			if(gs.size() == 1){
				listView.expandGroup(0);
			}
			updateWindowTitle(
					getString(R.string.action_menu_channels),
					getString(R.string.groupby_window_title_templte,
							getString(R.string.groupby_provider)));
			break;
		case MENU_NAME:
			if (ALL_CHANNELS_GROUP.isEmpty()) {
				ALL_CHANNELS_GROUP
						.add(getString(R.string.groupby_name_all_channels_group));
			}
			HashMap<String, ArrayList<Channel>> channels = new HashMap<String, ArrayList<Channel>>(
					1);
			channels.put(getString(R.string.groupby_name_all_channels_group),
					channelClient.getChannels());
			adapter.fill(ALL_CHANNELS_GROUP, channels, groupBy);
			listView.expandGroup(0);

			updateWindowTitle(R.string.action_menu_channels,
					R.string.groupby_name_all_channels_group);
			break;
		}
	}

	public void svdrpEvent(final SvdrpEvent event, final Channel result) {
		switch (event) {
		case ABORTED:
			say(R.string.aborted);
			break;
		case ERROR:
			say(R.string.epg_client_errors);
			break;
		case CONNECTING:
			break;
		case CONNECT_ERROR:
			say(R.string.progress_connect_error);
			switchNoConnection();
			break;
		case FINISHED_ABNORMALY:
			say(R.string.progress_connect_finished_abnormal);
			switchNoConnection();
			break;
		case LOGIN_ERROR:
			switchNoConnection();
			break;
		case CACHE_HIT:
			say(R.string.progress_cache_hit);
			fillAdapter();
			restoreViewSelection();
			break;
		case FINISHED_SUCCESS:
			fillAdapter();
			restoreViewSelection();
			break;

		}
	}
	

	public void svdrpException(final SvdrpException exception) {
		progress.svdrpException(exception);
		Log.w(TAG, exception);
		alert(getString(R.string.vdr_error_text, exception.getMessage()));
	}


	public boolean onPrepareOptionsMenu(Menu menu) {
		
		return super.onPrepareOptionsMenu(menu);
	}

	
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem item;
		item = menu.add(MENU_GROUP, MENU_GROUP, 0, R.string.menu_groupby);
		item.setIcon(android.R.drawable.ic_menu_sort_alphabetically);
		item.setAlphabeticShortcut('g');

		// item = menu.add(MENU_GROUP_PROVIDER, MENU_PROVIDER, 0,
		// R.string.groupby_provider);
		// item.setIcon(android.R.drawable.ic_menu_sort_alphabetically);
		// item.setAlphabeticShortcut('p');

		// item = menu.add(MENU_GROUP_NAME, MENU_NAME, 0,
		// R.string.groupby_name);
		// item.setIcon(android.R.drawable.ic_menu_sort_alphabetically);
		// item.setAlphabeticShortcut('n');

		return true;
	}

	private static final String[] EMPTY = new String[] {};

	private String[] getAvailableGroupByEntries() {
		ArrayList<String> entries = new ArrayList<String>(2);
		entries.add(getString(R.string.groupby_group));
		entries.add(getString(R.string.groupby_provider));
		entries.add(getString(R.string.groupby_name));
		return entries.toArray(EMPTY);
	}

	AlertDialog groupByDialog = null;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case MENU_GROUP:
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
										groupBy = which;
										fillAdapter();
										groupByDialog.dismiss();
									}
								}).create();
			}

			groupByDialog.show();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		int group = ExpandableListView
				.getPackedPositionGroup(info.packedPosition);
		int child = ExpandableListView
				.getPackedPositionChild(info.packedPosition);
		// Only create a context menu for child items
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			// Array created earlier when we built the expandable list
			Channel item = (Channel) adapter.getChild(group, child);
			// if (v.getId() == R.id.channel_list) {
			final MenuInflater inflater = getMenuInflater();
			menu.setHeaderTitle(item.getName());
			inflater.inflate(R.menu.channel_list_item_menu, menu);
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			Channel grp = (Channel) adapter.getGroup(group);
			final MenuInflater infl = getMenuInflater();
			menu.setHeaderTitle(grp.getName());
			infl.inflate(R.menu.channel_list_group_menu, menu);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
				.getMenuInfo();

		// String title = ((TextView) info.targetView).getText().toString();

		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);

		Channel channel = null;
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			int childPos = ExpandableListView
					.getPackedPositionChild(info.packedPosition);
			channel = (Channel) adapter.getChild(groupPos, childPos);
			switch (item.getItemId()) {
			case R.id.channel_item_menu_epg:
				startChannelEPG(channel);
				break;
			case R.id.channel_item_menu_stream:
				// show live stream
				Utils.stream(this, channel);
				break;
			}

			return true;
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			int groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);

			return true;
		}

		return false;

	}

	// @Override
	// public boolean onContextItemSelected(final MenuItem item) {
	//
	// final AdapterView.AdapterContextMenuInfo info =
	// (AdapterView.AdapterContextMenuInfo) item
	// .getMenuInfo();
	// // final Channel channel = adapter.getItem(info.position);
	// // if(channel.isGroupSeparator()){
	// //
	// // }
	//
	// switch (item.getItemId()) {
	// case R.id.channel_item_menu_epg:
	// //onItemClick(null, null, info.position, 0);
	// break;
	// case R.id.channel_item_menu_stream:
	// // show live stream
	// // Utils.stream(this, channel);
	// break;
	// }
	//
	// return true;
	// }

	@Override
	public boolean onSearchRequested() {
		InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.toggleSoftInput(0, 0);
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
			long arg3) {
		return true;
	}

	private void startChannelEPG(Channel channel) {
		// find and remember item
		// final Channel channel = adapter.getItem(position);
		// final VdrManagerApp app = (VdrManagerApp) getApplication();
		// app.setCurrentChannel(channel);

		// show details
		final Intent intent = new Intent();
		intent.putExtra(Intents.CURRENT_CHANNEL, channel);
		intent.setClass(this, EventEpgListActivity.class);
		startActivity(intent);
	}

	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Channel channel = (Channel) adapter.getChild(groupPosition,
				childPosition);
		startChannelEPG(channel);
		return false;
	}

	@Override
	protected void refresh() {
		backupViewSelection();
		startChannelQuery(false);
	}

	@Override
	protected void retry() {
		refresh();
	}

	@Override
	protected int getMainLayout() {
		return R.layout.channel_list;
	}

}