package de.bjusystems.androvdr.gui;

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
import de.bjusystems.androvdr.app.AndroVdrApp;
import de.bjusystems.androvdr.data.EventFormatter;
import de.bjusystems.androvdr.data.EventListItem;
import de.bjusystems.androvdr.data.Timer;
import de.bjusystems.androvdr.tasks.DeleteTimerTask;
import de.bjusystems.androvdr.tasks.ToggleTimerTask;
import de.bjusystems.androvdr.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.androvdr.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.androvdr.utils.svdrp.SvdrpClient;
import de.bjusystems.androvdr.utils.svdrp.SvdrpEvent;
import de.bjusystems.androvdr.utils.svdrp.SvdrpException;
import de.bjusystems.androvdr.utils.svdrp.TimerClient;
import de.bjusystems.androvdr.R;

/**
 * This class is used for showing what's
 * current running on all channels
 * @author bju
 */
public class TimerListActivity extends Activity
															 implements OnItemClickListener, SvdrpAsyncListener<Timer> {

	TimerClient timerClient;
	EventAdapter adapter;
	SvdrpProgressDialog progress;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		setContentView(R.layout.timer_list);

		// create an adapter
		adapter = new EventAdapter(this, true);

		// attach adapter to ListView
		final ListView listView = (ListView) findViewById(R.id.timer_list);
		listView.setAdapter(adapter);

		// set click listener
		listView.setOnItemClickListener(this);

		// context menu wanted
		registerForContextMenu(listView);

		// start query
		startTimerQuery();
  }

	@Override
	protected void onResume() {
		super.onResume();
		reloadIfNeeded();
	}

	private void reloadIfNeeded() {

		final AndroVdrApp app = (AndroVdrApp) getApplication();
		if (app.isReload()) {
			app.setReload(false);
			startTimerQuery();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (timerClient != null) {
			timerClient.abort();
		}
		if (progress != null) {
			progress.dismiss();
			progress = null;
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.timer_list) {
	    final MenuInflater inflater = getMenuInflater();
	    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

	    // set menu title
	    final EventListItem item = adapter.getItem(info.position);
	    final EventFormatter formatter = new EventFormatter(item);
	    menu.setHeaderTitle(formatter.getTitle());

	    inflater.inflate(R.menu.epg_list_item_menu, menu);

	    // remove unneeded menu items
    	menu.findItem(R.id.epg_item_menu_timer_add).setVisible(false);
    	final MenuItem enableMenuItem = menu.findItem(R.id.epg_item_menu_timer_toggle);
    	enableMenuItem.setTitle(item.getTimer().isEnabled() ? R.string.epg_item_menu_timer_disable : R.string.epg_item_menu_timer_enable);
		}
	}



	@Override
	public boolean onContextItemSelected(final MenuItem item) {

    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    final EventListItem event = adapter.getItem(info.position);

    switch (item.getItemId()) {
    case R.id.epg_item_menu_timer_modify:
    {
    	onItemClick(null, null, info.position, 0);
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

	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {

		// save selected item
		final Timer timer = adapter.getItem(position).getTimer();
		if (timer == null) {
			// header click
			return;
		}

		final AndroVdrApp app = (AndroVdrApp) getApplication();
		app.setCurrentTimer(timer);

		// after timer editing return to the timer list
		app.setNextActivity(TimerListActivity.class);
		app.clearActivitiesToFinish();

		// show timer details
		final Intent intent = new Intent();
		intent.setClass(this, TimerDetailsActivity.class);
		startActivity(intent);
	}

	private void startTimerQuery() {

		// get timer client
		timerClient = new TimerClient();

		// create backgound task
		final SvdrpAsyncTask<Timer, SvdrpClient<Timer>> task = new SvdrpAsyncTask<Timer, SvdrpClient<Timer>>(timerClient);

		// create progress dialog
		progress = new SvdrpProgressDialog(this, timerClient);

		// attach listener
		task.addListener(this);

		// start task
		task.run();
	}

	public void svdrpEvent(final SvdrpEvent event, final Timer result) {

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
			for(final Timer timer : timerClient.getResults()) {
				adapter.addItem(new EventListItem(timer));
			}
			adapter.sortItems();
			progress = null;
			break;
		}
	}

	public void svdrpException(final SvdrpException exception) {
		if (progress != null) {
			progress.svdrpException(exception);
		}
	}

	private void deleteTimer(final EventListItem item) {

		final DeleteTimerTask task = new DeleteTimerTask(this, item.getTimer()) {
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

		final ToggleTimerTask task = new ToggleTimerTask(this, item.getTimer()) {
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

}
