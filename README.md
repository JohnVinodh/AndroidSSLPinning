# AndroidSSLPinning
Android SSL Pinning

Instead of packing the certificates in the raw folder. 
In the sample we are creating certs folder under assets.
Placing the certificate downloaded in the certs folder.
Rest is taken care of https webservice call. 
However, you only need to validate whether the below value is set in JarvisSSLSocketFactoryURLConnection.java file.
private static int mNetworkTrustConfig = NETWORK_TRUST_CONFIG_ONLY_BUNDLED;
 Selecting None will not check for SSL Certificate Pinning.
