package de.bjusystems.vdrmanager.gui;

import java.util.Calendar;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Epg;
import de.bjusystems.vdrmanager.data.EpgSearchParams;
import de.bjusystems.vdrmanager.data.Event;
import de.bjusystems.vdrmanager.data.EventListItem;
import de.bjusystems.vdrmanager.data.Preferences;
import de.bjusystems.vdrmanager.data.db.EPGSearchSuggestionsProvider;
import de.bjusystems.vdrmanager.utils.date.DateFormatter;
import de.bjusystems.vdrmanager.utils.svdrp.EpgClient;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpAsyncTask;
import de.bjusystems.vdrmanager.utils.svdrp.SvdrpClient;

/**
 * This class is used for showing what's current running on all channels
 *
 * @author bju
 */
public class EpgSearchListActivity extends BaseTimerEditActivity<Epg> implements
		OnItemClickListener {

	private void initSearch(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			if (TextUtils.isEmpty(query) == false) {
				highlight = query.trim();
				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
						this, EPGSearchSuggestionsProvider.AUTHORITY,
						EPGSearchSuggestionsProvider.MODE);
				suggestions.saveRecentQuery(query, null);
			}
		}
	}

	@Override
	protected SvdrpClient<Epg> getClient() {
		return this.epgClient;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		initSearch(intent);
		startSearch();
	}

	private void startSearch() {
		startEpgQuery();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		Preferences.setLocale(this);
		// Preferences.init(this);

		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		initSearch(intent);
		adapter = new TimeEventAdapter(this);

		// Create adapter for EPG list
		adapter.setHideDescription(false);
		listView = (ListView) findViewById(R.id.whatson_list);
		listView.setAdapter(adapter);
		listView.setTextFilterEnabled(true);
		registerForContextMenu(listView);
		// register EPG item click
		listView.setOnItemClickListener(this);
		startSearch();
	}

	public void onNothingSelected(final AdapterView<?> arg0) {
		// startTimeEpgQuery(((EpgTimeSpinnerValue)timeSpinner.getAdapter().getItem(0)).getValue());
	}

	//

	private void startEpgQuery() {

		if (checkInternetConnection() == false) {
			return;
		}

		EpgSearchParams sp = new EpgSearchParams();
		sp.setTitle(highlight);
		setTitle(getWindowTitle());
		epgClient = new EpgClient(sp);
		// remove old listeners
		// epgClient.clearSvdrpListener();

		// create background task
		final SvdrpAsyncTask<Epg, SvdrpClient<Epg>> task = new SvdrpAsyncTask<Epg, SvdrpClient<Epg>>(
				epgClient);

		// create progress
		addListener(task);

		// start task
		task.run();
	}

	/*
	 * (non-Javadoc) TODO this method also should be used in startEpgQuery on
	 * cache hit
	 *
	 * @see de.bjusystems.vdrmanager.gui.BaseEpgListActivity#finishedSuccess()
	 */
	@Override
	protected boolean finishedSuccessImpl() {
		adapter.clear();
		adapter.highlight = this.highlight;

		Calendar cal = Calendar.getInstance();
		int day = -1;
		sortItemsByTime(results);
		for (Event e : results) {
			cal.setTime(e.getStart());
			int eday = cal.get(Calendar.DAY_OF_YEAR);
			if (eday != day) {
				day = eday;
				adapter.add(new EventListItem(new DateFormatter(cal)
						.getDailyHeader()));
			}
			adapter.add(new EventListItem((Epg) e));
		}
		listView.setSelectionAfterHeaderView();
		return adapter.getCount() > 0;
	}

	protected void prepareDetailsViewData(final EventListItem item) {
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		app.setCurrentEvent(item.getEvent());
		app.setCurrentEpgList(results);
	}

	@Override
	protected int getMainLayout() {
		return R.layout.search_epg_list;
	}

	@Override
	protected void refresh() {
		startEpgQuery();
	}

	@Override
	protected void retry() {
		startEpgQuery();
	}

	@Override
	protected String getWindowTitle() {
		if (TextUtils.isEmpty(highlight)) {
			return getString(R.string.epg_by_search);
		}

		return getString(R.string.epg_by_search_param, highlight);
	}

	@Override
	public boolean onSearchRequested() {
		startSearchManager();
		return true;
	}

	@Override
	protected int getListNavigationIndex() {
		return -1;
	}

	@Override
	protected boolean hasListNavigation() {
		return false;
	}

}
