package com.ineatconseil.networkmanager;

import org.json.JSONObject;

import android.content.Context;

public class ClientManager {

	private static ClientManager sInstance;

	public static ClientManager getInstance() {
		if (sInstance == null) {
			sInstance = new ClientManager();
		}
		return sInstance;
	}

	public interface ClientManagerListener {
		public void response(String response);
	}

	public void test(Context context, final ClientManagerListener listener) {

		NetworkRequest request = new NetworkRequest("http://www.google.fr");
        NetworkManager.getInstance().start(request, new NetworkTaskJsonCallback(){
            @Override
            public void onNetworkTaskJsonResponse(NetworkTask networkTask, JSONObject json) {
                listener.response("ok");
            }

			@Override
			public void onNetworkTaskError(NetworkTask networkTask, Exception e) {
				listener.response("" + e.toString());
			}

        });
        
        NetworkManager.getInstance().start(request, new NetworkTaskStringCallback() {
			
			@Override
			public void onNetworkTaskError(NetworkTask networkTask, Exception e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onNetworkTaskStringResponse(NetworkTask networkTask,
					String response) {
				// TODO Auto-generated method stub
				
			}
		});
	}

}
