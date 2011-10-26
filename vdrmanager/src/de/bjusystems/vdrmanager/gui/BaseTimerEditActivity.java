package de.bjusystems.vdrmanager.gui;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.tasks.CreateTimerTask;
import de.bjusystems.vdrmanager.tasks.DeleteTimerTask;
import de.bjusystems.vdrmanager.tasks.ModifyTimerTask;
import de.bjusystems.vdrmanager.tasks.ToggleTimerTask;
import de.bjusystems.vdrmanager.utils.svdrp.SetTimerClient.TimerOperation;

public abstract class BaseTimerEditActivity<T extends Event> extends
		BaseEventListActivity<T> implements OnClickListener, OnDateSetListener,
		OnTimeSetListener // SvdrpAsyncListener<Timer>,
{

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
	AlertDialog tDialog = null;

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
		EventFormatter f = new EventFormatter(timer,true);
		tView.channel.setText(timer.getChannelNumber() + " "
				+ timer.getChannelName());
		tView.title.setText(f.getTitle());
		tView.dateField.setText(f.getDate());
		tView.startField.setText(f.getTime());
		tView.endField.setText(f.getStop());
	}

	protected Timer getTimer(EventListItem item) {
		return item.getEpg().getTimer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final EventListItem event = adapter.getItem(info.position);

		if (tView == null) {
			tView = new EditTimerViewHolder();
			View view = getLayoutInflater()
					.inflate(R.layout.timer_detail, null);
			tView.view = view;
			tView.title = (TextView) view.findViewById(R.id.timer_detail_title);
			tView.channel = (TextView) view
					.findViewById(R.id.timer_detail_channel);
			tView.dateField = (TextView) view
					.findViewById(R.id.timer_detail_day);
			tView.startField = (TextView) view
					.findViewById(R.id.timer_detail_start);
			tView.endField = (TextView) view
					.findViewById(R.id.timer_detail_end);
			tView.saveButton = (Button) view
					.findViewById(R.id.timer_details_save);
			tView.modifyButton = (Button) view
					.findViewById(R.id.timer_details_modify);

			view.findViewById(R.id.timer_details_cancel).setOnClickListener(
					this);
			tView.dateField.setOnClickListener(this);
			tView.startField.setOnClickListener(this);
			tView.endField.setOnClickListener(this);
			tView.saveButton.setOnClickListener(this);
			tView.modifyButton.setOnClickListener(this);

			tDialog = new AlertDialog.Builder(this)

			.setView(view).create();
		}

		switch (item.getItemId()) {
		case R.id.epg_item_menu_timer_add:
			timer = event.createTimer();
			updateDisplay(TimerOperation.CREATE);
			tDialog.show();
			break;
		case R.id.epg_item_menu_timer_modify: {
			timer = getTimer(event);
			updateDisplay(TimerOperation.MODIFY);
			tDialog.show();
			// final Intent intent = new Intent();
			// intent.setClass(this, TimerDetailsActivity.class);
			// startActivityForResult(intent, REQUEST_CODE_TIMED_EDIT);
			break;
		}
		case R.id.epg_item_menu_timer_delete: {
			deleteTimer(getTimer(event));
			break;
		}
		case R.id.epg_item_menu_timer_toggle: {
			toggleTimer(getTimer(event));
			break;
		}
		default:
			return super.onContextItemSelected(item);
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 * android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {
		// if (v.getId() == R.id.whatson_list) {
		final MenuInflater inflater = getMenuInflater();
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		// set menu title
		final EventListItem item = adapter.getItem(info.position);
		final EventFormatter formatter = new EventFormatter(item);
		menu.setHeaderTitle(formatter.getTitle());

		inflater.inflate(R.menu.epg_list_item_menu, menu);
		Timer timer = getTimer(item);
		// remove unneeded menu items
		if (timer != null) {
			menu.findItem(R.id.epg_item_menu_timer_add).setVisible(false);
			menu.findItem(R.id.epg_item_menu_timer_modify).setVisible(true);
			menu.findItem(R.id.epg_item_menu_timer_delete).setVisible(true);
			final MenuItem enableMenuItem = menu
					.findItem(R.id.epg_item_menu_timer_toggle);
			enableMenuItem.setVisible(true);
			enableMenuItem
					.setTitle(timer.isEnabled() ? R.string.epg_item_menu_timer_disable
							: R.string.epg_item_menu_timer_enable);
		}

		super.onCreateContextMenu(menu, v, menuInfo);

		// }

	}

	protected void toggleTimer(Timer timer) {
		final ToggleTimerTask task = new ToggleTimerTask(this, timer) {
			@Override
			public void finished() {
				refresh();
			}
		};
		task.start();
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
			tDialog.dismiss();
			break;
		}
		case R.id.timer_details_modify:
			modifyTimer(timer);
			tDialog.dismiss();
			say(R.string.done);
			break;

		case R.id.timer_details_save: {
			createTimer(timer);
			tDialog.dismiss();
			say(R.string.done);
			break;
		}
		
		default:
			super.onClick(view);
		}
	}

	private void modifyTimer(Timer timer) {
		backupViewSelection();
		final ModifyTimerTask task = new ModifyTimerTask(this, timer) {
			@Override
			public void finished() {
				refresh();
				//say(R.string.done);
			}
		};
		task.start();
	}

	protected void deleteTimer(final Timer timer) {
		backupViewSelection();
		final DeleteTimerTask task = new DeleteTimerTask(this, timer) {
			@Override
			public void finished() {
				refresh();
				restoreViewSelection();
				//say(R.string.done);
			}
		};
		task.start();
	}

	private void createTimer(Timer timer) {
		backupViewSelection();
		final CreateTimerTask task = new CreateTimerTask(this, timer) {
			@Override
			public void finished() {
				refresh();
				//say(R.string.done);
			}
		};
		task.start();
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

}
