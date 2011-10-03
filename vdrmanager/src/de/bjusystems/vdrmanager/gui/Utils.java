package de.bjusystems.vdrmanager.gui;

import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.Preferences;

public class Utils {

	public static int getProgress(Date start, Date stop) {
		long now = System.currentTimeMillis();
		return getProgress(now, start.getTime(), stop.getTime());
	}

	public static int getProgress(Event e) {
		return getProgress(e.getStart(), e.getStop());
	}

	/**
	 * @param now
	 * @param time
	 * @param time2
	 * @return -1, is not not between start stop,
	 */
	private static int getProgress(long now, long start, long stop) {
		if (now >= start && now <= stop) {
			long dura = stop - start;
			long prog = now - start;
			return (int) (prog * 100 / dura);
		}
		return -1;
	}

	public static boolean isLive(Event event){
		long now = new Date().getTime();
		return now >= event.getStart().getTime() && now < event.getStop().getTime();
	}
	
	private static String getStreamUrl(String chn) {
		// "http://192.168.1.119:3000/TS/"
		StringBuilder sb = new StringBuilder();
		Preferences p = Preferences.getPreferences();
		sb.append("http://").append(p.getSvdrpHost()).append(":")
				.append(p.getStreamPort()).append("/")
				.append(p.getStreamFormat()).append("/").append(chn);
		return sb.toString();
	}

	
	public static void stream(Activity activity, Event event) {
		stream(activity, event.getChannelNumber());
	}

	public static void stream(Activity a, Channel c){
		stream(a, String.valueOf(c.getNumber()));
	}
	
	
	public static void stream(Activity activity, String chn) {
		String url = getStreamUrl(chn);
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(url.toString()), "video/*");
		activity.startActivityForResult(intent, 1);

	}

	public static int getDuration(Event event){
		long millis = event.getStop().getTime() - event.getStart().getTime();
		int minuts = (int)(millis / 1000 / 60);
		return minuts;
	}
	
}
