package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EpgSearchParams;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.P;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.data.db.EPGSearchSuggestionsProvider;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;

/**
 * This class is used for showing what's current running on all channels
 *
 * @author bju
 */
public class EpgSearchListActivity extends BaseTimerEditActivity<Epg> implements
OnItemClickListener {

  protected static ArrayList<Epg> CACHE = new ArrayList<Epg>();

  @Override
  protected List<Epg> getCACHE() {
    return CACHE;
  }

  private void initSearch(final Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      final String query = intent.getStringExtra(SearchManager.QUERY);
      if (TextUtils.isEmpty(query) == false) {
        highlight = query.trim();
        final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
            this, EPGSearchSuggestionsProvider.AUTHORITY,
            EPGSearchSuggestionsProvider.MODE);
        suggestions.saveRecentQuery(query, null);
      }
    }
  }

  @Override
  protected void onNewIntent(final Intent intent) {
    initSearch(intent);
    startSearch();
  }

  private void startSearch() {
    startEpgQuery();
  }

  @Override
  protected String getViewID(){
    return this.getClass().getSimpleName();
  }


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    Preferences.setLocale(this);
    // Preferences.init(this);

    super.onCreate(savedInstanceState);

    sortBy = Preferences.get(this, getViewID() + "_"
        + P.EPG_LAST_SORT, MENU_GROUP_DEFAULT);


    final Intent intent = getIntent();
    initSearch(intent);
    adapter = new TimeEventAdapter(this);

    // Create adapter for EPG list
    adapter.setHideDescription(false);
    listView = (ListView) findViewById(R.id.whatson_list);
    listView.setAdapter(adapter);
    listView.setTextFilterEnabled(true);
    registerForContextMenu(listView);
    // register EPG item click
    listView.setOnItemClickListener(this);
    startSearch();
  }

  public void onNothingSelected(final AdapterView<?> arg0) {
    // startTimeEpgQuery(((EpgTimeSpinnerValue)timeSpinner.getAdapter().getItem(0)).getValue());
  }

  //

  private void startEpgQuery() {

    if (checkInternetConnection() == false) {
      return;
    }

    final EpgSearchParams sp = new EpgSearchParams();
    sp.setTitle(highlight);
    setTitle(getWindowTitle());
    final EpgClient epgClient = new EpgClient(sp, getCertificateProblemDialog());
    // remove old listeners
    // epgClient.clearSvdrpListener();

    // create background task
    final SvdrpAsyncTask<Epg, SvdrpClient<Epg>> task = new SvdrpAsyncTask<Epg, SvdrpClient<Epg>>(
        epgClient);

    // create progress
    addListener(task);

    // start task
    task.run();
  }

  protected void sort() {
    /* */
    switch (sortBy) {
    case MENU_GROUP_DEFAULT: {
      //Collections.sort(CACHE, getTimeComparator(false));
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
  protected int getBaseMenu() {
    return R.menu.refresh_menu;
  }

  @Override
  protected synchronized void fillAdapter() {

    adapter.highlight = this.highlight;

    adapter.clear();

    if(CACHE.isEmpty()){
      return;
    }

    final Calendar cal = Calendar.getInstance();
    int day = -1;
    for (final Event e : CACHE) {
      cal.setTime(e.getStart());
      final int eday = cal.get(Calendar.DAY_OF_YEAR);
      if (eday != day) {
        day = eday;
        adapter.add(new EventListItem(new DateFormatter(cal)
        .getDailyHeader()));
      }
      adapter.add(new EventListItem(e));
    }
    adapter.notifyDataSetChanged();
  }


  @Override
  protected int getAvailableSortByEntries() {
    return R.array.epg_sort_by_time_alpha;
  }


  /*
   * (non-Javadoc) TODO this method also should be used in startEpgQuery on
   * cache hit
   *
   * @see de.bjusystems.vdrmanager.gui.BaseEpgListActivity#finishedSuccess()
   */
  @Override
  protected boolean finishedSuccessImpl(final List<Epg> results) {

    clearCache();
    for(final Epg e : results){
      CACHE.add(e);
    }
    pushResultCountToTitle();
    fillAdapter();
    listView.setSelectionAfterHeaderView();
    return adapter.getCount() > 0;
  }

  @Override
  protected void prepareDetailsViewData(final EventListItem item) {
    final VdrManagerApp app = (VdrManagerApp) getApplication();
    app.setCurrentEvent(item.getEvent());
    app.setCurrentEpgList(CACHE);
  }

  @Override
  protected int getMainLayout() {
    return R.layout.search_epg_list;
  }

  @Override
  protected void refresh() {
    startEpgQuery();
  }

  @Override
  protected void retry() {
    startEpgQuery();
  }

  @Override
  public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {
    // MenuItem item;
    // item = menu.add(MENU_GROUP_NEW_TIMER, MENU_NEW_TIMER, 0,
    // R.string.new_timer);
    // item.setIcon(android.R.drawable.ic_menu_add);;
    // /item.setAlphabeticShortcut('r');

    final com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
    inflater.inflate(R.menu.epg_search_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if(item.getItemId() == R.id.epg_search){
      startSearchManager();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected String getWindowTitle() {
    if (TextUtils.isEmpty(highlight)) {
      return getString(R.string.epg_by_search);
    }

    return getString(R.string.epg_by_search_param, highlight);
  }

  //@Override
  //public boolean onSearchRequested() {
  //startSearchManager();
  //return true;
  //}

  @Override
  protected int getListNavigationIndex() {
    return -1;
  }

  @Override
  protected boolean hasListNavigation() {
    return false;
  }

  @Override
  protected void timerModified(final Timer timer) {
    clearCache();
    super.timerModified(timer);
  }

}
