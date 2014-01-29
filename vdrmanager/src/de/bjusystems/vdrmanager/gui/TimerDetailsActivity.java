package de.bjusystems.vdrmanager.gui;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.EpgCache;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.tasks.CreateTimerTask;
import de.bjusystems.vdrmanager.tasks.ModifyTimerTask;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.SetTimerClient.TimerOperation;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;

public class TimerDetailsActivity extends Activity implements OnClickListener,
		OnDateSetListener, OnTimeSetListener {

	public static final int REQUEST_CODE_TIMER_MODIFIED = 34;

	public static final int REQUEST_CODE_TIMER_EDIT = 35;

	public static final int REQUEST_CODE_TIMER_ADD = 36;

	private CharSequence prevStart;

	private CharSequence prevEnd;

	private CharSequence prevDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = getLayoutInflater().inflate(R.layout.timer_detail, null);
		tView = new EditTimerViewHolder();
		tView.view = view;
		tView.title = (TextView) view.findViewById(R.id.timer_detail_title);
		tView.channel = (TextView) view.findViewById(R.id.timer_detail_channel);
		tView.dateField = (Button) view.findViewById(R.id.timer_detail_day);
		tView.startField = (Button) view.findViewById(R.id.timer_detail_start);
		tView.endField = (Button) view.findViewById(R.id.timer_detail_end);
		tView.saveButton = (Button) view.findViewById(R.id.timer_details_save);
		tView.modifyButton = (Button) view
				.findViewById(R.id.timer_details_modify);

		tView.repeat = (Button) view.findViewById(R.id.timer_detail_repeat);

		tView.vps = (CheckBox) view.findViewById(R.id.timer_detail_vps);

		tView.priority = (EditText) view
				.findViewById(R.id.timer_detail_priority);

		tView.lifecycle = (EditText) view
				.findViewById(R.id.timer_detail_lifetime);

		view.findViewById(R.id.timer_details_cancel).setOnClickListener(this);
		tView.dateField.setOnClickListener(this);
		tView.startField.setOnClickListener(this);
		tView.endField.setOnClickListener(this);
		tView.saveButton.setOnClickListener(this);
		tView.modifyButton.setOnClickListener(this);
		tView.repeat.setOnClickListener(this);
		setContentView(view);
		timer = getApp().getCurrentTimer().copy();
		original = getApp().getCurrentTimer().copy();

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

		if (timer.isVps() == false && timer.hasVPS() == false) {
			findViewById(R.id.timer_block).setVisibility(View.GONE);
		} else {
			tView.vps
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked == true) {
								vpsChecked(false);
							} else {
								vpsUnchecked();
							}
						}
					});
		}
	}

	private void vpsChecked(boolean initial) {

		if (initial == false) {
			prevStart = tView.startField.getText();
			prevEnd = tView.endField.getText();
			prevDate = tView.dateField.getText();
		}

		DateFormatter formatter = new DateFormatter(new Date(original.getVPS()));
		String date = formatter.getDateString();
		tView.startField.setEnabled(false);
		tView.startField.setText(formatter.getTimeString());
		timer.setStart(new Date(timer.getVPS()));

		DateFormatter stopF = new DateFormatter(original.getStop());

		tView.endField.setEnabled(false);
		tView.endField.setText(stopF.getTimeString());
		timer.setStop(original.getStop());

		tView.dateField.setEnabled(false);
		tView.dateField.setText(date);

	}

	private void vpsUnchecked() {
		if (prevStart != null) {
			tView.startField.setText(prevStart);
		}
		tView.startField.setEnabled(true);

		if (prevEnd != null) {
			tView.endField.setText(prevEnd);
		}
		tView.endField.setEnabled(true);

		if (prevDate != null) {
			tView.dateField.setText(prevDate);
		}
		tView.dateField.setEnabled(true);
	}

	public class EditTimerViewHolder {
		View view;
		TextView title;
		TextView channel;
		Button dateField;
		Button startField;
		Button endField;
		Button saveButton;
		Button modifyButton;
		CheckBox vps;
		Button repeat;
		EditText priority;
		EditText lifecycle;
	}

	EditTimerViewHolder tView = null;

	boolean editStart;

	// SetTimerClient setTimerClient;

	Timer timer;

	Timer original;

	private void updateDates(Date start, Date stop) {
		DateFormatter startF = new DateFormatter(start);
		DateFormatter endF = new DateFormatter(stop);
		tView.startField.setText(startF.getTimeString());
		tView.endField.setText(endF.getTimeString());
		tView.dateField.setText(startF.getDateString());
	}

	private void updateDisplay(TimerOperation op) {
		updateDisplay();

		switch (op) {
		case CREATE:
			tView.modifyButton.setVisibility(View.GONE);
			tView.saveButton.setVisibility(View.VISIBLE);
			tView.saveButton.setText(R.string.timer_details_create_title);
			Preferences prefs = Preferences.get();
			tView.priority.setText(String.valueOf(prefs
					.getTimerDefaultPriority()));
			tView.lifecycle.setText(String.valueOf(prefs
					.getTimerDefaultLifetime()));

			Date start = new Date(timer.getStart().getTime()
					- prefs.getTimerPreMargin() * 60000);
			timer.setStart(start);

			Date end = new Date(timer.getStop().getTime()
					+ prefs.getTimerPostMargin() * 60000);
			timer.setStop(end);
			
			updateDates(start, end);
			break;
		case MODIFY:
			tView.saveButton.setVisibility(View.GONE);
			tView.modifyButton.setVisibility(View.VISIBLE);
			tView.saveButton.setText(R.string.timer_details_save_title);
			tView.priority.setText(String.valueOf(timer.getPriority()));
			tView.lifecycle.setText(String.valueOf(timer.getLifetime()));
			if (timer.isVps()) {
				vpsChecked(true);
			} else {
				updateDates(timer.getStart(), timer.getStop());
			}

			break;
		default:
			throw new RuntimeException("Unknown Operation: " + op);
		}

	}

	private void updateDisplay() {
		tView.channel.setText(timer.getChannelNumber() + " "
				+ timer.getChannelName());
		// tView.title.setText(timer.isVps() ?
		// getString(R.string.timer_detail_title_vps, f.getTitle()) :
		// f.getTitle());
		EventFormatter f = new EventFormatter(timer, true);
		tView.title.setText(f.getTitle());
		tView.repeat.setText(getSelectedItems().toString(this, true));
		EpgCache.CACHE.remove(timer.getChannelId());
		EpgCache.NEXT_REFRESH.remove(timer.getChannelId());
		tView.vps.setChecked(timer.isVps());

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
			// finishActivity(REQUEST_CODE_TIMER_EDIT);
			finish();
			break;
		}
		case R.id.timer_details_modify:
			timer.setTitle(tView.title.getText().toString());
			timer.setVps(tView.vps.isChecked());
			timer.setPriority(getIntOr0(tView.priority));
			timer.setLifetime(getIntOr0(tView.lifecycle));

			modifyTimer(timer);
			// say(R.string.done);
			break;

		case R.id.timer_details_save: {
			timer.setTitle(tView.title.getText().toString());
			timer.setVps(tView.vps.isChecked());
			timer.setPriority(getIntOr0(tView.priority));
			timer.setLifetime(getIntOr0(tView.lifecycle));

			createTimer(timer);
			// say(R.string.done);
			break;
		}

		case R.id.timer_detail_repeat: {

			String[] weekdays = new DateFormatSymbols().getWeekdays();
			String[] values = new String[] { weekdays[Calendar.MONDAY],
					weekdays[Calendar.TUESDAY], weekdays[Calendar.WEDNESDAY],
					weekdays[Calendar.THURSDAY], weekdays[Calendar.FRIDAY],
					weekdays[Calendar.SATURDAY], weekdays[Calendar.SUNDAY], };

			final DaysOfWeek mNewDaysOfWeek = new DaysOfWeek(
					getSelectedItems().mDays);

			final AlertDialog b = new AlertDialog.Builder(this)
					.setMultiChoiceItems(values,
							getSelectedItems().getBooleanArray(),
							new DialogInterface.OnMultiChoiceClickListener() {
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									mNewDaysOfWeek.set(which, isChecked);
								}
							})
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									StringBuilder sb = new StringBuilder(7);
									sb.append(mNewDaysOfWeek.isSet(0) ? 'M'
											: '-');
									sb.append(mNewDaysOfWeek.isSet(1) ? 'T'
											: '-');
									sb.append(mNewDaysOfWeek.isSet(2) ? 'W'
											: '-');
									sb.append(mNewDaysOfWeek.isSet(3) ? 'T'
											: '-');
									sb.append(mNewDaysOfWeek.isSet(4) ? 'F'
											: '-');
									sb.append(mNewDaysOfWeek.isSet(5) ? 'S'
											: '-');
									sb.append(mNewDaysOfWeek.isSet(6) ? 'S'
											: '-');
									timer.setWeekdays(sb.toString());
									tView.repeat.setText(mNewDaysOfWeek
											.toString(
													TimerDetailsActivity.this,
													true));
								}
							}).create();

			b.show();
		}
		}
	}

	DaysOfWeek getSelectedItems() {
		String str = timer.getWeekdays();

		DaysOfWeek dow = new DaysOfWeek(0);
		if (str.length() != 7) {
			return dow;
		}

		dow.set(0, str.charAt(0) == 'M');
		dow.set(1, str.charAt(1) == 'T');
		dow.set(2, str.charAt(2) == 'W');
		dow.set(3, str.charAt(3) == 'T');
		dow.set(4, str.charAt(4) == 'F');
		dow.set(5, str.charAt(5) == 'S');
		dow.set(6, str.charAt(6) == 'S');

		return dow;
	}

	private int getIntOr0(EditText text) {
		if (TextUtils.isEmpty(text.getText().toString())) {
			return 0;
		}
		return Integer.valueOf(text.getText().toString());
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
		updateDates(timer.getStart(), timer.getStop());
	}

	public void onDateSet(final DatePicker view, final int year,
			final int monthOfYear, final int dayOfMonth) {
		timer.setStart(calculateDate(timer.getStart(), year, monthOfYear,
				dayOfMonth));
		updateDates(timer.getStart(), timer.getStop());
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
		final ModifyTimerTask task = new ModifyTimerTask(this, timer, original) {
			@Override
			public void finished(SvdrpEvent event) {
				done();
			}
		};
		task.start();
	}

	/*
	 * Days of week code as a single int. 0x00: no day 0x01: Monday 0x02:
	 * Tuesday 0x04: Wednesday 0x08: Thursday 0x10: Friday 0x20: Saturday 0x40:
	 * Sunday
	 */
	static final class DaysOfWeek {

		private static int[] DAY_MAP = new int[] { Calendar.MONDAY,
				Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
				Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY, };

		// Bitmask of all repeating days
		private int mDays;

		DaysOfWeek(int days) {
			mDays = days;
		}

		public String toString(Context context, boolean showNever) {
			StringBuilder ret = new StringBuilder();

			// no days
			if (mDays == 0) {
				return showNever ? context.getText(R.string.never).toString()
						: "";
			}

			// every day
			if (mDays == 0x7f) {
				return context.getText(R.string.every_day).toString();
			}

			// count selected days
			int dayCount = 0, days = mDays;
			while (days > 0) {
				if ((days & 1) == 1)
					dayCount++;
				days >>= 1;
			}

			// short or long form?
			DateFormatSymbols dfs = new DateFormatSymbols();
			String[] dayList = (dayCount > 1) ? dfs.getShortWeekdays() : dfs
					.getWeekdays();

			// selected days
			for (int i = 0; i < 7; i++) {
				if ((mDays & (1 << i)) != 0) {
					ret.append(dayList[DAY_MAP[i]]);
					dayCount -= 1;
					if (dayCount > 0)
						ret.append(context.getText(R.string.day_concat));
				}
			}
			return ret.toString();
		}

		private boolean isSet(int day) {
			return ((mDays & (1 << day)) > 0);
		}

		public void set(int day, boolean set) {
			if (set) {
				mDays |= (1 << day);
			} else {
				mDays &= ~(1 << day);
			}
		}

		public void set(DaysOfWeek dow) {
			mDays = dow.mDays;
		}

		public int getCoded() {
			return mDays;
		}

		// Returns days of week encoded in an array of booleans.
		public boolean[] getBooleanArray() {
			boolean[] ret = new boolean[7];
			for (int i = 0; i < 7; i++) {
				ret[i] = isSet(i);
			}
			return ret;
		}

		public boolean isRepeatSet() {
			return mDays != 0;
		}

	}
}
