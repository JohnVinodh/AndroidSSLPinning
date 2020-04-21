package com.jarvis.sslpinning.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.jarvis.sslpinning.JarvisSSLSocketFactoryURLConnection;
import com.jarvis.sslpinning.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by KH2195 on 10/4/2016.
 */

public class HTTPAsyncTask extends AsyncTask<String, Void, String> {


    private String mData = "",
            mResult = "",
            mHTTPMethod = "",
            mServiceCall = "",
            LOG_TAG = "HTTPAsyncTask";
    private HttpURLConnection mConnection = null;
    private static String TAG = HTTPAsyncTask.class.getSimpleName();
    private InputStream mInStream = null;
    private int mRespCode = 0;
    private Context mContext;
    private HttpAsyncTaskListener httpAsyncTaskListener;

    public HTTPAsyncTask(String pData, String pHTTPMethod, String pServiceCall, Context pContext) {
        mData = pData;
        mServiceCall = pServiceCall;
        mHTTPMethod = pHTTPMethod;
        mContext = pContext.getApplicationContext();
    }

    public void setHTTPAsyncTaskListener(HttpAsyncTaskListener httpAsyncTaskListener) {
        this.httpAsyncTaskListener = httpAsyncTaskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (httpAsyncTaskListener != null)
            httpAsyncTaskListener.httpTaskIsInProgress();
    }

    @Override
    protected String doInBackground(String... urls) {

        try {
            //CookieStore needs to be set before opening connection else cookies will not be saved after receiving response
            if (urls[0] == null)
                return "";
            URL url = new URL(urls[0]);
            mConnection = (HttpURLConnection) url.openConnection();
            boolean paramsSetSuccess = setConnectionParams(urls[0]);
            if (paramsSetSuccess == false)
                return "";
           /* mConnection.setReadTimeout(60000);
            mConnection.setConnectTimeout(60000);

            if(mReqMethod.equalsIgnoreCase("POST")) {
                mConnection.setDoOutput(true);
            } else {
                mConnection.setDoInput(true);
            }
            mConnection.setRequestMethod(mReqMethod);
            mConnection.setRequestProperty("Connection", "Keep-Alive");
            mConnection.setRequestProperty("Content-Type", "application/json");*/

            if (mData != null && !mData.isEmpty() && mHTTPMethod.equalsIgnoreCase("POST")) {
                OutputStream os = mConnection.getOutputStream();
                os.write(mData.getBytes());
                os.flush();
                Utils.safeCloseStream(os);
            }
            mRespCode = mConnection.getResponseCode();
            String responseType = getResponseContentType(mConnection);
            if (responseType == HttpsServiceMetaData.HTTP_RESPONSE_TYPE_RAWDATA) {
                // Need to implement if the output is rawbytes.
            } else {
                mInStream = (mRespCode == 200 || mRespCode == 201) ? mConnection.getInputStream() : mConnection.getErrorStream();
            }
            //mInStream = (mRespCode == 200 || mRespCode == 201) ? mConnection.getInputStream(): mConnection.getErrorStream();
            mResult = Utils.convertInputStreamToString(mInStream);
            Log.i("Kony", "Response :: " + mResult);
        } catch (MalformedURLException me) {
            Log.i(LOG_TAG, "MalformedURLException :: " + me.getMessage());
        } catch (IOException e) {
            Log.i(LOG_TAG, "IOException :: " + e.getMessage());
        } catch (Exception e) {
            Log.i(LOG_TAG, "Generic exception :: " + e.getMessage());
        } finally {

            if (mConnection != null) {
                mConnection.disconnect();
            }
        }
        return mResult;
    }

    private boolean setConnectionParams(String url) {
        URI requesturi = null;
        StringBuffer newurl = new StringBuffer(url);
        mConnection.setRequestProperty("Cache-Control", "no-cache");
        mConnection.setUseCaches(false);
        try {
            requesturi = new URI(newurl.toString());
            if (mHTTPMethod.equals(HttpsServiceMetaData.HTTP_DELETE)) {
                mConnection.setRequestMethod("DELETE");
            } else if (mHTTPMethod.equals(HttpsServiceMetaData.HTTP_GET)) {
                mConnection.setRequestMethod("GET");
            } else if (mHTTPMethod.equals(HttpsServiceMetaData.HTTP_HEAD)) {
                mConnection.setRequestMethod("HEAD");
            } else if (mHTTPMethod.equals(HttpsServiceMetaData.HTTP_POST)) {
                mConnection.setRequestMethod("POST");
            } else if (mHTTPMethod.equals(HttpsServiceMetaData.HTTP_PUT)) {
                mConnection.setRequestMethod("PUT");
            }
            {
                Log.i(TAG, "http request method is " + mHTTPMethod);
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
        boolean bExpect100Continue = Boolean.parseBoolean(mContext.getResources().getString(R.string.expect_100_continue));

       /* int resId  = mContext.getResources().getIdentifier("expect_100_continue", "string", mContext.getPackageName());
        if(mContext.getString(resId).equalsIgnoreCase("true"))
            bExpect100Continue = true;
        else
            bExpect100Continue = false;*/

        if (bExpect100Continue && (mHTTPMethod.equals(HttpsServiceMetaData.HTTP_POST) || mHTTPMethod.equals(HttpsServiceMetaData.HTTP_PUT)))
            mConnection.setRequestProperty("Expect", "100-continue");
        boolean isGZIPEnabled = Boolean.parseBoolean(mContext.getResources().getString(R.string.enable_gzip));
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

        return contentType;
    }

    @Override
    protected void onPostExecute(String pResult) {
        super.onPostExecute(pResult);
        if (httpAsyncTaskListener != null)
            httpAsyncTaskListener.onHttpTaskCompleted(pResult);
    }

    public interface HttpAsyncTaskListener {
        void onHttpTaskCompleted(String result);

        void httpTaskIsInProgress();
    }
}

