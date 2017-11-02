package de.bjusystems.vdrmanager.utils.svdrp;

import android.content.Context;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Recording;

public class RecordingClient extends SvdrpClient<Recording> {

  /**
   * Constructor
   * @param certificateProblemListener
   */
  public RecordingClient(final CertificateProblemListener certificateProblemListener) {
    super(certificateProblemListener);
  }

  @Override
  protected Recording parseAnswer(final String line) {
    return new Recording(line);
  }

  @Override
  public int getProgressTextId() {
    return R.string.progress_recordings_loading;
  }

  @Override
  public synchronized void run()   {
    runCommand("recordings");
  }

}
