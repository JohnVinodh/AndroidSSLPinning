package com.jarvis.sslpinning.utilities;

public interface HttpAsyncTaskListener {

    void onNetworkCallCompleted(String result);
    void onNetworkCallIsInProgress();
}
