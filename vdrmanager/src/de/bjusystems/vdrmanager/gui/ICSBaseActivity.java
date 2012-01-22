package de.bjusystems.vdrmanager.gui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;

public abstract class ICSBaseActivity extends Activity{

	public void initActionBar() {
		int api = Build.VERSION.SDK_INT;
		if (api < 14) {
			return;
		}

		ActionBar actionBar = getActionBar();
		//this is since 14 enabled
		actionBar.setHomeButtonEnabled(true);
	}
}
