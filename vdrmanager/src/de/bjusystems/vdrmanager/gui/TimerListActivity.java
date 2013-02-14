package de.bjusystems.vdrmanager.gui;

import java.util.Calendar;
import java.util.Comparator;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.TimerClient;

/**
 * This class is used for showing all the existing timers
 *
 * @author bju
 */
public class TimerListActivity extends BaseTimerEditActivity<Timer> implements
		OnItemClickListener, SvdrpAsyncListener<Timer> {

	private static final int MENU_NEW_TIMER = 2;

	private static final int MENU_GROUP_NEW_TIMER = 2;
	/**
	 *
	 */
	TimerClient timerClient;

	@Override
	protected SvdrpClient<Timer> getClient() {
		return this.timerClient;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.bjusystems.vdrmanager.gui.BaseEventListActivity#onCreate(android.os
	 * .Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Attach view
		// setContentView(getMainLayout());

		// create an adapter
		adapter = new TimeEventAdapter(this);

		// attach adapter to ListView
		listView = (ListView) findViewById(R.id.timer_list);
		listView.setAdapter(adapter);
		listView.setFastScrollEnabled(true);
		listView.setTextFilterEnabled(true);

		// set click listener
		listView.setOnItemClickListener(this);

		// context menu wanted
		registerForContextMenu(listView);

		// start query
		startTimerQuery();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void startTimerQuery() {

		if (checkInternetConnection() == false) {
			return;
		}

		// get timer client
		timerClient = new TimerClient();

		// create backgound task
		final SvdrpAsyncTask<Timer, SvdrpClient<Timer>> task = new SvdrpAsyncTask<Timer, SvdrpClient<Timer>>(
				timerClient);

		// create progress dialog
		// progress = new SvdrpProgressDialog(this, timerClient);

		// attach listener
		// task.addListener(progress);
		task.addListener(this);

		// start task
		task.run();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.bjusystems.vdrmanager.gui.BaseTimerEditActivity#getTimer(de.bjusystems
	 * .vdrmanager.data.EventListItem)
	 */
	@Override
	protected Timer getTimer(EventListItem item) {
		return (Timer) item.getEvent();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.bjusystems.vdrmanager.gui.BaseEventListActivity#prepareTimer(de.bjusystems
	 * .vdrmanager.data.EventListItem)
	 */
	protected void prepareDetailsViewData(final EventListItem item) {
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		// remember event for details view and timer things
		app.setCurrentEvent(item.getEvent());
		app.setCurrentEpgList(results);
	}

	protected Comparator<Timer> getTimeComparator(boolean reverse) {
		return new BaseEventComparator(reverse) {
			@Override
			public int compare(Timer item1, Timer item2) {
				if (item1.isRecurring()) {
					return 1;
				}
				if (item2.isRecurring()) {
					return -1;
				}
				return super.compare(item1, item2);
			}

		};
	}

	protected boolean finishedSuccessImpl() {
		adapter.clear();
		sortItemsByTime(results);
		int day = -1;
		Calendar cal = Calendar.getInstance();
		for (Timer e : results) {
			if (e.isRecurring()) {
				adapter.add(new EventListItem(e.getWeekdays()));
			} else {
				cal.setTime(e.getStart());
				int eday = cal.get(Calendar.DAY_OF_YEAR);
				if (eday != day) {
					day = eday;
					adapter.add(new EventListItem(new DateFormatter(cal)
							.getDailyHeader()));
				}
			}
			adapter.add(new EventListItem(e));
		}
		listView.setSelectionAfterHeaderView();
		return results.isEmpty() == false;
	}

	@Override
	protected boolean notifyDataSetChangedOnResume() {
		return true;
	}

	@Override
	protected String getWindowTitle() {
		return getString(R.string.action_menu_timers);
	}

	@Override
	protected int getMainLayout() {
		return R.layout.timer_list;
	}

	@Override
	protected void refresh() {
		startTimerQuery();
	}

	@Override
	protected void retry() {
		refresh();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * de.bjusystems.vdrmanager.gui.BaseActivity#onOptionsItemSelected(android
	 * .view.MenuItem)
	 */
	public boolean onOptionsItemSelected(final com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case R.id.timer_menu_add:
			say("Comming soon...");
			return true;
		}

		// switch (item.getItemId()) {
		// case R.id.epg_menu_search:
		// startSearchManager();
		// super.onSearchRequested();
		// break;
		// case R.id.epg_menu_times:
		// intent = new Intent();
		// /intent.setClass(this, EpgSearchTimesListActivity.class);
		// startActivity(intent);
		// break;
		// }
		return super.onOptionsItemSelected(item);
	}

	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {
		// MenuItem item;
		// item = menu.add(MENU_GROUP_NEW_TIMER, MENU_NEW_TIMER, 0,
		// R.string.new_timer);
		// item.setIcon(android.R.drawable.ic_menu_add);;
		// /item.setAlphabeticShortcut('r');

		final com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.timer_list_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

}
