package com.ineatconseil.networkmanager;

public abstract class NetworkTaskStringCallback extends NetworkTaskCallback{

	@Override
	public void onNetworkTaskResponse(NetworkTask networkTask) {
		try{
			String response = new String(networkTask.getResponse(),"utf-8");
			onNetworkTaskStringResponse(networkTask, response);
		}catch(Exception e){
			onNetworkTaskError(networkTask,e);
		}
	}
	
	public abstract void onNetworkTaskStringResponse(NetworkTask networkTask, String response);
	

}
