package de.bjusystems.vdrmanager.utils.svdrp;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


/**
 * SSLSocketFactory
 * @author bju
 */
public class MySSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory {

  /** the key store */
  private KeyStore keyStore;

  /** the real socket factory */
  private final SSLSocketFactory sslFactory;

  /** the trust managers */
  private X509TrustManager[] trustManagers;

  public MySSLSocketFactory(final boolean acceptAllCertificates, final CertificateProblemListener certProblemListener)
      throws KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {

    super(null);

    // accept all host names
    this.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

    // load the keystore
    initKeyStore();

    // init the trust managers
    if (acceptAllCertificates) {
      initInsecureTrustManagers();
    } else {
      initSecureTrustManagers(certProblemListener);
    }

    // create SSL context
    final SSLContext context = SSLContext.getInstance("TLS");
    context.init(null, trustManagers, new SecureRandom());

    // create the real factory
    sslFactory = context.getSocketFactory();

  }

  @Override
  public Socket createSocket() throws IOException {
    return sslFactory.createSocket();
  }

  /**
   * initialize the key store
   * @return KeyStore
   * @throws KeyStoreException
   */
  private void initKeyStore() throws KeyStoreException {

    keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    // keyStore.load(...);
  }

  /**
   * initialize the trust managers validating certificates
   * @param acceptAllCertificates accept all certificates
   * @param certProblemListener listener to inform about certificate problems
   * @throws NoSuchAlgorithmException
   * @throws KeyStoreException
   */
  private void initSecureTrustManagers(final CertificateProblemListener certProblemListener) throws NoSuchAlgorithmException, KeyStoreException {

    final List<X509TrustManager> trustManagerList = new ArrayList<X509TrustManager>();

    // init the trust manager accepting certificates contained in the key store
    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
    trustManagerFactory.init(keyStore);
    X509TrustManager trustManager = getTrustManager(trustManagerFactory);
    if (trustManager != null) {
      trustManagerList.add(trustManager);
    }

    // init the trust manager accepting certificates accepted from the system
    trustManagerFactory = TrustManagerFactory.getInstance("X509");
    trustManagerFactory.init((KeyStore)null);
    trustManager = getTrustManager(trustManagerFactory);
    if (trustManager != null) {
      trustManagerList.add(trustManager);
    }

    trustManagers = new X509TrustManager[] {
        new X509TrustManager() {

          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          @Override
          public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            CertificateException lastException = null;
            for(final X509TrustManager tm : trustManagerList) {
              try {
                tm.checkServerTrusted(chain, authType);
                return;
              } catch (final CertificateException e) {
                lastException = e;
              }
            }

            switch (certProblemListener.reportProblem(chain, authType)) {
            case ACCEPT_ONCE:
              return;
            case ACCEPT_FOREVER:
              saveCertificate(chain, authType);
              return;
            default:
              if (lastException != null) {
                throw lastException;
              }
              break;
            }


            throw new CertificateException("Certificate not validated");
          }

          @Override
          public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            // NOP
          }
        }
    };
  }

  /**
   * initializes the trust managers validating nothing
   */
  private void initInsecureTrustManagers() {

    trustManagers = new X509TrustManager[] {
        new X509TrustManager() {
          @Override
          public X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          @Override
          public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
          }

          @Override
          public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
          }
        }
    };
  }

  /**
   * finds the X509 trust manager
   * @param trustManagerFactory TrustManager factory
   * @return X509 trust manager
   */
  private X509TrustManager getTrustManager(final TrustManagerFactory trustManagerFactory) {

    for(final TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
      if (trustManager instanceof X509TrustManager) {
        return (X509TrustManager) trustManager;
      }
    }
    return null;
  }

  /**
   * Saves the certificate
   * @param chain certificate chain
   * @param authType authentication type
   */
  private void saveCertificate(final X509Certificate[] chain, final String authType) {
  }
}
