package de.bjusystems.vdrmanager.gui;

import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import de.bjusystems.vdrmanager.R;

/**
 * 
 * Basis class for PreferencesActivities with some goodies in it
 * @author lado
 *
 */
public abstract class BasePreferencesActivity extends PreferenceActivity {

	protected void updateSummary(Preference ep) {
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
	protected void updateSummary(EditTextPreference ep) {
		String text = ep.getText();
		if (text == null) {
			return;
		}
		
		if(isPassword(ep.getEditText())){
			text = text.replaceAll(".", "*");
		}
		
		setSummary(text, ep);
	}

	protected boolean isPassword(EditText et){
		if((et.getInputType() & EditorInfo.TYPE_TEXT_VARIATION_PASSWORD) == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD){
			return true;
		}
		return false;
	}
	
	protected void setSummary(CharSequence text, DialogPreference ep){
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
	
	protected void updateSummary(ListPreference ep) {
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
