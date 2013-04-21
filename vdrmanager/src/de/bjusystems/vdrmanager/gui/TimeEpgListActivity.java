package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EpgSearchTimeValue;
import de.bjusystems.vdrmanager.data.EpgSearchTimeValues;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;

/**
 * This class is used for showing what's current running on all channels
 *
 * @author bju
 */
public class TimeEpgListActivity extends BaseTimerEditActivity<Epg> implements
OnItemClickListener, OnItemSelectedListener, OnTimeSetListener {

  protected Spinner timeSpinner;

  protected View switcher;

  protected View clock;

  ArrayAdapter<EpgSearchTimeValue> timeSpinnerAdapter;

  protected static Date nextForceCache = null;

  private static String cachedTime = null;

  int selectedIndex = 0;

  protected static ArrayList<Epg> CACHE = new ArrayList<Epg>();

  @Override
  public int getProgressTextId() {
    return R.string.progress_whatson_loading;
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // create adapter for time spinner
    timeSpinnerAdapter = new ArrayAdapter<EpgSearchTimeValue>(this,
        android.R.layout.simple_spinner_item);
    timeSpinnerAdapter
    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    timeSpinner = (Spinner) findViewById(R.id.epg_list_time_spinner);
    timeSpinner.setAdapter(timeSpinnerAdapter);

    switcher = findViewById(R.id.switch_epg_view);
    switcher.setOnClickListener(this);

    clock = findViewById(R.id.epg_list_times);
    clock.setOnClickListener(this);

    // update gui
    adapter = new TimeEventAdapter(this);
    // searchLabel.setVisibility(View.GONE);
    timeSpinner.setOnItemSelectedListener(this);

    fillTimeSpinnerValues();

    // Create adapter for EPG list
    listView = (ListView) findViewById(R.id.whatson_list);
    listView.setFastScrollEnabled(true);
    listView.setTextFilterEnabled(true);

    listView.setAdapter(adapter);
    registerForContextMenu(listView);

    // register EPG item click
    listView.setOnItemClickListener(this);

  }

  private void fillTimeSpinnerValues() {
    final EpgSearchTimeValues values = new EpgSearchTimeValues(this);
    timeSpinnerAdapter.clear();
    for (final EpgSearchTimeValue value : values.getValues()) {
      timeSpinnerAdapter.add(value);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();

  }

  @Override
  public void onClick(final View view) {
    if (view == switcher) {
      final Intent intent = new Intent();
      intent.setClass(this, EventEpgListActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
          | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      startActivity(intent);
      finish();
    } else if (view == clock) {
      final Intent intent = new Intent();
      intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
          | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.setClass(this, EpgSearchTimesListActivity.class);
      startActivity(intent);
    } else {
      super.onClick(view);
    }
  }

  @Override
  public void onTimeSet(final TimePicker view, final int hourOfDay,
      final int minute) {
    final String tm = String.format("%02d:%02d", hourOfDay, minute);

    // timeSpinnerAdapter.add(time);
    final EpgSearchTimeValues values = new EpgSearchTimeValues(this);
    final List<EpgSearchTimeValue> vs = values.getValues();
    final EpgSearchTimeValue time = new EpgSearchTimeValue(3, tm);
    vs.add(vs.size() - 1, time);
    timeSpinnerAdapter.clear();
    int select = -1;
    int counter = 0;
    for (final EpgSearchTimeValue value : vs) {
      timeSpinnerAdapter.add(value);
      if (select == -1 && value.getText().equals(tm)) {
        select = counter;
      }
      counter++;
    }
    timeSpinner.setSelection(select);
    setTitle(resolveWindowTitle());
    // update search
    startEpgQuery(time.getValue(), false);
  }

  private String resolveWindowTitle() {
    if (timeSpinner == null) {
      return getString(R.string.epg_by_time);
    }
    final EpgSearchTimeValue v = (EpgSearchTimeValue) timeSpinner
        .getSelectedItem();
    if (v == null) {
      return getString(R.string.epg_by_time);
    }
    return getString(R.string.epg_by_time_args, v.getText());
  }

  @Override
  public void onItemSelected(final AdapterView<?> parent, final View view,
      final int position, final long id) {

    // get spinner value
    final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner
        .getSelectedItem();

    if (selection.getValue().equals("adhoc")) {
      final Calendar cal = Calendar.getInstance(TimeZone.getDefault());
      // show time selection
      final TimePickerDialog dialog = new TimePickerDialog(this, this,
          cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
          Preferences.get().isUse24hFormat());
      //			dialog.setOnDismissListener(new OnDismissListener() {

      //			@Override
      //		public void onDismiss(DialogInterface dialog) {


      //	}
      //});

      dialog.show();
    } else {
      // update search
      setTitle(getString(R.string.epg_by_time_args, selection.getText()));
      startEpgQuery(selection.getValue(), false);
    }
  }

  @Override
  public void onNothingSelected(final AdapterView<?> arg0) {
    // startTimeEpgQuery(((EpgTimeSpinnerValue)timeSpinner.getAdapter().getItem(0)).getValue());
  }

  @Override
  public void clearCache() {
    super.clearCache();
    cachedTime = null;
  }

  private boolean useCache(final String time) {

    if (cachedTime == null) {
      return false;
    }

    if (cachedTime.equals(time) == false) {
      return false;
    }

    if (nextForceCache == null) {
      return false;
    }
    final Date now = new Date();
    if (nextForceCache.before(now)) {
      return false;
    }
    return true;
  }

  private void startEpgQuery(final String time, final boolean force) {

    if (useCache(time) && !force) {
      fillAdapter();
      return;
    }

    if (checkInternetConnection() == false) {
      return;
    }

    final EpgClient epgClient = new EpgClient(time, getCertificateProblemDialog());

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

  @Override
  protected synchronized void fillAdapter() {

    adapter.clear();

    if (CACHE.isEmpty()) {
      return;
    }

    sort();
    listView.setFastScrollEnabled(false);
    adapter.add(new EventListItem(
        new DateFormatter(CACHE.get(0).getStart()).getDailyHeader()));

    for (final Event e : CACHE) {
      adapter.add(new EventListItem(e));
    }
    adapter.notifyDataSetChanged();
    listView.setFastScrollEnabled(true);
  }

  void sort() {
    if (sortBy == BaseEventListActivity.MENU_GROUP_ALPHABET) {
      Collections.sort(CACHE, new TitleComparator());
    } else if (sortBy == BaseEventListActivity.MENU_GROUP_DEFAULT) {
      Collections.sort(CACHE, new ChannelComparator());
    }
  }

  @Override
  protected int getAvailableSortByEntries() {
    return R.array.epg_sort_by_channels_alpha;
  }

  @Override
  protected String getViewID() {
    return TimeEpgListActivity.class.getSimpleName();
  }

  @Override
  protected boolean finishedSuccessImpl(final List<Epg> results) {
    clearCache();

    if (results.isEmpty()) {
      return false;
    }

    // get spinner value
    final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner
        .getSelectedItem();
    nextForceCache = FUTURE;
    cachedTime = selection.getValue();
    final Date now = new Date();

    // adapter.add(new EventListItem(new DateFormatter(results.get(0)
    // .getStart()).getDailyHeader()));

    for (final Epg e : results) {
      CACHE.add(e);
      if (e.getStop().before(nextForceCache) && e.getStop().after(now)) {
        nextForceCache = e.getStop();
      }
    }
    fillAdapter();
    pushResultCountToTitle();
    listView.setSelectionAfterHeaderView();
    return results.isEmpty() == false;

  }

  @Override
  protected void prepareDetailsViewData(final EventListItem item) {
    final VdrManagerApp app = (VdrManagerApp) getApplication();

    // remember event for details view and timer things
    app.setCurrentEvent(item.getEvent());
    app.setCurrentEpgList(CACHE);
  }

  @Override
  protected int getMainLayout() {
    return R.layout.time_epg_list;
  }

  @Override
  protected void refresh() {
    // get spi
    final EpgSearchTimeValue selection = (EpgSearchTimeValue) timeSpinner
        .getSelectedItem();
    // update search
    startEpgQuery(selection.getValue(), true);
  }

  @Override
  protected void retry() {
    refresh();
  }

  @Override
  protected String getWindowTitle() {
    return resolveWindowTitle();
  }

  private void nextEvent() {
    final int pos = timeSpinner.getSelectedItemPosition();
    if (pos + 1 >= timeSpinnerAdapter.getCount()) {
      say(R.string.navigae_at_the_end);
      return;
    }
    timeSpinner.setSelection(pos + 1, true);
  }

  private void prevEvent() {
    final int pos = timeSpinner.getSelectedItemPosition();
    if (pos <= 0) {
      say(R.string.navigae_at_the_start);
      return;
    }
    timeSpinner.setSelection(pos - 1, true);
  }

  @Override
  public void onSwipe(final int direction) {
    switch (direction) {
    case SimpleGestureFilter.SWIPE_RIGHT:
      prevEvent();
      break;
    case SimpleGestureFilter.SWIPE_LEFT:
      nextEvent();
      break;
    }
  }

  @Override
  protected int getListNavigationIndex() {
    return LIST_NAVIGATION_EPG_BY_TIME;
  }

  @Override
  protected List<Epg> getCACHE() {
    return CACHE;
  }

  @Override
  protected void timerModified(final Timer timer) {
    clearCache();
    super.timerModified(timer);
  }

}
