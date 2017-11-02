package de.bjusystems.vdrmanager.data.db;

import android.content.SearchRecentSuggestionsProvider;

public class EPGSearchSuggestionsProvider extends
		SearchRecentSuggestionsProvider {


	public final static String AUTHORITY = EPGSearchSuggestionsProvider.class.getName();

	public final static int MODE = DATABASE_MODE_QUERIES;// | DATABASE_MODE_2LINES;

	public EPGSearchSuggestionsProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}


	public static enum SecondLine {
		EPG,
		EPG_SEARCH,
	}

}
