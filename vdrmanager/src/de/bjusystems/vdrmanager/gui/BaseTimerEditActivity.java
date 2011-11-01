package de.bjusystems.vdrmanager.gui;

import android.app.Activity;
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
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.tasks.DeleteTimerTask;
import de.bjusystems.vdrmanager.tasks.ToggleTimerTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;

public abstract class BaseTimerEditActivity<T extends Event> extends
		BaseEventListActivity<T> implements OnClickListener // SvdrpAsyncListener<Timer>,
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		//
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final EventListItem event = adapter.getItem(info.position);
		getApp().setCurrentEvent(event.getEvent());
		switch (item.getItemId()) {
		case R.id.epg_item_menu_timer_add: {
			getApp().setCurrentTimer(event.createTimer());
			// updateDisplay(TimerOperation.CREATE);
			// tDialog.show();
			final Intent intent = new Intent();
			intent.setClass(this, TimerDetailsActivity.class);
			intent.putExtra(Intents.TIMER_OP, Intents.ADD_TIMER);
			startActivityForResult(intent,
					TimerDetailsActivity.REQUEST_CODE_TIMER_EDIT);
		}
			break;
		case R.id.epg_item_menu_timer_modify: {
			getApp().setCurrentTimer(getTimer(event));
			// updateDisplay(TimerOperation.MODIFY);
			// tDialog.show();
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
		final EventFormatter formatter = new EventFormatter(item);
		menu.setHeaderTitle(formatter.getTitle());

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

		// }

	}

	protected Timer getTimer(EventListItem item) {
		return item.getEpg().getTimer();
	}

	protected void toggleTimer(Timer timer) {
		final ToggleTimerTask task = new ToggleTimerTask(this, timer) {
			@Override
			public void finished(SvdrpEvent event) {
				refresh();
				restoreViewSelection();
			}
		};
		task.start();
	}

	protected void deleteTimer(final Timer timer) {
		//backupViewSelection();
		final DeleteTimerTask task = new DeleteTimerTask(this, timer) {
			@Override
			public void finished(SvdrpEvent event) {
				refresh();
				restoreViewSelection();
			}
		};
		task.start();
	}
	
	protected void timerModified(){
		backupViewSelection();
		refresh();
	}
		

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		if(requestCode == TimerDetailsActivity.REQUEST_CODE_TIMER_EDIT){
			timerModified();
		}
//		refresh();
	}

}
