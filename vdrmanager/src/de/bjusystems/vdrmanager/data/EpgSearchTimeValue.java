package de.bjusystems.vdrmanager.data;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class EpgSearchTimeValue {

	private final int index;
	private final String text;

	public EpgSearchTimeValue(final int index, final String text) {
		this.index = index;
		this.text = text;
	}

	public EpgSearchTimeValue() {
		this.index = 0;
		this.text = "";
	}

	public String getText(){
		return text;
	}
	
	public String getValue() {
		switch (index) {
		case -1:
			return "adhoc";
		case 0:
			return "now";
		case 1:
			return "next";
		default:
			
			final String[] values = text.split(":");
			final Calendar cal = new GregorianCalendar();
			cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(values[0]));
			cal.set(Calendar.MINUTE, Integer.parseInt(values[1]));
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			// next day?
			final Calendar now = new GregorianCalendar();
			if (now.after(cal)) {
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}

			return String.format("%d", cal.getTimeInMillis() / 1000);
		}
	}

	@Override
	public String toString() {
		return text;
	}
}