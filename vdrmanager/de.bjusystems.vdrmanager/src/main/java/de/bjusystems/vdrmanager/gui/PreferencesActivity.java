package de.bjusystems.vdrmanager.gui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.backup.BackupSettingsActivity;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.utils.VdrManagerExceptionHandler;

/**
 * The type Preferences activity.
 */
public class PreferencesActivity extends BasePreferencesActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceChangeListener,
		OnPreferenceClickListener {
//
//	Preference somePreference = findPreference(SOME_PREFERENCE_KEY);
//	PreferenceScreen preferenceScreen = getPreferenceScreen();
//	preferenceScreen.removePreference(somePreference);
//
//	you can later call:
//
//	preferenceScreen.addPreference(somePreference);
	private static final String TAG = "PreferencesActivity";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(VdrManagerExceptionHandler.get(this,
				Thread.getDefaultUncaughtExceptionHandler()));

		this.getPreferenceManager().setSharedPreferencesName(
				Preferences.getPreferenceFile(this));
		this.addPreferencesFromResource(R.xml.preferences);


	    Preference backupPreference = findPreference(getString(R.string.settings_backup_key));
	    backupPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
	        @Override
	      public boolean onPreferenceClick(Preference preference) {
	        Intent intent = new Intent(PreferencesActivity.this, BackupSettingsActivity.class);
	        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	        startActivity(intent);
	        return true;
	      }
	    });

		updateChildPreferences();
	}

	/**
	 * Gets shared preferences.
	 *
	 * @param context the context
	 * @return the shared preferences
	 */
// /** Return a properly configured SharedPreferences instance */
	public static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		Preferences.getSharedPreferences(this)
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		Preferences.getSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);

	}



	private void updateChildPreferences() {
		SharedPreferences sp = Preferences.getSharedPreferences(this);

		for (String key : sp.getAll().keySet()) {
			Preference p = findPreference(key);
			updateSummary(p);
		}


	}

	@Override
	public void onBackPressed() {
		//Preferences.getSharedPreferences(this)
		//.registerOnSharedPreferenceChangeListener(this);

		// finish this activity
		//Preferences.init(this);
		// restart main activity because
		// the buttons needs refreshing
		Intent intent = new Intent(this,  VdrManagerActivity.class);
		int flags = Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP;
		intent.setFlags(flags);
		startActivity(intent);
		finish();
	}

	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		updateChildPreferences();
		Preference p = findPreference(key);
		updateSummary(p);
		//Preferences.reset();
		Preferences.init(this);
		//Preferences.setLocale(getBaseContext());
	}

	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		return false;
	}

	public boolean onPreferenceClick(Preference arg0) {
		// if (arg0.getKey().equals(getString(R.string.wakeup_wol_mac_key))) {
		// // if(arg0.)
		// String host = Preferences.getPreferences().getSvdrpHost();
		//
		// if(host == null || host.length() == 0){
		// return true;
		// }
		//
		// try {
		// Socket echoSocket = new Socket(host, 7);
		// InetAddress ia = echoSocket.getInetAddress();
		//
		// } catch (Exception ex){
		// Log.w(TAG,ex);
		// }
		//
		// String mac = WakeOnLan.getMacFromArpCache(host);
		// System.err.println("mac");
		// }

		return true;
	}


}
