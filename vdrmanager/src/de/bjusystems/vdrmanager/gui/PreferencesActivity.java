package de.bjusystems.vdrmanager.gui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Preferences;

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

		this.getPreferenceManager().setSharedPreferencesName(
				Preferences.getPreferenceFile(this));
		this.addPreferencesFromResource(R.xml.preferences);

		updateChildPreferences();

		findPreference(getString(R.string.wakeup_wol_mac_key))
				.setOnPreferenceClickListener(this);

	}

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

	private void enableWolPreferences() {
		findPreference(getString(R.string.wakeup_wol_mac_key)).setEnabled(true);
		findPreference(getString(R.string.wakeup_wol_custom_broadcast_key))
		.setEnabled(true);
	}

	private void disableWolPreferences() {
		findPreference(getString(R.string.wakeup_wol_mac_key))
				.setEnabled(false);
		findPreference(getString(R.string.wakeup_wol_custom_broadcast_key))
		.setEnabled(false);

	}

	private void disableWakeupUrlPreferences() {
		findPreference(getString(R.string.wakeup_url_key)).setEnabled(false);
		findPreference(getString(R.string.wakeup_password_key)).setEnabled(
				false);
		findPreference(getString(R.string.wakeup_user_key)).setEnabled(false);
	}

	private void enableWakeupUrlPrefenreces() {
		findPreference(getString(R.string.wakeup_url_key)).setEnabled(true);
		findPreference(getString(R.string.wakeup_password_key))
				.setEnabled(true);
		findPreference(getString(R.string.wakeup_user_key)).setEnabled(true);
	}

	private void updateChildPreferences() {
		SharedPreferences sp = Preferences.getSharedPreferences(this);
		String wakup = sp.getString(getString(R.string.wakeup_method_key),
				"wol");

		if (wakup.equals("url")) {
			disableWolPreferences();
			enableWakeupUrlPrefenreces();
		} else {// remote url
			disableWakeupUrlPreferences();
			enableWolPreferences();
		}

		for (String key : sp.getAll().keySet()) {
			Preference p = findPreference(key);
			updateSummary(p);
		}

		
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
