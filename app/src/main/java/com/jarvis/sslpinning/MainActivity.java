package com.jarvis.sslpinning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jarvis.sslpinning.utilities.HTTPAsyncTask;
import com.jarvis.sslpinning.utilities.HttpAsyncTaskListener;
import com.jarvis.sslpinning.utilities.HttpsServiceMetaData;
import com.jarvis.sslpinning.utilities.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements HttpAsyncTaskListener {

    private EditText mEditTextURL;
    private static Context activity_context;
    private ProgressBar mProgressBarLoadingIndicator;
    private HttpAsyncTaskListener mHttpAsyncTaskListener;
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity_context = this;

        setContentView(R.layout.activity_main);
        mEditTextURL = findViewById(R.id.ed_url);
        //String url = getResources().getString(R.string.booksBaseURL) + "?q=" + "JohnVinodh" + "&key=" + getResources().getString(R.string.books_api_key);
        mEditTextURL.setText(R.string.test_service_dbx);
        mProgressBarLoadingIndicator = findViewById(R.id.loadingIndicator);
    }

    public void onBtnServiceCallClick(View view) {
       final String url = mEditTextURL.getText().toString();
        String pData = "{\n" +
                "\"language\" : \"EN\"\n" +
                "}";
        final String test = "{\n" +
                "   \"appID\":\"DohaBankR6S1\",\n" +
                "   \"serviceID\":\"getResourceBundle\",\n" +
                "   \"channel\":\"rc\"\n" +
                "}";

      HTTPAsyncTask httpAsyncTask = new HTTPAsyncTask(pData, HttpsServiceMetaData.HTTP_POST,"getInfoSupportData",getActivityContext());
      httpAsyncTask.setHTTPAsyncTaskListener(MainActivity.this);
      httpAsyncTask.execute(url);

    }

    public void onBtnServiceCallMultithreadsClick(View view) {
        final String url = mEditTextURL.getText().toString();
        final String test = "{\n" +
                "   \"appID\":\"DohaBankR6S1\",\n" +
                "   \"serviceID\":\"getResourceBundle\",\n" +
                "   \"channel\":\"rc\"\n" +
                "}";

        String result = "";
        mHttpAsyncTaskListener = (HttpAsyncTaskListener) MainActivity.this;
        //Using multiple threads to make a service call.
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                executeServiceCall(url,test,HttpsServiceMetaData.HTTP_POST);
                Looper.loop();
            }
        };

        for(int i =0; i<=5; i++) {
            new Thread(runnable).start();
        }
    }
    private void executeServiceCall(String url, String pData,String pHTTPMethod) {
        int respCode = 0;
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            boolean paramsSetSuccess = Utils.setConnectionParams(url,connection,pHTTPMethod,getApplicationContext());
            if (paramsSetSuccess == false) {
                Log.i(TAG,"unable to set the parameters so exiting the service call");
                return;
            }
            if (pData != null && !pData.isEmpty() && pHTTPMethod.equalsIgnoreCase("POST")) {
                OutputStream os = connection.getOutputStream();
                os.write(pData.getBytes());
                os.flush();
                Utils.safeCloseStream(os);
            }
            respCode = connection.getResponseCode();
            String responseType = Utils.getResponseContentType(connection);
            if (responseType == HttpsServiceMetaData.HTTP_RESPONSE_TYPE_RAWDATA) {
                // Need to implement if the output is rawbytes.
            } else {
                inputStream = (respCode == 200 || respCode == 201) ? connection.getInputStream() : connection.getErrorStream();
            }
            //mInStream = (mRespCode == 200 || mRespCode == 201) ? mConnection.getInputStream(): mConnection.getErrorStream();
            String result = Utils.convertInputStreamToString(inputStream);
            if(mHttpAsyncTaskListener !=null)
                mHttpAsyncTaskListener.onNetworkCallCompleted(result);
            Log.i("Kony", "Response :: " + result);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.i(TAG, "Generic exception :: " + e.getMessage());
        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    public static Context getAppContext() {
        return JarvisSSLApplication.getAppContext();
    }

    public static Context getActivityContext() {
      return activity_context;
    }

    @Override
    public void onNetworkCallCompleted(String result) {
        mProgressBarLoadingIndicator.setVisibility(View.GONE);
        Toast.makeText(getActivityContext(),"response is ::"+result,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNetworkCallIsInProgress() {
        mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
    }
}
