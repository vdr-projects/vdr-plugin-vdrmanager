package de.bjusystems.vdrmanager.gui;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.j256.ormlite.android.AndroidDatabaseResults;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Vdr;
import de.bjusystems.vdrmanager.data.db.DBAccess;
import de.bjusystems.vdrmanager.data.db.EPGSearchSuggestionsProvider;
import de.bjusystems.vdrmanager.remote.RemoteActivity;
import de.bjusystems.vdrmanager.utils.wakeup.AsyncWakeupTask;

public class VdrManagerActivity extends ActionBarActivity implements
		OnClickListener, OnQueryTextListener {

	public static final String TAG = "VdrManagerActivity";

	public static final String VDR_PORTAL = "http://www.vdr-portal.de";

	private SearchView search;

	private View actionMenuWakup;
	private View actionMenuRemote;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Preferences.initVDR(this);

		// if(Preferences.get().getCurrentVdr() == null){
		// finish();
		// return;
		// } android.support.v7.appcompat.R

		if (Preferences.initVDR(this) == false) {
      final Intent intent = new Intent();
			intent.setClass(this, VdrListActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(Intents.EMPTY_CONFIG, Boolean.TRUE);
			startActivity(intent);
			Toast.makeText(this, R.string.no_vdr, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

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
		findViewById(R.id.action_menu_remote).setOnClickListener(this);
//		View v = findViewById(R.id.action_menu_search);
//		if (v != null) {
//			v.setOnClickListener(this);
//		}
		//findViewById(R.id.main_logo).setOnClickListener(this);
		actionMenuWakup = findViewById(R.id.action_menu_wakeup);
		actionMenuRemote = findViewById(R.id.action_menu_remote);
		// add and register buttons
		// createButtons();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		// search = new SearchView(getSupportActionBar().getThemedContext());
		search = (SearchView) MenuItemCompat.getActionView( menu.findItem(R.id.menu_search));

		// search = (SearchView)
		// .getActionView();
		//
		// Object o = menu.findItem(R.id.menu_search);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		search.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));

		//search.setOnQueryTextListener(this);
		return true;
	}

	@Override
	protected void onResume() {
		Preferences.setLocale(this);
		if (Preferences.get().isWakeupEnabled() == false) {
			actionMenuWakup.setVisibility(View.GONE);
			actionMenuWakup.setOnClickListener(null);
		} else {
			actionMenuWakup.setVisibility(View.VISIBLE);
			actionMenuWakup.setOnClickListener(this);
		}

		if(Preferences.get().isRemoteEnabled() == false){
			actionMenuRemote.setVisibility(View.GONE);
			actionMenuRemote.setOnClickListener(null);
		} else {
			actionMenuRemote.setVisibility(View.VISIBLE);
			actionMenuRemote.setOnClickListener(this);
		}


		super.onResume();
	}

	@Override
	public boolean onOptionsItemSelected(
			final MenuItem item) {

		switch (item.getItemId()) {
		case R.id.main_menu_preferences: {
			Intent intent = new Intent(this,  PreferencesActivity.class);
			int flags = Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_CLEAR_TOP;
			intent.setFlags(flags);
			startActivity(intent);
			finish();
			break;
		}
		case R.id.main_menu_info: {
			if(isFinishing()){
				break;
			}
			About.show(this);
			break;
		}
		case R.id.main_menu_exit: {
			finish();
			break;
		}

		case R.id.main_menu_clear_search: {
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
					this, EPGSearchSuggestionsProvider.AUTHORITY,
					EPGSearchSuggestionsProvider.MODE);
			suggestions.clearHistory();
			break;
		}

		// case R.id.menu_search: {
		// if(Build.VERSION.SDK_INT <11){
		// onSearchRequested();
		// }
		// break;
		// }
		case R.id.main_menu_goto: {
			try {
				final Cursor cursor = ((AndroidDatabaseResults) DBAccess
						.get(this).getVdrDAO().iterator().getRawResults())
						.getRawCursor();
				startManagingCursor(cursor);
				final AlertDialog ad = new AlertDialog.Builder(this)
						.setSingleChoiceItems(cursor, findVdrCursor(cursor),
								"name", new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										cursor.moveToPosition(which);
										int id = cursor.getInt(cursor
												.getColumnIndex("_id"));
										Vdr vdr = DBAccess
												.get(VdrManagerActivity.this)
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
											Intent intent = getIntent();
											overridePendingTransition(0, 0);
											intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
											finish();

											overridePendingTransition(0, 0);
											startActivity(intent);
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
      finish();
    } else {
			super.onBackPressed();
		}

    try {
      // reassign a new and empty key store
      ((VdrManagerApp)getApplication()).initSessionKeyStore();
    } catch (final Exception e) {
      Log.e(getClass().getName(), "Can't clear session key store");
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
//		case R.id.action_menu_search:
//			onSearchRequested();
//			break;
		case R.id.action_menu_wakeup:
			final AsyncWakeupTask wakeupTask = new AsyncWakeupTask(this);
			wakeupTask.execute();
			break;
		case R.id.main_logo:
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(VDR_PORTAL));
			startActivity(i);
			break;

		case R.id.action_menu_remote:
			startActivity(RemoteActivity.class);
			break;

		}

	}

	protected void startSearchManager() {
		Bundle appData = new Bundle();
		startSearch(null, false, appData, false);
	}

	@Override
	public boolean onSearchRequested() {
		search.setVisibility(View.VISIBLE);
		// Bundle appData = new Bundle();
		// appData.putBoolean(SearchableActivity.JARGON, true);
		// startSearch(null, false, appData, false);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		return false;
	}
}
