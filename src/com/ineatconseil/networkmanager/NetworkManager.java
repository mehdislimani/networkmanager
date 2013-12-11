package com.ineatconseil.networkmanager;

import static android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED;

import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ineatconseil.networkmanager.annotations.BodyWS;
import com.ineatconseil.networkmanager.annotations.DynamicURLParamWS;
import com.ineatconseil.networkmanager.annotations.HeaderWS;
import com.ineatconseil.networkmanager.annotations.NetworkWS;
import com.ineatconseil.networkmanager.annotations.ParamWS;

public class NetworkManager {

	private static volatile NetworkManager sInstance = null;

	public static synchronized void build(Application application){
		if(sInstance == null){
			sInstance = new NetworkManager(application);
		}
	}
	
	public static NetworkManager createInstance(Context context){
		return new NetworkManager(context);
	}
	
	private final ThreadPoolExecutor mDownloadThreadPool;

	private static final int KEEP_ALIVE_TIME = 1;
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
	private static final int CORE_POOL_SIZE = 8;
	private static final int MAXIMUM_POOL_SIZE = 8;

	private final BlockingQueue<Runnable> mDownloadWorkQueue;
	private final Queue<NetworkTask> mNetworkTaskQueue;
	private Handler mHandler;
	private boolean mAirplaneModeEnabled;
	private NetworkBroadcastReceiver mNetworkBroadcastReceiver;
	
	public static synchronized NetworkManager getInstance() {
		return sInstance;
	}
	
	public static NetworkManager createBlockingNetworkManager(Context context){
		return new NetworkManager(context, 1);
	}

	public void networkStateChanged(NetworkInfo networkInfo) {
		int threadCount = NetworkUtils.getMaxThread(networkInfo);
		mDownloadThreadPool.setCorePoolSize(threadCount);
		mDownloadThreadPool.setMaximumPoolSize(threadCount);
	}
	
	public void setAirplaneModeEnabled(boolean airplaneModeEnabled) {
		mAirplaneModeEnabled = airplaneModeEnabled;
	}
	
	public boolean isAirplaneModeEnabled() {
		return mAirplaneModeEnabled;
	}

	private NetworkManager(Context context) {
		/*mNetworkTaskQueue = new LinkedBlockingQueue<NetworkTask>();
		mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();
		mDownloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE,
				MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT,
				mDownloadWorkQueue);

		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				NetworkTask networkTask = (NetworkTask) msg.obj;
				NetworkTaskListener listener = networkTask
						.getNetworkTaskListener();

				// TODO gestion erreur
				switch (msg.what) {
				case NetworkRunnable.FAILED:
				//case NetworkRunnable.STARTED:
				case NetworkRunnable.COMPLETED:
					if (listener != null) {
						listener.onNetworkTaskCompleted(networkTask);
					}

					recycleTask(networkTask);
					break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
		};
		mAirplaneModeEnabled = NetworkUtils.isAirplaneModeEnabled(context);
		mNetworkBroadcastReceiver = new NetworkBroadcastReceiver(context);
		mNetworkBroadcastReceiver.register();*/
		this(context, MAXIMUM_POOL_SIZE);
	}
	
	private NetworkManager(Context context, int size) {
		mNetworkTaskQueue = new LinkedBlockingQueue<NetworkTask>();
		mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();
		mDownloadThreadPool = new ThreadPoolExecutor(size,
				size, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT,
				mDownloadWorkQueue);

		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				NetworkTask networkTask = (NetworkTask) msg.obj;
				NetworkTaskListener listener = networkTask
						.getNetworkTaskListener();

				// TODO gestion erreur
				switch (msg.what) {
				case NetworkRunnable.FAILED:
					if (listener != null) {
						listener.onNetworkTaskConnectivityError(networkTask);
					}

					recycleTask(networkTask);
					break;
				//case NetworkRunnable.STARTED:
				case NetworkRunnable.COMPLETED:
					if (listener != null) {
						listener.onNetworkTaskCompleted(networkTask);
					}

					recycleTask(networkTask);
					break;
				default:
					super.handleMessage(msg);
					break;
				}
			}
		};
		mAirplaneModeEnabled = NetworkUtils.isAirplaneModeEnabled(context);
		mNetworkBroadcastReceiver = new NetworkBroadcastReceiver(context);
		mNetworkBroadcastReceiver.register();
	}
	
	public void destroy(){
		mDownloadThreadPool.shutdown();
		mNetworkBroadcastReceiver.unregister();
	}

	public void handleState(NetworkTask networkTask, int state) {
		// Notification sur l'état de la requete
		Message msg = mHandler.obtainMessage(state, networkTask);
		msg.obj = networkTask;
		msg.sendToTarget();
	}
	

	
	public NetworkTask start(NetworkTaskListener listener){
		
		// Construction de la basic request
		Class<?> classInstance = listener.getClass();
		NetworkWS networkWS = classInstance.getAnnotation(NetworkWS.class);
		if(networkWS == null){
			return null;
		}
		NetworkRequest request = new NetworkRequest(networkWS.url(), networkWS.method());
		request.setAcceptAllCertificate(networkWS.accepteAllCertificate());
		// Récupération des éventuelles paramètres
		for(Field field : classInstance.getDeclaredFields()){
			field.setAccessible(true);
			
			// Dynamic params url
			DynamicURLParamWS dynamicURLParamWS = field.getAnnotation(DynamicURLParamWS.class);
			if(dynamicURLParamWS != null){
				String key = dynamicURLParamWS.key();
				String value = new String();
				try{
					value = field.get(listener).toString();
				}catch(Exception e){
					value = null;
				}
				
				if(key != null && value != null){
					request.replaceDynamicURLParams(key, value);
				}
			}
			
			BodyWS bodyWS = field.getAnnotation(BodyWS.class);
			if(bodyWS != null){
				String value = new String();
				try {
					value = field.get(listener).toString();
				} catch (Exception e) {
					value = null;
				}
				
				if(value != null){
					request.setBody(value.getBytes());
				}
			}
			
			// Paramétres
			ParamWS paramWS = field.getAnnotation(ParamWS.class);
			if(paramWS != null){
				String key = paramWS.key();
				String value = new String();
				try {
					value = field.get(listener).toString();
				} catch (Exception e) {
					value = null;
				}
				
				if(key != null && value != null){
					request.addParam(key, value);
				}
				
			}
			
			
			// Header dans la construction 
			HeaderWS headerWS = field.getAnnotation(HeaderWS.class);
			if(headerWS != null){
				String key = headerWS.key();
				String value = new String();
				try {
					value = field.get(listener).toString();
				} catch (Exception e) {
					value = null;
				}
				
				if(key != null && value != null){
					request.addHeader(key, value);
				}
			}
			
	
		}
		
		return start(request, listener);
	}

	public NetworkTask start(NetworkRequest request,
			NetworkTaskListener listener) {
		NetworkTask networkTask = sInstance.mNetworkTaskQueue.poll();
		
		// La queue est vide alors nous créons une tache
		if (networkTask == null) {
			networkTask = new NetworkTask();
		}

		// Initialisation de la tache
		// le manager pour dispatcher le message
		// l'objet requete pour construire la requete http
		// et la listener pour notifier de la reponse
		networkTask.build(NetworkManager.sInstance, request, listener);
		if(isAirplaneModeEnabled()){
			listener.onNetworkTaskConnectivityError(networkTask);
		}else{
			sInstance.mDownloadThreadPool.execute(networkTask.getNetworkRunnable());
		}
		return networkTask;
	}
	
	public NetworkTask start(String url, NetworkTaskListener listener) {
		NetworkRequest request = new NetworkRequest(url);
		return start(request, listener);
	}

	private void recycleTask(NetworkTask networkTask) {
		// destruction des objets fortement dépendant afin de libérer la mémoire
		networkTask.recycle();
		// insertion de l'objet dans la queue afin de pouvoir le réexploiter
		// plus tard
		sInstance.mNetworkTaskQueue.offer(networkTask);
	}

	public static void cancelAll() {
		// Transformation de la queue en tableau
		NetworkTask[] networkTasks = new NetworkTask[sInstance.mDownloadWorkQueue
				.size()];
		sInstance.mDownloadWorkQueue.toArray(networkTasks);
		int length = networkTasks.length;
		synchronized (sInstance) {
			// si un thread est enregistré alors il est arrété.
			// la runnable écoute après chaque etape si le thread is encore
			// disponible
			for (int i = 0; i < length; i++) {
				NetworkTask networkTask = networkTasks[i];
				Thread thread = networkTask.getCurrentThread();
				if (thread != null) {
					thread.interrupt();
				}
			}
		}
	}

	public void stopTask(NetworkTask networkTask) {
		if (networkTask != null) {
			synchronized (sInstance) {
				// si un thread est enregistré alors il est arrété.
				// la runnable écoute après chaque etape si le thread is encore
				// disponible
				Thread thread = networkTask.getCurrentThread();
				if (thread != null) {
					thread.interrupt();
				}
			}

			sInstance.mDownloadThreadPool.remove(networkTask
					.getNetworkRunnable());
		}
	}

	private class NetworkBroadcastReceiver extends BroadcastReceiver {
		private final ConnectivityManager mConnectivityManager;
		private final Context mContext;

		public NetworkBroadcastReceiver(Context context) {
			mContext = context;
			mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		}

		public void register() {
			boolean hasPermission = NetworkUtils.hasPermission(mContext,Manifest.permission.ACCESS_NETWORK_STATE);
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			if(hasPermission){
				intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			}
			try{
				mContext.registerReceiver(this, intentFilter);
			}catch(IllegalArgumentException e){
				e.printStackTrace();
			}
		}
		
		public void unregister(){
			mContext.unregisterReceiver(this);
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
	
			if(ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())){
				setAirplaneModeEnabled(NetworkUtils.isAirplaneModeEnabled(context));
			}else{
				networkStateChanged(mConnectivityManager.getActiveNetworkInfo());
			}
		}

	}

}
