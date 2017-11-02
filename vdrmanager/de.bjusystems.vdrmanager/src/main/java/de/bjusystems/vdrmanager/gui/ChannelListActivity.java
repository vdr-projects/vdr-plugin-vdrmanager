package de.bjusystems.vdrmanager.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import de.bjusystems.vdrmanager.data.P;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.RecenteChannel;
import de.bjusystems.vdrmanager.data.db.DBAccess;
import de.bjusystems.vdrmanager.tasks.VoidAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.ChannelClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used for showing what's current running on all channels
 *
 * @author bju
 */
public class ChannelListActivity extends
		BaseActivity<Channel, ExpandableListView> implements
		OnChildClickListener, OnGroupClickListener {

	private static final String TAG = ChannelListActivity.class.getName();

	ChannelAdapter adapter;

	Preferences prefs;

	// private static final LinkedList<Channel> RECENT = new
	// LinkedList<Channel>();

	public static final int MENU_GROUP = 0;
	public static final int MENU_PROVIDER = 1;
	public static final int MENU_SOURCE = 2;
	public static final int MENU_NAME = 3;

	public static final boolean GROUP_NATURAL = false;

	public static final boolean GROUP_REVERSE = true;

	private int groupBy;

	private boolean groupByReverse;

	final static ArrayList<String> ALL_CHANNELS_GROUP = new ArrayList<String>(1);

	@Override
	protected void onResume() {
		super.onResume();
	}


	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		
		setTitle(getWindowTitle());
		initFlipper();

		groupBy = Preferences.get(this, P.CHANNELS_LAST_ORDER, MENU_GROUP);
		groupByReverse = Preferences.get(this, P.CHANNELS_LAST_ORDER_REVERSE,
				GROUP_NATURAL);

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

	

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void startChannelQuery() {
		backupViewSelection();
		startChannelQuery(true);
	}

	private void startChannelQuery(final boolean useCache) {

		if (checkInternetConnection() == false) {
			return;
		}

		final ChannelClient channelClient = new ChannelClient(
				getCertificateProblemDialog());

		if (useCache == false) {
			ChannelClient.clearCache();
		}

		// create background task
		final SvdrpAsyncTask<Channel, SvdrpClient<Channel>> task = new SvdrpAsyncTask<Channel, SvdrpClient<Channel>>(
				channelClient);

		addListener(task);
		// task.addSvdrpExceptionListener(this);
		// task.addSvdrpResultListener(this);
		// task.addSvdrpListener(this);
		// task.addSvdrpFinishedListener(this);

		// start task
		task.run();
	}

	static RecentChannelsAdapter RECENT_ADAPTER = null;

	static class RecentChannelsAdapter extends ArrayAdapter<Channel> {
		private final Activity context;
		int resId;

		public RecentChannelsAdapter(final Activity context) {
			super(context, android.R.layout.simple_list_item_1);
			this.context = context;
			showChannelNumbers = Preferences.get().isShowChannelNumbers();

			if (Build.VERSION.SDK_INT < 11) {
				resId = android.R.layout.select_dialog_item;
			} else {
				resId = android.R.layout.simple_list_item_1;
			}
		}

		public boolean showChannelNumbers;

		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {
			// recycle view?
			TextView text1;
			View view = convertView;
			if (view == null) {
				view = this.context.getLayoutInflater().inflate(resId, null);
				text1 = (TextView) view.findViewById(android.R.id.text1);
				view.setTag(text1);
			} else {
				text1 = (TextView) view.getTag();
			}

			final Channel c = getItem(position);
			String text = showChannelNumbers ? text = c.toString() : c
					.getName();
			text1.setText(text);
			return view;

		}
	}

	private ArrayAdapter<Channel> getRecentAdapter() {
		if (RECENT_ADAPTER != null) {
			RECENT_ADAPTER.showChannelNumbers = Preferences.get()
					.isShowChannelNumbers();
			RECENT_ADAPTER.notifyDataSetChanged();
			return RECENT_ADAPTER;
		}

		RECENT_ADAPTER = new RecentChannelsAdapter(this);
		return RECENT_ADAPTER;

	}

	private void fillAdapter() {
		switch (groupBy) {
		case MENU_GROUP:
			final ArrayList<String> cgs = ChannelClient.getChannelGroups();
			adapter.fill(cgs, ChannelClient.getGroupChannels(), groupBy,
					groupByReverse);
			if (cgs.size() == 1) {// one group or first no first group
				listView.expandGroup(0);
			} else if ((cgs.size() > 1 && TextUtils.isEmpty(cgs.get(0)))) {
				listView.expandGroup(0);
			}
			updateWindowTitle();
			break;

		case MENU_SOURCE:
			final ArrayList<String> css = ChannelClient.getChannelSources();
			adapter.fill(css, ChannelClient.getSourceChannels(), groupBy,
					groupByReverse);
			if (css.size() == 1) {// one group or first no first group
				listView.expandGroup(0);
			} else if ((css.size() > 1 && TextUtils.isEmpty(css.get(0)))) {
				listView.expandGroup(0);
			}
			updateWindowTitle();
			break;

		case MENU_PROVIDER:
			final ArrayList<String> gs = new ArrayList<String>(ChannelClient
					.getProviderChannels().keySet());
			adapter.fill(gs, ChannelClient.getProviderChannels(), groupBy,
					groupByReverse);
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
			final HashMap<String, ArrayList<Channel>> channels = new HashMap<String, ArrayList<Channel>>(
					1);
			ArrayList<Channel> channelsSorted = ChannelClient.getChannels();
			Collections.sort(channelsSorted, new Comparator<Channel>() {

				@Override
				public int compare(Channel lhs, Channel rhs) {
					String lhsn = lhs.getName();
					String rhsn = rhs.getName();
					if (lhsn == null) {
						return 1;
					}
					if (rhsn == null) {
						return -1;
					}
					return lhsn.compareToIgnoreCase(rhsn);
				}
			});
			channels.put(getString(R.string.groupby_name_all_channels_group),
					channelsSorted);
			adapter.fill(ALL_CHANNELS_GROUP, channels, groupBy, groupByReverse);
			listView.expandGroup(0);
			updateWindowTitle();
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.channellist, menu);

		return true;
	}

	private int getAvailableGroupByEntries() {
		return R.array.channels_group_by;
	}

	AlertDialog groupByDialog = null;

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		
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
									@Override
									public void onClick(
											final DialogInterface dialog,
											final int which) {

										final boolean reversed = which == groupBy ? true
												: false;
										groupBy = which;
										new VoidAsyncTask() {

											@Override
											protected Void doInBackground(
													final Void... params) {

												if (reversed) {
													if (groupByReverse == true) {
														groupByReverse = false;
													} else {
														groupByReverse = true;
													}
													Preferences
															.set(ChannelListActivity.this,
																	P.CHANNELS_LAST_ORDER_REVERSE,
																	groupByReverse);

												} else {
													Preferences
															.set(ChannelListActivity.this,
																	P.CHANNELS_LAST_ORDER,
																	groupBy);
												}
												return null;
											}
										}.execute();

										fillAdapter();
										groupByDialog.dismiss();
									}
								}).create();
			}

			groupByDialog.show();

			return true;
		case R.id.channels_recent_channels:

			final String order = Preferences.get(ChannelListActivity.this,
					"gui_recent_channels_order", "most");

			List<RecenteChannel> rcs = null;

			if (order.equals("most")) {
				rcs = DBAccess
						.get(ChannelListActivity.this)
						.getRecentChannelDAO()
						.loadByRecentUse(
								Preferences.get().getMaxRecentChannels());
			} else if (order.equals("last")) {
				rcs = DBAccess
						.get(ChannelListActivity.this)
						.getRecentChannelDAO()
						.loadByLastAccess(
								Preferences.get().getMaxRecentChannels());
			} else {
				return true;
			}

			if (rcs.isEmpty()) {
				say(R.string.recent_channels_no_history);
				return true;
			}

			if (Preferences.get().getMaxRecentChannels() <= 0) {
				say(R.string.recent_channels_no_history);
				return true;
			}

			final ArrayAdapter<Channel> recentAdapter = getRecentAdapter();

			recentAdapter.clear();
			for (final Channel c : DBAccess.get(ChannelListActivity.this)
					.getRecentChannelDAO()
					.getRecentChannels(ChannelClient.getIdChannels(), rcs)) {
				recentAdapter.add(c);
			}

			new AlertDialog.Builder(this)
					.setTitle(R.string.recent_channels)
					.setAdapter(getRecentAdapter(),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(
										final DialogInterface dialog,
										final int which) {
									final Channel c = recentAdapter
											.getItem(which);
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
		final ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		final int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		final int group = ExpandableListView
				.getPackedPositionGroup(info.packedPosition);
		final int child = ExpandableListView
				.getPackedPositionChild(info.packedPosition);
		// Only create a context menu for child items
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			// Array created earlier when we built the expandable list
			final Channel item = (Channel) adapter.getChild(group, child);
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
	public boolean onContextItemSelected(final MenuItem item) {

		final ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
				.getMenuInfo();

		// String title = ((TextView) info.targetView).getText().toString();

		final int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);

		Channel channel = null;
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			final int groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			final int childPos = ExpandableListView
					.getPackedPositionChild(info.packedPosition);
			channel = (Channel) adapter.getChild(groupPos, childPos);
			switch (item.getItemId()) {
			// case R.id.channel_item_menu_epg:
			// startChannelEPG(channel);
			// break;
			case R.id.channel_item_menu_stream:
				// show live stream
				Utils.stream(this, channel);
				break;

			// case R.id.channel_item_menu_hide:
			// TODO http://projects.vdr-developer.org/issues/722
			// break;
			// case R.id.channel_item_menu_hide_permanent:
			// TODO http://projects.vdr-developer.org/issues/722
			// break;

			case R.id.channel_item_menu_switch:
				Utils.switchTo(this, channel);
				break;
			}

			return true;
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			final int groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);

			return true;
		}

		return false;

	}

	@Override
	public boolean onSearchRequested() {
		final InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.toggleSoftInput(0, 0);
		return true;
	}

	@Override
	public boolean onGroupClick(final ExpandableListView arg0, final View arg1,
			final int arg2, final long arg3) {
		return true;
	}

	private void startChannelEPG(final Channel channel) {
		new VoidAsyncTask() {

			@Override
			protected Void doInBackground(final Void... arg0) {
				final int max = Preferences.get().getMaxRecentChannels();
				if (max <= 0) {
					return null;
				}

				DBAccess.get(ChannelListActivity.this).getRecentChannelDAO()
						.hit(channel.getId());

				return null;
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

	@Override
	public boolean onChildClick(final ExpandableListView parent, final View v,
			final int groupPosition, final int childPosition, final long id) {
		final Channel channel = (Channel) adapter.getChild(groupPosition,
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
		final StringBuilder sb = new StringBuilder();
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

		case MENU_SOURCE: {
			sb.append(getString(R.string.action_menu_channels))
					.append(" > ")
					.append(getString(R.string.groupby_window_title_templte,
							getString(R.string.groupby_source)));
			break;
		}
		}

		return sb.toString();
	}

	private void updateWindowTitle() {
		setTitle(getString(R.string.channels_window_title_count,
				resolveWindowTitle(), adapter.groups.size(), ChannelClient
						.getChannels().size()));
	}

	@Override
	protected synchronized boolean finishedSuccess(final List<Channel> results) {
		fillAdapter();
		restoreViewSelection();
		updateWindowTitle();
		return ChannelClient.getChannels().isEmpty() == false;
	}

	@Override
	protected void cacheHit() {
		fillAdapter();
		restoreViewSelection();
	}

	@Override
	protected String getWindowTitle() {
		return resolveWindowTitle();
	}

	@Override
	protected boolean displayingResults() {
		return ChannelClient.getChannels().isEmpty() == false;
	}

	@Override
	protected int getProgressTextId() {
		return R.string.progress_channels_loading;
	}

	@Override
	protected int getListNavigationIndex() {
		return LIST_NAVIGATION_CHANNELS;
	}
}
