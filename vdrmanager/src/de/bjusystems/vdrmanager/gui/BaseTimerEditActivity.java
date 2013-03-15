package de.bjusystems.vdrmanager.gui;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.data.CACHE;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.data.Timerable;
import de.bjusystems.vdrmanager.tasks.DeleteTimerTask;
import de.bjusystems.vdrmanager.tasks.ToggleTimerTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;

/**
 *
 * This class is a base class for all the listings, which can deal with timers
 *
 * @author lado
 *
 * @param <T>
 *            Class extending Event
 */
public abstract class BaseTimerEditActivity<T extends Event> extends
		BaseEventListActivity<T> implements OnClickListener // SvdrpAsyncListener<Timer>,
{

	// private static final ScheduledExecutorService worker = Executors
	// .newSingleThreadScheduledExecutor();

	// /@Override
	// public boolean onPrepareOptionsMenu(Menu menu) {
	// return super.onPrepareOptionsMenu(menu);
	// }
	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	// @Override
	public boolean onContextItemSelected(final MenuItem item) {
		//
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final EventListItem event = adapter.getItem(info.position);
		getApp().setCurrentEvent(event.getEvent());
		switch (item.getItemId()) {
		case R.id.epg_item_menu_timer_add: {

			getApp().setCurrentTimer(createTimer(event));
			final Intent intent = new Intent();
			intent.setClass(this, TimerDetailsActivity.class);
			intent.putExtra(Intents.TIMER_OP, Intents.ADD_TIMER);
			startActivityForResult(intent,
					TimerDetailsActivity.REQUEST_CODE_TIMER_ADD);
		}
			break;
		case R.id.epg_item_menu_timer_modify: {
			getApp().setCurrentTimer(getTimer(event));
			final Intent intent = new Intent();
			intent.setClass(this, TimerDetailsActivity.class);
			intent.putExtra(Intents.TIMER_OP, Intents.EDIT_TIMER);
			startActivityForResult(intent,
					TimerDetailsActivity.REQUEST_CODE_TIMER_EDIT);
			break;
		}
		case R.id.epg_item_menu_timer_delete: {
			backupViewSelection();
			deleteTimer(getTimer(event));
			break;
		}
		case R.id.epg_item_menu_timer_toggle: {
			backupViewSelection();
			toggleTimer(getTimer(event));
			break;
		}
		default:
			return super.onContextItemSelected(item);
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
		// if (v.getId() == R.id.whatson_list) {
		final MenuInflater inflater = getMenuInflater();
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		// set menu title
		final EventListItem item = adapter.getItem(info.position);

		if (item.isHeader()) {
			return;
		}

		// final EventFormatter formatter = new EventFormatter(item);
		menu.setHeaderTitle(item.getTitle());

		inflater.inflate(R.menu.epg_list_item_menu, menu);
		Timer timer = getTimer(item);
		// remove unneeded menu items
		if (timer != null) {
			menu.findItem(R.id.epg_item_menu_timer_add).setVisible(false);
			menu.findItem(R.id.epg_item_menu_timer_modify).setVisible(true);
			menu.findItem(R.id.epg_item_menu_timer_delete).setVisible(true);
			final MenuItem enableMenuItem = menu
					.findItem(R.id.epg_item_menu_timer_toggle);
			enableMenuItem.setVisible(true);
			enableMenuItem
					.setTitle(timer.isEnabled() ? R.string.epg_item_menu_timer_disable
							: R.string.epg_item_menu_timer_enable);
		}

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	protected Timer createTimer(EventListItem item) {
		Event e = item.getEvent();
		if (e instanceof Timerable == false) {
			return null;
		}
		return ((Timerable) e).createTimer();

	}

	/**
	 * Extract a Timer from a given {@link EventListItem}
	 *
	 * @param item
	 * @return Timer if any on the event
	 */
	protected Timer getTimer(EventListItem item) {
		Event e = item.getEvent();
		if (e instanceof Timerable == false) {
			return null;
		}
		return ((Timerable) e).getTimer();
	}

	protected void toggleTimer(final Timer timer) {
		final ToggleTimerTask task = new ToggleTimerTask(this, timer) {
			@Override
			public void finished(SvdrpEvent event) {
				timerModified(timer);
				restoreViewSelection();
			}
		};
		task.start();
	}

	/**
	 * Delete a given timer
	 *
	 * @param timer
	 */
	protected void deleteTimer(final Timer timer) {
		// backupViewSelection();
		final DeleteTimerTask task = new DeleteTimerTask(this, timer) {
			@Override
			public void finished(SvdrpEvent event) {
				timerModified(timer);
				restoreViewSelection();
			}
		};
		task.start();
	}

	protected void timerModified() {
		timerModified(null);
	}

	/**
	 * Is called, if a timer has been changed and so update of the list is
	 * required
	 */
	protected void timerModified(final Timer timer) {
		backupViewSelection();
		if(timer != null && timer.getChannelId()!=null){
			CACHE.CACHE.remove(timer.getChannelId());
		}
		// say(R.string.update_will_start_in);
		// Runnable task = new Runnable() {
		// public void run() {
		refresh();

		// }
		// };
		// worker.schedule(task, 1000, TimeUnit.MILLISECONDS);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK) {
			return;
		}
		if (requestCode == TimerDetailsActivity.REQUEST_CODE_TIMER_EDIT) {
			timerModified();
			return;
		}
		if (requestCode == TimerDetailsActivity.REQUEST_CODE_TIMER_MODIFIED) {
			timerModified();
			return;
		}

		if (requestCode == TimerDetailsActivity.REQUEST_CODE_TIMER_ADD) {
			timerModified();
			return;
		}

	}

}
