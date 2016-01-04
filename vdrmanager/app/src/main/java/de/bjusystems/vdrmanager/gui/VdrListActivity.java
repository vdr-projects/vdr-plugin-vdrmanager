package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.backup.BackupSettingsActivity;
import de.bjusystems.vdrmanager.backup.IntentUtils;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Vdr;
import de.bjusystems.vdrmanager.data.db.DBAccess;

public class VdrListActivity extends ActionBarActivity implements
		OnItemClickListener, OnItemLongClickListener, View.OnClickListener {

	private static final String TAG = VdrListActivity.class.getName();

	List<Vdr> list = new ArrayList<Vdr>();

	ArrayAdapter<Vdr> adapter = null;

	// Cursor cursor;

	String[] listItems = {};

	private boolean emptyConfig = false;

	// private void initCursor() {
	//
	// //if (cursor != null) {
	// //if (!cursor.isClosed()) {
	// //cursor.close();
	// // }
	// //}
	// try {
	// cursor = getHelper().getVdrCursor();
	// //startManagingCursor(cursor);
	// } catch (Exception ex) {
	// Log.w(TAG,ex);
	// }
	// }

	private void populateIntent() {
		emptyConfig = getIntent().getBooleanExtra(Intents.EMPTY_CONFIG,
				Boolean.FALSE);
	}


	@Override
	public void onClick(View v) {

		if(v.getId() == R.id.new_vdr){
			editVdr(null);
			return;
		}

	}

	static class Holder {
		public TextView text1;
		public TextView text2;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		populateIntent();

		setContentView(R.layout.vdr_list_add_delete);


		findViewById(R.id.new_vdr).setOnClickListener(this);

		// initCursor();
		final Vdr cur = Preferences.get().getCurrentVdr();
		adapter = new ArrayAdapter<Vdr>(this,
				android.R.layout.simple_list_item_2, list) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// recycle view?
				Holder holder = null;
				View view = convertView;
				if (view == null) {
					view = getLayoutInflater().inflate(
							android.R.layout.simple_list_item_2, null);
					holder = new Holder();

					holder.text1 = (TextView) view
							.findViewById(android.R.id.text1);
					holder.text2 = (TextView) view
							.findViewById(android.R.id.text2);
					view.setTag(holder);
				} else {
					holder = (Holder) view.getTag();
				}

				Vdr vdr = getItem(position);
				String name = (vdr.getName() != null ? vdr.getName() : "");
				String host = vdr.getHost();
				holder.text2.setText(host);

				if (cur != null && cur.getId() == vdr.getId()) {
					SpannableString content = new SpannableString(name);
					content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
					holder.text1.setText(content);
					// text1.setText(text1.getText());
				} else {
					holder.text1.setTypeface(Typeface.DEFAULT);
					holder.text1.setText(name);
				}
				return view;
			}

		};

		// adapter = new ArrayAdapter<Vdr>(
		// "name", "host" }, new int[] { android.R.id.text1,
		// android.R.id.text2}) {
		// @Override
		// public void bindView(View view, Context context,
		// Cursor cursor) {
		//
		// TextView text1 = (TextView)view.findViewById(android.R.id.text1);
		// TextView text2 = (TextView)view.findViewById(android.R.id.text2);
		// int id = cursor.getInt(cursor.getColumnIndex("_id"));
		// String name = cursor.getString(cursor.getColumnIndex("name"));
		// String host = cursor.getString(cursor.getColumnIndex("host"));
		// text2.setText(host);
		//
		// if(cur != null && cur.getId() == id) {
		// SpannableString content = new SpannableString(name);
		// content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		// text1.setText(content);
		// //text1.setText(text1.getText());
		// } else {
		// text1.setTypeface(Typeface.DEFAULT);
		// text1.setText(name);
		// }
		//
		//
		// }
		//
		// };
		ListView listView = (ListView) findViewById(R.id.vdr_list);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		listView.setLongClickable(true);
		listView.setEmptyView(findViewById(R.id.empty_view));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget
	 * .AdapterView, android.view.View, int, long)
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		editVdr(adapter.getItem(position).getId());
	}

	/**
	 * Start {@link VdrPreferencesActivity} to create or edit a vdr
	 *
	 * @param id
	 *            may be null. Then a new vdr is created
	 */
	private void editVdr(Integer id) {
		Intent intent = new Intent(this, VdrPreferencesActivity.class);
		intent.putExtra(Intents.VDR_ID, id);
		startActivityForResult(intent, Intents.EDIT_VDR);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != RESULT_OK) {
			return;
		}
		if (requestCode == Intents.EDIT_VDR) {
			refresh();
			return;
		}

	}

	@Override
	protected void onResume() {
		refresh();
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		if (list.isEmpty()) {
			finish();
			return;
		}
		if (emptyConfig) {
			Intent intent = new Intent(this, VdrManagerActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		} else {
			super.onBackPressed();
		}
	}

	/**
	 * Refresh the list
	 */
	private void refresh() {
		list.clear();
		list.addAll(DBAccess.get(this).getVdrDAO().queryForAll());
		adapter.notifyDataSetChanged();
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view,
			final int position, final long id) {

		new AlertDialog.Builder(this)
				.setMessage(R.string.vdr_device_delete_qeustion)//
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								if (DBAccess
										.get(VdrListActivity.this)
										.getVdrDAO()
										.deleteById(
												adapter.getItem(position)
														.getId()) > 0) {
									if (Preferences.get().getCurrentVdrContext(
											VdrListActivity.this) == id) {
										Preferences.setCurrentVdr(
												VdrListActivity.this, null);
									}
									refresh();
								}

							}
						})//
				.setNegativeButton(android.R.string.cancel, null)//
				.create()//
				.show();
		return true;
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vdrlist, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.main_menu_vdrlist_restore) {

			Intent intent = IntentUtils.newIntent(this,
					BackupSettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
