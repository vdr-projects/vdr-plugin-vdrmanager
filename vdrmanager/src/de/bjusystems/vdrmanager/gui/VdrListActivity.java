package de.bjusystems.vdrmanager.gui;

import java.util.Currency;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter;

import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.Intents;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Vdr;
import de.bjusystems.vdrmanager.data.db.OrmDatabaseHelper;

public class VdrListActivity extends OrmLiteBaseListActivity<OrmDatabaseHelper>
		implements OnItemClickListener, OnItemLongClickListener {

	private static final String TAG = VdrListActivity.class.getName();
	
	SimpleCursorAdapter adapter = null;

	Cursor cursor;

	String[] listItems = {};

	private boolean emptyConfig = false;

	private void initCursor() {

		//if (cursor != null) {
			//if (!cursor.isClosed()) {
				//cursor.close();
//			}
		//}
		try {
			cursor = getHelper().getVdrCursor();
			startManagingCursor(cursor);
		} catch (Exception ex) {
			Log.w(TAG,ex);
		}
	}

	private void populateIntent(){
		emptyConfig = getIntent().getBooleanExtra(Intents.EMPTY_CONFIG, Boolean.FALSE);
	}
	
	static class Holder{
		public TextView text1;
		public TextView  text2;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		populateIntent();
		
		setContentView(R.layout.vdr_list_add_delete);
		
		findViewById(R.id.add_item).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						editVdr(null);
					}
				});

		initCursor();
		final Vdr cur = Preferences.get().getCurrentVdr();
		adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_2, cursor, new String[] {
						"name", "host" }, new int[] { android.R.id.text1,
						android.R.id.text2}) {
							@Override
							public void bindView(View view, Context context,
									Cursor cursor) {
								
								TextView text1 = (TextView)view.findViewById(android.R.id.text1);
								TextView text2 = (TextView)view.findViewById(android.R.id.text2);
								int id = cursor.getInt(cursor.getColumnIndex("_id"));
								String name = cursor.getString(cursor.getColumnIndex("name"));
								String host = cursor.getString(cursor.getColumnIndex("host"));
								text2.setText(host);

								if(cur != null && cur.getId() == id) {
									 SpannableString content = new SpannableString(name);
									 content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
									text1.setText(content);
									//text1.setText(text1.getText());
								} else {
									text1.setTypeface(Typeface.DEFAULT);
									text1.setText(name);
								}
								
								
							}
			
		};
		setListAdapter(adapter);
		registerForContextMenu(getListView());
		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener(this);
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
		editVdr(Long.valueOf(id).intValue());
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
		super.onResume();
	}
	
	@Override
	public void onBackPressed() {
		if(cursor.getCount() == 0){
			finish();
			return;
		}
		if (emptyConfig) {
			Intent intent = new Intent(this, VdrManagerActivity.class);
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
		initCursor();
		adapter.changeCursor(cursor);
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view,
			final int position, final long id) {

		new AlertDialog.Builder(this)
				.setMessage(R.string.vdr_device_delete_qeustion)//
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								if (getHelper().getVdrDAO()
										.deleteById((int) id) > 0) {
									refresh();
								}

							}
						})//
				.setNegativeButton(android.R.string.cancel, null)//
				.create()//
				.show();
		return false;
	}
}
