package org.sh.utils.common.FixSSL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrustManagerCustom {
    static SSLSocketFactory orig;
    public static void setFullTrust() {
    // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
                }
                @Override
                public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
                @Override
                public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
                }
            }        
        };
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            orig = HttpsURLConnection.getDefaultSSLSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
        }
        // Now you can access an https URL without having the certificate in the truststore
        //try {
        //    URL url = new URL("https://hostname/index.html");
        //} catch (MalformedURLException e) {
        //}
    }
    public static void unsetFullTrust() throws Exception {
        if (orig == null) throw new Exception("Trust manager has not been set"); // Install the orig trust manager
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(orig);
        } catch (Exception e) {
        }
        // Now you can access an https URL without having the certificate in the truststore
        //try {
        //    URL url = new URL("https://hostname/index.html");
        //} catch (MalformedURLException e) {
        //}
    }
}