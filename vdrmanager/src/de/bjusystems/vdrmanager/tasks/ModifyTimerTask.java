package de.bjusystems.vdrmanager.tasks;

import android.app.Activity;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Timer;
import de.bjusystems.vdrmanager.gui.CertificateProblemDialog;
import de.bjusystems.vdrmanager.utils.svdrp.SetTimerClient;
import de.bjusystems.vdrmanager.utils.svdrp.SetTimerClient.TimerOperation;

public abstract class ModifyTimerTask extends AsyncProgressTask<Timer> {

  public ModifyTimerTask(final Activity activity, final Timer newTimer, final Timer oldTimer) {
    super(activity, new SetTimerClient(newTimer, oldTimer, TimerOperation.MODIFY, new CertificateProblemDialog(activity)) {
      @Override
      public int getProgressTextId() {
        return R.string.progress_timer_modify;
      }


    });


  }
}
