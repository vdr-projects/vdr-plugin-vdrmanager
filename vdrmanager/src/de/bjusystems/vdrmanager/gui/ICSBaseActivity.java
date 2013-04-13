package de.bjusystems.vdrmanager.gui;

import com.actionbarsherlock.app.SherlockActivity;

public abstract class ICSBaseActivity extends SherlockActivity {

  public void initActionBar() {
    //		int api = Build.VERSION.SDK_INT;
    //	if (api < 14) {
    //	return;
    //}
    final com.actionbarsherlock.app.ActionBar actionBar = getSupportActionBar();
    if(actionBar == null){
      return;
    }
    actionBar.setHomeButtonEnabled(true);
  }

  protected CertificateProblemDialog getCertificateProblemDialog() {
    return new CertificateProblemDialog(this);
  }
}
