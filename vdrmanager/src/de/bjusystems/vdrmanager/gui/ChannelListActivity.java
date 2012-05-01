package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.tasks.VoidAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.ChannelClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;
import de.bjusystems.vdrmanager.utils.svdrp.SwitchChannelClient;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class ChannelListActivity extends
		BaseActivity<Channel, ExpandableListView> implements
		OnChildClickListener, OnGroupClickListener {

	private static final String TAG = ChannelListActivity.class.getName();

	ChannelClient channelClient;

	ChannelAdapter adapter;

	Preferences prefs;

	private static final LinkedList<Channel> RECENT = new LinkedList<Channel>();

	public static final int MENU_GROUP = 0;
	public static final int MENU_PROVIDER = 1;
	public static final int MENU_NAME = 2;

	private int groupBy = MENU_GROUP;
	
	private int groupByInverse = 0;

	final static ArrayList<String> ALL_CHANNELS_GROUP = new ArrayList<String>(1);

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		setContentView(getMainLayout());
		setTitle(getWindowTitle());
		initFlipper();
		adapter = new ChannelAdapter(this);

		listView = (ExpandableListView) findViewById(R.id.channel_list);
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

		if (checkInternetConnection() == false) {
			return;
		}

		if(channelClient == null){
			// get channel task
			channelClient = new ChannelClient();
		} else {
			channelClient.removeSvdrpListener(this);
		}
		
		if(useCache == false){
			ChannelClient.clearCache();
		}

		// create background task
		final SvdrpAsyncTask<Channel, SvdrpClient<Channel>> task = new SvdrpAsyncTask<Channel, SvdrpClient<Channel>>(
				channelClient);

		task.addListener(this);
		// start task
		task.run();
	}

	static RecentChannelsAdapter RECENT_ADAPTER = null;

	
	static class RecentChannelsAdapter extends ArrayAdapter<Channel>{
		private Activity context;
		int resId;
		
		public RecentChannelsAdapter(Activity context, List<Channel> list) {
			super(context,	android.R.layout.simple_list_item_1, list);
			this.context = context;
			showChannelNumbers = Preferences.get().isShowChannelNumbers();
			
			if (Build.VERSION.SDK_INT < 11) {
				resId = android.R.layout.select_dialog_item;
			} else {
				resId = 	android.R.layout.simple_list_item_1;
			}
		}
		
		public boolean showChannelNumbers;
		
		public View getView(int position, View convertView, ViewGroup parent) {
			// recycle view?
			TextView text1;
			View view = convertView;
			if (view == null) {
				view = this.context.getLayoutInflater().inflate(
						resId, null);
				text1 = (TextView) view.findViewById(android.R.id.text1);
				view.setTag(text1);
			} else {
				text1 = (TextView) view.getTag();
			}

			Channel c = getItem(position);
			String text = showChannelNumbers ? text = c.toString() : c.getName();
			text1.setText(text);
			return view;

		}
	}

	private ArrayAdapter<Channel> getRecentAdapter() {
		if (RECENT_ADAPTER != null)
		{
			RECENT_ADAPTER.showChannelNumbers = Preferences.get().isShowChannelNumbers();
			RECENT_ADAPTER.notifyDataSetChanged();
			return RECENT_ADAPTER;
		}
		
		RECENT_ADAPTER = new RecentChannelsAdapter(this, RECENT);
		return RECENT_ADAPTER;

	}

	@Override
	public void reset() {
		channelClient.clearCache();
	}

	private void fillAdapter() {
		switch (groupBy) {
		case MENU_GROUP:
			ArrayList<String> cgs = ChannelClient.getChannelGroups();
			adapter.fill(cgs, ChannelClient.getGroupChannels(), groupBy);
			if (cgs.size() == 1) {// one group or first no first group
				listView.expandGroup(0);
			} else if ((cgs.size() > 1 && TextUtils.isEmpty(cgs.get(0)))) {
				listView.expandGroup(0);
			}
			updateWindowTitle();
			break;
		case MENU_PROVIDER:
			ArrayList<String> gs = new ArrayList<String>(ChannelClient
					.getProviderChannels().keySet());
			adapter.fill(gs, ChannelClient.getProviderChannels(), groupBy);
			if (gs.size() == 1) {
				listView.expandGroup(0);
			}
			updateWindowTitle();
			break;
		case MENU_NAME:
			if (ALL_CHANNELS_GROUP.isEmpty()) {
				ALL_CHANNELS_GROUP
						.add(getString(R.string.groupby_name_all_channels_group));
			}
			HashMap<String, ArrayList<Channel>> channels = new HashMap<String, ArrayList<Channel>>(
					1);
			channels.put(getString(R.string.groupby_name_all_channels_group),
					ChannelClient.getChannels());
			adapter.fill(ALL_CHANNELS_GROUP, channels, groupBy);
			listView.expandGroup(0);
			updateWindowTitle();
		}
	}

	public boolean onPrepareOptionsMenu(Menu menu) {

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.channellist, menu);

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
		case R.id.channels_groupby:
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
		case R.id.channels_recent_channels:
			if (RECENT.isEmpty()) {
				say(R.string.recent_channels_no_history);
				return true;
			}

			if(Preferences.get().getMaxRecentChannels() <= 0){
				RECENT.clear();
				say(R.string.recent_channels_no_history);
				return true;
			}
			
			new AlertDialog.Builder(this)
					.setTitle(R.string.recent_channels)
					.setAdapter(getRecentAdapter(), new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							Channel c = RECENT.get(which);
							startChannelEPG(c);
						}
					})//
					.create().show();
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
			/*
			 * http://projects.vdr-developer.org/issues/722 String grp =
			 * adapter.getGroup(group); final MenuInflater infl =
			 * getMenuInflater(); menu.setHeaderTitle(grp);
			 * infl.inflate(R.menu.channel_list_group_menu, menu);
			 */
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
			case R.id.channel_item_menu_hide:
				// TODO http://projects.vdr-developer.org/issues/722
				break;
			case R.id.channel_item_menu_hide_permanent:
				// TODO http://projects.vdr-developer.org/issues/722
				break;
			
			case R.id.channel_item_menu_switch:
				final String name = channel.getName();
				final SwitchChannelClient scc = new SwitchChannelClient(channel.getId());
				SvdrpAsyncTask<String, SwitchChannelClient> task = new SvdrpAsyncTask<String, SwitchChannelClient>(scc);
				task.addListener(new SvdrpAsyncListener<String>() {
					public void svdrpEvent(SvdrpEvent event, String result) {
						if(event == SvdrpEvent.FINISHED_SUCCESS){
							say(getString(R.string.switching_success, name));
						} else if(event == SvdrpEvent.CONNECT_ERROR || event == SvdrpEvent.FINISHED_ABNORMALY || event == SvdrpEvent.ABORTED || event == SvdrpEvent.ERROR || event == SvdrpEvent.CACHE_HIT){
							say(getString(R.string.switching_failed, name, event.name()));
						}
						
					}
					
					public void svdrpException(SvdrpException e) {
						Log.w(TAG, e.getMessage(), e);
						say(e.getMessage());
					}
				});
				task.run();
			}
			


			return true;
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			int groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);

			return true;
		}

		return false;

	}

	@Override
	public boolean onSearchRequested() {
		InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.toggleSoftInput(0, 0);
		return true;
	}

	public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2,
			long arg3) {
		return true;
	}

	private void startChannelEPG(final Channel channel) {
		new VoidAsyncTask() {

			@Override
			protected Void doInBackground(Void... arg0) {
				int max = Preferences.get().getMaxRecentChannels();
				if(max <= 0){
					return null;
				}
				Iterator<Channel> i = RECENT.iterator();
				while (i.hasNext()) {
					Channel c = i.next();
					if (c.equals(channel)) {
						i.remove();
						break;
					}
				}

				if (RECENT.size() >= Preferences.get().getMaxRecentChannels()) {
					RECENT.removeLast();

				}
				RECENT.addFirst(channel);
				return (Void) null;
			}
		}.execute((Void) null);
		// for(int i = 0; i < recent)
		// find and remember item
		// final Channel channel = adapter.getItem(position);
		// final VdrManagerApp app = (VdrManagerApp) getApplication();
		// app.setCurrentChannel(channel);

		// show details
		final Intent intent = new Intent();
		getApp().setCurrentChannel(channel);
		// intent.putExtra(Intents.CURRENT_CHANNEL, channel);
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

	private String resolveWindowTitle() {
		StringBuilder sb = new StringBuilder();
		switch (groupBy) {
		case MENU_NAME:
			sb.append(getString(R.string.action_menu_channels))
					.append(" > ")
					.append(getString(R.string.groupby_name_all_channels_group));
			break;
		case MENU_PROVIDER:
			sb.append(getString(R.string.action_menu_channels))
					.append(" > ")
					.append(getString(R.string.groupby_window_title_templte,
							getString(R.string.groupby_provider)));
			break;
		case MENU_GROUP:
			sb.append(getString(R.string.action_menu_channels))
					.append(" > ")
					.append(getString(R.string.groupby_window_title_templte,
							getString(R.string.groupby_group)));
			break;
		}
		return sb.toString();
	}

	private void updateWindowTitle() {
		setTitle(getString(R.string.channels_window_title_count,
				resolveWindowTitle(), adapter.groups.size(), ChannelClient
						.getChannels().size()));
	}

	@Override
	protected boolean finishedSuccess() {
		fillAdapter();
		restoreViewSelection();
		updateWindowTitle();
		return ChannelClient.getChannels().isEmpty() == false;
	}

	@Override
	protected void resultReceived(Channel result) {
	}

	protected void cacheHit() {
		fillAdapter();
		restoreViewSelection();
	}

	@Override
	protected String getWindowTitle() {
		return resolveWindowTitle();
	}

	protected boolean displayingResults() {
		return ChannelClient.getChannels().isEmpty() == false;
	}

	@Override
	protected SvdrpClient<Channel> getClient() {
		return channelClient;
	}
	
	public void svdrpEvent(SvdrpEvent event, Channel result){
		super.svdrpEvent(event, result);
	}

}