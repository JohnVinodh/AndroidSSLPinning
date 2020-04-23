package com.jarvis.sslpinning.utilities;

/**
 * Created by KH2195 on 10/4/2016.
 */

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.jarvis.sslpinning.JarvisSSLSocketFactoryURLConnection;
import com.jarvis.sslpinning.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by johnvinodhtalluri on 31/07/16.
 */
public class Utils {
    private Context mContext = null;
    private static String TAG = Utils.class.getSimpleName();
    private static InputStream mInputStream = null;
    private static   String result = "";
    private static  String responseType = "";
    //private static  HttpURLConnection mConnection = null;
    public Utils(Context pContext) {
        mContext = pContext;
    }

    /**
     * convert input stream to string
     *
     * @param inputStream Inputstream from server
     * @return the string
     */
    public static String convertInputStreamToString(InputStream inputStream) throws IOException
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String res = "";

        while ((line = bufferedReader.readLine()) != null) {
            res += line;
        }

        inputStream.close();
        return res;
    }

    public static void adjustFontScale(Configuration configuration,Context context,int screenwidth)   {
        //configuration.fontScale =1.0f;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        //metrics.scaledDensity = configuration.fontScale * metrics.density;
        configuration.fontScale = (float)((metrics.widthPixels/metrics.density)/screenwidth);

        metrics.scaledDensity = configuration.fontScale * metrics.density;
        context.getResources().updateConfiguration(configuration, metrics);
    }

    public static byte[] convertInputStreamToByteArray(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 4096;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public InputStream getInputStreamForVirtualFile(Uri uri, String mimeTypeFilter,Context context)
            throws IOException {

        ContentResolver resolver = context.getContentResolver();

        String[] openableMimeTypes = resolver.getStreamTypes(uri, mimeTypeFilter);

        if (openableMimeTypes == null ||
                openableMimeTypes.length < 1) {
            throw new FileNotFoundException();
        }

        return resolver
                .openTypedAssetFileDescriptor(uri, openableMimeTypes[0], null)
                .createInputStream();
    }

    public static void safeCloseStream(Closeable fs) {
        if (fs != null) {
            try {
                fs.close();
            } catch (Exception e) {

                Log.e(TAG, Log.getStackTraceString(e));
            }
            fs = null;
        }
    }

    public static boolean setConnectionParams(String url, HttpURLConnection connection,String pHTTPMethod,Context pContext ) {
        URI requesturi = null;
        HttpURLConnection  mConnection = connection;
        StringBuffer newurl = new StringBuffer(url);
          mConnection = connection;
        mConnection.setRequestProperty("Cache-Control", "no-cache");
        mConnection.setUseCaches(false);
        try {
            requesturi = new URI(newurl.toString());
            if (pHTTPMethod.equals(HttpsServiceMetaData.HTTP_DELETE)) {
                mConnection.setRequestMethod("DELETE");
            } else if (pHTTPMethod.equals(HttpsServiceMetaData.HTTP_GET)) {
                mConnection.setRequestMethod("GET");
            } else if (pHTTPMethod.equals(HttpsServiceMetaData.HTTP_HEAD)) {
                mConnection.setRequestMethod("HEAD");
            } else if (pHTTPMethod.equals(HttpsServiceMetaData.HTTP_POST)) {
                mConnection.setRequestMethod("POST");
            } else if (pHTTPMethod.equals(HttpsServiceMetaData.HTTP_PUT)) {
                mConnection.setRequestMethod("PUT");
            } else {
                Log.i(TAG, "http request method is " + pHTTPMethod);
            }
        } catch (URISyntaxException e) {
            {
                Log.e(TAG, "" + e.getMessage());
            }
            return false;
        } catch (ProtocolException e) {
            {
                Log.e(TAG, "" + e.getMessage());
            }
            return false;
        }
        if (HttpsServiceMetaData.timeoutIntervalForRequest != null) {
            mConnection.setConnectTimeout(HttpsServiceMetaData.timeoutIntervalForRequest * 1000);

            Log.i(TAG, "setConnectionParams : timeoutIntervalForRequest : setConnectTimeout :: " + HttpsServiceMetaData.timeoutIntervalForRequest);
        }
        if (HttpsServiceMetaData.timeoutIntervalForResource != null) {
            mConnection.setReadTimeout(HttpsServiceMetaData.timeoutIntervalForResource * 1000);

            Log.i(TAG, "setConnectionParams : timeoutIntervalForResource : setReadTimeout :: " + HttpsServiceMetaData.timeoutIntervalForResource);

        }
        if (requesturi.toString().startsWith("https://")) {
            JarvisSSLSocketFactoryURLConnection.setHostNameVerifier((HttpsURLConnection) mConnection);
            ((HttpsURLConnection) mConnection).setSSLSocketFactory(JarvisSSLSocketFactoryURLConnection.getSocketFactory());
        }
        if (HttpsServiceMetaData.enableKeepAlive) {
            mConnection.setRequestProperty("Connection", "Keep-Alive");
        } else {
            mConnection.setRequestProperty("Connection", "close");
        }

        if (HttpsServiceMetaData.EXPECT_100_CONTINUE && (pHTTPMethod.equals(HttpsServiceMetaData.HTTP_POST) || pHTTPMethod.equals(HttpsServiceMetaData.HTTP_PUT)))
            mConnection.setRequestProperty("Expect", "100-continue");
        boolean isGZIPEnabled = Boolean.parseBoolean(pContext.getResources().getString(R.string.enable_gzip));
        if(HttpsServiceMetaData.IS_GZIP_ENABLED) {
            mConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");
            Log.i(TAG,"ContentType is :: gzip,deflate");
        }
         else {
            mConnection.setRequestProperty("Content-Type", "application/json");
            Log.i(TAG,"ContentType is :: application/json");
        }
        mConnection.setInstanceFollowRedirects(false);
        return true;
    }


    public static String getResponseContentType(HttpURLConnection conn) {
        if (conn == null)
            return "";
        String contentType = null;
        String header = conn.getContentType();
        if (header == null)
            return HttpsServiceMetaData.HTTP_RESPONSE_TYPE_RAWDATA;
        else if (header.contains("application/text") || header.contains("text/plain"))
            contentType = HttpsServiceMetaData.HTTP_RESPONSE_TYPE_TEXT;
        else if (header.contains("application/json"))
            contentType = HttpsServiceMetaData.HTTP_RESPONSE_TYPE_JSON;
        else if (header.contains("application/xml") || header.contains("text/xml") || header.contains("text/html") || header.contains("application/rss+xml"))
            contentType = HttpsServiceMetaData.HTTP_RESPONSE_TYPE_DOCUMENT;
        else
            contentType = HttpsServiceMetaData.HTTP_RESPONSE_TYPE_RAWDATA;

        Log.i(TAG,"getResponseContentType::"+contentType);
        return contentType;
    }

    public static String invokeServiceCall(String purl, String pData,HttpURLConnection pHttpURLConnection,String pHTTPMethod,Context pContext) {
        int respCode = 0;
        HttpURLConnection mConnection = pHttpURLConnection;
        try {
            //URL url = new URL(purl);
           // mConnection = (HttpURLConnection) url.openConnection();
            boolean paramsSetSuccess = setConnectionParams(purl,mConnection,pHTTPMethod,pContext);
            if (paramsSetSuccess == false) {
                Log.i(TAG,"unable to set the parameters so exiting the service call");
                return"";
            }
            if (pData != null && !pData.isEmpty() && pHTTPMethod.equalsIgnoreCase("POST")) {
                OutputStream os = mConnection.getOutputStream();
                os.write(pData.getBytes());
                os.flush();
                safeCloseStream(os);
            }
            respCode = mConnection.getResponseCode();
            responseType = Utils.getResponseContentType(mConnection);
            if (responseType == HttpsServiceMetaData.HTTP_RESPONSE_TYPE_RAWDATA) {
                // Need to implement if the output is rawbytes.
            } else {
                mInputStream = (respCode == 200 || respCode == 201) ? mConnection.getInputStream() : mConnection.getErrorStream();
            }
            //mInStream = (mRespCode == 200 || mRespCode == 201) ? mConnection.getInputStream(): mConnection.getErrorStream();
            result = convertInputStreamToString(mInputStream);

            //if(pNetworkTaskListener !=null)
              //  pNetworkTaskListener.onNetworkCallCompleted(result);
            Log.i(TAG, "Response :: " + result+"Thread info is ::"+Thread.currentThread().getName());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.i(TAG, "Generic exception :: " + e.getMessage());
        } finally {
            closeConnection(mConnection,purl);
        }
        return result;
    }

    private static void closeConnection(HttpURLConnection connection, String requestUrl) {
        printThreadSignatureAndStackTrace();
        if(responseType != HttpsServiceMetaData.HTTP_RESPONSE_TYPE_RAWDATA) {
            //Close connection if responseType is not a rawbytes case as the stream is yet to read in case of rawbytes.
            if(connection != null) {
                Log.i(TAG,"Entered the close section and connection is not null");
                connection.disconnect();
                Log.i(TAG,"Connection Closed for the url ::"+requestUrl);
            }
        }
    }
    public static void printThreadSignatureAndStackTrace() {
        Thread t = Thread.currentThread();
        long l = t.getId();
        String name = t.getName();
        long p = t.getPriority();
        String gname = t.getThreadGroup().getName();
        StringWriter w = new StringWriter();
        Exception e = new Exception();
        e.printStackTrace(new PrintWriter(w));
        System.out.println("Thread Signature : id = " + name + " , priority = " + p + " , group = " + gname + " , stacktrace = " + w.toString().replace("\n", " ").replace("\r", " "));

    }
}
