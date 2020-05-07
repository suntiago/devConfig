package viroyal.com.dev.util;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HandleSSLHandshakeUtil {

  /**
   * 忽略https的证书校验
   * 避免Glide加载https图片报错：
   * javax.net.ssl.SSLHandshakeException: java.security.cert.CertPathValidatorException: Trust ancho
   * r for certification path not found.
   */
  public static void handleSSLHandshake() {
    try {
      TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
      }};

      SSLContext sc = SSLContext.getInstance("TLS");
      // trustAllCerts信任所有的证书
      sc.init(null, trustAllCerts, new SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    } catch (Exception ignored) {
    }
  }
}
