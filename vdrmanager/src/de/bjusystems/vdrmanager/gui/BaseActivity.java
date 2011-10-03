package de.bjusystems.vdrmanager.gui;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.app.VdrManagerApp;
import de.bjusystems.vdrmanager.data.Channel;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public abstract class BaseActivity extends Activity implements OnClickListener{


	public static final int MENU_GROUP_REFRESH = 99;
	
	public static final int MENU_REFRESH = 99;
	
	abstract protected int getMainLayout();
	
	protected void switchNoConnection(){
		View view = findViewById(R.id.main_content);
		if(view != null){
			view.setVisibility(View.GONE);
		}
		findViewById(R.id.no_connection_layout).setVisibility(View.VISIBLE);
		Button b  = (Button) findViewById(R.id.retry_button);
		b.setOnClickListener(this);
	}

	public void onClick(View v) {
		if(v.getId() == R.id.retry_button){
			findViewById(R.id.no_connection_layout).setVisibility(View.GONE);
			View view = findViewById(R.id.main_content);
			if(view != null){
				view.setVisibility(View.VISIBLE);
			}
			retry();
		}
	}
	
	protected void updateWindowTitle(int topic, int subtopic) {
		String title;
		title = getString(topic); 
		if (subtopic != -1) {
			title += " > " + getString(subtopic);
		}
		setTitle(title);
	}

	protected void updateWindowTitle(String topic, String subtopic) {
		String title = topic;
		if (subtopic != null) {
			title += " > " + subtopic;
		}
		setTitle(title);
	}


	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		MenuItem item;
		item = menu.add(MENU_GROUP_REFRESH, MENU_REFRESH, 0, R.string.refresh);
		item.setIcon(android.R.drawable.ic_menu_rotate);
		item.setAlphabeticShortcut('r');
		return true;
	}
	
	abstract protected void refresh(); 
	
	abstract protected void retry(); 
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH:
			refresh();
			return true;

		default:
			return false;
		}
	}


	protected void setAsCurrent(Channel channel) {
		getApp().setCurrentChannel(channel);
	}


	protected VdrManagerApp getApp(){
		final VdrManagerApp app = (VdrManagerApp) getApplication();
		return app;
	}
	
	//protected Channel getCurrentChannel(){
		//final Channel channel = ((VdrManagerApp) getApplication())
		//.getCurrentChannel();
		//return channel;
//	}

	
	
}
