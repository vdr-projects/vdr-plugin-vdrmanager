package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.gui.SimpleGestureFilter.SimpleGestureListener;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

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

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
	
		highlight = i.getStringExtra(Intents.HIGHLIGHT);

		// Attach view
		setContentView(R.layout.epg_detail);

		detector = new SimpleGestureFilter(this, this);

		// event_left = (ImageButton) findViewById(R.id.epg_event_left);
		// event_right = (ImageButton) findViewById(R.id.epg_event_right);

		// current event
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		epgs = app.getCurrentEpgList();
		Event epg = app.getCurrentEvent();

		counter = 0;
		for (Event e : epgs) {
			if (epg == e) {
				break;
			}
			counter++;
		}

		publishEPG(epg);
	}

	private void setState(ImageView view, int res) {
		view.setVisibility(View.VISIBLE);
		view.setImageResource(res);
	}

	public void publishEPG(Event event) {

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
		ImageView state = (ImageView) findViewById(R.id.epg_timer_state);

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
		// setThisAsOnClickListener(R.id.epg_event_left);
		// setThisAsOnClickListener(R.id.epg_event_right);

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
		// TODO check here if we are still live ?

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.epg_event_livetv:
			Utils.stream(this, cEvent.getChannelNumber());
			break;
		case R.id.epg_event_create_timer:
			Toast.makeText(this, "Soon we get here the timer menu",
					Toast.LENGTH_SHORT).show();
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
//		case R.id.epg_event_share:
//			shareEvent(cEvent);
//			break;
		}
	}

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
			epg = epgs.get(0);
		} else {
			epg = epgs.get(--counter);
		}

		publishEPG(epg);

	}

	List<Event> epgs = new ArrayList<Event>();
	
	int counter = 0;
/*
	public void initEPGs() {

		if (epgs != null) {
			return;
		}
		epgs = new ArrayList<Event>();

		final VdrManagerApp app = (VdrManagerApp) getApplication();
		final Event event = app.getCurrentEvent();
		EpgClient c = new EpgClient(new Channel() {
			@Override
			public String getName() {
				return event.getChannelName();
			}

			@Override
			public int getNumber() {
				return Integer.valueOf(event.getChannelNumber());
			}
		});

		try {
			c.run();
		} catch (SvdrpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<Epg> e = c.getResults();
		if (e == null || e.isEmpty()) {
			return;
		}

		epgs.addAll(e);
		Event fe = epgs.get(0);
		if (event.getStart().equals(fe.getStart()) == false) {
			epgs.set(0, event);
			;
		}
	}
*/
	private void nextEPG() {
		if (counter < epgs.size() - 1) {
			counter++;
		}
		Event epg = epgs.get(counter);
		publishEPG(epg);
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
}
