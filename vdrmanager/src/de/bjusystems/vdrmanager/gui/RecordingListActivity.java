package de.bjusystems.vdrmanager.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventFormatter;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.Recording;
import de.bjusystems.vdrmanager.data.RecordingListItem;
import de.bjusystems.vdrmanager.tasks.DeleteRecordingTask;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.RecordingClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpEvent;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpStartListener;

/**
 * This class is used for showing what's current running on all channels
 * 
 * @author bju
 */
public class RecordingListActivity extends BaseEventListActivity<Recording>
		implements OnItemLongClickListener, SvdrpStartListener {

	// RecordingClient recordingClient;

	// public static final int MENU_GROUP_CHANNEL = 2;

	public static final int ASC = 0;

	public static final int DESC = 1;

	// protected static ArrayList<Recording> CACHE = new ArrayList<Recording>();

	// private static Map<String, List<Recording>> CACHE = new TreeMap<String,
	// List<Recording>>();

	// public static final Map<String, Set<String>> FOLDERS = new
	// TreeMap<String, Set<String>>();

	private RecordingDir currentFolder; // Recording.ROOT_FOLDER;

	private final int ASC_DESC = ASC;

	private static final List<Recording> EMPTY = new ArrayList<Recording>(0);

	private final Stack<String> stack = new Stack<String>();

	private TextView folderInfo;

	private TextView currentCount;

	private TextView driverInfo;

	private View driverInfoContainer;

	private ProgressBar drive_info_pb;

	private int totalMB = -1;

	private int freeMB = -1;

	private int percent = -1;

	private RecordingDir ROOT = new RecordingDir();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new RecordingAdapter(this);
		currentFolder = ROOT; 
		// attach adapter to ListView
		listView = (ListView) findViewById(R.id.recording_list);
		folderInfo = (TextView) findViewById(R.id.folder_info);
		driverInfoContainer = findViewById(R.id.driver_info_container);
		driverInfo = (TextView) driverInfoContainer
				.findViewById(R.id.drive_info);
		drive_info_pb = (ProgressBar) driverInfoContainer
				.findViewById(R.id.drive_info_pb);
		currentCount = (TextView) findViewById(R.id.current_count);
		listView.setAdapter(adapter);

		// set click listener
		listView.setOnItemLongClickListener(this);
		// register EPG item click
		listView.setOnItemClickListener(this);
		// context menu wanted
		registerForContextMenu(listView);
		listView.setFastScrollEnabled(true);
		listView.setTextFilterEnabled(true);
		// start query
		startRecordingQuery();
	}

	@Override
	protected int getAvailableSortByEntries() {
		return R.array.recordings_group_by;
	};

	// AlertDialog groupByDialog = null;

	// @Override
	// public boolean onOptionsItemSelected(
	// final com.actionbarsherlock.view.MenuItem item) {
	//
	// switch (item.getItemId()) {
	// case R.id.menu_groupby:
	// // case MENU_PROVIDER:
	// // case MENU_NAME:
	// if (groupByDialog == null) {
	// groupByDialog = new AlertDialog.Builder(this)
	// .setTitle(R.string.menu_groupby)
	// .setIcon(android.R.drawable.ic_menu_sort_alphabetically)
	// .setSingleChoiceItems(getAvailableGroupByEntries(),
	// groupBy, new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog,
	// int which) {
	// if (groupBy == which) {
	// ASC_DESC = ASC_DESC == ASC ? DESC
	// : ASC;
	// } else {
	// groupBy = which;
	// ASC_DESC = ASC;
	// }
	// // fillAdapter();
	// groupByDialog.dismiss();
	// say("Comming soon...");
	// }
	// }).create();
	// }
	//
	// groupByDialog.show();
	//
	// return true;
	// default:
	// return super.onOptionsItemSelected(item);
	// }
	// }

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {

		final RecordingListItem item = (RecordingListItem) adapter
				.getItem(position);
		if (item.isFolder()) {
			currentFolder = item.folder;
			// if (currentFolder.equals(Recording.ROOT_FOLDER)) {
			// currentFolder = item.folder;
			// } else {
			// currentFolder = currentFolder + Recording.FOLDERDELIMCHAR
			// + item.folder;
			// }
			// stack.push(currentFolder);
			fillAdapter();
		} else {
			super.onItemClick(parent, view, position, id);
		}
	}

	private void updateCurrentFolderInfo(int size) {
		folderInfo.setText(currentFolder.getPath());
		currentCount.setText(String.valueOf(size));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.bjusystems.vdrmanager.gui.BaseActivity#onCreateOptionsMenu(android
	 * .view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(
			final com.actionbarsherlock.view.Menu menu) {
		final com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.recording_list_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected int prepareDetailsViewData(final EventListItem event, int position) {
		getApp().setCurrentEvent(event.getEvent());
		List<Recording> cachEget = CACHEget(currentFolder);
		getApp().setCurrentEpgList(cachEget);
		
		for(int i = 0; i < position; ++i){
			if(cachEget.get(i) == event.getEvent()){
				return i; 
			}
		}
		
		return 0;
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v,
			final ContextMenuInfo menuInfo) {

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		final EventListItem item = adapter.getItem(info.position);
		if (item.isHeader()) {
			return;
		}

		if (v.getId() == R.id.recording_list) {
			final MenuInflater inflater = getMenuInflater();
			// set menu title
			final EventFormatter formatter = new EventFormatter(item);
			menu.setHeaderTitle(formatter.getTitle());

			inflater.inflate(R.menu.recording_list_item_menu, menu);
			if (Preferences.get().isEnableRecStream() == false) {
				menu.removeItem(R.id.recording_item_menu_stream);
			}

		}

		super.onCreateContextMenu(menu, v, menuInfo);
		//
		// http://projects.vdr-developer.org/issues/863
		// if (Utils.isLive(item)) {
		menu.removeItem(R.id.epg_item_menu_live_tv);
		// }
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final EventListItem event = adapter.getItem(info.position);
		final Recording rec = (Recording) event.getEvent();
		switch (item.getItemId()) {
		case R.id.recording_item_menu_delete: {
			final DeleteRecordingTask drt = new DeleteRecordingTask(this, rec) {
				@Override
				public void finished(final SvdrpEvent event) {
					if (event == SvdrpEvent.FINISHED_SUCCESS) {
						backupViewSelection();
						refresh();
					}
				}
			};
			drt.start();
			break;
		}
		case R.id.recording_item_menu_stream: {
			Utils.streamRecording(this, rec);
			// say("Sorry, not yet. It would be. File -> " + rec.getFileName());
			break;
		}

		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	private void startRecordingQuery() {

		if (checkInternetConnection() == false) {
			return;
		}

		// get timer client
		final RecordingClient recordingClient = new RecordingClient(
				getCertificateProblemDialog());

		recordingClient.addStartListener(this);

		// create backgound task
		final SvdrpAsyncTask<Recording, SvdrpClient<Recording>> task = new SvdrpAsyncTask<Recording, SvdrpClient<Recording>>(
				recordingClient);

		// create progress dialog

		addListener(task);

		// start task
		task.run();
	}

	@Override
	protected void retry() {
		startRecordingQuery();
	}

	@Override
	protected void refresh() {
		startRecordingQuery();
	}

	@Override
	protected int getMainLayout() {
		return R.layout.recording_list;
	}

	@Override
	protected String getWindowTitle() {
		return getString(R.string.action_menu_recordings);
	}

	protected void sort() {
		/* */
		switch (sortBy) {
		case MENU_GROUP_DEFAULT: {
			sortItemsByTime(CACHEget(currentFolder), true);
			break;
		}
		case MENU_GROUP_ALPHABET: {
			Collections.sort(CACHEget(currentFolder), new TitleComparator());
			break;
		}
		// case MENU_GROUP_CHANNEL: {
		// sortItemsByChannel(results);
		// }
		}
	}

	private List<Recording> CACHEget(RecordingDir currentFolder) {
		return currentFolder.recordings;
	}


	@Override
	protected void fillAdapter() {

		adapter.clear();

		sort();

		final Calendar cal = Calendar.getInstance();
		int day = -1;

		final Collection<RecordingDir> folders = currentFolder.dirs.values();
		for (final RecordingDir d : folders) {
			final RecordingListItem recordingListItem = new RecordingListItem(
					d.name);
			recordingListItem.folder = d;
			// final String sf = currentFolder.length() > 0 ? currentFolder
			// + Recording.FOLDERDELIMCHAR + f : f;
			// final List<Recording> list2 = CACHE.get(sf);
			// if (list2 != null) {
			// recordingListItem.getCcount = getCountInFolder(currentFolder, f);
			// }
			adapter.add(recordingListItem);
		}

		updateCurrentFolderInfo(currentFolder.size());

		for (final Event rec : CACHEget(currentFolder)) {
			cal.setTime(rec.getStart());
			final int eday = cal.get(Calendar.DAY_OF_YEAR);
			if (eday != day) {
				day = eday;
				adapter.add(new RecordingListItem(new DateFormatter(cal)
						.getDailyHeader()));
			}
			adapter.add(new RecordingListItem((Recording) rec));
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onBackPressed() {
		if (currentFolder.parent == null) {
			super.onBackPressed();
			return;
		}
		currentFolder = currentFolder.parent;
		fillAdapter();
	}

//	private List<Recording> readDummy() {
//
//		List<Recording> list = new ArrayList<Recording>();
//		try {
//			LineNumberReader r = new LineNumberReader(new FileReader(new File(
//					"/sdcard/vdrmanager.txt")));
//
//			String line;
//
//			while ((line = r.readLine()) != null) {
//				Recording rec = null;
//				try {
//					rec = new Recording(line);
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//				list.add(rec);
//			}
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//		return list;
//
//	}

	@Override
	protected boolean finishedSuccessImpl(final List<Recording> results) {

	//	results.clear();
	//	results.addAll(readDummy());

		clearCache();

		for (final Recording r : results) {
			final String folder = r.getFolder();

			if (folder.length() == 0) {
				ROOT.recordings.add(r);
			} else {
				final String[] split = folder.split(Recording.FOLDERDELIMCHAR);

				RecordingDir parent = ROOT;
				RecordingDir dir = null; // "a~b~c";
				for (int i = 0; i < split.length; ++i) {
					String dn = split[i];

					dir = parent.dirs.get(dn);
					if(dir == null) {
						dir = new RecordingDir();
						dir.parent = parent;
						dir.name = dn;
						parent.dirs.put(dn, dir);
					}
					parent = dir;
				}
				dir.recordings.add(r);

				// StringBuilder sb = new StringBuilder();
				// String sep = "";
				// for(int i = 0; i < split.length - 1; ++i){
				// sb.append(sep).append(split[i]);
				// sep = Recording.FOLDERDELIMCHAR;
				// }
				// key = folder.subSequence(0,
				// folder.length() - (value.length() + 1)).toString();

				// Set<String> list = FOLDERS.get(key);
				// if (list == null) {
				// list = new TreeSet<String>(new Comparator<String>() {
				// @Override
				// public int compare(final String lhs, final String rhs) {
				// return lhs.compareToIgnoreCase(rhs);
				// }
				// });
				// FOLDERS.put(key, list);
				// }
				//
				// list.add(value);
				//
				// if (key.equals(Recording.ROOT_FOLDER) == false) {
				// Set<String> set = FOLDERS.get(Recording.ROOT_FOLDER);
				// if (set == null) {
				// set = new HashSet<String>();
				// FOLDERS.put(key, set);
				// }
				// set.add(key);
				// }

				// a b
				// a
				// c
				// a~b > k

			}
			// List<Recording> list = CACHE.get(folder);
			// if (list == null) {
			// list = new ArrayList<Recording>();
			// CACHE.put(folder, list);
			// }
			// list.add(r);
		}

		pushResultCountToTitle();
		fillAdapter();
		return adapter.isEmpty() == false;
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1,
			final int arg2, final long arg3) {

		return false;
	}

	@Override
	protected int getListNavigationIndex() {
		return LIST_NAVIGATION_RECORDINGS;
	}

	@Override
	public void clearCache() {
		ROOT.clear();
		// CACHE.clear();
		// FOLDERS.clear();
	}

	@Override
	protected List<Recording> getCACHE() {

		return currentFolder.recordings;
	}

	@Override
	public void start(final String meta) {
		if (meta == null) {
			return;
		}
		try {

			runOnUiThread(new Runnable() {
				public void run() {

					// stuff that updates ui

					String[] split = meta.split(":");
					if (split.length != 3) {
						Log.w(TAG, "Recoring list meta is wrong");
						return;
					}

					totalMB = Integer.valueOf(split[0]);
					freeMB = Integer.valueOf(split[1]);
					percent = Integer.valueOf(split[2]);
					driverInfoContainer.setVisibility(View.VISIBLE);
					driverInfo.setText(getString(R.string.drive_info,
							(freeMB) / 1024, totalMB / 1024, 100 - percent));
					drive_info_pb.setProgress(percent);
				}
			});
		} catch (Exception ex) {
			Log.w(TAG, ex.getMessage(), ex);
		}

	}
}
