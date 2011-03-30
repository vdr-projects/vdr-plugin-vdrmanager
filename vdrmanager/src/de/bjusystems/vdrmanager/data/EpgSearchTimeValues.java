package de.bjusystems.vdrmanager.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import de.bjusystems.vdrmanager.R;

public class EpgSearchTimeValues {

	private final List<EpgSearchTimeValue> values = new ArrayList<EpgSearchTimeValue>();
	private final Context context;

	public EpgSearchTimeValues(final Context context) {

		this.context = context;
	}

	public List<EpgSearchTimeValue> getValues() {

		final Preferences prefs = Preferences.getPreferences();

		// fixed values for now and next
		values.add(new EpgSearchTimeValue(0, context.getString(R.string.epg_list_time_now)));
		values.add(new EpgSearchTimeValue(1, context.getString(R.string.epg_list_time_next)));

		// get user defined values
		final String userValueString = prefs.getEpgSearchTimes();

		final String[] userValues = userValueString.split(",");

		Arrays.sort(userValues);

		for(final String userValue : userValues) {
			if (userValue.contains(":")) {
				values.add(new EpgSearchTimeValue(values.size(), userValue));
			}
		}

		return values;
	}

	public void saveValues(final List<EpgSearchTimeValue> values) {

		// get old values
		final Preferences prefs = Preferences.getPreferences();

		// add value
		String newValues = "";
		for(int i = 2; i < values.size(); i++) {
			final EpgSearchTimeValue value = values.get(i);
			if (newValues.length() > 0) {
				newValues += ",";
			}
			newValues += value.toString();
		}

		// save new values
		prefs.setEpgSearchTimes(context, newValues);
	}
}
