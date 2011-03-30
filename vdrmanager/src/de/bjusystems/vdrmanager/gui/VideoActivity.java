package de.bjusystems.vdrmanager.gui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Preferences;

/**
 * This class is used for showing what's
 * current running on all channels
 * @author bju
 */
public class VideoActivity extends Activity
				implements OnClickListener {

	Preferences prefs;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Attach view
		setContentView(R.layout.video);

		// set stream
		final VideoView videoView = (VideoView) findViewById(R.id.video_video);
		final MediaController mediaController = new MediaController(this);
		mediaController.setAnchorView(videoView);
		final Uri video = Uri.parse("http://192.168.178.20:3000/1");
		videoView.setMediaController(mediaController);
		videoView.setVideoURI(video);
		videoView.start();

    // register button
    final Button button = (Button) findViewById(R.id.video_button);
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
//		final EpgSearchParams search = new EpgSearchParams();
//		search.setTitle(text.getText().toString());
//		((VdrManagerApp)getApplication()).setCurrentSearch(search);

		// show timer details
//		final Intent intent = new Intent();
//		intent.setClass(this, VdrManagerActivity.class);
//		startActivity(intent);
	}
}
