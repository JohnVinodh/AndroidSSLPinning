package com.jarvis.sslpinning.net;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class JarvisAllowAllTrustManager
{
    private static X509TrustManager tm = new X509TrustManager()
    {
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException
        {}

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException
        {}

        public X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }
    };

    public static X509TrustManager getInstance()
    {
        return tm;
    }
}