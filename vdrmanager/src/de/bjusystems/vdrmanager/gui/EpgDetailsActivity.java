package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.gui.SimpleGestureFilter.SimpleGestureListener;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpException;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class EpgDetailsActivity extends Activity implements OnClickListener,
		SimpleGestureListener {

	Preferences prefs;

	private SimpleGestureFilter detector;
	
	ImageButton event_left;
	
	ImageButton event_right;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		setContentView(R.layout.epg_detail);
		detector = new SimpleGestureFilter(this, this);

		//event_left = (ImageButton) findViewById(R.id.epg_event_left);
		//event_right = (ImageButton) findViewById(R.id.epg_event_right);
		
		
		// current event
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		epgs = app.getCurrentEpgList();
		Epg epg = app.getCurrentEvent();
	
		
		counter = 0;
		for(Epg e : epgs){
			if(epg == e){
				break;
			}
			counter++;
		}
		

		
		new AsyncTask<Void,Void,Void>(){
			
			protected void onPreExecute() {
//				event_left.setEnabled(false);
	//			event_right.setEnabled(false);
			};
			
			protected  Void doInBackground(Void... params) {
				initEPGs();
				return null;
			};
			
			protected void onPostExecute(Void result) {
		//		event_left.setEnabled(true);
			//	event_right.setEnabled(true);
			};
			
		}.execute((Void)null);

		publishEPG(epg);
		// final EventFormatter formatter = new EventFormatter(event);
		//
		// final TextView title = (TextView)
		// findViewById(R.id.epg_detail_title);
		// title.setText(formatter.getTitle());
		// title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize()
		// * (float) 1.3);
		//
		// ((TextView) findViewById(R.id.epg_detail_time)).setText(formatter
		// .getTime());
		// ((TextView) findViewById(R.id.epg_detail_channel)).setText(event
		// .getChannelName());
		// ((TextView) findViewById(R.id.epg_detail_date)).setText(formatter
		// .getLongDate());
		// final TextView textView = (TextView)
		// findViewById(R.id.epg_detail_description);
		// textView.setText(formatter.getDescription());
		//
		// // copy color for separator lines
		// final int color = textView.getTextColors().getDefaultColor();
		// // ((TextView) findViewById(R.id.epg_detail_separator_1))
		// // .setBackgroundColor(color);
		//
		// ((ProgressBar) findViewById(R.id.epg_detail_progress))
		// .setProgress(Utils.getProgress(event));
		//
		// ((TextView) findViewById(R.id.epg_detail_separator_2))
		// .setBackgroundColor(color);
		//
		// // register button handler
		// final ImageButton timeButton = (ImageButton)
		// findViewById(R.id.epg_event_create_timer);
		// timeButton.setOnClickListener(this);
		//
		// final ImageButton livetvButton = (ImageButton)
		// findViewById(R.id.epg_event_livetv);
		// livetvButton.setOnClickListener(this);
		//
		//
		// // set button text
		// if (event.getTimer() == null) {
		// // timeButton.setText(R.string.epg_event_create_timer_text);
		// } else {
		// // timeButton.setText(R.string.epg_event_modify_timer_text);
		// }

		// clear list of activities to finish
		app.clearActivitiesToFinish();
	}

	public void publishEPG(Epg event) {
		
		String cn = event.getChannelName();
		
		setTitle(getString(R.string.epg_of_a_channel,cn));
		
		final EventFormatter formatter = new EventFormatter(event);

		final TextView title = (TextView) findViewById(R.id.epg_detail_title);
		title.setText(formatter.getTitle());
		// title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize()
		// * (float) 1.3);

		((TextView) findViewById(R.id.epg_detail_time)).setText(formatter
				.getDate() + " " + formatter.getTime());
		
		TextView dura = (TextView) findViewById(R.id.epg_detail_duration);
		
		((TextView) findViewById(R.id.epg_detail_channel)).setText(event
				.getChannelName());
		//((TextView) findViewById(R.id.epg_detail_date)).setText(formatter
			//	.getLongDate());
		
		final TextView shortText = (TextView) findViewById(R.id.epg_detail_shorttext);
		shortText.setText(formatter.getShortText());

		
		final TextView textView = (TextView) findViewById(R.id.epg_detail_description);
		textView.setText(formatter.getDescription());

		// copy color for separator lines
		final int color = textView.getTextColors().getDefaultColor();
		// ((TextView) findViewById(R.id.epg_detail_separator_1))
		// .setBackgroundColor(color);

		int p = Utils.getProgress(event);
		
		((ProgressBar) findViewById(R.id.epg_detail_progress))
				.setProgress(p);
		int dm = Utils.getDuration(event);
		if(Utils.isLive(event)){
			int rest = dm - (dm * p / 100);
			dura.setText(getString(R.string.epg_duration_template_live, rest, dm));
		} else {
			dura.setText(getString(R.string.epg_duration_template, dm));
		}

		((TextView) findViewById(R.id.epg_detail_separator_2))
				.setBackgroundColor(color);

		// register button handler
		setThisAsOnClickListener(R.id.epg_event_create_timer);
		View b = findViewById(R.id.epg_event_livetv);
		if (Utils.isLive(event) == false) {
			b.setVisibility(View.GONE);
		} else {
			b.setVisibility(View.VISIBLE);
			setThisAsOnClickListener(b);
		}
//		setThisAsOnClickListener(R.id.epg_event_left);
//		setThisAsOnClickListener(R.id.epg_event_right);

		// set button text
		if (event.getTimer() == null) {
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
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void onClick(final View v) {

		final VdrManagerApp app = (VdrManagerApp) getApplication();
		switch (v.getId()) {
		case R.id.epg_event_livetv:
			Epg event = app.getCurrentEvent();
			Utils.stream(this, event.getChannelNumber());
			break;
		case R.id.epg_event_create_timer:
			Toast.makeText(this, "Soon we get here the timer menu", Toast.LENGTH_SHORT).show();
			break;

//		case R.id.epg_event_left:
//			prevEPG();
//			break;
//		case R.id.epg_event_right:
//			nextEPG();
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
		Epg epg;
		if (counter == 0) {
			epg = epgs.get(0);
		} else {
			epg = epgs.get(--counter);
		}

		publishEPG(epg);

	}

	ArrayList<Epg> epgs = null;
	int counter = 0;

	public void initEPGs() {
		
	//	epgs = ((VdrManagerApp)getApplication()).getCurrentEpgList();
		
		if (epgs != null) {
			return;
		}
		epgs = new ArrayList<Epg>();

		final VdrManagerApp app = (VdrManagerApp) getApplication();
		final Epg event = app.getCurrentEvent();
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
		Epg fe = epgs.get(0);
		if (event.getStart().equals(fe.getStart()) == false) {
			epgs.set(0, event);
			;
		}
	}

	private void nextEPG() {
		if(counter < epgs.size() - 1){
			counter ++ ;
		}
		Epg epg = epgs.get(counter);
		publishEPG(epg);
	}

	public void onDoubleTap() {

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		this.detector.onTouchEvent(me);
		return super.dispatchTouchEvent(me);
	}

}
