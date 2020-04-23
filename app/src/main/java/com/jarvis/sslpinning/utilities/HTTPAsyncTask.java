package com.jarvis.sslpinning.utilities;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
            httpAsyncTaskListener.onNetworkCallIsInProgress();
    }

    @Override
    protected String doInBackground(String... urls) {

        try {
            //CookieStore needs to be set before opening connection else cookies will not be saved after receiving response
            if (urls[0] == null)
                return "";
            URL url = new URL(urls[0]);
            mConnection = (HttpURLConnection) url.openConnection();
            boolean paramsSetSuccess = Utils.setConnectionParams(urls[0],mConnection,mHTTPMethod,mContext);
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
            String responseType = Utils.getResponseContentType(mConnection);
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





    @Override
    protected void onPostExecute(String pResult) {
        super.onPostExecute(pResult);
        if (httpAsyncTaskListener != null)
            httpAsyncTaskListener.onNetworkCallCompleted(pResult);
    }
}

