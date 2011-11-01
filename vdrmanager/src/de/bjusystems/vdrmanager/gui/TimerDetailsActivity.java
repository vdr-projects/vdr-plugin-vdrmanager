package de.bjusystems.vdrmanager.gui;

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
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.tasks.CreateTimerTask;
import de.bjusystems.vdrmanager.tasks.ModifyTimerTask;
import de.bjusystems.vdrmanager.utils.svdrp.SetTimerClient.TimerOperation;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;

public class TimerDetailsActivity extends Activity implements OnClickListener,
		OnDateSetListener, OnTimeSetListener {

	public static final int REQUEST_CODE_TIMER_EDIT = 84;
	
	public static final int REQUEST_CODE_TIMER_ADD = 85;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View view = getLayoutInflater().inflate(R.layout.timer_detail, null);
		tView = new EditTimerViewHolder();
		tView.view = view;
		tView.title = (TextView) view.findViewById(R.id.timer_detail_title);
		tView.channel = (TextView) view.findViewById(R.id.timer_detail_channel);
		tView.dateField = (TextView) view.findViewById(R.id.timer_detail_day);
		tView.startField = (TextView) view
				.findViewById(R.id.timer_detail_start);
		tView.endField = (TextView) view.findViewById(R.id.timer_detail_end);
		tView.saveButton = (Button) view.findViewById(R.id.timer_details_save);
		tView.modifyButton = (Button) view
				.findViewById(R.id.timer_details_modify);

		view.findViewById(R.id.timer_details_cancel).setOnClickListener(this);
		tView.dateField.setOnClickListener(this);
		tView.startField.setOnClickListener(this);
		tView.endField.setOnClickListener(this);
		tView.saveButton.setOnClickListener(this);
		tView.modifyButton.setOnClickListener(this);
		setContentView(view);
		timer = getApp().getCurrentTimer();
		int op = getIntent().getExtras().getInt(Intents.TIMER_OP);
		switch (op) {
		case Intents.ADD_TIMER:
			setTitle(R.string.timer_details_add_title);
			add();
			break;
		case Intents.EDIT_TIMER:
			setTitle(R.string.timer_details_modify_title);
			modify();
			break;

		default:
			finish();
		}

	}

	public class EditTimerViewHolder {
		View view;
		TextView title;
		TextView channel;
		TextView dateField;
		TextView startField;
		TextView endField;
		Button saveButton;
		Button modifyButton;
	}

	EditTimerViewHolder tView = null;

	boolean editStart;

	// SetTimerClient setTimerClient;

	Timer timer;

	private void updateDisplay(TimerOperation op) {
		updateDisplay();
		switch (op) {
		case CREATE:
			tView.modifyButton.setVisibility(View.GONE);
			tView.saveButton.setVisibility(View.VISIBLE);
			tView.saveButton.setText(R.string.timer_details_create_title);
			break;
		case MODIFY:
			tView.saveButton.setVisibility(View.GONE);
			tView.modifyButton.setVisibility(View.VISIBLE);
			tView.saveButton.setText(R.string.timer_details_save_title);
			break;
		}

	}

	private void updateDisplay() {
		EventFormatter f = new EventFormatter(timer, true);
		tView.channel.setText(timer.getChannelNumber() + " "
				+ timer.getChannelName());
		tView.title.setText(f.getTitle());
		tView.dateField.setText(f.getDate());
		tView.startField.setText(f.getTime());
		tView.endField.setText(f.getStop());
	}

	protected VdrManagerApp getApp() {
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		return app;
	}

	public void add() {
		updateDisplay(TimerOperation.CREATE);
	}

	public void modify() {
		updateDisplay(TimerOperation.MODIFY);
	}

	public void onClick(final View view) {
		switch (view.getId()) {
		case R.id.timer_detail_day: {
			final Calendar cal = new GregorianCalendar();
			cal.setTime(timer.getStart());
			final DatePickerDialog dialog = new DatePickerDialog(this, this,
					cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
					cal.get(Calendar.DAY_OF_MONTH));
			dialog.show();
			break;
		}
		case R.id.timer_detail_start: {
			final Calendar cal = new GregorianCalendar();
			cal.setTime(timer.getStart());
			editStart = true;
			final TimePickerDialog dialog = new TimePickerDialog(this, this,
					cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
					true);
			dialog.show();
			break;
		}
		case R.id.timer_detail_end: {
			final Calendar cal = new GregorianCalendar();
			cal.setTime(timer.getStop());
			editStart = false;
			final TimePickerDialog dialog = new TimePickerDialog(this, this,
					cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
					true);
			dialog.show();
			break;
		}
		case R.id.timer_details_cancel: {
			finishActivity(REQUEST_CODE_TIMER_EDIT);
			finish();
			break;
		}
		case R.id.timer_details_modify:
			modifyTimer(timer);
			say(R.string.done);
			break;

		case R.id.timer_details_save: {
			createTimer(timer);
			say(R.string.done);
			break;
		}
	
		}
	}

	protected void say(int res) {
		Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
	}

	public void onTimeSet(final TimePicker view, final int hourOfDay,
			final int minute) {
		if (editStart) {
			timer.setStart(calculateTime(timer.getStart(), hourOfDay, minute,
					null));
		} else {
			timer.setStop(calculateTime(timer.getStop(), hourOfDay, minute,
					timer.getStart()));
		}
		updateDisplay();
	}

	public void onDateSet(final DatePicker view, final int year,
			final int monthOfYear, final int dayOfMonth) {
		timer.setStart(calculateDate(timer.getStart(), year, monthOfYear,
				dayOfMonth));
		updateDisplay();
	}

	private Date calculateDate(final Date oldDate, final int year,
			final int monthOfYear, final int dayOfMonth) {

		final Calendar cal = new GregorianCalendar();
		cal.setTime(oldDate);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, monthOfYear);
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

		return cal.getTime();
	}

	private Date calculateTime(final Date oldTime, final int hourOfDay,
			final int minute, final Date startTime) {

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

	private void createTimer(Timer timer) {
		final CreateTimerTask task = new CreateTimerTask(this, timer) {
			@Override
			public void finished(SvdrpEvent event) {
				done();
			}
		};
		task.start();
	}

	public void done() {
		setResult(RESULT_OK);
		finish();
	}

	private void modifyTimer(Timer timer) {
		final ModifyTimerTask task = new ModifyTimerTask(this, timer) {
			@Override
			public void finished(SvdrpEvent event) {
				done();
			}
		};
		task.start();
	}

}
