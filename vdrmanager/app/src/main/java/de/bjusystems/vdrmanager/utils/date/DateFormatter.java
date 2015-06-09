package de.bjusystems.vdrmanager.utils.date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.bjusystems.vdrmanager.data.Preferences;


/**
 * Class for formatting date and time values
 * @author bju
 *
 */
public class DateFormatter {

	private final String timeString;
	private final String dateString;
	private final String dailyHeader;

	public DateFormatter(final Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(Preferences.get().getTimeFormat());
		timeString = sdf.format(date);
		sdf = new SimpleDateFormat("EEE dd.MM.yy");
		dateString = sdf.format(date);
		dailyHeader = DateFormat.getDateInstance(DateFormat.FULL).format(date);
	}

	public DateFormatter(final long seconds) {
		this(new Date(seconds * 1000));
	}

	public DateFormatter(final Calendar cal) {
		this(cal.getTime());
	}

	public String getDateString() {
		return dateString;
	}

	public String getTimeString() {
		return timeString;
	}

	public String getDailyHeader() {
		return dailyHeader;
	}
}
