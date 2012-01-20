package de.bjusystems.vdrmanager.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SearchView;
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Vdr;
import de.bjusystems.vdrmanager.utils.wakeup.AsyncWakeupTask;

public class VdrManagerActivity extends Activity implements OnClickListener {

	public static final String TAG = "VdrManagerActivity";

	public static final String VDR_PORTAL = "http://www.vdr-portal.de";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Preferences.setLocale(this);
		// this.getActionBar().setDisplayShowCustomEnabled(true);
		// this.getActionBar().setDisplayShowTitleEnabled(false);
		// setTitle(getString(R.string.app_name));
		// attach view
		setContentView(R.layout.vdrmanager);

		// Preferences.loadPreferences(this);

		findViewById(R.id.action_menu_channels).setOnClickListener(this);
		findViewById(R.id.action_menu_recordings).setOnClickListener(this);
		findViewById(R.id.action_menu_timers).setOnClickListener(this);
		findViewById(R.id.action_menu_epg).setOnClickListener(this);
		findViewById(R.id.action_menu_search).setOnClickListener(this);
		findViewById(R.id.main_logo).setOnClickListener(this);
		if (Preferences.get().isWakeupEnabled() == false) {
			findViewById(R.id.action_menu_wakeup).setVisibility(View.GONE);
		} else {
			findViewById(R.id.action_menu_wakeup).setOnClickListener(this);
		}

		// add and register buttons
		// createButtons();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		int api = Build.VERSION.SDK_INT; 
		if ( api >= 11){
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false); // Do not iconify the widget;

		} 
		return true;
	}

	@Override
	protected void onResume() {
		Preferences.setLocale(this);
		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
		case R.id.main_menu_preferences: {
			// remember activity for finishing
			final VdrManagerApp app = (VdrManagerApp) getApplication();
			app.clearActivitiesToFinish();
			app.addActivityToFinish(this);

			Intent intent = new Intent();
			intent.setClass(this, PreferencesActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.main_menu_info: {
			About.show(this);
			break;
		}
		case R.id.main_menu_exit: {
			finish();
			break;
		}
		case R.id.menu_search: {
			if(Build.VERSION.SDK_INT <11){ 
				onSearchRequested();
			}
			break;
		}
		case R.id.main_menu_goto: {
			try {
				final Cursor cursor = Preferences.getDatabaseHelper()
						.getVdrCursor();
				startManagingCursor(cursor);
				final AlertDialog ad = new AlertDialog.Builder(this)
						.setSingleChoiceItems(cursor, findVdrCursor(cursor),
								"name", new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										cursor.moveToPosition(which);
										int id = cursor.getInt(cursor
												.getColumnIndex("_id"));
										Vdr vdr = Preferences
												.getDatabaseHelper()
												.getVdrDAO().queryForId(id);
										if (vdr == null) {
											Toast.makeText(
													VdrManagerActivity.this,
													R.string.main_menu_goto_no_vdr,
													Toast.LENGTH_SHORT).show();
										} else {
											Preferences.setCurrentVdr(
													VdrManagerActivity.this,
													vdr);
											Toast.makeText(
													VdrManagerActivity.this,
													getString(
															R.string.main_menu_switched_to,
															vdr.getName()),
													Toast.LENGTH_SHORT).show();
											((VdrManagerApp) getApplication())
													.finishActivities();
										}
										dialog.dismiss();
									}
								})//
						.setTitle(R.string.main_menu_goto_title)//
						.create();
				ad.show();

			} catch (Exception ex) {
				Log.w(TAG, ex);
			}

			break;
		}
		}
		return true;
	}

	private int findVdrCursor(Cursor c) {
		if (Preferences.get().getCurrentVdr() == null) {
			return -1;
		}

		int cid = Preferences.get().getCurrentVdr().getId();

		int position = 0;
		c.moveToPosition(-1);
		while (c.moveToNext()) {
			if (c.getInt(c.getColumnIndex("_id")) == cid) {
				break;
			}
			position++;
		}
		return position;
	}

	@Override
	public void onBackPressed() {
		if (Preferences.get().isQuiteOnBackButton()) {
			super.onBackPressed();
		}
	}

	public void startActivity(Class<?> clazz) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setClass(this, clazz);
		startActivity(intent);
	}

	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.action_menu_channels:
			startActivity(ChannelListActivity.class);
			break;
		case R.id.action_menu_recordings:
			startActivity(RecordingListActivity.class);
			break;
		case R.id.action_menu_timers:
			startActivity(TimerListActivity.class);
			break;
		case R.id.action_menu_epg:
			startActivity(TimeEpgListActivity.class);
			break;
		case R.id.action_menu_search:
			onSearchRequested();
			break;
		case R.id.action_menu_wakeup:
			final AsyncWakeupTask wakeupTask = new AsyncWakeupTask(this);
			wakeupTask.execute();
			break;
		case R.id.main_logo:
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(VDR_PORTAL));
			startActivity(i);
			break;
		}

	}

	@Override
	public boolean onSearchRequested() {
		Bundle appData = new Bundle();
		// appData.putBoolean(SearchableActivity.JARGON, true);
		startSearch(null, false, appData, false);
		return true;
	}
}
