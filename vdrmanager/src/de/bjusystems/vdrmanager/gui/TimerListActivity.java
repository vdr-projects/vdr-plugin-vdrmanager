package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Bundle;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.TimerClient;

/**
 * This class is used for showing all the existing timers
 *
 * @author bju
 */
public class TimerListActivity extends BaseTimerEditActivity<Timer> implements
OnItemClickListener {

  private static final int MENU_NEW_TIMER = 2;

  private static final int MENU_GROUP_NEW_TIMER = 2;

  protected static ArrayList<Timer> CACHE = new ArrayList<Timer>();

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
    adapter = new TimerEventAdapter(this);

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
    final TimerClient timerClient = new TimerClient(getCertificateProblemDialog());

    // create backgound task
    final SvdrpAsyncTask<Timer, SvdrpClient<Timer>> task = new SvdrpAsyncTask<Timer, SvdrpClient<Timer>>(
        timerClient);

    // create progress dialog
    // progress = new SvdrpProgressDialog(this, timerClient);

    // attach listener
    // task.addListener(progress);
    addListener(task);

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
  protected Timer getTimer(final EventListItem item) {
    return (Timer) item.getEvent();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.bjusystems.vdrmanager.gui.BaseEventListActivity#prepareTimer(de.bjusystems
   * .vdrmanager.data.EventListItem)
   */
  @Override
  protected int prepareDetailsViewData(final EventListItem item, int position) {
    final VdrManagerApp app = (VdrManagerApp) getApplication();
    // remember event for details view and timer things
    app.setCurrentEvent(item.getEvent());
    app.setCurrentEpgList(CACHE);
	for(int i = 0; i < position; ++i){
		if(CACHE.get(i) == item.getEvent()){
			return i;
		}
	}

	return 0;
  }

  protected Comparator<Timer> getTimeComparator(final boolean reverse) {
    return new Comparator<Timer>() {
      TimeAndChannelComparator c = new TimeAndChannelComparator();
      @Override
      public int compare(final Timer item1, final Timer item2) {
        if (item1.isRecurring()) {
        	if(item2.isRecurring()){
        		return 0;
        	}
          return 1;
        }
        if (item2.isRecurring()) {
          return -1;
        }
        return c.compare(item1, item2);
      }

    };
  }



  @Override
  protected boolean finishedSuccessImpl(final List<Timer> results) {
    clearCache();
    for(final Timer r :results){
      CACHE.add(r);
    }
    pushResultCountToTitle();
    fillAdapter();
    return adapter.isEmpty() == false;

  }


  protected void sort() {
    /* */
    switch (sortBy) {
    case MENU_GROUP_DEFAULT: {
      Collections.sort(CACHE, getTimeComparator(false));
      break;
    }
    case MENU_GROUP_ALPHABET: {
      Collections.sort(CACHE, new TitleComparator());
      break;
    }
    //case MENU_GROUP_CHANNEL: {
    //sortItemsByChannel(results);
    //}
    }
  }


  @Override
  protected void fillAdapter() {

    adapter.clear();

    if (CACHE.isEmpty()) {
      return;
    }

    sort();

    int day = -1;
    final Calendar cal = Calendar.getInstance();

    for (final Timer e : CACHE) {
      if (e.isRecurring()) {
        adapter.add(new EventListItem(e.getWeekdays()));
      } else {
        cal.setTime(e.getStart());
        final int eday = cal.get(Calendar.DAY_OF_YEAR);
        if (eday != day) {
          day = eday;
          adapter.add(new EventListItem(new DateFormatter(cal)
          .getDailyHeader()));
        }
      }
      adapter.add(new EventListItem(e));
    }
    adapter.notifyDataSetChanged();
  }

  //	protected boolean finishedSuccessImpl() {
  //		adapter.clear();
  //		sortItemsByTime(results);
  //		int day = -1;
  //		Calendar cal = Calendar.getInstance();
  //		for (Timer e : results) {
  //			if (e.isRecurring()) {
  //				adapter.add(new EventListItem(e.getWeekdays()));
  //			} else {
  //				cal.setTime(e.getStart());
  //				int eday = cal.get(Calendar.DAY_OF_YEAR);
  //				if (eday != day) {
  //					day = eday;
  //					adapter.add(new EventListItem(new DateFormatter(cal)
  //							.getDailyHeader()));
  //				}
  //			}
  //			adapter.add(new EventListItem(e));
  //		}
  //		listView.setSelectionAfterHeaderView();
  //		return adapter.isEmpty() == false;
  //	}

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

  @Override
  protected int getAvailableSortByEntries() {
    return R.array.epg_sort_by_time_alpha;
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

  @Override
  protected int getListNavigationIndex() {
    return LIST_NAVIGATION_TIMERS;
  }

  @Override
  protected List<Timer> getCACHE() {
    return CACHE;
  }

}
