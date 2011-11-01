package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.gui.SimpleGestureFilter.SimpleGestureListener;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

/**
 * @author lado
 * 
 */
public abstract class BaseEventListActivity<T extends Event> extends BaseActivity<T, ListView> implements OnItemClickListener,
		 SimpleGestureListener {

	public static final String TAG = BaseEventListActivity.class.getName();

	public static final int MENU_GROUP_SHARE = 90;

	public static final int MENU_SHARE = 90;

	private SimpleGestureFilter detector;

	protected EpgClient epgClient;

	protected EventAdapter adapter;

	protected String highlight = null;

	protected static final Date FUTURE = new Date(Long.MAX_VALUE);
	

	// private static final Date BEGIN = new Date(0);

	protected Channel currentChannel = null;

	abstract protected int getWindowTitle();

	protected List<Event> results = new ArrayList<Event>();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Attach view
		setContentView(getMainLayout());
		setTitle(getWindowTitle());
		initFlipper();
		detector = new SimpleGestureFilter(this, this);
		
		initChannel();
	}

	private void initChannel() {
		currentChannel = getApp().getCurrentChannel();
		//currentChannel = getIntent()
			//	.getParcelableExtra(Intents.CURRENT_CHANNEL);
	}

	private boolean refreshViewOnResume = false;

	@Override
	protected void onResume() {
		super.onResume();
		if (refreshViewOnResume) {
			adapter.notifyDataSetChanged();
		}
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

		int itemId = item.getItemId();

		switch (itemId) {

		case R.id.epg_item_menu_live_tv: {
			Utils.stream(this, event);
			break;
		}
		case MENU_SHARE: {
			Utils.shareEvent(this, event);
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
	 * @see
	 * de.bjusystems.vdrmanager.gui.BaseActivity#onOptionsItemSelected(android
	 * .view.MenuItem)
	 */
	public boolean onOptionsItemSelected(final MenuItem item) {

		Intent intent;

		switch (item.getItemId()) {
//		case R.id.epg_menu_search:
			// startSearchManager();
	//		super.onSearchRequested();
		//	break;
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

		// if (v.getId() == R.id.whatson_list) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		// set menu title
		final EventListItem item = adapter.getItem(info.position);

		MenuItem mi = menu.findItem(R.id.epg_item_menu_live_tv);
		if (item.isLive()) {
			mi.setVisible(true);
		} else {
			mi.setVisible(false);
		}
		menu.add(MENU_GROUP_SHARE, MENU_SHARE, 0, R.string.share);
		super.onCreateContextMenu(menu, v, menuInfo);

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
		refreshViewOnResume = true;
		// if (epgClient != null) {
		// epgClient.abort();
		// }
		// if (progress != null) {
		// progress.dismiss();
		// progress = null;
		// }
	}


	protected void resultReceived(Event result) {
		results.add(result);
	}

	@Override
	protected void connected() {
		if(flipper != null){
			flipper.setDisplayedChild(0);
		}
		results.clear();
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

	protected void dismiss(AlertDialog dialog) {
		if (dialog == null) {
			return;
		}
		dialog.dismiss();
	}


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

	protected void sortItemsByTime(List<Event> result) {
		final Comparator<Event> comparator = new Comparator<Event>() {

			public int compare(final Event item1, final Event item2) {
				int c = item1.getStart().compareTo(item2.getStart());
				if (c != 0) {
					return c;
				}
				return Integer.valueOf(item1.getChannelNumber()).compareTo(
						Integer.valueOf(item2.getChannelNumber()));
			}
		};
		Collections.sort(result, comparator);
	}

	public void svdrpException(final SvdrpException exception) {
		Log.w(TAG, exception);
		alert(getString(R.string.vdr_error_text, exception.getMessage()));
	}

}
