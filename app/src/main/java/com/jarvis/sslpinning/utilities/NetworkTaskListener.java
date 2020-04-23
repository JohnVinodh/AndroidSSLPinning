package com.jarvis.sslpinning.utilities;

public interface NetworkTaskListener {

    void onNetworkCallCompleted(String result);
    void onNetworkCallIsInProgress();
}
