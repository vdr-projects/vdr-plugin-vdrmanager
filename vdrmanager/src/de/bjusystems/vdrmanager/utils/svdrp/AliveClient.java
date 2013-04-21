package de.bjusystems.vdrmanager.utils.svdrp;

import de.bjusystems.vdrmanager.data.AliveState;

/**
 * Class for retrieving informations about the running program
 * @author bju
 *
 */
public class AliveClient extends SvdrpClient<AliveState> {

  /**
   * Constructor
   * @param certificateProblemListener
   */
  public AliveClient(final CertificateProblemListener certificateProblemListener) {
    super(certificateProblemListener);
  }

  /**
   * Starts the EPG request
   * @param parameter parameter for lste
   */
  @Override
  public void run()   {
    runCommand("aliv");
  }

  @Override
  public AliveState parseAnswer(final String line) {

    if (line.startsWith("200")) {
      return AliveState.ALIVE;
    }
    if (line.startsWith("400")) {
      return AliveState.DEAD;
    }
    return AliveState.UNKNOWN;
  }

  @Override
  public int getProgressTextId() {
    return 0;
  }


}