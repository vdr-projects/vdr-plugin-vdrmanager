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

public class PreferencesActivity extends PreferenceActivity implements
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

	private void updateSummary(Preference ep) {
		if (ep instanceof EditTextPreference) {
			updateSummary((EditTextPreference) ep);
		} else if (ep instanceof ListPreference) {
			updateSummary((ListPreference) ep);
		}
	}

	/**
	 * If text set add it to the summary
	 * 
	 * @param ep
	 */
	private void updateSummary(EditTextPreference ep) {
		String text = ep.getText();
		if (text == null) {
			return;
		}
		
		if(isPassword(ep.getEditText())){
			text = text.replaceAll(".", "*");
		}
		
		setSummary(text, ep);
	}

	private boolean isPassword(EditText et){
		if((et.getInputType() & EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD){
			return true;
		}
		return false;
	}
	
	private void setSummary(CharSequence text, DialogPreference ep){
		CharSequence sm = ep.getSummary();
		String sum;
		if (sm != null) {
			sum = ep.getSummary().toString();
			sum = substringBeforeLast(sum,
					getString(R.string.prefs_current_value)).trim();
		} else {
			sum = "";
		}
		
		if(TextUtils.isEmpty(text)){
			text = getString(R.string.prefs_current_value_not_set);
		}
		
		if (isBlank(sum)) {
			sum = getString(R.string.prefs_current_value_template, text);
		} else {
			sum = sum + " "
					+ getString(R.string.prefs_current_value_template, text);
		}
		ep.setSummary(sum);
	}
	
	private void updateSummary(ListPreference ep) {
		CharSequence text = ep.getEntry();

		if (text == null) {
			return;
		}
		setSummary(text, ep);
	}

	/**
	 * <p>
	 * Gets the substring before the last occurrence of a separator. The
	 * separator is not returned.
	 * </p>
	 * 
	 * <p>
	 * A <code>null</code> string input will return <code>null</code>. An empty
	 * ("") string input will return the empty string. An empty or
	 * <code>null</code> separator will return the input string.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.substringBeforeLast(null, *)      = null
	 * StringUtils.substringBeforeLast("", *)        = ""
	 * StringUtils.substringBeforeLast("abcba", "b") = "abc"
	 * StringUtils.substringBeforeLast("abc", "c")   = "ab"
	 * StringUtils.substringBeforeLast("a", "a")     = ""
	 * StringUtils.substringBeforeLast("a", "z")     = "a"
	 * StringUtils.substringBeforeLast("a", null)    = "a"
	 * StringUtils.substringBeforeLast("a", "")      = "a"
	 * </pre>
	 * 
	 * @param str
	 *            the String to get a substring from, may be null
	 * @param separator
	 *            the String to search for, may be null
	 * @return the substring before the last occurrence of the separator,
	 *         <code>null</code> if null String input
	 * @since 2.0
	 */
	public static String substringBeforeLast(String str, String separator) {
		if (isEmpty(str) || isEmpty(separator)) {
			return str;
		}
		int pos = str.lastIndexOf(separator);
		if (pos == -1) {
			return str;
		}
		return str.substring(0, pos);
	}

	// Empty checks
	// -----------------------------------------------------------------------
	/**
	 * <p>
	 * Checks if a String is empty ("") or null.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.isEmpty(null)      = true
	 * StringUtils.isEmpty("")        = true
	 * StringUtils.isEmpty(" ")       = false
	 * StringUtils.isEmpty("bob")     = false
	 * StringUtils.isEmpty("  bob  ") = false
	 * </pre>
	 * 
	 * <p>
	 * NOTE: This method changed in Lang version 2.0. It no longer trims the
	 * String. That functionality is available in isBlank().
	 * </p>
	 * 
	 * @param str
	 *            the String to check, may be null
	 * @return <code>true</code> if the String is empty or null
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	/**
	 * <p>
	 * Checks if a String is whitespace, empty ("") or null.
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 * 
	 * @param str
	 *            the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 * @since 2.0
	 */
	public static boolean isBlank(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(str.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

}
