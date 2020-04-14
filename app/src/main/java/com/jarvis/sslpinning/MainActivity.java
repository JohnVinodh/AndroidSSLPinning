package com.jarvis.sslpinning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jarvis.sslpinning.utilities.HTTPAsyncTask;
import com.jarvis.sslpinning.utilities.HttpsServiceMetaData;

import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements HTTPAsyncTask.HttpAsyncTaskListener {

    private EditText mEditTextURL;
    private static Context activity_context;
    private ProgressBar mProgressBarLoadingIndicator;
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
        String url = mEditTextURL.getText().toString();
        String pData = "{\n" +
                "\"language\" : \"EN\"\n" +
                "}";
        String test = "{\n" +
                "   \"appID\":\"DohaBankR6S1\",\n" +
                "   \"serviceID\":\"getResourceBundle\",\n" +
                "   \"channel\":\"rc\"\n" +
                "}";

      HTTPAsyncTask httpAsyncTask = new HTTPAsyncTask(pData, HttpsServiceMetaData.HTTP_POST,"getInfoSupportData",getActivityContext());
      httpAsyncTask.setHTTPAsyncTaskListener(MainActivity.this);
      httpAsyncTask.execute(url);
    }

    public static Context getAppContext() {
        return JarvisSSLApplication.getAppContext();
    }

    public static Context getActivityContext() {
      return activity_context;
    }

    @Override
    public void onHttpTaskCompleted(String result) {
        mProgressBarLoadingIndicator.setVisibility(View.GONE);
        Toast.makeText(getActivityContext(),"response is ::"+result,Toast.LENGTH_LONG).show();
    }

    @Override
    public void httpTaskIsInProgress() {
        mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
    }
}
