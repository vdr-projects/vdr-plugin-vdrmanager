package de.bjusystems.vdrmanager.gui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

public abstract class BaseActivity<Result, T extends ListView> extends ICSBaseActivity
		implements OnClickListener, SvdrpAsyncListener<Result>, Cache {

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

	public void reset() {

	}

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


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Preferences.setLocale(this);
		progress = new ProgressDialog(this);
		getApp().addActivityToFinish(this);
		
		initActionBar();

		// your logic for click listner
		// setListenerForActionBarCustomView(actionBarView);

		// private void setListenerForActionBarCustomView(View actionBarView) {
		// ActionBarCustomViewOnClickListener actionBarCustomViewOnClickListener
		// = new ActionBarCustomViewOnClickListener();
		// actionBarView.findViewById(R.id.text_view_title).setOnClickListener(actionBarCustomViewOnClickListener);
		// }

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {

		// MenuItem item;
		// item = menu.add(MENU_GROUP_REFRESH, MENU_REFRESH, 0,
		// R.string.refresh);
		// item.setIcon(R.drawable.ic_menu_refresh);
		// item.setAlphabeticShortcut('r');
		MenuInflater inf = getMenuInflater();
		inf.inflate(R.menu.refresh_menu, menu);

		// SearchView searchView = (SearchView)
		// menu.findItem(R.id.menu_search).getActionView();
		// searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return true;
	}

	abstract protected void refresh();

	abstract protected void retry();

	abstract protected SvdrpClient<Result> getClient();

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.list_refresh:
			backupViewSelection();
			refresh();
			return true;
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
		if(isFinishing()){
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

	public void svdrpEvent(final SvdrpEvent event, final Result result) {

		switch (event) {
		case LOGIN:
			break;
		case COMMAND_SENDING:
			break;
		case CONNECTING:
			progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			setMessage(R.string.progress_connect);
			progress.show();
			break;
		case LOGGED_IN:
			setMessage(R.string.progress_login);
			break;
		case COMMAND_SENT:
			setMessage(getClient().getProgressTextId());
			break;
		case DISCONNECTING:
			setMessage(R.string.progress_disconnect);
			break;
		case DISCONNECTED:
			break;
		case ABORTED:
			say(R.string.aborted);
			break;
		case ERROR:
			alert(R.string.epg_client_errors);
			progress.dismiss();
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
			cacheHit();
			progress.dismiss();
			return;
		case FINISHED_SUCCESS:

			if (finishedSuccess()) {
				finishedSuccess = true;
				restoreViewSelection();
			} else {
				say(R.string.epg_no_items);
			}
			progress.dismiss();
			break;
		case RESULT_RECEIVED:
			resultReceived(result);
			break;
		}
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
	protected abstract boolean finishedSuccess();

	/**
	 * @param result
	 */
	protected abstract void resultReceived(Result result);

	protected void connected() {

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

}
