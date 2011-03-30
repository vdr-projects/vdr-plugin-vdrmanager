package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.utils.svdrp.ChannelClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

/**
 * This class is used for showing what's
 * current running on all channels
 * @author bju
 */
public class ChannelListActivity extends Activity
															   implements OnItemClickListener, SvdrpAsyncListener<Channel> {

	ChannelClient channelClient;
	ChannelAdapter adapter;
	Preferences prefs;
	List<Channel> channels;
	SvdrpProgressDialog progress;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		setContentView(R.layout.channel_list);

		// Create adapter for ListView
		adapter = new ChannelAdapter(this);
		final ListView listView = (ListView) findViewById(R.id.channel_list);
		listView.setAdapter(adapter);
		// register context menu
		registerForContextMenu(listView);


		// create channel list
		channels = new ArrayList<Channel>();

		listView.setOnItemClickListener(this);
	}

	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {

		// find and remember item
		final Channel channel =  (Channel) parent.getAdapter().getItem(position);
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		app.setCurrentChannel(channel);
		app.setChannels(channels);

		// show details
		final Intent intent = new Intent();
		intent.setClass(this, EpgListActivity.class);
		startActivity(intent);
	}



	@Override
	protected void onResume() {
		super.onResume();
		startChannelQuery();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (channelClient != null) {
			channelClient.abort();
		}
		if (progress != null) {
			progress.dismiss();
			progress = null;
		}
	}

	private void startChannelQuery() {

		// get channel task
		channelClient = new ChannelClient();

		// create background task
		final SvdrpAsyncTask<Channel, SvdrpClient<Channel>> task = new SvdrpAsyncTask<Channel, SvdrpClient<Channel>>(channelClient);

		// create progress
		progress = new SvdrpProgressDialog(this, channelClient);

		// attach listener
		task.addListener(this);

		// start task
		task.run();
	}

	public void svdrpEvent(final SvdrpEvent event, final Channel result) {

		if (progress != null) {
			progress.svdrpEvent(event);
		}

		switch (event) {
		case CONNECTING:
			adapter.clear();
			channels.clear();
			break;
		case LOGIN_ERROR:
			this.finish();
			break;
		case FINISHED:
			channels.addAll(channelClient.getResults());
			for(final Channel channel : channels) {
				adapter.add(channel);
			}
			progress = null;
		}
	}

	public void svdrpException(final SvdrpException exception) {
		if (progress != null) {
			progress.svdrpException(exception);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.channel_list) {
	    final MenuInflater inflater = getMenuInflater();
	    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

	    // set menu title
	    final Channel item = adapter.getItem(info.position);
	    menu.setHeaderTitle(item.getName());

	    inflater.inflate(R.menu.channel_list_item_menu, menu);
		}
	}



	@Override
	public boolean onContextItemSelected(final MenuItem item) {

    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    final Channel channel = adapter.getItem(info.position);

    switch (item.getItemId()) {
    case R.id.channel_item_menu_epg:
    	onItemClick(null, null, info.position, 0);
    	break;
    case R.id.channel_item_menu_stream:
    	// show live stream
    	showStream(channel);
    	break;
		}

		return true;
	}

	private void showStream(final Channel channel) {
  	// show stream
		final Intent intent = new Intent();
		intent.setClass(this, VideoActivity.class);
		startActivity(intent);
	}
}
