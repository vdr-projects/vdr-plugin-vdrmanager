package de.bjusystems.vdrmanager.gui;

import java.security.cert.X509Certificate;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import de.bjusystems.vdrmanager.R;
import de.bjusystems.vdrmanager.utils.svdrp.CertificateProblemListener;

public class CertificateProblemDialog implements CertificateProblemListener {

  /** Context */
  private final Activity activity;
  /** User wanted action */
  CertificateProblemAction action;

  /**
   * Constructor
   * @param activity Context
   */
  public CertificateProblemDialog(final Activity activity) {
    this.activity = activity;
  }

  @Override
  public CertificateProblemAction reportProblem(final X509Certificate[] chain, final String authType) {

    // Semaphore to implement a modal dialog
    final Semaphore semaphore = new Semaphore(0, true);

    // certificate properties
    final String host = "myhost.de";
    final String key = "Key";
    final String fingerprint = "Fingerprint";
    final String issuer = "Issuer";

    // message
    final CharSequence message = String.format(activity.getString(R.string.certificate_problem_message_text), host, key, fingerprint, issuer);

    // create dialog builder
    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
    alertDialogBuilder.setTitle(R.string.certificate_problem_message_title);
    alertDialogBuilder.setMessage(message);
    alertDialogBuilder.setCancelable(false);

    // buttons
    alertDialogBuilder.setNegativeButton(R.string.certificate_not_accepted, new OnClickListener() {
      @Override
      public void onClick(final DialogInterface dialog, final int which) {
        action = CertificateProblemAction.ABORT;
        dialog.cancel();
        semaphore.release();
      }
    });
    alertDialogBuilder.setNeutralButton(R.string.certificate_accept_once, new OnClickListener() {
      @Override
      public void onClick(final DialogInterface dialog, final int which) {
        action = CertificateProblemAction.ACCEPT_ONCE;
        dialog.cancel();
        semaphore.release();
      }
    });
    alertDialogBuilder.setPositiveButton(R.string.certificate_accepted_forever, new OnClickListener() {
      @Override
      public void onClick(final DialogInterface dialog, final int which) {
        action = CertificateProblemAction.ACCEPT_FOREVER;
        dialog.cancel();
        semaphore.release();
      }
    });

    // show the dialog

    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
      }
    });


    try {
      semaphore.acquire();
    } catch (final InterruptedException e) {
      // NOP
    }

    return action;
  }
}
