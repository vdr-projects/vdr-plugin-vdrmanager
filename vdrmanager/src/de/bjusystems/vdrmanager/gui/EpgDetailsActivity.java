package de.bjusystems.vdrmanager.gui;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TitleProvider;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EpgCache;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Recording;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.data.TimerMatch;
import de.bjusystems.vdrmanager.data.Timerable;
import de.bjusystems.vdrmanager.data.Timerable.TimerState;
import de.bjusystems.vdrmanager.tasks.CreateTimerTask;
import de.bjusystems.vdrmanager.tasks.DeleteTimerTask;
import de.bjusystems.vdrmanager.tasks.ToggleTimerTask;
import de.bjusystems.vdrmanager.tasks.VoidAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class EpgDetailsActivity extends ICSBaseActivity implements
		OnClickListener, OnPageChangeListener {

	public static final String TAG = "EpgDetailsActivity";

	public static String IMDB_BASE_URL = "http://%s";

	public static String IMDB_URL_QUERY = "/find?s=tt&q=%s";

	public static String OMDB_URL = "http://www.omdb.org/search?search[text]=%s";

	private static final String IMDB_URL_ENCODING = "UTF-8";

	private static final String OMDB_URL_ENCODING = "UTF-8";

	private static final String TMDB_URL_ENCODING = "UTF-8";

	public static String TMDB_URL = "http://www.themoviedb.org/search?search=%s";

	private String highlight = null;

	// private Event cEvent;

	// private ImageView state;

	private boolean modifed = false;

	// private int current;

	private ViewPager pager;

	private Adapter adapter;

	// private Timerable timerable = null;

	class Adapter extends PagerAdapter implements TitleProvider {

		public Adapter() {

		}

		public String getTitle(int position) {
			return epgs.get(position).getChannelName();
		}

		public int getCount() {
			return epgs.size();
		}

		public Object instantiateItem(View pager, int position) {
			View view = getLayoutInflater().inflate(R.layout.epg_detail, null);
			// Event e = epgs.get(position);
			publishEPG(view, position);
			((ViewPager) pager).addView(view, 0);

			return view;
		}

		public void destroyItem(View pager, int position, Object view) {
			((ViewPager) pager).removeView((View) view);
		}

		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		public void finishUpdate(View view) {
		}

		public void restoreState(Parcelable p, ClassLoader c) {
		}

		public Parcelable saveState() {
			return null;
		}

		public void startUpdate(View view) {
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		Intent i = getIntent();

		highlight = i.getStringExtra(Intents.HIGHLIGHT);

		initActionBar();

		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.titlebar);

		// Attach view
		setContentView(R.layout.epgdetails);

		// detector = new SimpleGestureFilter(this, this);

		// state = (ImageView) findViewById(R.id.epg_timer_state);

		final Event epg = getApp().getCurrentEvent();
		if (epg == null) {
			finish();
		}

		final Event cEvent = epg;

		if (epg instanceof Timerable) {
			// timerable = (Timerable) cEvent;
		}

		adapter = new Adapter();
		pager = (ViewPager) findViewById(R.id.viewpager);
		pager.setOnPageChangeListener(this);
		pager.setAdapter(adapter);

		new VoidAsyncTask() {

			int counter = 0;

			@Override
			protected void onPreExecute() {
				setProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected Void doInBackground(Void... params) {
				// current event
				final VdrManagerApp app = (VdrManagerApp) getApplication();
				epgs = app.getCurrentEpgList();

				if (epgs.isEmpty()) {
					epgs.add(cEvent);
					return (Void) null;
				}

				for (Event e : epgs) {
					if (epg.equals(e)) {
						break;
					}
					counter++;
				}

				if (counter == epgs.size()) {// not found?
					epgs.add(0, cEvent);
					counter = 0;
				}
				return (Void) null;
			}

			@Override
			protected void onPostExecute(Void result) {

				// cEvent = epgs.get(counter);
				pager.setCurrentItem(counter);
				onPageSelected(counter);
				// current = counter;
				// indicator.setViewPager(pager);

				// publishEPG(epg);
			}
		}.execute((Void) null);

	}

	private void setState(ImageView view, int res) {
		view.setVisibility(View.VISIBLE);
		view.setImageResource(res);
	}

	private static String encode(String str, String enc) {
		try {
			return URLEncoder.encode(str, enc);
		} catch (Exception ex) {
			Log.w(TAG, ex);
			return URLEncoder.encode(str);
		}
	}

	public void publishEPG(final View view, int position) {

		Event event = epgs.get(position);

		Timerable timerable = null;

		if (event instanceof Timerable) {
			timerable = (Timerable) event;
		}

		view.setTag(event);
		// view.setTag(event);

		final EventFormatter formatter = new EventFormatter(event);

		final TextView title = (TextView) view
				.findViewById(R.id.epg_detail_title);
		String titleText = formatter.getTitle();
		title.setText(Utils.highlight(titleText, highlight));
		// title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize()
		// * (float) 1.3);

		((TextView) view.findViewById(R.id.epg_detail_time)).setText(formatter
				.getDate() + " " + formatter.getTime());

		TextView dura = (TextView) view.findViewById(R.id.epg_detail_duration);

		((TextView) view.findViewById(R.id.epg_detail_channel)).setText(event
				.getChannelName());
		// ((TextView) findViewById(R.id.epg_detail_date)).setText(formatter
		// .getLongDate());
		ImageView state = (ImageView) view.findViewById(R.id.epg_timer_state);
		if (timerable == null) {
			setState(state, R.drawable.timer_none);
		} else {

			TimerMatch match = timerable.getTimerMatch();

			switch (timerable.getTimerState()) {
			case Active:
				setState(state, Utils.getTimerStateDrawable(match,
						R.drawable.timer_active, R.drawable.timer_active_begin,
						R.drawable.timer_active_end));
				break;
			case Inactive:
				setState(state, Utils.getTimerStateDrawable(match,
						R.drawable.timer_inactive,
						R.drawable.timer_inactive_begin,
						R.drawable.timer_inactive_end));
				break;
			case Recording:
				setState(state, Utils.getTimerStateDrawable(match,
						R.drawable.timer_recording,
						R.drawable.timer_recording_begin,
						R.drawable.timer_recording_end));
				break;
			default:
				setState(state, R.drawable.timer_none);
			}
		}
		final TextView shortText = (TextView) view
				.findViewById(R.id.epg_detail_shorttext);
		shortText.setText(Utils.highlight(formatter.getShortText(), highlight));

		final TextView textView = (TextView) view
				.findViewById(R.id.epg_detail_description);
		textView.setText(Utils.highlight(formatter.getDescription(), highlight));

		if (event.getAudio().isEmpty() == false) {
			view.findViewById(R.id.audio_block).setVisibility(View.VISIBLE);
			final TextView audioTracks = (TextView) view
					.findViewById(R.id.epg_detail_audio);
			audioTracks.setText(Utils.formatAudio(this, event.getAudio()));
		} else {
			view.findViewById(R.id.audio_block).setVisibility(View.GONE);
		}

		TextView contentView = ((TextView) view
				.findViewById(R.id.epg_detail_cats));
		if (event.getContent().length > 0) {
			contentView.setVisibility(View.VISIBLE);
			contentView
					.setText(Utils.getContenString(this, event.getContent()));
		} else {
			contentView.setVisibility(View.GONE);
		}

		// copy color for separator lines
		// final int color = textView.getTextColors().getDefaultColor();
		// ((TextView) findViewById(R.id.epg_detail_separator_1))
		// .setBackgroundColor(color);

		int p = Utils.getProgress(event);

		((ProgressBar) view.findViewById(R.id.epg_detail_progress))
				.setProgress(p);
		int dm = Utils.getDuration(event);
		if (Utils.isLive(event)) {
			int rest = dm - (dm * p / 100);
			dura.setText(getString(R.string.epg_duration_template_live, rest,
					dm));
		} else {
			dura.setText(getString(R.string.epg_duration_template, dm));
		}

		// ((TextView) view.findViewById(R.id.epg_detail_separator_2))
		// .setBackgroundColor(color);

		// register button handler
		if (timerable == null) {
			view.findViewById(R.id.epg_event_create_timer).setVisibility(
					View.GONE);
		} else {
			setThisAsOnClickListener(view, R.id.epg_event_create_timer);
		}

		View b = view.findViewById(R.id.epg_event_imdb);

		if (Preferences.get().isShowImdbButton() == false) {
			b.setVisibility(View.GONE);
		} else {
			b.setVisibility(View.VISIBLE);
			b.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					startFilmDatabaseBrowseIntent(
							String.format(IMDB_BASE_URL, Preferences.get()
									.getImdbUrl())
									+ IMDB_URL_QUERY, view, IMDB_URL_ENCODING);
				}
			});
		}

		b = view.findViewById(R.id.epg_event_omdb);

		if (Preferences.get().isShowOmdbButton() == false) {
			b.setVisibility(View.GONE);
		} else {
			b.setVisibility(View.VISIBLE);
			b.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					startFilmDatabaseBrowseIntent(OMDB_URL, view,
							OMDB_URL_ENCODING);
				}
			});
		}

		b = view.findViewById(R.id.epg_event_tmdb);

		if (Preferences.get().isShowTmdbButton() == false) {
			b.setVisibility(View.GONE);
		} else {
			b.setVisibility(View.VISIBLE);
			b.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					startFilmDatabaseBrowseIntent(TMDB_URL, view,
							TMDB_URL_ENCODING);
				}
			});
		}

		b = view.findViewById(R.id.epg_event_livetv);
		if (Utils.isLive(event) == false
				&& (event instanceof Recording == false || Preferences.get()
						.isEnableRecStream() == false)) {
			b.setVisibility(View.GONE);
		} else {
			b.setVisibility(View.VISIBLE);
			setThisAsOnClickListener(b);
		}
		// setThisAsOnClickListener(view, R.id.epg_event_left);
		// setThisAsOnClickListener(view, R.id.epg_event_right);

		// set button text
		if (event instanceof Timer) {
			// timeButton.setText(R.string.epg_event_create_timer_text);
		} else {
			// timeButton.setText(R.string.epg_event_modify_timer_text);
		}

	}

	private void startFilmDatabaseBrowseIntent(String url, View view,
			String encoding) {
		final TextView title = (TextView) view
				.findViewById(R.id.epg_detail_title);
		url = String.format(url,
				encode(String.valueOf(title.getText()), encoding));
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		i.addCategory(Intent.CATEGORY_BROWSABLE);
		try {
			startActivity(i);
		} catch (ActivityNotFoundException anfe) {
			Log.w(TAG, anfe);
			say(anfe.getLocalizedMessage());
		}
	}

	private void setThisAsOnClickListener(View v) {
		if (v != null) {
			v.setOnClickListener(this);
		}
	}

	private void setThisAsOnClickListener(View root, int view) {
		setThisAsOnClickListener(root.findViewById(view));
	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO Check here whether the config has changed for imdb
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	class Wrapper {
		public int id;
		public String value;

		public Wrapper(int id) {
			this.id = id;
			this.value = getString(id);
		}

		public String toString() {
			return value;
		}
	}

	public Timer getTimer(Event event) {
		if (event instanceof Timer) {
			return (Timer) event;
		}
		if (event instanceof Epg) {
			return ((Epg) event).getTimer();
		}
		return null;
	}

	protected VdrManagerApp getApp() {
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		return app;
	}

	public void onClick(final View v) {

		final Event cEvent = epgs.get(pager.getCurrentItem());

		switch (v.getId()) {
		case R.id.epg_event_livetv:
			if (cEvent instanceof Recording) {
				Utils.streamRecording(this, (Recording) cEvent);
			} else {
				Utils.stream(this, String.valueOf(cEvent.getChannelNumber()));
			}
			break;
		case R.id.epg_event_create_timer:
			final ArrayAdapter<Wrapper> ada = new ArrayAdapter<Wrapper>(this,
					android.R.layout.simple_dropdown_item_1line);
			final Timer timer = getTimer(cEvent);
			TimerMatch tm = Utils.getTimerMatch(cEvent, timer);
			// remove unneeded menu items
			if (timer != null && tm == TimerMatch.Full) {
				ada.add(new Wrapper(R.string.epg_item_menu_timer_modify));
				ada.add(new Wrapper(R.string.epg_item_menu_timer_delete));
				if (timer.isEnabled()) {
					ada.add(new Wrapper(R.string.epg_item_menu_timer_disable));
				} else {
					ada.add(new Wrapper(R.string.epg_item_menu_timer_enable));
				}
			} else if (cEvent instanceof Recording) {
				ada.add(new Wrapper(R.string.epg_item_menu_timer_delete));
			} else {

				ada.add(new Wrapper(R.string.epg_item_menu_timer_add));
				if (Utils.isLive(cEvent) && (cEvent instanceof Timerable)
						&& ((Timerable) cEvent).getTimer() == null) {
					ada.add(new Wrapper(R.string.epg_item_menu_timer_record));
				}
			}

			final Timerable timerable;

			if (cEvent instanceof Timerable) {
				timerable = (Timerable) cEvent;
			} else {
				return;
			}

			new AlertDialog.Builder(this)
					.setAdapter(ada, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Wrapper w = ada.getItem(which);
							switch (w.id) {
							case R.string.epg_item_menu_timer_add: {
								getApp().setCurrentTimer(
										timerable.createTimer());
								final Intent intent = new Intent();
								intent.setClass(EpgDetailsActivity.this,
										TimerDetailsActivity.class);
								intent.putExtra(Intents.TIMER_OP,
										Intents.ADD_TIMER);
								startActivityForResult(
										intent,
										TimerDetailsActivity.REQUEST_CODE_TIMER_ADD);
								break;
							}
							case R.string.epg_item_menu_timer_modify: {
								getApp().setCurrentTimer(timer);
								final Intent intent = new Intent();
								intent.setClass(EpgDetailsActivity.this,
										TimerDetailsActivity.class);
								intent.putExtra(Intents.TIMER_OP,
										Intents.EDIT_TIMER);
								startActivityForResult(
										intent,
										TimerDetailsActivity.REQUEST_CODE_TIMER_EDIT);
								break;
							}
							case R.string.epg_item_menu_timer_delete: {
								deleteTimer(timer);
								break;
							}
							case R.string.epg_item_menu_timer_enable:
							case R.string.epg_item_menu_timer_disable: {
								toggleTimer(timer);
								break;
							}

							case R.string.epg_item_menu_timer_record: {
								final Timer timer = new Timer(cEvent);
								final CreateTimerTask task = new CreateTimerTask(
										EpgDetailsActivity.this, timer) {
									@Override
									public void finished(SvdrpEvent event) {
										modifed = true;
										EpgCache.CACHE.remove(timer
												.getChannelId());
										say(R.string.recording_started);
									}
								};
								task.start();

							}
							}
						}
					}).create()//
					.show();//

			break;
		// case R.id.epg_event_imdb:

		// break;

		// case R.id.epg_event_share:
		// shareEvent(cEvent);
		// break;
		}
	}

	protected void toggleTimer(final Timer timer) {
		final ToggleTimerTask task = new ToggleTimerTask(this, timer) {
			@Override
			public void finished(SvdrpEvent event) {
				if (event == SvdrpEvent.FINISHED_SUCCESS) {
					TimerState state = timer.getTimerState();
					TimerMatch match = timer.getTimerMatch();
					int res = -1;
					if (state == TimerState.Active) {
						res = Utils.getTimerStateDrawable(match,
								R.drawable.timer_inactive,
								R.drawable.timer_inactive_begin,
								R.drawable.timer_inactive_end);
					} else if (state == TimerState.Inactive) {
						Utils.getTimerStateDrawable(match,
								R.drawable.timer_active,
								R.drawable.timer_active_begin,
								R.drawable.timer_active_end);
						res = R.drawable.timer_active;
					}
					if (res != -1) {
						setState(
								(ImageView) findViewById(R.id.epg_timer_state),
								res);
					}
				}
			}
		};
		task.start();
	}

	private List<Event> epgs = new ArrayList<Event>();

	protected void say(int res) {
		Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
	}

	protected void say(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public final boolean onCreateOptionsMenu(
			com.actionbarsherlock.view.Menu menu) {
		super.onCreateOptionsMenu(menu);

		final com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.epg_details, menu);

		// mShareActionProvider = (ShareActionProvider)
		// menu.findItem(R.id.epg_details_menu_share).getActionProvider();
		// mShareActionProvider.setShareIntent(getDefaultShareIntent());

		return true;
	}

	private void shareEvent(Event event) {
		Utils.shareEvent(this, event);
	}

	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {

		Event cEvent = epgs.get(pager.getCurrentItem());

		if (item.getItemId() == R.id.epg_details_menu_share) {
			shareEvent(cEvent);
			return true;
		}

		if (item.getItemId() == R.id.epg_details_menu_add_to_cal) {
			Utils.addCalendarEvent(this, cEvent);
		}

		if (item.getItemId() == R.id.epg_details_menu_search_repeat) {
			Intent intent = new Intent(this, EpgSearchListActivity.class);
			intent.setAction(Intent.ACTION_SEARCH);
			intent.putExtra(SearchManager.QUERY, cEvent.getTitle());
			startActivity(intent);
			return true;
		}

		if (item.getItemId() == R.id.epg_details_menu_switch) {
			Utils.switchTo(this, cEvent.getChannelId(), cEvent.getChannelName());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void deleteTimer(final Timer timer) {
		final DeleteTimerTask task = new DeleteTimerTask(this, timer) {
			@Override
			public void finished(SvdrpEvent event) {
				if (event == SvdrpEvent.FINISHED_SUCCESS) {
					setState((ImageView) findViewById(R.id.epg_timer_state),
							R.drawable.timer_none);
					modifed = true;
					EpgCache.CACHE.remove(timer.getChannelId());
				}
			}
		};
		task.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}

		// View view = pager.getChildAt(current);
		// ImageView state = (ImageView)
		// view.findViewById(R.id.epg_timer_state);

		if (requestCode == TimerDetailsActivity.REQUEST_CODE_TIMER_ADD) {
			modifed = true;
			// setState(
			// state,
			// Utils.isLive(getApp().getCurrentEvent()) ?
			// R.drawable.timer_recording
			// : R.drawable.timer_active);
		} else if (requestCode == TimerDetailsActivity.REQUEST_CODE_TIMER_EDIT) {
			modifed = true;
			// ??
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		if (modifed) {
			setResult(RESULT_OK);
			finish();
		} else {
			super.onBackPressed();
		}
	}

	public void onPageScrollStateChanged(int state) {
	}

	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	public void onPageSelected(int position) {

		Event cEvent = epgs.get(position);
		String cn = cEvent.getChannelName();
		// View view = pager.getChildAt(arg0);
		// state = (ImageView) view.findViewById(R.id.epg_timer_state);
		setTitle(getString(R.string.epg_of_a_channel, cn, position + 1,
				epgs.size()));
	}

}
