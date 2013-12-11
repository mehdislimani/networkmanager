package com.ineatconseil.networkmanager;

/**
 * Created by mehdi on 24/09/13.
 */
public interface NetworkTaskListener {
    public void onNetworkTaskCompleted(NetworkTask networkTask);
    public void onNetworkTaskConnectivityError(NetworkTask networkTask);
}
