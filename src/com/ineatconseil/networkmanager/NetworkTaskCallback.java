package com.ineatconseil.networkmanager;

import com.ineatconseil.networkmanager.exceptions.AirplaneException;
import com.ineatconseil.networkmanager.exceptions.ConnectionErrorException;

public abstract class NetworkTaskCallback implements NetworkTaskListener {

	@Override
	public void onNetworkTaskCompleted(NetworkTask networkTask) {
		onNetworkTaskResponse(networkTask);
	}
	
	@Override
	public void onNetworkTaskConnectivityError(NetworkTask networkTask) {
		if(NetworkManager.getInstance().isAirplaneModeEnabled()){
			onNetworkTaskError(networkTask, new AirplaneException("Pas de connection Internet. Le mode avion est activé."));
		}else{
			onNetworkTaskError(networkTask, new ConnectionErrorException("Pas de connection Internet"));
		}
	}
	
	public abstract void onNetworkTaskResponse(NetworkTask networkTask);
	public abstract void onNetworkTaskError(NetworkTask networkTask, Exception e);

}
