package de.bjusystems.vdrmanager.gui;

import java.util.Calendar;

import android.os.Bundle;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Event;
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

	/**
	 * 
	 */
	TimerClient timerClient;

	
	/* (non-Javadoc)
	 * @see de.bjusystems.vdrmanager.gui.BaseEventListActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Attach view
//		setContentView(getMainLayout());

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
			switchNoConnection();
			return;
		}		

		// get timer client
		timerClient = new TimerClient();

		// create backgound task
		final SvdrpAsyncTask<Timer, SvdrpClient<Timer>> task = new SvdrpAsyncTask<Timer, SvdrpClient<Timer>>(
				timerClient);

		// create progress dialog
		progress = new SvdrpProgressDialog(this, timerClient);

		// attach listener
		task.addListener(this);

		// start task
		task.run();
	}

	/* (non-Javadoc)
	 * @see de.bjusystems.vdrmanager.gui.BaseTimerEditActivity#getTimer(de.bjusystems.vdrmanager.data.EventListItem)
	 */
	@Override
	protected Timer getTimer(EventListItem item) {
		return (Timer)item.getEvent();
	}
	
	
	/* (non-Javadoc)
	 * @see de.bjusystems.vdrmanager.gui.BaseEventListActivity#prepareTimer(de.bjusystems.vdrmanager.data.EventListItem)
	 */
	protected void prepareDetailsViewData(final EventListItem item) {
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		// remember event for details view and timer things
		app.setCurrentEvent(item.getEvent());
		app.setCurrentEpgList(results);
	}

	protected boolean finishedSuccessImpl() {
		adapter.clear();
		sortItemsByTime(results);
		for(Event e : results) {
		//results.add(e);
		Calendar cal = Calendar.getInstance();
		int day = -1;
			cal.setTime(e.getStart());
			int eday = cal.get(Calendar.DAY_OF_YEAR);
			if (eday != day) {
				day = eday;
				adapter.add(new EventListItem(new DateFormatter(cal)
						.getDailyHeader()));
			}
			adapter.add(new EventListItem(e));
		}
		listView.setSelectionAfterHeaderView();
		dismiss(progress);
		return results.isEmpty() == false;
	}

	@Override
	protected int getWindowTitle() {
		return R.string.action_menu_timers;
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

}
