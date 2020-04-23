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
            mServiceCall = "";

    private HttpURLConnection mConnection = null;
    private static String TAG = HTTPAsyncTask.class.getSimpleName();
    private int mRespCode = 0;
    private Context mContext;
    private NetworkTaskListener mNetworkTaskListener;

    public HTTPAsyncTask(String pData, String pHTTPMethod, String pServiceCall, Context pContext) {
        mData = pData;
        mServiceCall = pServiceCall;
        mHTTPMethod = pHTTPMethod;
        mContext = pContext.getApplicationContext();
    }

    public void setNetworkTaskTaskListener(NetworkTaskListener mNetworkTaskListener) {
        this.mNetworkTaskListener = mNetworkTaskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mNetworkTaskListener != null)
            mNetworkTaskListener.onNetworkCallIsInProgress();
    }

    @Override
    protected String doInBackground(String... urls) {

        try {
            //CookieStore needs to be set before opening connection else cookies will not be saved after receiving response
            if (urls[0] == null)
                return "";
            URL url = new URL(urls[0]);
            mConnection = (HttpURLConnection) url.openConnection();
            mResult = Utils.invokeServiceCall(urls[0], mData,mConnection, mHTTPMethod, mContext);
            Log.i(TAG, "Response :: " + mResult + " of Service call :: " + mServiceCall);
        } catch (Exception e) {
            Log.i(TAG, "Generic exception :: " + e.getMessage());
        }
        return mResult;
    }

    @Override
    protected void onPostExecute(String pResult) {
        super.onPostExecute(pResult);
        if (mNetworkTaskListener != null)
            mNetworkTaskListener.onNetworkCallCompleted(pResult);
    }
}

