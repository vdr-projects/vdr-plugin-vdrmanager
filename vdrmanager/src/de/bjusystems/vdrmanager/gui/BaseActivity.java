package de.bjusystems.vdrmanager.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.view.Menu;
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
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

public abstract class BaseActivity<Result, T extends ListView> extends Activity
		implements OnClickListener, SvdrpAsyncListener<Result> {
	
	public static final String TAG = BaseActivity.class.getName();

	public static final int MENU_GROUP_REFRESH = 99;

	public static final int MENU_REFRESH = 99;

	protected T listView;

	protected ViewFlipper flipper;

	private Button retry;
	
	protected SvdrpProgressDialog progress;

	
	abstract protected int getMainLayout();

	
	protected void switchNoConnection() {
		if (flipper == null) {
			return;
		}
		flipper.setDisplayedChild(1);
	}

	protected void initFlipper() {
		this.flipper = (ViewFlipper) findViewById(R.id.flipper);
		retry =  (Button) findViewById(R.id.retry_button);
		retry.setOnClickListener(this);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.retry_button) {
			retry();
		}
	}

	protected void updateWindowTitle(int topic, int subtopic) {
		String title;
		title = getString(topic);
		if (subtopic != -1) {
			title += " > " + getString(subtopic);
		}
		setTitle(title);
	}

	protected void updateWindowTitle(String topic, String subtopic) {
		String title = topic;
		if (subtopic != null) {
			title += " > " + subtopic;
		}
		setTitle(title);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		MenuItem item;
		item = menu.add(MENU_GROUP_REFRESH, MENU_REFRESH, 0, R.string.refresh);
		item.setIcon(R.drawable.ic_menu_refresh);
		item.setAlphabeticShortcut('r');
		return true;
	}

	abstract protected void refresh();

	abstract protected void retry();

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			backupViewSelection();
			refresh();
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
		Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
	}

	protected void say(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	protected boolean noConnection(SvdrpEvent event) {
		switch (event) {
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
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// test for connection
		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {
			return true;
		}
		return false;
	}

	public void svdrpEvent(final SvdrpEvent event, final Result result) {

		switch (event) {

		case ABORTED:
			alert(R.string.aborted);
			break;
		case ERROR:
			alert(R.string.epg_client_errors);
			// say(R.string.epg_client_errors);
			// dismiss(progress);
			break;
		case CONNECTING:
			break;
		case CONNECTED:
			connected();
			break;
		case CONNECT_ERROR:
		case FINISHED_ABNORMALY:
		case LOGIN_ERROR:
			noConnection(event);
			break;
		case CACHE_HIT:
			cacheHit();
			return;
		case FINISHED_SUCCESS:
			if (finishedSuccess() == false) {
				say(R.string.epg_no_items);
			} else {
				restoreViewSelection();
			}
			break;
		case RESULT_RECEIVED:
			resultReceived(result);
			break;
		}
	}

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
		progress.svdrpException(exception);
		// Log.w(TAG, exception);
		alert(getString(R.string.vdr_error_text, exception.getMessage()));
	}

}
