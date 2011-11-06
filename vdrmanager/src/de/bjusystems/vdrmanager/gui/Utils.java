package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.C;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.Preferences;

public class Utils {

	public static final List EMPTY_LIST = new ArrayList(0);
	public static final ForegroundColorSpan HIGHLIGHT_TEXT = new ForegroundColorSpan(
			Color.RED);

	public static CharSequence highlight(String where, String what) {
		if (TextUtils.isEmpty(what)) {
			return where;
		}

		String str = where.toLowerCase();
		what = what.toLowerCase();
		int idx = str.indexOf(what);
		if (idx == -1) {
			return where;
		}
		SpannableString ss = new SpannableString(where);
		ss.setSpan(HIGHLIGHT_TEXT, idx, idx + what.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}

	public static Pair<Boolean, CharSequence> highlight2(String where,
			String what) {
		if (TextUtils.isEmpty(what)) {
			return Pair.create(Boolean.FALSE, (CharSequence) where);
		}

		String str = where.toLowerCase();
		what = what.toLowerCase();
		int idx = str.indexOf(what);
		if (idx == -1) {
			return Pair.create(Boolean.FALSE, (CharSequence) where);
		}
		SpannableString ss = new SpannableString(where);
		ss.setSpan(HIGHLIGHT_TEXT, idx, idx + what.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return Pair.create(Boolean.TRUE, (CharSequence) ss);
	}

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

	public static boolean isLive(Event event) {
		long now = System.currentTimeMillis();
		return now >= event.getStart().getTime()
				&& now < event.getStop().getTime();
	}

	private static String getBaseUrl() {
		StringBuilder sb = new StringBuilder();
		Preferences p = Preferences.getPreferences();
		String auth = p.getStreamingUsername().trim() + ":"
				+ p.getStreamingPassword();
		if (auth.length() == 1) {
			auth = "";
		} else {
			auth += "@";
		}

		sb.append("http://").append(auth).append(p.getSvdrpHost()).append(":")
				.append(p.getStreamPort());
		return sb.toString();
	}

	private static String getStreamUrl(String chn) {
		// "http://192.168.1.119:3000/TS/"
		Preferences p = Preferences.getPreferences();
		StringBuilder sb = new StringBuilder();
		sb.append(getBaseUrl()).append("/").append(p.getStreamFormat())
				.append("/").append(chn);
		return sb.toString();
	}

	private static String getRemuxStreamUrl(String chn) {
		// "http://192.168.1.119:3000/TS/"
		StringBuilder sb = new StringBuilder();
		Preferences p = Preferences.getPreferences();
		sb.append(getBaseUrl()).append("/")
				.append(p.getRemuxCommand()).append(";")
				.append(p.getRemuxParameter()).append("/").append(chn);
		return sb.toString();
	}

	public static void stream(Activity activity, Event event) {
		stream(activity, event.getChannelNumber());
	}

	public static void stream(Activity a, Channel c) {
		stream(a, String.valueOf(c.getNumber()));
	}

	public static void stream(final Activity activity, final String chn) {

		if (Preferences.get().isEnableRemux() == false) {
			String url = getStreamUrl(chn);
			startStream(activity, url);
			return;
		}

		String sf = Preferences.get().getStreamFormat();
		String ext = activity.getString(R.string.remux_title);
		new AlertDialog.Builder(activity)
				.setTitle(R.string.stream_via_as)
				//
				.setItems(
						new String[] {
								activity.getString(R.string.stream_as, sf),
								activity.getString(R.string.stream_via, ext) },// TODO
																				// add
																				// here
																				// what
																				// will
																				// be
																				// used
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								String url = null;
								switch (which) {
								case 0:
									url = getStreamUrl(chn);
									break;
								case 1:
									url = getRemuxStreamUrl(chn);
									break;
								}
								startStream(activity, url);
							}
						}).create().show();
	}

	public static void startStream(Activity activity, String url) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(url.toString()), "video/*");
		activity.startActivityForResult(intent, 1);
	}

	public static int getDuration(Event event) {
		long millis = event.getStop().getTime() - event.getStart().getTime();
		int minuts = (int) (millis / 1000 / 60);
		return minuts;
	}

	public static void shareEvent(Activity activity, Event event) {
		final Intent share = new Intent(android.content.Intent.ACTION_SEND);
		share.setType("text/plain");
		StringBuilder sb = new StringBuilder();
		sb.append(event.getTitle());
		sb.append("\n");
		EventFormatter ef = new EventFormatter(event, false);
		sb.append(ef.getDate()).append(" ").append(ef.getTime());
		String title = sb.toString();
		share.putExtra(android.content.Intent.EXTRA_SUBJECT, sb.toString());
		sb = new StringBuilder();
		sb.append(title).append("\n\n");
		sb.append(event.getChannelNumber() + " " + event.getChannelName());
		sb.append("\n\n");
		sb.append(ef.getShortText());
		sb.append("\n\n");
		sb.append(ef.getDescription());
		sb.append("\n");
		share.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
		activity.startActivity(Intent.createChooser(share,
				activity.getString(R.string.share_chooser)));
	}

	public static String mapSpecialChars(String src) {
		if (src == null) {
			return "";
		}
		return src.replace("|##", C.DATA_SEPARATOR).replace("||#", "\n");
	}

	public static String unMapSpecialChars(String src) {
		if (src == null) {
			return "";
		}
		return src.replace(C.DATA_SEPARATOR, "|##").replace("\n", "||#");
	}

	public static PackageInfo getPackageInfo(Context ctx) {
		PackageInfo pi = null;
		try {
			pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}
	

	public static boolean checkInternetConnection(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		// test for connection
		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {
			return true;
		}
		return false;
	}
}
