package com.jarvis.sslpinning;

import android.app.Application;
import android.content.Context;

public class JarvisSSLApplication extends Application {

    private static Context app_context = null;

    @Override
    public void onCreate() {
        super.onCreate();
        app_context = this;
    }

    public static Context getAppContext() {
        return app_context;
    }

}
