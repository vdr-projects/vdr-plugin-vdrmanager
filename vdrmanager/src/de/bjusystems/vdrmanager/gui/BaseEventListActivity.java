package de.bjusystems.vdrmanager.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import de.bjusystems.vdrmanager.data.P;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.gui.SimpleGestureFilter.SimpleGestureListener;
import de.bjusystems.vdrmanager.tasks.VoidAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

/**
 * @author lado
 *
 */
public abstract class BaseEventListActivity<T extends Event> extends
BaseActivity<T, ListView> implements OnItemClickListener,
SimpleGestureListener {

  public static final String TAG = BaseEventListActivity.class.getName();

  public static final int MENU_GROUP_SHARE = 90;

  public static final int MENU_SHARE = 90;

  public static final int MENU_GROUP_TO_CAL = 91;

  public static final int MENU_TO_CAL = 91;

  private SimpleGestureFilter detector;

  protected EventAdapter adapter;

  protected String highlight = null;

  protected Date lastUpdate = null;

  protected static final Date FUTURE = new Date(Long.MAX_VALUE);

  // private static final Date BEGIN = new Date(0);

  protected Channel currentChannel = null;

  //protected List<T> results = new ArrayList<T>();

  AlertDialog sortByDialog = null;

  public static final int MENU_GROUP_DEFAULT = 0;

  public static final int MENU_GROUP_ALPHABET = 1;

  protected int sortBy;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    sortBy = Preferences.get(this, getViewID() + "_"
        + P.EPG_LAST_SORT, MENU_GROUP_DEFAULT);
    // Attach view
    setContentView(getMainLayout());
    setTitle(getWindowTitle());
    initFlipper();
    detector = new SimpleGestureFilter(this, this);

    initChannel();
  }

  private void initChannel() {
    currentChannel = getApp().getCurrentChannel();
    // currentChannel = getIntent()
    // .getParcelableExtra(Intents.CURRENT_CHANNEL);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (notifyDataSetChangedOnResume()) {
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
  public boolean onCreateOptionsMenu(
      final com.actionbarsherlock.view.Menu menu) {
    super.onCreateOptionsMenu(menu);
    final com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
    inflater.inflate(R.menu.epg_list_menu, menu);
    return true;
  }

  /**
   * Prepare the current event and the chained events for
   *
   * @param event
   */
  protected void prepareDetailsViewData(final EventListItem event) {

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

    final int itemId = item.getItemId();

    switch (itemId) {

    case R.id.epg_item_menu_live_tv: {
      Utils.stream(this, event);
      break;
    }


    case MENU_SHARE: {
      Utils.shareEvent(this, event);
      break;
    }

    case MENU_TO_CAL: {
      Utils.addCalendarEvent(this, event);
      break;
    }

    case R.id.epg_item_menu_switchto: {
      Utils.switchTo(this, event.getChannelId(), event.getChannelName());
      break;
    }

    default:
      return super.onContextItemSelected(item);
    }

    return true;
  }



  protected int getAvailableSortByEntries() {
    return 0;
  }

  protected void fillAdapter() {

  }

  /*
   * (non-Javadoc)
   *
   * @see
   * de.bjusystems.vdrmanager.gui.BaseActivity#onOptionsItemSelected(android
   * .view.MenuItem)
   */
  public boolean onOptionsItemSelected(
      final com.actionbarsherlock.view.MenuItem item) {

    switch (item.getItemId()) {

    case R.id.epg_list_sort_menu: {

      if (sortByDialog == null) {
        sortByDialog = new AlertDialog.Builder(this)
        .setTitle(R.string.sort)
        .setIcon(android.R.drawable.ic_menu_sort_alphabetically)
        .setSingleChoiceItems(getAvailableSortByEntries(),
            sortBy, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(final DialogInterface dialog,
              final int which) {

            if (sortBy == which) {
              sortByDialog.dismiss();
              return;
            }

            sortBy = which;

            new VoidAsyncTask() {

              @Override
              protected Void doInBackground(
                  final Void... params) {
                Preferences
                .set(BaseEventListActivity.this,
                    getViewID()
                    + "_"
                    + P.EPG_LAST_SORT,
                    sortBy);
                return null;
              }
            }.execute();

            sortByDialog.dismiss();
            fillAdapter();
          }

        }).create();
      }

      sortByDialog.show();

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

    if (item.isHeader()) {
      return;
    }

    final MenuItem mi = menu.findItem(R.id.epg_item_menu_live_tv);
    if (item.isLive() && item.getStreamId() != null) {

      mi.setVisible(true);

    } else {

      mi.setVisible(false);
    }
    menu.add(MENU_GROUP_SHARE, MENU_SHARE, 0, R.string.share);
    menu.add(MENU_GROUP_TO_CAL, MENU_TO_CAL, 0, R.string.addtocal);
    super.onCreateContextMenu(menu, v, menuInfo);

  }

  /**
   * @param parent
   * @param view
   * @param position
   * @param id
   */
  @Override
  public void onItemClick(final AdapterView<?> parent, final View view,
      final int position, final long id) {

    // find and remember item
    final EventListItem item = adapter.getItem(position);

    if (item.isHeader()) {
      return;
    }

    prepareDetailsViewData(item);

    // show details
    final Intent intent = new Intent(this, EpgDetailsActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    if (highlight != null) {
      intent.putExtra(Intents.HIGHLIGHT, highlight);
    }
    startActivityForResult(intent,
        TimerDetailsActivity.REQUEST_CODE_TIMER_MODIFIED);
  }

  protected boolean notifyDataSetChangedOnResume() {
    return true;
  }

  @Override
  protected void onPause() {
    super.onPause();
    // if (epgClient != null) {
    // epgClient.abort();
    // }
    // if (progress != null) {
    // progress.dismiss();
    // progress = null;
    // }
  }

  //	protected void resultReceived(T result) {
  //		results.add(result);
  //	}

  @Override
  protected void onRestoreInstanceState(final Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    final int index = savedInstanceState.getInt("INDEX");
    final int top = savedInstanceState.getInt("TOP");
    listView.setSelectionFromTop(index, top);
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState) {
    final int index = listView.getFirstVisiblePosition();
    final View v = listView.getChildAt(0);
    final int top = (v == null) ? 0 : v.getTop();
    outState.putInt("INDEX", index);
    outState.putInt("TOP", top);
    super.onSaveInstanceState(outState);
  }

  protected void dismiss(final AlertDialog dialog) {
    if (dialog == null) {
      return;
    }
    dialog.dismiss();
  }

  public boolean onSearchRequested() {
    final InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    inputMgr.toggleSoftInput(0, 0);
    return true;
  }

  protected void startSearchManager() {
    final Bundle appData = new Bundle();
    startSearch(highlight, false, appData, false);
  }

  @Override
  public boolean dispatchTouchEvent(final MotionEvent me) {
    this.detector.onTouchEvent(me);
    return super.dispatchTouchEvent(me);
  }

  @Override
  public void onSwipe(final int direction) {

  }

  @Override
  public void onDoubleTap() {

  }

  protected void sortItemsByChannel(final List<Event> result) {
    final Comparator<Event> comparator = new Comparator<Event>() {

      @Override
      public int compare(final Event item1, final Event item2) {
        return item1.getChannelNumber().compareTo(
            item2.getChannelNumber());
      }
    };
    Collections.sort(result, comparator);
  }

  protected void sortItemsByTime(final List<T> result) {
    sortItemsByTime(result, false);
  }

  protected void sortItemsByTime(final List<T> result, final boolean reverse) {
    Collections.sort(result, new TimeAndChannelComparator(reverse));
  }

  @Override
  public void svdrpException(final SvdrpException exception) {
    Log.w(TAG, exception);
    alert(getString(R.string.vdr_error_text, exception.getMessage()));
  }

  abstract protected boolean finishedSuccessImpl(List<T> results);

  protected String getViewID(){
    return this.getClass().getSimpleName();
  }

  protected void pushResultCountToTitle(){
    setTitle(getString(R.string.epg_window_title_count, getWindowTitle(),
        getCACHE().size()));
  }


  @Override
  synchronized protected final boolean finishedSuccess(final List<T> results) {
    //ProgressDialog dialog = new ProgressDialog(this);
    //dialog.setMessage("Loading");
    //dialog.show();
    try {
      lastUpdate = new Date();
      final boolean r = finishedSuccessImpl(results);
      if(r == false){
        adapter.clear();
        adapter.notifyDataSetChanged();
      }
      return r;
    } finally {
      //			dialog.dismiss();
      //results.clear();
    }
  }

  @Override
  protected boolean displayingResults() {
    return getCACHE().isEmpty() == false;
  }

  class TitleComparator implements Comparator<Event> {

    @Override
    public int compare(final Event lhs, final Event rhs) {
      if (lhs == null || lhs.getTitle() == null) {
        return 1;
      }
      if (rhs == null || rhs.getTitle() == null) {
        return 0;
      }
      return lhs.getTitle().compareToIgnoreCase(rhs.getTitle());
    }
  };

  class TimeAndChannelComparator implements Comparator<Event> {
    boolean r = false;

    TimeAndChannelComparator() {
      this(false);
    }
    TimeAndChannelComparator(final boolean r) {
      this.r = r;
    }

    @Override
    public int compare(final Event item1, final Event item2) {

      final int c = item1.getStart().compareTo(item2.getStart());
      if (c != 0) {
        if (r == false) {
          return c;
        }
        return -1 * c;
      }
      if (item1.getChannelNumber() == null
          && item2.getChannelNumber() == null) {
        return 0;
      }
      if (item1.getChannelNumber() == null) {
        return 1;
      }
      if (item2.getChannelNumber() == null) {
        return -1;
      }
      return item1.getChannelNumber().compareTo(item2.getChannelNumber());
    }
  }


  class TimeComparator implements Comparator<Event> {
    boolean r = false;

    TimeComparator(final boolean r) {
      this.r = r;
    }

    @Override
    public int compare(final Event item1, final Event item2) {

      final int c = item1.getStart().compareTo(item2.getStart());
      if (c == 0) {
        return c;
      }
      if (r == false) {
        return c;
      }
      return -1 * c;
    }
  }

  class ChannelComparator implements Comparator<Event> {

    @Override
    public int compare(final Event item1, final Event item2) {

      if (item1.getChannelNumber() == null
          && item2.getChannelNumber() == null) {
        return 0;
      }
      if (item1.getChannelNumber() == null) {
        return 1;
      }
      if (item2.getChannelNumber() == null) {
        return -1;
      }
      return item1.getChannelNumber().compareTo(item2.getChannelNumber());
    }
  }



  public void clearCache() {
    getCACHE().clear();
  }

  protected abstract List<T> getCACHE();

  //	@Override
  //	protected void connected() {
  //		super.connected();
  //		results.clear();
  //	}

}
