package de.bjusystems.vdrmanager.gui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.View;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.ZonePicker;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.data.MacFetchEditTextPreference;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Vdr;
import de.bjusystems.vdrmanager.data.VdrSharedPreferences;
import de.bjusystems.vdrmanager.data.db.DBAccess;
import de.bjusystems.vdrmanager.tasks.VoidAsyncTask;

public class VdrPreferencesActivity extends BasePreferencesActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	public static final int REQUEST_CODE_PICK_A_TIME_ZONE  = 1;

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


	@Override
	protected void updateSummary(Preference ep) {
	 if(ep.getKey().equals("key_timezone")) {
			String text = vdr.getServerTimeZone();
			if (text == null) {
				return;
			}
			setSummary(text, ep);
			return;
		}
		super.updateSummary(ep);
	}
	private void initVDRInstance() {
		id = getIntent().getIntExtra(Intents.VDR_ID, -1);
		if (id == -1) {// new vdr
			vdr = new Vdr();

		} else {// edit
			Vdr v = DBAccess.get(this).getVdrDAO().queryForId(id);
			if (v != null) {
				vdr = v;
			} else {
				vdr = new Vdr();
				id = -1;
			}
		}
		pref.setInstance(vdr);
	}

	public static String ARP_CACHE = "/proc/net/arp";

	/**
	 * return mac address as a string.
	 *
	 * @param ip
	 * @return
	 */
	public static String getMacFromArpCache(String ip) {

		if (ip == null) {
			return null;
		}

		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(ARP_CACHE));

			String line;

			while ((line = br.readLine()) != null) {
				String[] values = line.split("\\s+");
				if (values != null && values.length >= 4
						&& ip.equals(values[0])) {
					// format check
					String mac = values[3];
					if (mac.matches("..:..:..:..:..:..")) {
						return mac;
					} else {
						return null;
					}
				}
			}
		} catch (Exception e) {

		} finally {
			try {
				br.close();
			} catch (IOException e) {

			}
		}
		return null;
	}

	private String getIp() throws Exception {
		final Preferences prefs = Preferences.get();
		String host = prefs.getSvdrpHost();
		return InetAddress.getByName(host).getHostAddress();
	}

	private void ping(String ip) throws Exception {
		final Preferences prefs = Preferences.get();
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress(ip, prefs.getSvdrpPort()),
				5 * 1000);
		socket.setSoTimeout(5 * 1000);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		pref = new VdrSharedPreferences();

		pref.dao = DBAccess.get(this).getVdrDAO();

		initVDRInstance();

		this.addPreferencesFromResource(R.xml.vdr_prefs);

		// this.getPreferenceManager().setSharedPreferencesName(Preferences.getPreferenceFile(this));

		pref.registerOnSharedPreferenceChangeListener(this);



		String recstream = pref.getString("key_recstream_method", "vdr-live");

		if (recstream.equals("vdr-live") == false) {
			Preference p = findPreference("key_live_port");
			p.setEnabled(false);
			// PreferenceCategory cat = (PreferenceCategory)
			// findPreference("key_streaming_category");
			// cat.removePreference(p);
		}

		final String host = pref.getString(getString(R.string.vdr_host_key),
				null);

		// create background task

		// start task

		final MacFetchEditTextPreference macedit = (MacFetchEditTextPreference) findPreference(getString(R.string.wakeup_wol_mac_key));
		String mac = vdr.getMac();
		macedit.setText(mac);
		macedit.setCompoundButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (host == null) {
					Utils.say(VdrPreferencesActivity.this,
							getString(R.string.vdr_host_not_defined));
					return;
				}

				new VoidAsyncTask() {

					ProgressDialog pd;

					private String mac;

					String message;

					protected void onPreExecute() {
						pd = new ProgressDialog(VdrPreferencesActivity.this);
						pd.setMessage(getString(R.string.processing));
						pd.show();
					};

					protected void onPostExecute(Void result) {
						pd.dismiss();
						if (message != null) {
							Utils.say(VdrPreferencesActivity.this, message);
							return;
						}
						macedit.setEditText(mac);
					};

					@Override
					protected Void doInBackground(Void... params) {
						try {
							String ip = getIp();
							ping(ip);
							mac = getMacFromArpCache(ip);
						} catch (Exception ex) {
							message = ex.getLocalizedMessage();
						}

						return null;
					}
				}.execute();
			}
		});

		updateChildPreferences();

		findPreference(getString(R.string.timezone_key)).setOnPreferenceClickListener(this);

	}

	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		updateChildPreferences();
		Preference p = findPreference(key);
		updateSummary(p);

		if (key != null && key.equals("key_recstream_method")) {
			String recstream = pref.getString("key_recstream_method",
					"vdr-live");
			Preference pk = findPreference("key_live_port");
			if (recstream.equals("vdr-live") == false) {
				pk.setEnabled(false);
				// PreferenceCategory cat = (PreferenceCategory)
				// findPreference("key_streaming_category");
				// cat.removePreference(p);
			} else {
				pk.setEnabled(true);
			}

			// if(pk)
			// cat.addPreference(pk);
			// } else {
			// cat.removePreference(pk);
			// }
		}

		Preferences.reloadVDR(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		pref.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		pref.unregisterOnSharedPreferenceChangeListener(this);
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

	@Override
	public void onBackPressed() {
		if (id != -1) {// no new devices
			setResult(RESULT_OK);
			finish();
			return;
		}
		if (pref.commits < 2) {// user has not changed anything
			DBAccess.get(this).getVdrDAO().delete(pref.getInstance());
			finish();
			return;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {

		String timezone = vdr.getServerTimeZone();

		Intent intent = new Intent(this, ZonePicker.class);
		intent.putExtra("current_tz", timezone);
		startActivityForResult(intent, REQUEST_CODE_PICK_A_TIME_ZONE);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK){
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}

		if(requestCode == REQUEST_CODE_PICK_A_TIME_ZONE){
			String ntz = data.getStringExtra("new_tz");
			if(ntz != null){
				vdr.setServerTimeZone(ntz);
				Editor editor = findPreference("key_timezone").getEditor();
				editor.putString("key_timezone", ntz);
				editor.commit();
				//setSummary(ntz, );
			}
		}
	}

}
