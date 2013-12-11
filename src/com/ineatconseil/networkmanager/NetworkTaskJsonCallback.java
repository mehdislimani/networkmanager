package com.ineatconseil.networkmanager;

import org.json.JSONObject;

public abstract class NetworkTaskJsonCallback extends NetworkTaskCallback{
	
	
    @Override
    public void onNetworkTaskResponse(NetworkTask networkTask) {
        try {
            String response = new String(networkTask.getResponse(), "utf-8");
            JSONObject json = new JSONObject(response);
            onNetworkTaskJsonResponse(networkTask, json);
        } catch (Exception e) {
            onNetworkTaskError(networkTask, e);
        }
    }

    public abstract void onNetworkTaskJsonResponse(NetworkTask networkTask, JSONObject json);

}
