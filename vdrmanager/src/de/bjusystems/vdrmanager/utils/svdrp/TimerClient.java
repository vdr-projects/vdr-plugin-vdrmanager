package de.bjusystems.vdrmanager.utils.svdrp;

import java.util.HashMap;
import java.util.Map;

import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.data.Channel;
import de.bjusystems.vdrmanager.data.Timer;

/**
 * Class for retrieving informations about the running program
 * @author bju
 *
 */
public class TimerClient extends SvdrpClient<Timer> {

  /** channel names for timer */
  Map<String, Channel> channels;

  /**
   * Constructor
   * @param certificateProblemListener CertificateProblemListener
   */
  public TimerClient(final CertificateProblemListener certificateProblemListener) {
    super(certificateProblemListener);
    this.channels = new HashMap<String, Channel>();
  }

  /**
   * Starts the EPG request
   */
  @Override
  public synchronized void run()   {
    runCommand("timers conflicts");
  }

  @Override
  public Timer parseAnswer(final String line) {
    return new Timer(line);
  }

  @Override
  public int getProgressTextId() {
    return R.string.progress_timers_loading;
  }
}

