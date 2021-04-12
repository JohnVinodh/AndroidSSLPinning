package com.jarvis.sslpinning;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.jarvis.sslpinning.utilities.HTTPAsyncTask;
import com.jarvis.sslpinning.utilities.HttpsServiceMetaData;
import com.jarvis.sslpinning.utilities.NetworkTaskListener;
import com.jarvis.sslpinning.utilities.Utils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements NetworkTaskListener {

    private EditText mEditTextURL,mEditTextThreadCount;
    private static Context activity_context;
    private ProgressBar mProgressBarLoadingIndicator;
    private NetworkTaskListener mNetworkTaskListener;
    private Spinner mSpinner;
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity_context = this;
        setContentView(R.layout.activity_main);
        mEditTextURL = findViewById(R.id.ed_url);
        mEditTextThreadCount = findViewById(R.id.ed_thread_count);
        mSpinner = findViewById(R.id.spinner_http_methods);
        //String url = getResources().getString(R.string.test_service_healogics);//getResources().getString(R.string.booksBaseURL) + "?q=" + "JohnVinodh" + "&key=" + getResources().getString(R.string.books_api_key);
        mEditTextURL.setText(R.string.test_service_url);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.request_method_arrays, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);
        mProgressBarLoadingIndicator = findViewById(R.id.loadingIndicator);
    }

    public void onBtnServiceCallClick(View view) {
       final String url = mEditTextURL.getText().toString();//getResources().getString(R.string.test_service_dbx);//mEditTextURL.getText().toString();
       String data = null;
        String pData = "{\n" +
                "\"language\" : \"EN\"\n" +
                "}";
        final String test = "{\n" +
                "   \"appID\":\"DohaBankR6S1\",\n" +
                "   \"serviceID\":\"getResourceBundle\",\n" +
                "   \"channel\":\"rc\"\n" +
                "}";
      HTTPAsyncTask httpAsyncTask = new HTTPAsyncTask(data, mSpinner.getSelectedItem().toString(),"getInfoSupportData",activity_context);
      httpAsyncTask.setNetworkTaskTaskListener(MainActivity.this);
      httpAsyncTask.execute(url);

    }

    public void onBtnServiceCallMultithreadsClick(View view) {
        final String url = mEditTextURL.getText().toString();
        HttpsServiceMetaData.threadCount = Integer.parseInt(mEditTextThreadCount.getText().toString());
        final String test = "{\n" +
                "   \"appID\":\"DohaBankR6S1\",\n" +
                "   \"serviceID\":\"getResourceBundle\",\n" +
                "   \"channel\":\"rc\"\n" +
                "}";
        final String data = "{}";

        mNetworkTaskListener = (NetworkTaskListener) activity_context;
        final String[] res = new String[1];
        if(mNetworkTaskListener !=null)
            mNetworkTaskListener.onNetworkCallIsInProgress();
        //Using multiple threads to make a service call.
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                URL uRL = null;
                try {
                    uRL = new URL(url);
                    HttpURLConnection connection =(HttpURLConnection) uRL.openConnection();
                    if(mNetworkTaskListener !=null)
                        res[0] =   Utils.invokeServiceCall(url,data,connection,mSpinner.getSelectedItem().toString(),activity_context);
                    mNetworkTaskListener.onNetworkCallCompleted(res[0]);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Looper.loop();
            }
        };

        for(int i =1; i<=HttpsServiceMetaData.threadCount; i++) {
            new Thread(runnable).start();
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
