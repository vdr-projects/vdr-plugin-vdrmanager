package de.bjusystems.vdrmanager.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.Preferences;

/**
 * This class is used for showing what's
 * current running on all channels
 * @author bju
 */
public class EpgDetailsActivity extends Activity
				implements OnClickListener {

	Preferences prefs;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		setContentView(R.layout.epg_detail);


    // current event
		final VdrManagerApp app = (VdrManagerApp) getApplication();
    final Epg event = app.getCurrentEvent();
    final EventFormatter formatter = new EventFormatter(event);

    final TextView title = (TextView)findViewById(R.id.epg_detail_title);
    title.setText(formatter.getTitle());
    title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize()*(float)1.3);

    ((TextView) findViewById(R.id.epg_detail_time)).setText(formatter.getTime());
    ((TextView)findViewById(R.id.epg_detail_channel)).setText(event.getChannelName());
    ((TextView)findViewById(R.id.epg_detail_date)).setText(formatter.getLongDate());
    final TextView textView = (TextView)findViewById(R.id.epg_detail_description);
    textView.setText(formatter.getDescription());

    // copy color for separator lines
    final int color = textView.getTextColors().getDefaultColor();
    ((TextView)findViewById(R.id.epg_detail_separator_1)).setBackgroundColor(color);
    ((TextView)findViewById(R.id.epg_detail_separator_2)).setBackgroundColor(color);

    // register button handler
    final Button timeButton = (Button) findViewById(R.id.epg_event_create_timer);
    timeButton.setOnClickListener(this);

    // set button text
    if (event.getTimer() == null) {
    	timeButton.setText(R.string.epg_event_create_timer_text);
    } else {
    	timeButton.setText(R.string.epg_event_modify_timer_text);
    }

    // clear list of activities to finish
    app.clearActivitiesToFinish();
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

		// after timer creation/modification return to the epg list
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		app.addActivityToFinish(this);

		// show timer details
		final Intent intent = new Intent();
		intent.setClass(this, TimerDetailsActivity.class);
		startActivity(intent);
	}
}
