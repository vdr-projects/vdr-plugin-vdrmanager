package de.bjusystems.vdrmanager.gui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ViewFlipper;

import java.util.List;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.remote.RemoteActivity;
import de.bjusystems.vdrmanager.utils.VdrManagerExceptionHandler;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpExceptionListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpFinishedListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpListener;

public abstract class BaseActivity<Result, T extends ListView> extends
        AppCompatActivity implements OnClickListener, SvdrpListener,
        SvdrpExceptionListener, SvdrpFinishedListener<Result> {

    public static final String TAG = BaseActivity.class.getName();

    public static final int MENU_GROUP_REFRESH = 99;

    public static final int MENU_REFRESH = 99;

    protected T listView;

    protected ViewFlipper flipper;

    private Button retry;

    private ProgressDialog progress;

    protected Preferences getPrefs() {
        return Preferences.get();
    }

    // protected SvdrpProgressDialog progress;

    private CharSequence mDrawerTitle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mTitles;

    abstract protected String getWindowTitle();

    abstract protected int getMainLayout();

    protected void noInternetConnection() {
        alert(R.string.no_internet_connection);
    }

    abstract protected boolean displayingResults();

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    protected boolean isForceRefresh() {
        if (forceRefresh == false) {
            return false;
        }
        forceRefresh = false;
        return true;
    }

    protected boolean forceRefresh = false;

    protected void switchNoConnection() {
        if (flipper == null) {
            say(R.string.no_connection);
            return;
        }

        if (displayingResults()) {
            say(R.string.no_connection);
        } else {
            flipper.setDisplayedChild(1);
        }
    }

    protected CertificateProblemDialog getCertificateProblemDialog() {
        return new CertificateProblemDialog(this);
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        Preferences.setLocale(this);
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        Preferences.init(this);
        super.onResume();
    }

    protected void initFlipper() {
        this.flipper = (ViewFlipper) findViewById(R.id.flipper);
        retry = (Button) findViewById(R.id.retry_button);
        retry.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.retry_button) {
            retry();
        }
    }

    //
    // protected void updateWindowTitle(int topic, int subtopic) {
    // String title;
    // title = getString(topic);
    // if (subtopic != -1) {
    // title += " > " + getString(subtopic);
    // }
    // setTitle(title);
    // }
    //
    // protected void updateWindowTitle(String topic, String subtopic) {
    // String title = topic;
    // if (subtopic != null) {
    // title += " > " + subtopic;
    // }
    // setTitle(title);
    // }

    abstract protected int getListNavigationIndex();

    public static final int LIST_NAVIGATION_CHANNELS = 0;
    public static final int LIST_NAVIGATION_EPG_BY_TIME = 1;
    public static final int LIST_NAVIGATION_EPG_BY_CHANNEL = 2;
    public static final int LIST_NAVIGATION_RECORDINGS = 3;
    public static final int LIST_NAVIGATION_TIMERS = 4;
    public static final int LIST_NAVIGATION_REMOTE = 5;

    protected boolean hasListNavigation() {
        return true;
    }

    protected void initListNavigation() {

        if (hasListNavigation() == false) {
            return;
        }

        // getSupportActionBar().setDisplayShowTitleEnabled(false);
        //
        // getSupportActionBar().setNavigationMode(
        // getSupportActionBar().NAVIGATION_MODE_LIST);
        //
        // final ArrayAdapter<CharSequence> mSpinnerAdapter = ArrayAdapter
        // .createFromResource(this, R.array.navigation_array,
        // android.R.layout.simple_spinner_dropdown_item);
        //
        // getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter,
        // new OnNavigationListener() {
        //
        // private boolean firstHit = true;
        //
        // @Override
        // public boolean onNavigationItemSelected(
        // final int itemPosition, final long itemId) {
        //
        // if (firstHit == true) {
        // firstHit = false;
        // return false;
        // }
        // switch (itemPosition) {
        //
        // case LIST_NAVIGATION_CHANNELS: {
        // startActivity(ChannelListActivity.class);
        // return true;
        // }
        // case LIST_NAVIGATION_EPG_BY_TIME: {
        // startActivity(TimeEpgListActivity.class);
        // return true;
        // }
        //
        // case LIST_NAVIGATION_EPG_BY_CHANNEL: {
        // startActivity(EventEpgListActivity.class);
        // return true;
        // }
        //
        // case LIST_NAVIGATION_RECORDINGS: {
        // startActivity(RecordingListActivity.class);
        // return true;
        // }
        //
        // case LIST_NAVIGATION_TIMERS: {
        // startActivity(TimerListActivity.class);
        // return true;
        // }
        //
        // }
        // return false;
        // }
        // });
        // getSupportActionBar().setSelectedNavigationItem(
        // getListNavigationIndex());

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(VdrManagerExceptionHandler.get(this,
                Thread.getDefaultUncaughtExceptionHandler()));
        Preferences.setLocale(this);
        setContentView(getMainLayout());
        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setCanceledOnTouchOutside(false);
        // progress.setOnCancelListener(new OnCancelListener() {
        // @Override
        // public void onCancel(DialogInterface dialog) {
        //
        // }
        // });

        initActionBar();

        initListNavigation();

        initLeftDrawer(savedInstanceState);

        // new OnNavigationListener() {
        // @Override
        // public boolean onNavigationItemSelected(int itemPosition, long
        // itemId) {
        // System.err.println("itemPosition: "+ itemPosition +", itemId:" +
        // itemId);
        // rturn false;
        // }
        // });

        // your logic for click listner
        // setListenerForActionBarCustomView(actionBarView);

        // private void setListenerForActionBarCustomView(View actionBarView) {
        // ActionBarCustomViewOnClickListener actionBarCustomViewOnClickListener
        // = new ActionBarCustomViewOnClickListener();
        // actionBarView.findViewById(R.id.text_view_title).setOnClickListener(actionBarCustomViewOnClickListener);
        // }

    }

    protected void initLeftDrawer(final Bundle savedInstanceState) {

        mDrawerTitle = getTitle();

        mTitles = getResources().getStringArray(R.array.navigation_array);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                // getSupportActionBar().setTitle(mTitle);
                //invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                // getSupportActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

//		if (savedInstanceState == null) {
//			selectItem(0);
//		}

    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        // setTitle(mPlanetTitles[position]);

        switch (position) {

            case LIST_NAVIGATION_CHANNELS: {
                startActivity(ChannelListActivity.class);
                break;
            }
            case LIST_NAVIGATION_EPG_BY_TIME: {
                startActivity(TimeEpgListActivity.class);
                break;
            }

            case LIST_NAVIGATION_EPG_BY_CHANNEL: {
                startActivity(EventEpgListActivity.class);
                break;
            }

            case LIST_NAVIGATION_RECORDINGS: {
                startActivity(RecordingListActivity.class);
                break;
            }

            case LIST_NAVIGATION_TIMERS: {
                startActivity(TimerListActivity.class);
                break;
            }
            case LIST_NAVIGATION_REMOTE: {
                startActivity(RemoteActivity.class);
                break;
            }
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    protected void initActionBar() {
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public void startActivity(final Class<?> clazz) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(this, clazz);
        startActivity(intent);
        finish();
    }

    protected int getBaseMenu() {
        return R.menu.refresh_filter_menu;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        // MenuItem item;
        // item = menu.add(MENU_GROUP_REFRESH, MENU_REFRESH, 0,
        // R.string.refresh);
        // item.setIcon(R.drawable.ic_menu_refresh);
        // item.setAlphabeticShortcut('r');
        final MenuInflater inf = getMenuInflater();
        inf.inflate(getBaseMenu(), menu);

        // SearchView searchView = (SearchView)
        // menu.findItem(R.id.menu_search).getActionView();
        // searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    abstract protected void refresh();

    abstract protected void retry();

    // abstract protected SvdrpClient<Result> getClient();

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.list_refresh:
                backupViewSelection();
                refresh();
                return true;
            case R.id.list_filter: {
                onSearchRequested();
                return true;
            }
            case android.R.id.home:
                final Intent intent = new Intent(this, VdrManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    protected void setCurrent(final Channel channel) {
        getApp().setCurrentChannel(channel);
    }

    protected VdrManagerApp getApp() {
        final VdrManagerApp app = (VdrManagerApp) getApplication();
        return app;
    }

    // protected Channel getCurrentChannel(){
    // final Channel channel = ((VdrManagerApp) getApplication())
    // .getCurrentChannel();
    // return channel;
    // }

    protected void say(final int res) {
        say(this.getString(res));
    }

    protected void say(final String msg) {
        Utils.say(this, msg);
    }

    protected boolean noConnection(final SvdrpEvent event) {
        switch (event) {
            case CONNECTION_TIMEOUT:
                say(R.string.progress_connect_timeout);
                switchNoConnection();
            case CONNECT_ERROR:
                say(R.string.progress_connect_error);
                switchNoConnection();
                break;
            case FINISHED_ABNORMALY:
                alert(R.string.progress_connect_finished_abnormal);
                switchNoConnection();
                break;
            case LOGIN_ERROR:
                say(R.string.progress_login_error);
                switchNoConnection();
                break;
            default:
                return false;
        }
        return true;
    }

    protected void alert(final String msg) {
        if (isFinishing()) {
            return;
        }
        new AlertDialog.Builder(this)//
                .setMessage(msg)//
                .setPositiveButton(android.R.string.ok, null)//
                .create()//
                .show();//
    }

    protected void alert(final int resId) {
        alert(getString(resId));
    }

    protected void restoreViewSelection() {
        listView.setSelectionFromTop(index, top);
    }

    protected void backupViewSelection() {
        index = listView.getFirstVisiblePosition();
        final View v = listView.getChildAt(0);
        top = (v == null) ? 0 : v.getTop();
    }

    int index;
    int top;

    protected boolean checkInternetConnection() {
        if (Utils.checkInternetConnection(this)) {
            return true;
        }
        noInternetConnection();
        return false;
    }

    // public void svdrpEvent(Result result) {
    // resultReceived(result);
    // }

    @Override
    public void svdrpEvent(final SvdrpEvent event, final Throwable t) {
        progress.dismiss();
        Utils.say(this, t.getMessage());
    }

    protected void addListener(
            final SvdrpAsyncTask<Result, SvdrpClient<Result>> task) {
        task.addSvdrpExceptionListener(this);
        task.addSvdrpListener(this);
        task.addSvdrpFinishedListener(this);
    }

    @Override
    public void svdrpEvent(final SvdrpEvent event) {

        switch (event) {
            case LOGIN:
                break;
            case COMMAND_SENDING:
                break;
            case CONNECTING:
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                setMessage(R.string.progress_connect);
                if (!isFinishing()) {
                    progress.show();
                }
                break;
            case LOGGED_IN:
                setMessage(R.string.progress_login);
                break;
            case COMMAND_SENT:
                setMessage(getProgressTextId());
                break;
            case DISCONNECTING:
                setMessage(R.string.progress_disconnect);
                break;
            case DISCONNECTED:
                break;
            case ABORTED:
                progress.dismiss();
                say(R.string.aborted);
                break;
            case ERROR:
                progress.dismiss();
                alert(R.string.epg_client_errors);
                break;
            case CONNECTED:
                connected();
                break;
            case CONNECTION_TIMEOUT:
            case CONNECT_ERROR:
            case FINISHED_ABNORMALY:
            case LOGIN_ERROR:
                progress.dismiss();
                noConnection(event);
                break;
            case CACHE_HIT:
                progress.dismiss();
                cacheHit();
                return;
            case FINISHED_SUCCESS:
                progress.dismiss();
                break;
        }
        // case RESULT_RECEIVED:
        // resultReceived(result);
        // break;
        // }
    }

    protected int getProgressTextId() {
        return R.string.progress_loading;
    }

    private void setMessage(final int progressConnect) {
        progress.setMessage(getString(progressConnect));
    }

    protected boolean finishedSuccess = false;

    protected void cacheHit() {

    }

    /**
     * @return false, if no results found
     */
    protected abstract boolean finishedSuccess(List<Result> results);

    // /**
    // * @param result
    // */
    // protected abstract void resultReceived(Result result);

    protected void connected() {
        if (flipper != null) {
            flipper.setDisplayedChild(0);
        }
    }

    public void svdrpException(final SvdrpException exception) {
        // svdrpException(exception);
        // Log.w(TAG, exception);
        alert(getString(R.string.vdr_error_text, exception.getMessage()));
    }

    @Override
    protected void onDestroy() {
        if (progress.isShowing()) {
            progress.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public void finished(final List<Result> results) {
        if (finishedSuccess(results)) {
            finishedSuccess = true;
            restoreViewSelection();
        } else {
            say(R.string.epg_no_items);
        }
    }

}
