package de.bjusystems.vdrmanager.gui;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Cache;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpExceptionListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpFinishedListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpListener;

public abstract class BaseActivity<Result, T extends ListView> extends
		ICSBaseActivity implements OnClickListener, SvdrpListener,
		SvdrpExceptionListener, SvdrpFinishedListener<Result>, Cache {

	public static final String TAG = BaseActivity.class.getName();

	public static final int MENU_GROUP_REFRESH = 99;

	public static final int MENU_REFRESH = 99;

	protected T listView;

	protected ViewFlipper flipper;

	private Button retry;

	private ProgressDialog progress;


	// protected SvdrpProgressDialog progress;

	abstract protected String getWindowTitle();

	abstract protected int getMainLayout();

	protected void noInternetConnection() {
		alert(R.string.no_internet_connection);
	}

	abstract protected boolean displayingResults();

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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Preferences.setLocale(this);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		Preferences.setLocale(this);
		// Preferences.init(this);
		super.onResume();
	}

	protected void initFlipper() {
		this.flipper = (ViewFlipper) findViewById(R.id.flipper);
		retry = (Button) findViewById(R.id.retry_button);
		retry.setOnClickListener(this);
	}

	public void onClick(View v) {
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

	protected boolean hasListNavigation() {
		return true;
	}

	protected void initListNavigation() {

		if (hasListNavigation() == false) {
			return;
		}

		getSupportActionBar().setDisplayShowTitleEnabled(false);

		getSupportActionBar().setNavigationMode(
				getSupportActionBar().NAVIGATION_MODE_LIST);

		ArrayAdapter<CharSequence> mSpinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.navigation_array,
						android.R.layout.simple_spinner_dropdown_item);

		getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter,
				new OnNavigationListener() {

					private boolean firstHit = true;

					@Override
					public boolean onNavigationItemSelected(int itemPosition,
							long itemId) {

						if (firstHit == true) {
							firstHit = false;
							return false;
						}
						switch (itemPosition) {

						case LIST_NAVIGATION_CHANNELS: {
							startActivity(ChannelListActivity.class);
							return true;
						}
						case LIST_NAVIGATION_EPG_BY_TIME: {
							startActivity(TimeEpgListActivity.class);
							return true;
						}

						case LIST_NAVIGATION_EPG_BY_CHANNEL: {
							startActivity(EventEpgListActivity.class);
							return true;
						}

						case LIST_NAVIGATION_RECORDINGS: {
							startActivity(RecordingListActivity.class);
							return true;
						}

						case LIST_NAVIGATION_TIMERS: {
							startActivity(TimerListActivity.class);
							return true;
						}

						}
						return false;
					}
				});
		getSupportActionBar().setSelectedNavigationItem(
				getListNavigationIndex());

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Preferences.setLocale(this);
		progress = new ProgressDialog(this);


		getApp().addActivityToFinish(this);

		initActionBar();

		initListNavigation();

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

	public void startActivity(Class<?> clazz) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClass(this, clazz);
		startActivity(intent);
		finish();
	}

	protected int getBaseMenu() {
		return R.menu.refresh_filter_menu;
	}

	@Override
	public boolean onCreateOptionsMenu(
			final com.actionbarsherlock.view.Menu menu) {

		// MenuItem item;
		// item = menu.add(MENU_GROUP_REFRESH, MENU_REFRESH, 0,
		// R.string.refresh);
		// item.setIcon(R.drawable.ic_menu_refresh);
		// item.setAlphabeticShortcut('r');
		com.actionbarsherlock.view.MenuInflater inf = getSupportMenuInflater();
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
	public boolean onOptionsItemSelected(
			final com.actionbarsherlock.view.MenuItem item) {
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
			Intent intent = new Intent(this, VdrManagerActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return false;
		}
	}

	protected void setCurrent(Channel channel) {
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

	protected void say(int res) {
		say(this.getString(res));
	}

	protected void say(String msg) {
		Utils.say(this, msg);
	}

	protected boolean noConnection(SvdrpEvent event) {
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

	protected void alert(String msg) {
		if (isFinishing()) {
			return;
		}
		new AlertDialog.Builder(this)//
				.setMessage(msg)//
				.setPositiveButton(android.R.string.ok, null)//
				.create()//
				.show();//
	}

	protected void alert(int resId) {
		alert(getString(resId));
	}

	protected void restoreViewSelection() {
		listView.setSelectionFromTop(index, top);
	}

	protected void backupViewSelection() {
		index = listView.getFirstVisiblePosition();
		View v = listView.getChildAt(0);
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
	public void svdrpEvent(SvdrpEvent event, Throwable t) {
		Utils.say(this, t.getLocalizedMessage());
	}

	protected void addListener(SvdrpAsyncTask<Result, SvdrpClient<Result>> task) {
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

	private void setMessage(int progressConnect) {
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
	public void finished(List<Result> results) {
		if (finishedSuccess(results)) {
			finishedSuccess = true;
			restoreViewSelection();
		} else {
			say(R.string.epg_no_items);
		}
	}

}
