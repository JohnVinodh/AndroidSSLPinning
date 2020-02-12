package com.jarvis.sslpinning;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class JarvisSSLSocketFactoryURLConnection extends SSLSocketFactory {

    private static String TAG = "JarvisSSLSocketFactoryURLConnection";
    private static KeyManager[] kMgrs = null;
    private static int NETWORK_TRUST_CONFIG_NONE = 0;
    private static int NETWORK_TRUST_CONFIG_ONLY_BUNDLED = 1;
    private static int NETWORK_TRUST_CONFIG_ALL = 2;
    private static int NETWORK_TRUST_CONFIG_ALLOW_PINNED = 3;
    private static int mSDKVersion;
    private static int mNetworkTrustConfig = NETWORK_TRUST_CONFIG_ONLY_BUNDLED;
    private static TrustManager[] tMgrs = null;

    private SSLContext getSSLContext() throws IOException {
        return createSSLContext();
    }

    private static SSLContext createSSLContext() throws IOException {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kMgrs, getTrustManagers(), null);
            return context;
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    private static TrustManager[] getTrustManagers() {
        if (mNetworkTrustConfig == NETWORK_TRUST_CONFIG_ALLOW_PINNED)
            return null; // No need to check further as TrustManagers are maintained in KonyPublicKeyPinningManager.
        else if (tMgrs == null)//load only once as server certs are statically bundled
            tMgrs = loadTrustManagers();
        return tMgrs;
    }

    private static TrustManager[] loadTrustManagers() {
        if (mNetworkTrustConfig == NETWORK_TRUST_CONFIG_ONLY_BUNDLED) {
            mSDKVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
            if (mSDKVersion >= 11) { // 11-> HoneyCombm
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = null;
                try {
                    tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                    tmf.init(getBundledCertsKeyStore());
                    return tmf.getTrustManagers();
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, e.getMessage());
                } catch (KeyStoreException e) {
                    Log.e(TAG, e.getMessage());
                }
                return null;
            } else { // less than HoneyComb i.e  2.x.x devices
                TrustManager tm = null;
                try {
                    tm = new CustomTrustManager(getBundledCertsKeyStore());
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, e.getMessage());
                } catch (KeyStoreException e) {
                    Log.e(TAG, e.getMessage());
                }
                return new TrustManager[]{tm};
            }
        } else if (mNetworkTrustConfig == NETWORK_TRUST_CONFIG_ALL) {
            //X509TrustManager tmf = KonyAllowAllTrustManager.getInstance();
            //return new TrustManager[] {tmf};
            Log.d(TAG, "Not handling Selfsigned certificates");
        }
        return null;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        String[] DefaultCipherSuits = new String[0];
        try {
            DefaultCipherSuits = getSSLContext().getSocketFactory().getDefaultCipherSuites();
        } catch (IOException e) {

            Log.d(TAG, Log.getStackTraceString(e));
        }
        Log.d(TAG, "Default CipherSuites:");
        for (String cipher : DefaultCipherSuits)
            Log.d(TAG, "" + cipher);
        return DefaultCipherSuits;
    }

    @Override
    public String[] getSupportedCipherSuites() {
        String[] SupportedCipherSuits = new String[0];
        try {
            SupportedCipherSuits = getSSLContext().getSocketFactory().getSupportedCipherSuites();
        } catch (IOException e) {

            Log.d(TAG, Log.getStackTraceString(e));
        }
        Log.d(TAG, "Default Supported CipherSuites: ");
        for (String cipher : SupportedCipherSuits)
            Log.d(TAG, "" + cipher);
        return SupportedCipherSuits;
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        //First createSocket called
        //Incase of connection failure(ex: handshake  failure) underlying implementation silently tries 4 times to reconnect and hence this API gets called 4 times in failure cases.
        // Refer https://android.googlesource.com/platform/external/okhttp/+/android-7.1.1_r23/android/main/java/com/squareup/okhttp/HttpsHandler.java
        Socket sckt = enableSSLProtocolsAndCipherSuites(getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose));
        //Register Handshake listener only in debug mode to Print Protocol and CipherSuites info
        if (sckt instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) sckt;
            sslSocket.addHandshakeCompletedListener(new JarvisHandShakeListener());
        }
        return sckt;
    }

    private static class CustomTrustManager implements X509TrustManager {

        private final TrustManager[] originalTrustManagers;
        private final KeyStore trustStore;

        /**
         * @param trustStore A KeyStore containing the server certificate that should be trusted
         * @throws NoSuchAlgorithmException
         * @throws KeyStoreException
         */
        public CustomTrustManager(KeyStore trustStore) throws NoSuchAlgorithmException, KeyStoreException {
            this.trustStore = trustStore;

            final TrustManagerFactory originalTrustManagerFactory = TrustManagerFactory.getInstance("X509");
            originalTrustManagerFactory.init(trustStore);

            originalTrustManagers = originalTrustManagerFactory.getTrustManagers();
        }

        /**
         * No-op. Never invoked by client, only used in server-side implementations
         *
         * @return
         */
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        /**
         * No-op. Never invoked by client, only used in server-side implementations
         *
         * @return
         */
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }


        /**
         * Given the partial or complete certificate chain provided by the peer,
         * build a certificate path to a trusted root and return if it can be validated and is trusted
         * for client SSL authentication based on the authentication type. The authentication type is
         * determined by the actual certificate used. For instance, if RSAPublicKey is used, the authType should be "RSA".
         * Checking is case-sensitive.
         * Defers to the default trust manager first, checks the cert supplied in the ctor if that fails.
         *
         * @param chain    the server's certificate chain
         * @param authType the authentication type based on the client certificate
         * @throws CertificateException
         */
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                for (TrustManager originalTrustManager : originalTrustManagers) {
                    ((X509TrustManager) originalTrustManager).checkServerTrusted(chain, authType);
                }
            } catch (CertificateException originalException) {
                try {
                    // Ordering issue?
                    X509Certificate[] reorderedChain = reorderCertificateChain(chain);
                    if (!Arrays.equals(chain, reorderedChain)) {
                        checkServerTrusted(reorderedChain, authType);
                        return;
                    }
                    for (int i = 0; i < chain.length; i++) {
                        if (validateCert(reorderedChain[i])) {
                            return;
                        }
                    }
                    throw originalException;
                } catch (Exception ex) {
                    Log.e(TAG, Log.getStackTraceString(ex));
                    throw originalException;
                }
            }

        }

        /**
         * Checks if we have added the certificate in the trustStore, if that's the case we trust the certificate
         *
         * @param x509Certificate the certificate to check
         * @return true if we know the certificate, false otherwise
         * @throws KeyStoreException on problems accessing the key store
         */
        private boolean validateCert(final X509Certificate x509Certificate) throws KeyStoreException {
            return trustStore.getCertificateAlias(x509Certificate) != null;
        }

        /**
         * Puts the certificate chain in the proper order, to deal with out-of-order
         * certificate chains as are sometimes produced by Apache's mod_ssl
         *
         * @param chain the certificate chain, possibly with bad ordering
         * @return the re-ordered certificate chain
         */
        private X509Certificate[] reorderCertificateChain(X509Certificate[] chain) {

            X509Certificate[] reorderedChain = new X509Certificate[chain.length];
            List<X509Certificate> certificates = Arrays.asList(chain);

            int position = chain.length - 1;
            X509Certificate rootCert = findRootCert(certificates);
            reorderedChain[position] = rootCert;

            X509Certificate cert = rootCert;
            while ((cert = findSignedCert(cert, certificates)) != null && position > 0) {
                reorderedChain[--position] = cert;
            }

            return reorderedChain;
        }

        /**
         * A helper method for certificate re-ordering.
         * Finds the root certificate in a possibly out-of-order certificate chain.
         *
         * @param certificates the certificate change, possibly out-of-order
         * @return the root certificate, if any, that was found in the list of certificates
         */
        private X509Certificate findRootCert(List<X509Certificate> certificates) {
            X509Certificate rootCert = null;

            for (X509Certificate cert : certificates) {
                X509Certificate signer = findSigner(cert, certificates);
                if (signer == null || signer.equals(cert)) { // no signer present, or self-signed
                    rootCert = cert;
                    break;
                }
            }

            return rootCert;
        }

        /**
         * A helper method for certificate re-ordering.
         * Finds the first certificate in the list of certificates that is signed by the sigingCert.
         */
        private X509Certificate findSignedCert(X509Certificate signingCert, List<X509Certificate> certificates) {
            X509Certificate signed = null;

            for (X509Certificate cert : certificates) {
                Principal signingCertSubjectDN = signingCert.getSubjectDN();
                Principal certIssuerDN = cert.getIssuerDN();
                if (certIssuerDN.equals(signingCertSubjectDN) && !cert.equals(signingCert)) {
                    signed = cert;
                    break;
                }
            }

            return signed;
        }

        /**
         * A helper method for certificate re-ordering.
         * Finds the certificate in the list of certificates that signed the signedCert.
         */
        private X509Certificate findSigner(X509Certificate signedCert, List<X509Certificate> certificates) {
            X509Certificate signer = null;

            for (X509Certificate cert : certificates) {
                Principal certSubjectDN = cert.getSubjectDN();
                Principal issuerDN = signedCert.getIssuerDN();
                if (certSubjectDN.equals(issuerDN)) {
                    signer = cert;
                    break;
                }
            }

            return signer;
        }
    }

    public static KeyStore getBundledCertsKeyStore() {
        CertificateFactory cf;
        KeyStore keyStore = null;
        try {
            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);

            cf = CertificateFactory.getInstance("X.509");
            String[] certFiles = null;
            String dir = "";
            certFiles = MainActivity.getAppContext().getAssets().list(dir + "certs");
            for (int i = 0; i < certFiles.length; i++) {
                String fileName = "certs/" + certFiles[i];
                if (fileName.endsWith(".json"))
                    continue; // Skip public_keys.json file as it might be in the certs folder.
                InputStream caInput = new BufferedInputStream(MainActivity.getAppContext().getAssets().open(dir + fileName));
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                } finally {
                    caInput.close();
                }
                keyStore.setCertificateEntry("ca" + i, ca);
            }
        } catch (CertificateException e) {

            Log.e(TAG, e.getMessage());
        } catch (IOException e) {

            Log.e(TAG, e.getMessage());
        } catch (NoSuchAlgorithmException e) {

            Log.e(TAG, e.getMessage());
        } catch (KeyStoreException e) {

            Log.e(TAG, e.getMessage());
        }
        return keyStore;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return enableSSLProtocolsAndCipherSuites(getSSLContext().getSocketFactory().createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return enableSSLProtocolsAndCipherSuites(getSSLContext().getSocketFactory().createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableSSLProtocolsAndCipherSuites(getSSLContext().getSocketFactory().createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableSSLProtocolsAndCipherSuites(getSSLContext().getSocketFactory().createSocket(address, port, localAddress, localPort));
    }

    private static Socket enableSSLProtocolsAndCipherSuites(Socket socket) {

        if (socket != null && socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            String[] argsProtocols;
            String[] argsCipherSuites;
            mSDKVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
            if (mSDKVersion < 21) {// below android 5.0 we are enabling the cipher suites and protocols
                argsProtocols = sslSocket.getSupportedProtocols();
                argsCipherSuites = sslSocket.getSupportedCipherSuites();
                if (argsProtocols != null)
                    sslSocket.setEnabledProtocols(argsProtocols);
                if (argsCipherSuites != null) {
                    ArrayList<String> list = new ArrayList<>(Arrays.asList(argsCipherSuites));
                    if (list.contains("TLS_FALLBACK_SCSV"))
                        list.remove("TLS_FALLBACK_SCSV");
                    argsCipherSuites = list.toArray(new String[list.size()]);
                    sslSocket.setEnabledCipherSuites(argsCipherSuites);
                }
            } else {//5.0 and above we are just printing default enabled ciphers usites ond evice for debugging purpose.
                argsProtocols = sslSocket.getEnabledProtocols();
                argsCipherSuites = sslSocket.getEnabledCipherSuites();
            }
            Log.d(TAG, "Supported Protocols: ");
            for (String protocol : argsProtocols)
                Log.d(TAG, "" + protocol);
            Log.d(TAG, "Supported CipherSuites:");
            for (String cipher : argsCipherSuites)
                Log.d(TAG, "" + cipher);
        }
        return socket;
    }

    private static class JarvisHandShakeListener implements HandshakeCompletedListener {
        @Override
        public void handshakeCompleted(HandshakeCompletedEvent event) {
            {
                SSLSession session = event.getSession();
                Log.d(TAG, "SSL Handshake Completed Successfully");
                Log.d(TAG, "Connection is using SSL/TLS protocol: " + session.getProtocol() + " connected to host: " + session.getPeerHost() + " using cipher suit: " + session.getCipherSuite());
            }
        }
    }
}
