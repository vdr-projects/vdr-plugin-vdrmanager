package de.bjusystems.vdrmanager.utils.svdrp;

import java.security.cert.X509Certificate;

/**
 * Interface for reporting problems with the SSL certificate
 * @author bju
 *
 */
public interface CertificateProblemListener {

  /**
   * Possible user decisions on certificate problems
   */
  public enum CertificateProblemAction {

    /** Abort the connection */
    ABORT,
    /** Accept the certificate this time */
    ACCEPT_ONCE,
    /** Accept the certificate forever */
    ACCEPT_FOREVER
  }

  /**
   * Reports the certificate problem and waits for a user decision
   * @param chain Certificate trust chain
   * @param authType authentication type
   */
  CertificateProblemAction reportProblem(final X509Certificate[] chain, final String authType);
}
