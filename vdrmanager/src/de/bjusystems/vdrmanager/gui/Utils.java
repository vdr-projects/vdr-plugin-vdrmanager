package de.bjusystems.vdrmanager.gui;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
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
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.StringUtils;
import de.bjusystems.vdrmanager.app.C;
import de.bjusystems.vdrmanager.data.AudioTrack;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventContentGroup;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Recording;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.data.TimerMatch;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpListener;
import de.bjusystems.vdrmanager.utils.svdrp.SwitchChannelClient;

public class Utils {

	public static final String TAG = Utils.class.getName();

	public static final List EMPTY_LIST = new ArrayList(0);
	public static final String[] EMPTY = new String[] {};
	public static final ForegroundColorSpan HIGHLIGHT_TEXT = new ForegroundColorSpan(

	Color.RED);

	public static CharSequence highlight(final String where, String what) {
		if (TextUtils.isEmpty(what)) {
			return where;
		}

		final String str = where.toLowerCase();
		what = what.toLowerCase();
		final int idx = str.indexOf(what);
		if (idx == -1) {
			return where;
		}
		final SpannableString ss = new SpannableString(where);
		ss.setSpan(HIGHLIGHT_TEXT, idx, idx + what.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ss;
	}

	public static Pair<Boolean, CharSequence> highlight2(final String where,
			String what) {
		if (TextUtils.isEmpty(what)) {
			return Pair.create(Boolean.FALSE, (CharSequence) where);
		}

		final String str = where.toLowerCase();
		what = what.toLowerCase();
		final int idx = str.indexOf(what);
		if (idx == -1) {
			return Pair.create(Boolean.FALSE, (CharSequence) where);
		}
		final SpannableString ss = new SpannableString(where);
		ss.setSpan(HIGHLIGHT_TEXT, idx, idx + what.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return Pair.create(Boolean.TRUE, (CharSequence) ss);
	}

	public static int getProgress(final Date start, final Date stop) {
		final long now = System.currentTimeMillis();
		return getProgress(now, start.getTime(), stop.getTime());
	}

	public static int getProgress(final Event e) {
		if (e instanceof Recording == false) {
			return getProgress(e.getStart(), e.getStop());
		}
		final Recording r = ((Recording) e);
		if (r.getTimerStopTime() == null) {
			return getProgress(e.getStart(), e.getStop());
		}
		return getProgress(r.getStart(), r.getTimerStopTime());

	}

	/**
	 * @param now
	 * @param time
	 * @param time2
	 * @return -1, is not not between start stop,
	 */
	private static int getProgress(final long now, final long start,
			final long stop) {
		if (now >= start && now <= stop) {
			final long dura = stop - start;
			final long prog = now - start;
			return (int) (prog * 100 / dura);
		}
		return -1;
	}

	public static boolean isLive(final Event event) {
		final long now = System.currentTimeMillis();
		return now >= event.getStart().getTime()
				&& now <= event.getStop().getTime();
	}

	private static String trimToEmpty(final String str) {
		if (str == null) {
			return "";
		}
		if (TextUtils.isEmpty(str)) {
			return "";
		}
		return str;
	}

	private static String getBaseUrl() {
		final StringBuilder sb = new StringBuilder();
		final Preferences p = Preferences.getPreferences();
		String auth = trimToEmpty(p.getStreamingUsername()) + ":"
				+ trimToEmpty(p.getStreamingPassword());
		if (auth.length() == 1) {
			auth = "";
		} else {
			auth += "@";
		}

		sb.append("http://").append(auth).append(p.getSvdrpHost()).append(":")
				.append(p.getStreamPort());
		return sb.toString();
	}

	private static String getStreamUrl(final String chn) {
		// "http://192.168.1.119:3000/TS/"
		final Preferences p = Preferences.getPreferences();
		final StringBuilder sb = new StringBuilder();
		sb.append(getBaseUrl()).append("/").append(p.getStreamFormat())
				.append("/").append(chn);

		return sb.toString();
	}

	private static String getRemuxStreamUrl(final String chn) {
		// "http://192.168.1.119:3000/TS/"
		final StringBuilder sb = new StringBuilder();
		final Preferences p = Preferences.getPreferences();
		sb.append(getBaseUrl()).append("/").append(p.getRemuxCommand())
				.append(";").append(p.getRemuxParameter()).append("/")
				.append(chn);
		return sb.toString();
	}

	public static void stream(final Activity activity, final Event event) {
		stream(activity, event.getStreamId());
	}

	public static void stream(final Activity activity, final Channel channel) {
		stream(activity, channel.getId());
	}

	public static void stream(final Activity activity, final String idornr) {

		if (Preferences.get().isEnableRemux() == false) {
			final String url = getStreamUrl(idornr);
			startStream(activity, url);
			return;
		}

		final String sf = Preferences.get().getStreamFormat();
		final String ext = activity.getString(R.string.remux_title);
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
							@Override
							public void onClick(final DialogInterface dialog,
									final int which) {
								String url = null;
								switch (which) {
								case 0:
									url = getStreamUrl(idornr);
									break;
								case 1:
									url = getRemuxStreamUrl(idornr);
									break;
								}
								startStream(activity, url);
							}
						}).create().show();
	}

	public static void startStream(final Activity activity, final String url) {
		try {
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(url.toString()), "video/*");
			activity.startActivityForResult(intent, 1);
		} catch (final ActivityNotFoundException anfe) {
			Log.w(TAG, anfe);
			Toast.makeText(activity, anfe.getLocalizedMessage(),
					Toast.LENGTH_SHORT).show();
		}
	}

	public static final String md5(final String s) {
		try {
			// Create MD5 Hash
			final MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			final byte messageDigest[] = digest.digest();

			// Create Hex String
			final StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String h = Integer.toHexString(0xFF & messageDigest[i]);
				while (h.length() < 2) {
					h = "0" + h;
				}
				hexString.append(h);
			}
			return hexString.toString();

		} catch (final NoSuchAlgorithmException e) {
			Log.w(TAG, e);
		}
		return "";
	}

	public static int getDuration(final Event event) {
		final long millis = event.getDuration();
		final int minuts = (int) (millis / 1000 / 60);
		return minuts;
	}

	public static void shareEvent(final Activity activity, final Event event) {
		final Intent share = new Intent(android.content.Intent.ACTION_SEND);
		share.setType("text/plain");
		StringBuilder sb = new StringBuilder();
		sb.append(event.getTitle());
		sb.append("\n");
		final EventFormatter ef = new EventFormatter(event, false);
		sb.append(ef.getDate()).append(" ").append(ef.getTime());
		final String title = sb.toString();
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

	public static void addCalendarEvent(final Activity activity,
			final Event event) {
		final Intent intent = new Intent(Intent.ACTION_EDIT);
		intent.setType("vnd.android.cursor.item/event");
		intent.putExtra("title", event.getTitle());
		intent.putExtra("description", event.getShortText());
		intent.putExtra("beginTime", event.getStart().getTime());
		intent.putExtra("endTime", event.getStop().getTime());
		activity.startActivity(intent);
	}

	public static String mapSpecialChars(final String src) {
		if (src == null) {
			return "";
		}
		return src.replace("|##", C.DATA_SEPARATOR).replace("||#", "\n");
	}

	public static String unMapSpecialChars(final String src) {
		if (src == null) {
			return "";
		}
		return src.replace(C.DATA_SEPARATOR, "|##").replace("\n", "||#");
	}

	public static PackageInfo getPackageInfo(final Context ctx) {
		PackageInfo pi = null;
		try {
			pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
		} catch (final PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}

	public static boolean checkInternetConnection(final Context ctx) {
		final ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// test for connection
		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	private static String getRecordingStream(final Activity ctx,
			final Recording rec) {

		final String m = Preferences.get().getRecStreamMethod();

		final StringBuilder url = new StringBuilder();

		if (StringUtils.equals(m, "vdr-live")) {
			url.append("http://")
					.append(Preferences.get().getSvdrpHost())
					//
					.append(":")
					.append(Integer.valueOf(Preferences.get().getLivePort()))
					//
					.append("/recstream.html?recid=recording_")
					.append(Utils.md5(rec.getFileName()));
			// http://192.168.1.119:8008/b0cdedeed2d36508dfd924f0876a851b
			final String urlstring = url.toString();
			return urlstring;
		} else if (StringUtils.equals(m, "vdr-streamdev")) {
			url.append("http://").append(Preferences.get().getSvdrpHost())
					//
					.append(":")
					.append(Integer.valueOf(Preferences.get().getStreamPort()))
					//
					.append("/").append(rec.getDevInode());
		}
		return url.toString();
	}

	public static void streamRecording(final Activity ctx, final Recording rec) {
		final String urlstring = getRecordingStream(ctx, rec);
		Log.d(TAG, "try stream: " + urlstring);
		Utils.startStream(ctx, urlstring);
	}

	public static void switchTo(final Activity activity, final Channel channel) {
		switchTo(activity, channel.getId(), channel.getName());
	}

	/**
	 * @param ctx
	 * @param id
	 * @param name
	 *            Optional für die Anzeige
	 */
	public static void switchTo(final Activity activity, final String id,
			final String name) {

		final SwitchChannelClient scc = new SwitchChannelClient(id,
				new CertificateProblemDialog(activity));
		final SvdrpAsyncTask<String, SwitchChannelClient> task = new SvdrpAsyncTask<String, SwitchChannelClient>(
				scc);
		task.addSvdrpListener(new SvdrpListener() {
			@Override
			public void svdrpEvent(final SvdrpEvent event) {
				if (event == SvdrpEvent.FINISHED_SUCCESS) {
					Utils.say(activity, activity.getString(
							R.string.switching_success, (name != null ? name
									: id)));
				} else if (event == SvdrpEvent.CONNECT_ERROR
						|| event == SvdrpEvent.FINISHED_ABNORMALY
						|| event == SvdrpEvent.ABORTED
						|| event == SvdrpEvent.ERROR
						|| event == SvdrpEvent.CACHE_HIT) {
					Utils.say(activity, activity.getString(
							R.string.switching_failed, (name != null ? name
									: id), event.name()));
				}
			}

			public void svdrpException(final SvdrpException e) {
				Log.w(TAG, e.getMessage(), e);
				Utils.say(activity, e.getMessage());
			}
		});
		task.run();
	}

	public static void say(final Context ctx, final String msg) {
		final Toast t = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	public static void say(final Context ctx, final int msg) {
		final Toast t = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}

	/**
	 * Formats the date and time based on user's phone date/time preferences.
	 *
	 * @param context
	 *            the context
	 * @param time
	 *            the time in milliseconds
	 */

	public static String formatDateTime(final Context context, final long time) {
		return android.text.format.DateFormat.getDateFormat(context).format(
				time)
				+ " "
				+ DateUtils.formatDateTime(context, time,
						DateUtils.FORMAT_SHOW_TIME).toString();
	}

	public static int getTimerStateDrawable(final TimerMatch match,
			final int full, final int begin, final int end) {
		if (match == TimerMatch.Full) {
			return full;
		}

		if (match == TimerMatch.Begin) {
			return begin;
		}

		return end;
	}

	public static String formatAudio(final Context context,
			final List<AudioTrack> tracks) {

		final StringBuilder sb = new StringBuilder();

		String sep = "";
		for (final AudioTrack a : tracks) {
			sb.append(sep).append(a.display);
			if (a.type.equals("d")) {
				sb.append(" (")
						.append(context.getString(R.string.audio_track_dolby))
						.append(")");
			}
			sep = ", ";
		}
		return sb.toString();

	}

	public static TimerMatch getTimerMatch(Event event, Timer timer) {
		if (timer == null) {
			return null;
		}
		TimerMatch timerMatch = null;
		Date start = event.getStart();
		Date stop = event.getStop();
		if (start.before(timer.getStart())) {
			timerMatch = TimerMatch.End;
		} else if (stop.after(timer.getStop())) {
			timerMatch = TimerMatch.Begin;
		} else {
			timerMatch = TimerMatch.Full;
		}
		return timerMatch;
	}

	public static int contentToString(int c) {
		;
		switch (c & 0xF0) {
		case EventContentGroup.MovieDrama:
			switch (c & 0x0F) {
			default:
			case 0x00:
				return R.string.Content$Movie__Drama;
			case 0x01:
				return R.string.Content$Detective__Thriller;
			case 0x02:
				return R.string.Content$Adventure__Western__War;
			case 0x03:
				return R.string.Content$Science_Fiction__Fantasy__Horror;
			case 0x04:
				return R.string.Content$Comedy;
			case 0x05:
				return R.string.Content$Soap__Melodrama__Folkloric;
			case 0x06:
				return R.string.Content$Romance;
			case 0x07:
				return R.string.Content$Serious__Classical__Religious__Historical_Movie__Drama;
			case 0x08:
				return R.string.Content$Adult_Movie__Drama;
			}
		case EventContentGroup.NewsCurrentAffairs:
			switch (c & 0x0F) {
			default:
			case 0x00:
				return R.string.Content$News__Current_Affairs;
			case 0x01:
				return R.string.Content$News__Weather_Report;
			case 0x02:
				return R.string.Content$News_Magazine;
			case 0x03:
				return R.string.Content$Documentary;
			case 0x04:
				return R.string.Content$Discussion__Inverview__Debate;
			}

		case EventContentGroup.Show:
			switch (c & 0x0F) {
			default:
			case 0x00:
				return R.string.Content$Show__Game_Show;
			case 0x01:
				return R.string.Content$Game_Show__Quiz__Contest;
			case 0x02:
				return R.string.Content$Variety_Show;
			case 0x03:
				return R.string.Content$Talk_Show;
			}

		case EventContentGroup.Sports:
			switch (c & 0x0F) {
			default:
			case 0x00:
				return R.string.Content$Sports;
			case 0x01:
				return R.string.Content$Special_Event;
			case 0x02:
				return R.string.Content$Sport_Magazine;
			case 0x03:
				return R.string.Content$Football__Soccer;
			case 0x04:
				return R.string.Content$Tennis__Squash;
			case 0x05:
				return R.string.Content$Team_Sports;
			case 0x06:
				return R.string.Content$Athletics;
			case 0x07:
				return R.string.Content$Motor_Sport;
			case 0x08:
				return R.string.Content$Water_Sport;
			case 0x09:
				return R.string.Content$Winter_Sports;
			case 0x0A:
				return R.string.Content$Equestrian;
			case 0x0B:
				return R.string.Content$Martial_Sports;
			}

		case EventContentGroup.ChildrenYouth:
			switch (c & 0x0F) {
			default:
			case 0x00:
				return R.string.Content$Childrens__Youth_Programme;
			case 0x01:
				return R.string.Content$Preschool_Childrens_Programme;
			case 0x02:
				return R.string.Content$Entertainment_Programme_for_6_to_14;
			case 0x03:
				return R.string.Content$Entertainment_Programme_for_10_to_16;
			case 0x04:
				return R.string.Content$Informational__Educational__School_Programme;
			case 0x05:
				return R.string.Content$Cartoons__Puppets;
			}

		case EventContentGroup.MusicBalletDance:
			switch (c & 0x0F) {
			default:
			case 0x00:
				return R.string.Content$Music__Ballet__Dance;
			case 0x01:
				return R.string.Content$Rock__Pop;
			case 0x02:
				return R.string.Content$Serious__Classical_Music;
			case 0x03:
				return R.string.Content$Folk__Tradional_Music;
			case 0x04:
				return R.string.Content$Jazz;
			case 0x05:
				return R.string.Content$Musical__Opera;
			case 0x06:
				return R.string.Content$Ballet;
			}

		case EventContentGroup.ArtsCulture:
			switch (c & 0x0F) {
			default:
			case 0x00:
				return R.string.Content$Arts__Culture;
			case 0x01:
				return R.string.Content$Performing_Arts;
			case 0x02:
				return R.string.Content$Fine_Arts;
			case 0x03:
				return R.string.Content$Religion;
			case 0x04:
				return R.string.Content$Popular_Culture__Traditional_Arts;
			case 0x05:
				return R.string.Content$Literature;
			case 0x06:
				return R.string.Content$Film__Cinema;
			case 0x07:
				return R.string.Content$Experimental_Film__Video;
			case 0x08:
				return R.string.Content$Broadcasting__Press;
			case 0x09:
				return R.string.Content$New_Media;
			case 0x0A:
				return R.string.Content$Arts__Culture_Magazine;
			case 0x0B:
				return R.string.Content$Fashion;
			}

		case EventContentGroup.SocialPoliticalEconomics:
			switch (c & 0x0F) {
			default:
			case 0x00:
				return R.string.Content$Social__Political__Economics;
			case 0x01:
				return R.string.Content$Magazine__Report__Documentary;
			case 0x02:
				return R.string.Content$Economics__Social_Advisory;
			case 0x03:
				return R.string.Content$Remarkable_People;
			}

		case EventContentGroup.EducationalScience:
			switch (c & 0x0F) {
			default:
			case 0x00:
				return R.string.Content$Education__Science__Factual;
			case 0x01:
				return R.string.Content$Nature__Animals__Environment;
			case 0x02:
				return R.string.Content$Technology__Natural_Sciences;
			case 0x03:
				return R.string.Content$Medicine__Physiology__Psychology;
			case 0x04:
				return R.string.Content$Foreign_Countries__Expeditions;
			case 0x05:
				return R.string.Content$Social__Spiritual_Sciences;
			case 0x06:
				return R.string.Content$Further_Education;
			case 0x07:
				return R.string.Content$Languages;
			}

		case EventContentGroup.LeisureHobbies:
			switch (c & 0x0F) {
			default:
			case 0x00:
				return R.string.Content$Leisure__Hobbies;
			case 0x01:
				return R.string.Content$Tourism__Travel;
			case 0x02:
				return R.string.Content$Handicraft;
			case 0x03:
				return R.string.Content$Motoring;
			case 0x04:
				return R.string.Content$Fitness_and_Health;
			case 0x05:
				return R.string.Content$Cooking;
			case 0x06:
				return R.string.Content$Advertisement__Shopping;
			case 0x07:
				return R.string.Content$Gardening;
			}
		case EventContentGroup.Special:
			switch (c & 0x0F) {
			case 0x00:
				return R.string.Content$Original_Language;
			case 0x01:
				return R.string.Content$Black_and_White;
			case 0x02:
				return R.string.Content$Unpublished;
			case 0x03:
				return R.string.Content$Live_Broadcast;
			default:
				;
			}
			break;
		default:
			;
		}
		return R.string.Content$Unknown;
	}

	public static String getContenString(Context ctx, int[] contents) {

		if (contents.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		String sep = "";

		for (int content : contents) {

			sb.append(sep).append(ctx.getString(contentToString(content)));
			sep = ", ";
		}

		return sb.toString();
	}

	public static boolean isSerie(int[] contents) {
		if (contents.length == 0) {
			return false;
		}

		for (int c : contents) {
			if (c == 21) {
				return true;

			}
		}

		return false;
	}
}
