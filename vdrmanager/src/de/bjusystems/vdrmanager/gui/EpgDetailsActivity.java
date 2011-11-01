package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.Event.TimerState;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Recording;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.gui.SimpleGestureFilter.SimpleGestureListener;
import de.bjusystems.vdrmanager.tasks.DeleteTimerTask;
import de.bjusystems.vdrmanager.tasks.ModifyTimerTask;
import de.bjusystems.vdrmanager.tasks.ToggleTimerTask;
import de.bjusystems.vdrmanager.tasks.VoidAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class EpgDetailsActivity extends Activity implements OnClickListener,
		SimpleGestureListener {

	public static String IMDB_URL = "http://%s/find?s=all&q=%s";

	private SimpleGestureFilter detector;

	private String highlight = null;

	ImageButton event_left;

	ImageButton event_right;

	Event cEvent;

	ImageView state;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		Intent i = getIntent();

		highlight = i.getStringExtra(Intents.HIGHLIGHT);

		// Attach view
		setContentView(R.layout.epg_detail);

		detector = new SimpleGestureFilter(this, this);

		event_left = (ImageButton) findViewById(R.id.epg_event_left);
		event_right = (ImageButton) findViewById(R.id.epg_event_right);
		state = (ImageView) findViewById(R.id.epg_timer_state);

		new VoidAsyncTask() {

			private Event epg;

			@Override
			protected void onPreExecute() {
				setProgressBarIndeterminateVisibility(true);
			}

			@Override
			protected Void doInBackground(Void... params) {
				// current event
				final VdrManagerApp app = (VdrManagerApp) getApplication();
				epgs = app.getCurrentEpgList();
				epg = app.getCurrentEvent();

				counter = 0;
				for (Event e : epgs) {
					if (epg == e) {
						break;
					}
					counter++;
				}
				return (Void) null;
			}

			@Override
			protected void onPostExecute(Void result) {
				publishEPG(epg);
			}
		}.execute((Void) null);

	}

	private void setState(ImageView view, int res) {
		view.setVisibility(View.VISIBLE);
		view.setImageResource(res);
	}

	public void publishEPG(final Event event) {
		setProgressBarIndeterminateVisibility(true);
		publishEPGImpl(event);
		setProgressBarIndeterminateVisibility(false);
	}

	public void publishEPGImpl(Event event) {

		cEvent = event;

		String cn = event.getChannelName();

		setTitle(getString(R.string.epg_of_a_channel, cn));

		final EventFormatter formatter = new EventFormatter(event);

		final TextView title = (TextView) findViewById(R.id.epg_detail_title);
		String titleText = formatter.getTitle();
		title.setText(Utils.highlight(titleText, highlight));
		// title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize()
		// * (float) 1.3);

		((TextView) findViewById(R.id.epg_detail_time)).setText(formatter
				.getDate() + " " + formatter.getTime());

		TextView dura = (TextView) findViewById(R.id.epg_detail_duration);

		((TextView) findViewById(R.id.epg_detail_channel)).setText(event
				.getChannelName());
		// ((TextView) findViewById(R.id.epg_detail_date)).setText(formatter
		// .getLongDate());
		

		switch (event.getTimerState()) {
		case Active:
			setState(state, R.drawable.timer_active);
			break;
		case Inactive:
			setState(state, R.drawable.timer_inactive);
			break;
		case Recording:
			setState(state, R.drawable.timer_recording);
			break;
		default:
			setState(state, R.drawable.timer_none);
		}

		final TextView shortText = (TextView) findViewById(R.id.epg_detail_shorttext);
		shortText.setText(Utils.highlight(formatter.getShortText(), highlight));

		final TextView textView = (TextView) findViewById(R.id.epg_detail_description);
		textView.setText(Utils.highlight(formatter.getDescription(), highlight));

		// copy color for separator lines
		final int color = textView.getTextColors().getDefaultColor();
		// ((TextView) findViewById(R.id.epg_detail_separator_1))
		// .setBackgroundColor(color);

		int p = Utils.getProgress(event);

		((ProgressBar) findViewById(R.id.epg_detail_progress)).setProgress(p);
		int dm = Utils.getDuration(event);
		if (Utils.isLive(event)) {
			int rest = dm - (dm * p / 100);
			dura.setText(getString(R.string.epg_duration_template_live, rest,
					dm));
		} else {
			dura.setText(getString(R.string.epg_duration_template, dm));
		}

		((TextView) findViewById(R.id.epg_detail_separator_2))
				.setBackgroundColor(color);

		// register button handler
		setThisAsOnClickListener(R.id.epg_event_create_timer);

		View b = findViewById(R.id.epg_event_imdb);

		if (Preferences.get().isShowImdbButton() == false) {
			b.setVisibility(View.GONE);
		} else {
			b.setVisibility(View.VISIBLE);
			setThisAsOnClickListener(b);
		}

		b = findViewById(R.id.epg_event_livetv);
		if (Utils.isLive(event) == false) {
			b.setVisibility(View.GONE);
		} else {
			b.setVisibility(View.VISIBLE);
			setThisAsOnClickListener(b);
		}
		setThisAsOnClickListener(R.id.epg_event_left);
		setThisAsOnClickListener(R.id.epg_event_right);

		// set button text
		if (event instanceof Timer) {
			// timeButton.setText(R.string.epg_event_create_timer_text);
		} else {
			// timeButton.setText(R.string.epg_event_modify_timer_text);
		}

	}

	private void setThisAsOnClickListener(View v) {
		if (v != null) {
			v.setOnClickListener(this);
		}
	}

	private void setThisAsOnClickListener(int view) {
		setThisAsOnClickListener(findViewById(view));
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
		switch (v.getId()) {
		case R.id.epg_event_livetv:
			Utils.stream(this, cEvent.getChannelNumber());
			break;
		case R.id.epg_event_create_timer:
			final ArrayAdapter<Wrapper> ada = new ArrayAdapter<Wrapper>(this,
					R.layout.timer_operation_list_item);
			final Timer timer = getTimer(cEvent);
			// remove unneeded menu items
			if (timer != null) {
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
			}
			new AlertDialog.Builder(this)
					.setAdapter(ada, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							final Timer t;
							if (timer == null) {
								t = cEvent.createTimer();
							} else {
								t = timer;
							}
							getApp().setCurrentTimer(t);
							Wrapper w = ada.getItem(which);
							switch (w.id) {
							case R.string.epg_item_menu_timer_add: {
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
							}
						}
					}).create()//
					.show();//

			break;
		case R.id.epg_event_imdb:
			final TextView title = (TextView) findViewById(R.id.epg_detail_title);
			String url = String.format(IMDB_URL,
					Preferences.get().getImdbUrl(),
					String.valueOf(title.getText()));
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			break;
		case R.id.epg_event_left:
			prevEPG();
			break;
		case R.id.epg_event_right:
			nextEPG();
			break;
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
					int res = -1;
					if (state == TimerState.Active) {
						res = R.drawable.timer_inactive;
					} else if (state == TimerState.Inactive) {
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

	// EditTimerViewHolder tView = null;

	public void onSwipe(int direction) {
		switch (direction) {
		case SimpleGestureFilter.SWIPE_RIGHT:
			prevEPG();
			break;
		case SimpleGestureFilter.SWIPE_LEFT:
			nextEPG();
			break;
		}
	}

	private void prevEPG() {
		Event epg;
		if (counter == 0) {
			say(R.string.navigae_at_the_start);
			return;
		}
		epg = epgs.get(--counter);
		publishEPG(epg);
	}

	List<Event> epgs = new ArrayList<Event>();

	int counter = 0;

	/*
	 * public void initEPGs() {
	 * 
	 * if (epgs != null) { return; } epgs = new ArrayList<Event>();
	 * 
	 * final VdrManagerApp app = (VdrManagerApp) getApplication(); final Event
	 * event = app.getCurrentEvent(); EpgClient c = new EpgClient(new Channel()
	 * {
	 * 
	 * @Override public String getName() { return event.getChannelName(); }
	 * 
	 * @Override public int getNumber() { return
	 * Integer.valueOf(event.getChannelNumber()); } });
	 * 
	 * try { c.run(); } catch (SvdrpException e) { // TODO Auto-generated catch
	 * block e.printStackTrace(); }
	 * 
	 * List<Epg> e = c.getResults(); if (e == null || e.isEmpty()) { return; }
	 * 
	 * epgs.addAll(e); Event fe = epgs.get(0); if
	 * (event.getStart().equals(fe.getStart()) == false) { epgs.set(0, event); ;
	 * } }
	 */
	private void nextEPG() {
		if (counter < epgs.size() - 1) {
			counter++;
			Event epg = epgs.get(counter);
			publishEPG(epg);
		} else {
			say(R.string.navigae_at_the_end);
		}
	}

	protected void say(int res) {
		Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
	}

	protected void say(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void onDoubleTap() {

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		this.detector.onTouchEvent(me);
		return super.dispatchTouchEvent(me);
	}

	private static final int MENU_SHARE = 0;

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem item;
		item = menu.add(MENU_SHARE, MENU_SHARE, 0, R.string.share);
		item.setIcon(android.R.drawable.ic_menu_share);
		item.setAlphabeticShortcut('s');
		return true;
	}

	private void shareEvent(Event event) {
		Utils.shareEvent(this, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_SHARE) {
			shareEvent(cEvent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void modifyTimer(Timer timer) {
		final ModifyTimerTask task = new ModifyTimerTask(this, timer) {
			@Override
			public void finished(SvdrpEvent event) {

			}
		};
		task.start();
	}

	protected void deleteTimer(final Timer timer) {
		final DeleteTimerTask task = new DeleteTimerTask(this, timer) {
			@Override
			public void finished(SvdrpEvent event) {
				if (event == SvdrpEvent.FINISHED_SUCCESS) {
					setState((ImageView) findViewById(R.id.epg_timer_state),
							R.drawable.timer_none);
				}
			}
		};
		task.start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != RESULT_OK){
			return;
		}
		if(requestCode == TimerDetailsActivity.REQUEST_CODE_TIMER_ADD){
			setState(state, Utils.isLive(getApp().getCurrentEvent()) ? R.drawable.timer_recording :  R.drawable.timer_active);
		} else if( requestCode == TimerDetailsActivity.REQUEST_CODE_TIMER_EDIT){
			//??
		}
	}
	
}
