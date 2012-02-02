package de.bjusystems.vdrmanager.gui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Vdr;
import de.bjusystems.vdrmanager.data.VdrSharedPreferences;

public class VdrPreferencesActivity extends BasePreferencesActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	Vdr vdr;
	VdrSharedPreferences pref;

	int id = -1;

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		return this.pref;
	}

	@Override
	public Preference findPreference(CharSequence key) {
		return super.findPreference(key);
	}

	private void initVDR() {
		id = getIntent().getIntExtra(Intents.VDR_ID, -1);
		if (id == -1) {// new vdr
			vdr = new Vdr();
			pref = new VdrSharedPreferences();
			pref.instance = vdr;
		} else {// edit
			Vdr v = getHelper().getVdrDAO().queryForId(id);
			if (v != null) {
				vdr = v;
				pref = new VdrSharedPreferences(vdr);
			} else {
				vdr = new Vdr();
				pref = new VdrSharedPreferences();
				pref.instance = vdr;
				id = -1;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		initVDR();

		// this.getPreferenceManager().setSharedPreferencesName(Preferences.getPreferenceFile(this));

		pref.instance = vdr;
		pref.dao = getHelper().getVdrDAO();
		pref.registerOnSharedPreferenceChangeListener(this);

		this.addPreferencesFromResource(R.xml.vdr_prefs);

		updateChildPreferences();

		findPreference(getString(R.string.wakeup_wol_mac_key))
				.setOnPreferenceClickListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		updateChildPreferences();
		Preference p = findPreference(key);
		updateSummary(p);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		pref.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		pref.unregisterOnSharedPreferenceChangeListener(this);
		Preferences.reloadVDR();
	}

	private void enableWolPreferences() {
		Preference p = findPreference(getString(R.string.wakeup_wol_mac_key));
		if (p != null)

			p.setEnabled(true);
		p = findPreference(getString(R.string.wakeup_wol_custom_broadcast_key));
		if (p != null) {
			p.setEnabled(true);
		}
	}

	private void disableWolPreferences() {
		Preference p = findPreference(getString(R.string.wakeup_wol_mac_key));
		if (p != null)
			p.setEnabled(false);

		p = findPreference(getString(R.string.wakeup_wol_custom_broadcast_key));
		if (p != null)
			p.setEnabled(false);

	}

	private void disableWakeupUrlPreferences() {
		Preference p = findPreference(getString(R.string.wakeup_url_key));
		if (p != null) {
			p.setEnabled(false);
		}
		p = findPreference(getString(R.string.wakeup_password_key));
		if (p != null) {
			p.setEnabled(false);
		}

		p = findPreference(getString(R.string.wakeup_user_key));
		if (p != null) {
			p.setEnabled(false);
		}
	}

	private void enableWakeupUrlPrefenreces() {
		Preference p = findPreference(getString(R.string.wakeup_url_key));
		if (p != null) {
			p.setEnabled(true);
		}

		p = findPreference(getString(R.string.wakeup_password_key));
		if (p != null) {
			p.setEnabled(true);
		}
		p = findPreference(getString(R.string.wakeup_user_key));
		if (p != null) {
			p.setEnabled(true);
		}
	}

	private void updateChildPreferences() {
		String wakup = pref.getString(getString(R.string.wakeup_method_key),
				"wol");

		if (wakup.equals("url")) {
			disableWolPreferences();
			enableWakeupUrlPrefenreces();
		} else {// remote url
			disableWakeupUrlPreferences();
			enableWolPreferences();
		}

		for (String key : pref.getAll().keySet()) {
			Preference p = findPreference(key);
			updateSummary(p);
		}

	}

	public boolean onPreferenceClick(Preference arg0) {

		return false;
	}

	@Override
	public void onBackPressed() {
		if (id != -1) {// no new devices
			setResult(RESULT_OK);
			finish();
			return;
		}
		if (pref.commits < 2) {// user has not changed anything
			getHelper().getVdrDAO().delete(pref.instance);
			finish();
			return;
		}
		super.onBackPressed();
	}

}
