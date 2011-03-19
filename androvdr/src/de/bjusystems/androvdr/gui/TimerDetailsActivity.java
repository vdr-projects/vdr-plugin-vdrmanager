package de.bjusystems.androvdr.gui;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import de.bjusystems.androvdr.app.AndroVdrApp;
import de.bjusystems.androvdr.data.Preferences;
import de.bjusystems.androvdr.data.Timer;
import de.bjusystems.androvdr.utils.date.DateFormatter;
import de.bjusystems.androvdr.utils.svdrp.SetTimerClient;
import de.bjusystems.androvdr.utils.svdrp.SvdrpAsyncListener;
import de.bjusystems.androvdr.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.androvdr.utils.svdrp.SvdrpClient;
import de.bjusystems.androvdr.utils.svdrp.SvdrpEvent;
import de.bjusystems.androvdr.utils.svdrp.SvdrpException;
import de.bjusystems.androvdr.R;

/**
 * This class is used for showing what's
 * current running on all channels
 * @author bju
 */
public class TimerDetailsActivity extends Activity
				implements OnClickListener, OnDateSetListener, OnTimeSetListener, SvdrpAsyncListener<Timer> {

	Preferences prefs;
	TextView dateField;
	TextView startField;
	TextView endField;
	boolean editStart;
	Timer timer;
	SvdrpProgressDialog progress;
	SetTimerClient setTimerClient;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		setContentView(R.layout.timer_detail);

    // timer
    timer = ((AndroVdrApp)getApplication()).getCurrentTimer();

    // update display
    updateDisplay();

    // register buttons
    final Button saveButton = (Button) findViewById(R.id.timer_details_save);
    saveButton.setOnClickListener(this);
    if (timer.getNumber() > 0) {
    	saveButton.setText(R.string.timer_details_save_title);
    } else {
    	saveButton.setText(R.string.timer_details_create_title);
    }

    // register text fields for editing
    dateField.setOnClickListener(this);
    startField.setOnClickListener(this);
    endField.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void onClick(final View view) {
		switch (view.getId()) {
		case R.id.timer_detail_day:
		{
			final Calendar cal = new GregorianCalendar();
			cal.setTime(timer.getStart());
			final DatePickerDialog dialog = new DatePickerDialog(this, this, cal.get(Calendar.YEAR),
																									cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
			dialog.show();
			break;
		}
		case R.id.timer_detail_start:
		{
			final Calendar cal = new GregorianCalendar();
			cal.setTime(timer.getStart());
			editStart = true;
			final TimePickerDialog dialog = new TimePickerDialog(this, this, cal.get(Calendar.HOUR_OF_DAY),
																									cal.get(Calendar.MINUTE), true);
			dialog.show();
			break;
		}
		case R.id.timer_detail_end:
		{
			final Calendar cal = new GregorianCalendar();
			cal.setTime(timer.getStop());
			editStart = false;
			final TimePickerDialog dialog = new TimePickerDialog(this, this, cal.get(Calendar.HOUR_OF_DAY),
																									cal.get(Calendar.MINUTE), true);
			dialog.show();
			break;
		}
		case R.id.timer_details_save:
		{
			// collect values
	    timer.setTitle(((TextView)findViewById(R.id.timer_detail_title)).getText().toString());

			// create client for saving the timer
			setTimerClient = new SetTimerClient(timer, false);

			// create backgound task
			final SvdrpAsyncTask<Timer, SvdrpClient<Timer>> task = new SvdrpAsyncTask<Timer, SvdrpClient<Timer>>(setTimerClient);

			// create progress
			progress = new SvdrpProgressDialog(this, setTimerClient);

			// attach listener
			task.addListener(this);

			// start task
			task.run();
		}
		}
	}

	public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
		if (editStart) {
			timer.setStart(calculateTime(timer.getStart(), hourOfDay, minute, null));
		} else {
			timer.setStop(calculateTime(timer.getStop(), hourOfDay, minute, timer.getStart()));
		}
		updateDisplay();
	}

	public void onDateSet(final DatePicker view, final int year, final int monthOfYear, final int dayOfMonth) {
		timer.setStart(calculateDate(timer.getStart(), year, monthOfYear, dayOfMonth));
		updateDisplay();
	}

	private Date calculateDate(final Date oldDate, final int year, final int monthOfYear, final int dayOfMonth) {

		final Calendar cal = new GregorianCalendar();
		cal.setTime(oldDate);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, monthOfYear);
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

		return cal.getTime();
	}

	private Date calculateTime(final Date oldTime, final int hourOfDay, final int minute, final Date startTime) {

		// set hour and minute
		final Calendar cal = new GregorianCalendar();
		cal.setTime(oldTime);
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minute);

		// go to the next day if end time before start time
		if (startTime != null) {
			if (cal.getTime().before(startTime)) {
				cal.add(Calendar.DAY_OF_MONTH, 1);
			}
		}

		return cal.getTime();

	}

	private void updateDisplay() {

    ((TextView)findViewById(R.id.timer_detail_title)).setText(timer.getTitle());
    ((TextView)findViewById(R.id.timer_detail_channel)).setText(timer.getChannelName());
    dateField = (TextView)findViewById(R.id.timer_detail_day);
    final DateFormatter dateFormatter = new DateFormatter(timer.getStart());
    dateField.setText(dateFormatter.getDateString());
    startField = (TextView)findViewById(R.id.timer_detail_start);
    startField.setText(dateFormatter.getTimeString());
    endField = (TextView)findViewById(R.id.timer_detail_end);
    endField.setText(new DateFormatter(timer.getStop()).getTimeString());

    final Button button = (Button) findViewById(R.id.timer_details_save);
    if (timer.getNumber() > 0) {
    	// existing timer
    	button.setText(R.string.timer_details_save_title);
    } else {
    	// new timer
    	button.setText(R.string.timer_details_create_title);
    }
	}

	public void svdrpEvent(final SvdrpEvent event, final Timer result) {

		progress.svdrpEvent(event);

		switch (event) {
		case FINISHED:
			// remove this activity from stack
			finish();

			// finish previous activities
			final AndroVdrApp app = (AndroVdrApp) getApplication();
			app.finishActivities();

			// refresh last view
			app.setReload(true);

			// free progress dialog
			progress = null;

			break;
		}
	}

	public void svdrpException(final SvdrpException exception) {
		progress.svdrpException(exception);
	}
}
