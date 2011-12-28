package de.bjusystems.vdrmanager.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.utils.wakeup.AsyncWakeupTask;

public class VdrManagerActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Preferences.setLocale(this);
		setTitle(getString(R.string.app_name));
		// attach view
		setContentView(R.layout.vdrmanager);

		
		// Preferences.loadPreferences(this);

		findViewById(R.id.action_menu_channels).setOnClickListener(this);
		findViewById(R.id.action_menu_recordings).setOnClickListener(this);
		findViewById(R.id.action_menu_timers).setOnClickListener(this);
		findViewById(R.id.action_menu_epg).setOnClickListener(this);
		findViewById(R.id.action_menu_search).setOnClickListener(this);

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
		case R.id.main_menu_exit:
			finish();
			break;
		}
		return true;
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
