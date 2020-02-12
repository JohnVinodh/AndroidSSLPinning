package com.jarvis.sslpinning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private EditText mEditTextURL;
    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_HEAD = "HEAD";
    private static Context activity_context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity_context = this;
        setContentView(R.layout.activity_main);
        mEditTextURL = findViewById(R.id.ed_url);
    }

    public void onBtnServiceCallClick(View view) {
      String url = mEditTextURL.getText().toString();
        URI requesturi = null;
        try {
            requesturi = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (requesturi.toString().startsWith("https://")) {
            //JarvisSSLSocketFactoryURLConnection.setHostNameVerifier((HttpsURLConnection)connection);
            //((HttpsURLConnection) connection).setSSLSocketFactory(KonySSLSocketFactoryURLConnection.getSocketFactory());
        }
    }

    public static Context getAppContext() {
        return JarvisSSLApplication.getAppContext();
    }

    public static Context getActivityContext() {
      return activity_context;
    }
}
