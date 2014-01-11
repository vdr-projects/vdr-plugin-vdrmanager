package de.bjusystems.vdrmanager.data;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.text.TextUtils;
import de.bjusystems.vdrmanager.StringUtils;
import de.bjusystems.vdrmanager.app.C;
import de.bjusystems.vdrmanager.gui.Utils;

/**
 * Class for timer data
 *
 * @author bju
 */
public class Timer extends Event implements Timerable {


//			 tfActive    = 0x0001,
//             tfInstant   = 0x0002,
//             tfVps       = 0x0004,
//             tfRecording = 0x0008,
//             tfAll       = 0xFFFF,
	private static final int ACTIVE = 1;
	private static final int INSTANT = 2;
	private static final int VPS = 4;
	private static final int RECORDING = 8;

	private int number;
	private int flags;
	private int priority;
	private int lifetime;
	private String weekdays = "-------";
    private boolean conflict;

	public void setPriority(int priority) {
		this.priority = priority;
	}


	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}


	public String getWeekdays() {
		return weekdays;
	}


	public void setWeekdays(String weekdays) {
		this.weekdays = weekdays;
	}


	/**
	 * Constructs a timer from SvdrpHelper result line
	 *
	 * @param timerData
	 *            result line
	 */
	public Timer(final String timerData) {

		final String[] values = StringUtils.splitPreserveAllTokens(timerData,
				C.DATA_SEPARATOR);

		// number
		this.number = Integer.valueOf(values[0].substring(1));

		// flags, channel number and channel name
		this.flags = Integer.valueOf(values[1]);
		this.channelNumber = Long.valueOf(values[2]);
		this.channelName = Utils.mapSpecialChars(values[3]);

		// get start and stop
		this.start = new Date(Long.parseLong(values[4]) * 1000);
		this.stop = new Date(Long.parseLong(values[5]) * 1000);

		// priority and lifetime
		this.priority = Integer.valueOf(values[6]);
		this.lifetime = Integer.valueOf(values[7]);

		// title and description
		this.title = Utils.mapSpecialChars(values[8]);

		this.description = values.length > 9 ? values[9] : "";// aux

		// 10 and 11 are optional if there where event with this timer
		this.shortText = values.length > 10 ? Utils
				.mapSpecialChars(values[10]) : "";

		if (values.length > 11) {
			this.description = values[11]; // if real description, set it
		}

		if(values.length > 12 ){
			this.channelId = values[12];
		}

		if(values.length > 13) {
			this.weekdays = values[13];
		}

        if (values.length > 14) {
            this.conflict = values[14].equals("1");
        }


		description = Utils.mapSpecialChars(description);
	}


	public Timer copy(){
		Timer t = new Timer(this);
		t.flags = flags;
		t.number = number;
		t.priority = priority;
		t.lifetime = lifetime;
		t.start = new Date(start.getTime());
		t.stop = new Date(stop.getTime());
		t.title = title;
		t.weekdays = weekdays;
        t.conflict = conflict;
		return t;
	}

	public Timer(final Event event) {
		final Preferences prefs = Preferences.getPreferences();

		this.number = 0;
		this.flags = ACTIVE;
		this.channelNumber = event.getChannelNumber();
		this.channelName = event.getChannelName();
		this.channelId = event.getChannelId();
		this.priority = prefs.getTimerDefaultPriority();
		this.lifetime = prefs.getTimerDefaultLifetime();

		this.start = new Date(event.getStart().getTime()
				- prefs.getTimerPreMargin() * 60000);
		this.stop = new Date(event.getStop().getTime()
				+ prefs.getTimerPostMargin() * 60000);

		this.title = event.getTitle();
		if(Utils.isSerie(event.getContent())){
			if(TextUtils.isEmpty(event.getShortText()) == false){
				this.title+="~"+event.getShortText();
			}
		}
		this.description = event.getDescription();
	}

	public boolean isRecurring(){
		return weekdays.equals("-------") == false;
	}

	public String toCommandLine() {

		final StringBuilder line = new StringBuilder();

		//line.append(number).append(":");
		line.append(flags).append(":");
		line.append(channelNumber).append(":");

		final Calendar cal = Calendar.getInstance();
		cal.setTime(start);

		cal.setTimeZone(TimeZone.getTimeZone(Preferences.get().getCurrentVdr().getServerTimeZone()));

		line.append((weekdays.equals("-------") == false ? weekdays + "@" : "") + String.format("%04d-%02d-%02d:", cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
		line.append(String.format("%02d%02d:", cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE)));

		cal.setTime(stop);
		line.append(String.format("%02d%02d:", cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE)));

		line.append(priority).append(":");
		line.append(lifetime).append(":");
		line.append(Utils.unMapSpecialChars(title));
		return line.toString();
	}

	public int getNumber() {
		return number;
	}

	public int getPriority() {
		return priority;
	}

	public int getLifetime() {
		return lifetime;
	}

	public TimerState getTimerState() {
		if (isEnabled()) {
			if (isRecording()) {
				return TimerState.Recording;
			}
			return TimerState.Active;
		}
		return TimerState.Inactive;
	}

	public boolean isEnabled() {
		return (flags & ACTIVE) == ACTIVE;
	}

	public boolean isInstant() {
		return (flags & INSTANT) == INSTANT;
	}

	public boolean isVps() {
		return (flags & VPS) == VPS;
	}

	public boolean isRecording() {
		return (flags & RECORDING) == RECORDING;
	}

    public boolean isConflict() {
        return conflict;
    }

	public void setStart(final Date start) {
		this.start = start;
	}

	public void setStop(final Date stop) {
		this.stop = stop;
	}

	public void setVps(boolean useVps){
		if(useVps){
			flags = flags | VPS;
		} else {
			flags = flags & ~VPS;
		}
	}

	public void setEnabled(final boolean enabled) {
		if (enabled) {
			flags = flags | ACTIVE;
		} else {
			flags = flags & ~ACTIVE;
		}
	}

	public Timer getTimer() {
		return this;
	}

	public Timer createTimer() {
		return new Timer(this);
	}


	@Override
	public TimerMatch getTimerMatch() {

        if (isConflict())
            return TimerMatch.Conflict;

        return TimerMatch.Full;
	}



}
