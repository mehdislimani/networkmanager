package com.ineatconseil.networkmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.webkit.URLUtil;

import org.apache.http.NameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public class NetworkUtils {

	private static final int DEFAULT_MAX_THREAD = 3;
	
	public static boolean isAirplaneModeEnabled(Context context){
		ContentResolver contextResolver = context.getContentResolver();
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return Settings.System.getInt(contextResolver, Settings.Global.AIRPLANE_MODE_ON,0) != 0;
		}
		
		return Settings.System.getInt(contextResolver, Settings.System.AIRPLANE_MODE_ON,0) != 0;
	}
	
	public static boolean hasPermission(Context context, String permission){
		return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
	}

	public static int getMaxThread(NetworkInfo networkInfo) {
		if (networkInfo == null || networkInfo.isConnectedOrConnecting()) {
			return DEFAULT_MAX_THREAD;
		}

		switch (networkInfo.getType()) {
		case ConnectivityManager.TYPE_WIFI:
		case ConnectivityManager.TYPE_WIMAX:
		case ConnectivityManager.TYPE_ETHERNET:
			return 8;
		case ConnectivityManager.TYPE_MOBILE:
			switch (networkInfo.getSubtype()) {
			// R�seau 4G
			case TelephonyManager.NETWORK_TYPE_LTE: 
			case TelephonyManager.NETWORK_TYPE_HSPAP:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
				return 7;
			// R�seau 3G
			case TelephonyManager.NETWORK_TYPE_UMTS: 
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
				return 5;
			// R�seau 2G
			case TelephonyManager.NETWORK_TYPE_GPRS: 
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return 2;
			default:
				return DEFAULT_MAX_THREAD;
			}
		default:
			return DEFAULT_MAX_THREAD;
		}
	}

    public static String buildURLParamsEncoded(List<NameValuePair> params, String charset){
        if(params == null || params.size() == 0){
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < params.size(); i++){
            NameValuePair paramPair = params.get(i);
            String key = paramPair.getName();
            String value = paramPair.getValue();
            if(value == null){
                value = new String();
            }

            try{
                buffer.append(String.format(Locale.getDefault(), "%s=%s&", key, URLEncoder.encode(value,charset)));
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }
        String paramsLine= buffer.toString();
        if(paramsLine.length() > 0){
            return paramsLine.substring(0, paramsLine.length() - 1);
        }

        return "";
    }
    
}
