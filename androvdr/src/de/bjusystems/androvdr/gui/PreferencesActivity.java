package de.bjusystems.androvdr.gui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.bjusystems.androvdr.app.AndroVdrApp;
import de.bjusystems.androvdr.data.Preferences;
import de.bjusystems.androvdr.R;

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
		final AndroVdrApp app = (AndroVdrApp) getApplication();
		app.addActivityToFinish(this);
		app.finishActivities();

		// restart main activity because
		// the buttons needs refreshing
		final Intent intent = new Intent();
		intent.setClass(this, AndroVdrActivity.class);
		startActivity(intent);
	}
}
