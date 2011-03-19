package de.bjusystems.androvdr.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.bjusystems.androvdr.app.AndroVdrApp;
import de.bjusystems.androvdr.data.EpgSearchParams;
import de.bjusystems.androvdr.data.Preferences;
import de.bjusystems.androvdr.R;

/**
 * This class is used for showing what's
 * current running on all channels
 * @author bju
 */
public class EpgSearchActivity extends Activity
				implements OnClickListener {

	Preferences prefs;
	TextView text;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		setContentView(R.layout.epg_search);

		// save fields
		text = (TextView) findViewById(R.id.epg_search_text);

    // register button
    final Button button = (Button) findViewById(R.id.epg_search_button);
    button.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void onClick(final View v) {

		// Save search parameters
		final EpgSearchParams search = new EpgSearchParams();
		search.setTitle(text.getText().toString());
		((AndroVdrApp)getApplication()).setCurrentSearch(search);

		// show timer details
		final Intent intent = new Intent();
		intent.setClass(this, EpgListActivity.class);
		startActivity(intent);
	}
}