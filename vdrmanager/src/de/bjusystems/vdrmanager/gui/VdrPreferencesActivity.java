package de.bjusystems.vdrmanager.gui;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Preferences;
import android.os.Bundle;

public class VdrPreferencesActivity extends BasePreferencesActivity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.getPreferenceManager().setSharedPreferencesName(
				Preferences.getPreferenceFile(this));
		this.addPreferencesFromResource(R.xml.vdr_prefs);

		
	}
}
