package de.bjusystems.vdrmanager.gui;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.gui.SimpleGestureFilter.SimpleGestureListener;
import de.bjusystems.vdrmanager.tasks.DeleteTimerTask;
import de.bjusystems.vdrmanager.tasks.ToggleTimerTask;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;

/**
 * @author lado
 * 
 */
public abstract class BaseEpgListActivity extends BaseActivity implements
		SimpleGestureListener {

	private static final int REQUEST_CODE_TIMED_EDIT = 41;

	private SimpleGestureFilter detector;

	protected EpgClient epgClient;

	protected EventAdapter adapter;

	protected SvdrpProgressDialog progress;

	protected String highlight = null;

	protected static final Date FUTURE = new Date(Long.MAX_VALUE);

	// private static final Date BEGIN = new Date(0);

	protected Channel currentChannel = null;

	protected ListView listView;

	abstract protected int getWindowTitle();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Attach view
		setContentView(getMainLayout());
		detector = new SimpleGestureFilter(this, this);
		setTitle(getWindowTitle());
		initChannel();
	}

	private void initChannel() {
		currentChannel = getIntent()
				.getParcelableExtra(Intents.CURRENT_CHANNEL);
	}

	protected void deleteTimer(final EventListItem item) {

		final DeleteTimerTask task = new DeleteTimerTask(this, item.getEpg()
				.getTimer()) {
			@Override
			public void finished() {
				refresh();
			}
		};
		task.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.bjusystems.vdrmanager.gui.BaseActivity#onCreateOptionsMenu(android
	 * .view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.epg_list_menu, menu);
		return true;
	}

	protected void prepareTimer(EventListItem event) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final EventListItem event = adapter.getItem(info.position);

		switch (item.getItemId()) {
		case R.id.epg_item_menu_timer_add:
		case R.id.epg_item_menu_timer_modify: {
			prepareTimer(event);
			final Intent intent = new Intent();
			intent.setClass(this, TimerDetailsActivity.class);
			startActivityForResult(intent, REQUEST_CODE_TIMED_EDIT);
			break;
		}
		case R.id.epg_item_menu_timer_delete: {
			deleteTimer(event);
			break;
		}
		case R.id.epg_item_menu_timer_toggle: {
			toggleTimer(event);
			break;
		}
		case R.id.epg_item_menu_live_tv: {
			Utils.stream(this, event.getEvent());
			break;

		}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.bjusystems.vdrmanager.gui.BaseActivity#onOptionsItemSelected(android
	 * .view.MenuItem)
	 */
	public boolean onOptionsItemSelected(final MenuItem item) {

		Intent intent;

		switch (item.getItemId()) {
		case R.id.epg_menu_search:
			//startSearchManager();
			super.onSearchRequested();
			break;
		case R.id.epg_menu_times:
			intent = new Intent();
			intent.setClass(this, EpgSearchTimesListActivity.class);
			startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
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
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.whatson_list) {
			final MenuInflater inflater = getMenuInflater();
			final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

			// set menu title
			final EventListItem item = adapter.getItem(info.position);
			final EventFormatter formatter = new EventFormatter(item);
			menu.setHeaderTitle(formatter.getTitle());

			inflater.inflate(R.menu.epg_list_item_menu, menu);

			// remove unneeded menu items
			if (item.getEpg().getTimer() != null) {
				menu.findItem(R.id.epg_item_menu_timer_add).setVisible(false);
				menu.findItem(R.id.epg_item_menu_timer_modify).setVisible(true);
				menu.findItem(R.id.epg_item_menu_timer_delete).setVisible(true);
				final MenuItem enableMenuItem = menu
						.findItem(R.id.epg_item_menu_timer_toggle);
				enableMenuItem.setVisible(true);
				enableMenuItem
						.setTitle(item.getEpg().getTimer().isEnabled() ? R.string.epg_item_menu_timer_disable
								: R.string.epg_item_menu_timer_enable);
			}

			if (item.isLive()) {
				menu.findItem(R.id.epg_item_menu_live_tv).setVisible(true);
			}

		}

	}

	protected void toggleTimer(final EventListItem item) {
		final ToggleTimerTask task = new ToggleTimerTask(this, item.getEpg()
				.getTimer()) {
			@Override
			public void finished() {
				refresh();
			}
		};
		task.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_TIMED_EDIT) {
			if (resultCode == Activity.RESULT_OK) {
				refresh();
			}
		}
	}

	/**
	 * @param parent
	 * @param view
	 * @param position
	 * @param id
	 */
	public void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {

		// find and remember item
		final EventListItem item = adapter.getItem(position);

		if (item.isHeader()) {
			return;
		}

		// prepare timer if we want to program
		prepareTimer(item);

		// show details
		final Intent intent = new Intent();
		intent.setClass(this, EpgDetailsActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		if (highlight != null) {
			intent.putExtra(Intents.HIGHLIGHT, highlight);
		}
		startActivity(intent);
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

	public void svdrpEvent(final SvdrpEvent event, final Epg result) {

		if (progress != null) {
			progress.svdrpEvent(event);
		}

		switch (event) {
		case CONNECTING:
			break;
		case CONNECT_ERROR:
		case FINISHED_ABNORMALY:
		case LOGIN_ERROR:
			switchNoConnection();
			break;
		case FINISHED_SUCCESS:
			finishedSuccess();
			break;
		case RESULT_RECEIVED:
			break;
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int index = savedInstanceState.getInt("INDEX");
		int top = savedInstanceState.getInt("TOP");
		listView.setSelectionFromTop(index, top);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		int index = listView.getFirstVisiblePosition();
		View v = listView.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();
		outState.putInt("INDEX", index);
		outState.putInt("TOP", top);
		super.onSaveInstanceState(outState);
	}

	protected abstract void finishedSuccess();

	public boolean onSearchRequested() {
		InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMgr.toggleSoftInput(0, 0);
		return true;
	}

	protected void startSearchManager() {
		Bundle appData = new Bundle();
		startSearch(highlight, false, appData, false);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		this.detector.onTouchEvent(me);
		return super.dispatchTouchEvent(me);
	}

	public void onSwipe(int direction) {

	}

	public void onDoubleTap() {
		// TODO Auto-generated method stub

	}

}
