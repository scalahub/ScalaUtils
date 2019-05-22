package org.sh.utils.ssl.alternate;



import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

/**
 * code from
 * http://www.howardism.org/Technical/Java/SelfSignedCerts.html
 *
 */


/**
 * This Trust Manager is "naive" because it trusts everyone.
 **/
public class NaiveTrustManager implements X509TrustManager
{
  /**
   * Doesn't throw an exception, so this is how it approves a certificate.
   * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], String)
   **/
  public void checkClientTrusted ( X509Certificate[] cert, String authType )
              throws CertificateException
  {
  }

  /**
   * Doesn't throw an exception, so this is how it approves a certificate.
   * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], String)
   **/
  public void checkServerTrusted ( X509Certificate[] cert, String authType )
     throws CertificateException
  {
      
  }

  /**
   * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
   **/
  public X509Certificate[] getAcceptedIssuers ()
  {
    return null;  // I've seen someone return new X509Certificate[ 0 ];
  }

  private static SSLSocketFactory sslSocketFactory;

    /**
 * Returns a SSL Factory instance that accepts all server certificates.
 * <pre>SSLSocket sock =
 *     (SSLSocket) getSocketFactory.createSocket ( host, 443 ); </pre>
 * @return  An SSL-specific socket factory.
     **/
    public static final SSLSocketFactory getSocketFactory() throws NoSuchAlgorithmException
    {
      if ( sslSocketFactory == null ) {
        try {
          TrustManager[] tm = new TrustManager[] { new NaiveTrustManager() };
          SSLContext context = SSLContext.getInstance ("SSL");
          context.init( new KeyManager[0], tm, new SecureRandom( ) );

          sslSocketFactory = (SSLSocketFactory) context.getSocketFactory ();

        } catch (KeyManagementException e) {
          System.out.println ("No SSL algorithm support: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
          System.out.println ("Exception when setting up the Naive key management.");
        }
      }
      return sslSocketFactory;
    }
}