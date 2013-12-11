package com.ineatconseil.networkmanager;


public class NetworkTask implements NetworkTaskMethod {

	
	//private WeakReference<NetworkTaskListener> mListener;
	private int mResponseCode;
	private NetworkRequest mRequest;
	private NetworkTaskListener mListener;
	private NetworkRunnable mNetworkRunnable;
	
	private Thread mCurrentThread;
	private byte[] mResponse;
	
	
	private static NetworkManager sNetworkManager;
	
	public NetworkTask(){
		mNetworkRunnable = new NetworkRunnable(this);
	}
	
	public void build(NetworkManager networkManager, NetworkRequest request, NetworkTaskListener listener){
		sNetworkManager = networkManager;
		mRequest = request;
		mListener = listener;//new WeakReference<NetworkTask.NetworkTaskListener>(listener);
	}
	
	public void setResponseCode(int responseCode) {
		mResponseCode = responseCode;
	}
	
	public int getResponseCode() {
		return mResponseCode;
	}
	
	public static NetworkManager getNetworkManager() {
		return sNetworkManager;
	}
	
	public NetworkRunnable getNetworkRunnable() {
		return mNetworkRunnable;
	}
	
	public NetworkRequest getRequest() {
		return mRequest;
	}
	
	public void setCurrentThread(Thread currentThread) {
		synchronized(sNetworkManager){
			mCurrentThread = currentThread;
		}
	}
	
	public Thread getCurrentThread() {
		synchronized(sNetworkManager){
			return mCurrentThread;
		}
	}
	
	public byte[] getResponse() {
		return mResponse;
	}
	
	public void setResponse(byte[] response) {
		mResponse = response;
	}
	
	public void handleState(int state){
		sNetworkManager.handleState(this, state);
	}
	
	public NetworkTaskListener getNetworkTaskListener(){
		/*if( mListener != null){
			return mListener.get();
		}
		
		return null;*/
		return mListener;
	}
	
	public void recycle(){
		// on libere la r�f�rence
		if(mListener != null){
			//mListener.clear();
			mListener = null;
		}
		
		mRequest = null;
		mResponse = null;
	}
	
	public void cancel(){
		sNetworkManager.stopTask(this);
	}

}
