package com.ineatconseil.networkmanager;


public interface NetworkTaskMethod {
	
	public NetworkRunnable getNetworkRunnable(); 
	public void setCurrentThread(Thread currentThread);
	public Thread getCurrentThread();
	public byte[] getResponse();
	public void setResponse(byte[] response);
	public void handleState(int state);
	public NetworkTaskListener getNetworkTaskListener();
	public void recycle();
}
