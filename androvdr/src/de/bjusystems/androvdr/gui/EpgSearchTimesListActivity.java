package de.bjusystems.androvdr.gui;

import java.util.List;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;
import de.bjusystems.androvdr.data.EpgSearchTimeValue;
import de.bjusystems.androvdr.data.EpgSearchTimeValues;
import de.bjusystems.androvdr.data.Preferences;
import de.bjusystems.androvdr.R;

/**
 * This class is used for showing what's
 * current running on all channels
 * @author bju
 */
public class EpgSearchTimesListActivity extends Activity
				implements OnClickListener, OnTimeSetListener {

	ArrayAdapter<EpgSearchTimeValue> adapter;
	List<EpgSearchTimeValue> values;
	Preferences prefs;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		setContentView(R.layout.epg_search_times_list);

		// Create adapter for ListView
		adapter = new ArrayAdapter<EpgSearchTimeValue>(this, R.layout.epg_search_times_item);
		final ListView listView = (ListView) findViewById(R.id.epg_search_times_list);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);

		// create channel list
		updateList();

		// button handler
		final Button addButton = (Button) findViewById(R.id.epg_search_times_add);
		addButton.setOnClickListener(this);
	}

	public void onClick(final View v) {

		// show time selection
		final TimePickerDialog dialog = new TimePickerDialog(this, this, 0, 0, true);
		dialog.show();
	}

	public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {

		final EpgSearchTimeValue time = new EpgSearchTimeValue(values.size(), String.format("%02d:%02d", hourOfDay, minute));
		values.add(time);
		adapter.add(time);

		save();

		updateList();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == R.id.epg_search_times_list) {
	    final MenuInflater inflater = getMenuInflater();
	    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    menu.setHeaderTitle(values.get(info.position+2).toString());

	    inflater.inflate(R.menu.epg_search_time_item_menu, menu);
		}
	}



	@Override
	public boolean onContextItemSelected(final MenuItem item) {

    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		final EpgSearchTimeValue time = values.get(info.position+2);

		if (item.getItemId() == R.id.epg_search_time_delete) {
			values.remove(time);
			adapter.remove(time);

			save();
		}

		return true;
	}

	private void updateList() {

		// get values
		values = new EpgSearchTimeValues(this).getValues();
		adapter.clear();
		for(int i = 2; i < values.size(); i++) {
			adapter.add(values.get(i));
		}
	}

	private void save() {
		new EpgSearchTimeValues(this).saveValues(values);
	}
}
