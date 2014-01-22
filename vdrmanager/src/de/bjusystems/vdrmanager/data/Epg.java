package de.bjusystems.vdrmanager.data;

import java.util.Date;

import android.text.TextUtils;
import de.bjusystems.vdrmanager.StringUtils;
import de.bjusystems.vdrmanager.app.C;
import de.bjusystems.vdrmanager.gui.Utils;
import static de.bjusystems.vdrmanager.gui.Utils.mapSpecialChars;

/**
 * Class for EPG events
 * 
 * @author bju
 */
public class Epg extends Event implements Timerable {

	private Timer timer;

	private TimerMatch timerMatch;

	public TimerMatch getTimerMatch() {
		return timerMatch;
	}

	public void setTimerMatch(TimerMatch timerMatch) {
		this.timerMatch = timerMatch;
	}

	public Epg(final String line) {
		final String[] words = StringUtils.splitPreserveAllTokens(line,
				C.DATA_SEPARATOR);
		channelNumber = Long.valueOf(words[0].substring(1));
		channelName = words[1];
		start = new Date(Long.parseLong(words[2]) * 1000);
		stop = new Date(Long.parseLong(words[3]) * 1000);
		title = mapSpecialChars(words[4]);
		description = words.length > 5 ? mapSpecialChars(words[5]) : "";
		shortText = words.length > 6 ? mapSpecialChars(words[6]) : "";
		channelId = words.length > 7 ? mapSpecialChars(words[7]) : "";
		rawAudio = words.length > 8 ? mapSpecialChars(words[8]) : "";
		if (words.length > 9) {
			String contents = words[9].trim();
			if (contents.length() > 0) {
				String[] caray = contents.split(" ");
				if (caray.length > 0) {
					content = new int[caray.length];
					for (int i = 0; i < caray.length; ++i) {
						content[i] = Integer.valueOf(caray[i]);
					}
				}
			}

		}
		if (words.length > 10) {
			if (TextUtils.isEmpty(words[10]) == false) {
				vps = Long.valueOf(words[10]) * 1000;
			}
		}
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(final Timer timer) {
		this.timer = timer;
		timerMatch = Utils.getTimerMatch(this, timer);
	}

	public TimerState getTimerState() {
		if (timer == null) {
			return TimerState.None;
		} else {
			return timer.getTimerState();
		}
	}

	public Timer createTimer() {
		return new Timer(this);
	}
}
