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
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by johnvinodhtalluri on 31/07/16.
 */
public class Utils {
    private Context mContext = null;
    private static String TAG = Utils.class.getSimpleName();
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
        HttpURLConnection  mConnection = null;
        StringBuffer newurl = new StringBuffer(url);
        if(connection !=null)
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
            }
            {
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
        boolean bExpect100Continue = Boolean.parseBoolean(pContext.getResources().getString(R.string.expect_100_continue));

       /* int resId  = mContext.getResources().getIdentifier("expect_100_continue", "string", mContext.getPackageName());
        if(mContext.getString(resId).equalsIgnoreCase("true"))
            bExpect100Continue = true;
        else
            bExpect100Continue = false;*/

        if (bExpect100Continue && (pHTTPMethod.equals(HttpsServiceMetaData.HTTP_POST) || pHTTPMethod.equals(HttpsServiceMetaData.HTTP_PUT)))
            mConnection.setRequestProperty("Expect", "100-continue");
        boolean isGZIPEnabled = Boolean.parseBoolean(pContext.getResources().getString(R.string.enable_gzip));
        if(isGZIPEnabled)
            mConnection.setRequestProperty("Accept-Encoding", "gzip,deflate");

        //mConnection.setRequestProperty("Content-Type", "application/json");
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
}
