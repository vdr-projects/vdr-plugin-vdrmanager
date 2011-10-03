package de.bjusystems.vdrmanager.gui;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.MenuActionHandler;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.utils.wakeup.AsyncWakeupTask;

public class VdrManagerActivity extends Activity {

	static class WakeupAction implements MenuActionHandler {
		public void executeAction(final Context context) {
			// create AsyncTask
			final AsyncWakeupTask wakeupTask = new AsyncWakeupTask(context);
			wakeupTask.execute();
		}
	}

	@SuppressWarnings("rawtypes")
	static class MenuActivity {
		int iconId;
		int textId;
		Class handler;
		boolean enabled;

		public int getIconId() {
			return iconId;
		}

		public int getTextId() {
			return textId;
		}

		public Class getHandler() {
			return handler;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public MenuActivity(final int iconId, final int textId,
				final Class handler) {
			this.iconId = iconId;
			this.textId = textId;
			this.handler = handler;
			this.enabled = true;
		}

		public void setEnabled(final boolean enabled) {
			this.enabled = enabled;
		}
	}

	class MenuAdapter extends ArrayAdapter<MenuActivity> implements
			OnClickListener {

		private final LayoutInflater inflater;

		public MenuAdapter(final Context context, final int viewId) {
			super(context, viewId);
			inflater = LayoutInflater.from(context);
		}

		@SuppressWarnings("unchecked")
		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {

			View view = convertView;
			View itemView;
			if (view == null) {
				view = inflater.inflate(R.layout.vdrmanager_menu_item, null);
				itemView = view.findViewById(R.id.vdrmanager_menu_item);
				view.setTag(itemView);

				// attach click listener
				itemView.setOnClickListener(this);

			} else {
				itemView = (View) view.getTag();
			}

			// attach menu item
			final MenuActivity menuItem = getItem(position);
			if (itemView instanceof TextView) {
				final TextView textView = (TextView) itemView;
				textView.setText(menuItem.getTextId());
			} else {
				final ImageButton imageButton = (ImageButton) itemView;
				imageButton.setImageResource(menuItem.getIconId());
			}
			itemView.setOnClickListener(this);
			menuActivityMap.put(itemView, menuItem.getHandler());

			return view;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public void onClick(final View v) {

			// refresh preferences
			Preferences.loadPreferences(getContext());

			// clear state
			((VdrManagerApp) getApplication()).clear();

			// start activity
			final Class actionClass = menuActivityMap.get(v);
			if (Activity.class.isAssignableFrom(actionClass)) {
				final Intent intent = new Intent();
				intent.setClass(getContext(), actionClass);
				startActivity(intent);
			} else if (MenuActionHandler.class.isAssignableFrom(actionClass)) {
				final Class<? extends MenuActionHandler> handlerClass = actionClass;
				try {
					final MenuActionHandler handler = handlerClass
							.newInstance();
					handler.executeAction(getContext());
				} catch (final InstantiationException e) {

				} catch (final IllegalAccessException e) {

				}
			}
		}
	}

	MenuActivity[] menuItems = new MenuActivity[] {
			new MenuActivity(R.drawable.whatson, R.string.action_menu_epg,
					TimeEpgListActivity.class),
			new MenuActivity(0, R.string.action_menu_search,
					EpgSearchActivity.class),
			new MenuActivity(R.drawable.timers, R.string.action_menu_timers,
					TimerListActivity.class),
			new MenuActivity(R.drawable.timers, R.string.action_menu_recordings,
						RecordingListActivity.class),

			new MenuActivity(R.drawable.channels,
					R.string.action_menu_channels, ChannelListActivity.class),
			new MenuActivity(0, R.string.action_menu_wakeup, WakeupAction.class), };
	Map<View, Class<? extends Activity>> menuActivityMap = new HashMap<View, Class<? extends Activity>>();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// attach view
		setContentView(R.layout.vdrmanager);

		Preferences.loadPreferences(this);
		
		// add and register buttons
		createButtons();
	}

	// add main menu buttons
	private void createButtons() {

		// refresh preferences
		Preferences.loadPreferences(this);
		final Preferences prefs = Preferences.getPreferences();

		// get list
		final AbsListView listView = (AbsListView) findViewById(R.id.vdrmanager_menu);
		final MenuAdapter adapter = new MenuAdapter(this,
				R.layout.vdrmanager_menu_item);
		listView.setAdapter(adapter);

		// add menu items
		for (final MenuActivity menuItem : menuItems) {
			if (menuItem.getTextId() != R.string.action_menu_wakeup
					|| prefs.isWakeupEnabled()) {
				adapter.add(menuItem);
			}
		}

		// set grid layout dimensions
		if (listView instanceof GridView) {
			final GridView grid = (GridView) listView;
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				grid.setNumColumns(2);
			} else {
				grid.setNumColumns(menuItems.length);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);

		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
		case R.id.main_menu_preferences:
			// remember activity for finishing
			final VdrManagerApp app = (VdrManagerApp) getApplication();
			app.clearActivitiesToFinish();
			app.addActivityToFinish(this);

			final Intent intent = new Intent();
			intent.setClass(this, PreferencesActivity.class);
			startActivity(intent);
			break;
		case R.id.main_menu_exit:
			finish();
			break;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
	}
}
