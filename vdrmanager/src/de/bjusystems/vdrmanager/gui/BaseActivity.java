package de.bjusystems.vdrmanager.gui;

import android.app.Activity;
import android.app.AlertDialog;
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
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

public abstract class BaseActivity<T extends ListView> extends Activity
		implements OnClickListener {

	public static final int MENU_GROUP_REFRESH = 99;

	public static final int MENU_REFRESH = 99;

	protected T listView;
	
	protected ViewFlipper flipper;

	protected SvdrpProgressDialog progress;

	abstract protected int getMainLayout();

	public void svdrpException(final SvdrpException exception) {
		// Log.w(TAG, exception);
		alert(getString(R.string.vdr_error_text, exception.getMessage()));
	}

	protected void switchNoConnection() {
		View view = findViewById(R.id.main_content);
		if (view != null) {
			view.setVisibility(View.GONE);
		}
		view = findViewById(R.id.no_connection_layout);
		if (view != null) {
			view.setVisibility(View.VISIBLE);
		}
		Button b = (Button) findViewById(R.id.retry_button);
		b.setOnClickListener(this);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.retry_button) {
			View view = findViewById(R.id.no_connection_layout);
			if (view != null) {
				view.setVisibility(View.GONE);
			}
			view = findViewById(R.id.main_content);
			if (view != null) {
				view.setVisibility(View.VISIBLE);
			}
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
		item.setIcon(android.R.drawable.ic_menu_rotate);
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

	protected void setAsCurrent(Channel channel) {
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

}
