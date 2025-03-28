package org.vash.vate.tls;

import java.lang.reflect.Method;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class VTTLSVerificationDisabler
{
  private static class OverlyOptimisticHostnameVerifier implements HostnameVerifier
  {
    public boolean verify(String arg0, SSLSession arg1)
    {
      return true;
    }
  }
  
  @SuppressWarnings("all")
  private static class OverlyOptimisticTrustManager implements TrustManager, X509TrustManager
  {
    public java.security.cert.X509Certificate[] getAcceptedIssuers()
    {
      return new java.security.cert.X509Certificate[] {};
    }
    
    public boolean isServerTrusted(java.security.cert.X509Certificate[] certs)
    {
      return true;
    }
    
    public boolean isClientTrusted(java.security.cert.X509Certificate[] certs)
    {
      return true;
    }
    
    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
    {
      return;
    }
    
    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
    {
      return;
    }
  }
  
  @SuppressWarnings("all")
  private static class OverlyOptimisticKeyManager implements KeyManager
  {
    public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2)
    {
      return "";
    }
    
    public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2)
    {
      return "";
    }
    
    public X509Certificate[] getCertificateChain(String arg0)
    {
      return new X509Certificate[] {};
    }
    
    public String[] getClientAliases(String arg0, Principal[] arg1)
    {
      return new String[] {};
    }
    
    public PrivateKey getPrivateKey(String arg0)
    {
      return null;
    }
    
    public String[] getServerAliases(String arg0, Principal[] arg1)
    {
      return new String[] {};
    }
  }
  
  public static boolean install()
  {
    try
    {
      System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
      //System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
      TrustManager[] trustAnything = new TrustManager[]
      { new OverlyOptimisticTrustManager() };
      //KeyManager[] manageNothing = new KeyManager[] { new OverlyOptimisticKeyManager() };
      SSLContext unverifiedTLS = SSLContext.getInstance("TLS");
      unverifiedTLS.init(null, trustAnything, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(unverifiedTLS.getSocketFactory());
      HttpsURLConnection.setDefaultHostnameVerifier(new OverlyOptimisticHostnameVerifier());
      try
      {
        Method setDefaultMethod = SSLContext.class.getDeclaredMethod("setDefault", SSLContext.class);
        setDefaultMethod.invoke(null, unverifiedTLS);
        // SSLContext.setDefault(unverifiedSSL);
      }
      catch (Throwable ei)
      {
        //ei.printStackTrace();
        // return false;
      }
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
      return false;
    }
    return true;
  }
  
  public SSLContext createOverlyOptimisticTLSContext() throws Exception
  {
    TrustManager[] trustAnything = new TrustManager[]
    { new OverlyOptimisticTrustManager() };
    //KeyManager[] manageNothing = new KeyManager[] { new OverlyOptimisticKeyManager() };
    SSLContext unverifiedTLS = SSLContext.getInstance("TLS");
    unverifiedTLS.init(null, trustAnything, new java.security.SecureRandom());
    return unverifiedTLS;
  }
}