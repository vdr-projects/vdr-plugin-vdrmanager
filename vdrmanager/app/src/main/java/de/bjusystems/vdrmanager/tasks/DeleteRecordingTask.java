package de.bjusystems.vdrmanager.tasks;

import android.app.Activity;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Recording;
import de.bjusystems.vdrmanager.gui.CertificateProblemDialog;
import de.bjusystems.vdrmanager.utils.svdrp.DelRecordingClient;

public abstract class DeleteRecordingTask extends AsyncProgressTask<Recording> {
  public DeleteRecordingTask(final Activity activity, final Recording r) {
    super(activity, new DelRecordingClient(r, new CertificateProblemDialog(activity)) {
      @Override
      public int getProgressTextId() {
        return R.string.progress_recording_delete;
      }
    });
  }
}
