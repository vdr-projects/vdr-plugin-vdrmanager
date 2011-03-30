package de.bjusystems.vdrmanager.gui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Preferences;

public class PreferencesActivity extends PreferenceActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.getPreferenceManager().setSharedPreferencesName(Preferences.getPreferenceFile(this));
		this.addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onBackPressed() {

		// finish this activity
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		app.addActivityToFinish(this);
		app.finishActivities();

		// restart main activity because
		// the buttons needs refreshing
		final Intent intent = new Intent();
		intent.setClass(this, VdrManagerActivity.class);
		startActivity(intent);
	}
}
