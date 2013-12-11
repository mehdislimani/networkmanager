package com.ineatconseil.networkmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.NameValuePair;

import android.util.Log;

public class NetworkRunnable implements Runnable {

	public static final int FAILED = -1;
	//public static final int STARTED = 0;
	public static final int COMPLETED = 1;

	final NetworkTask mNetworkTask;

	public NetworkRunnable(NetworkTask networkTask) {
		mNetworkTask = networkTask;
	}

	@Override
	public void run() {
		mNetworkTask.setCurrentThread(Thread.currentThread());
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		
		
		HttpURLConnection connection = null;
		byte[] response = null;
        InputStream responseInputStream = null;
		try {
			
			//Thread.sleep(2000);
			
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			NetworkRequest request = mNetworkTask.getRequest();
			if(request == null){
				throw new NullPointerException("Pas de requete");
			}

            if(request.getUrl() == null){
                throw new NullPointerException("L'url ne peut être vide");
            }

            StringBuffer urlBuffer = new StringBuffer(request.getUrl());

            // Si la requête est en GET alors nous devons construire l'URL avec
            // les parametres
            if(request.getMethod() == NetworkRequest.Method.GET){
                List<NameValuePair> params = request.getParams();
                if(params != null && params.size() > 0){
                    urlBuffer.append(String.format(Locale.getDefault(), "?%s", NetworkUtils.buildURLParamsEncoded(params, NetworkRequest.CHARSET)));
                }
            }



			//mNetworkTask.handleState(STARTED);
            URL url = null;
            String urlRequest = urlBuffer.toString();
            Log.d("URLRequest","URL = " + urlRequest);
            if (urlRequest.startsWith("https") && mNetworkTask.getRequest().isAcceptAllCertificate()){
            	try{
            		url = Certificate.getSslURL(urlRequest);
            	}catch(Exception e){
            		e.printStackTrace();
            	}
            }
            
            if(url == null){
            	url = new URL(urlRequest);
            }
			
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(request.getMethodType());

            // nous allons ajouter des headers si besoin y est
            List<NameValuePair> headersParams = request.getHeaders();
            if(headersParams != null && headersParams.size() > 0){
                for(int i = 0; i < headersParams.size(); i++){
                    NameValuePair header = headersParams.get(i);
                    String field = header.getName();
                    String value = header.getValue();
                    if(field == null || value == null){
                        continue;
                    }
                    connection.setRequestProperty(field, value);
                }
            }
            connection.setDoInput(true);
            connection.setConnectTimeout(1000 * 10);
            connection.setReadTimeout(1000 * 10);
            connection.setUseCaches(false);

            System.setProperty("http.keepAlive", "false");
            if( (request.getBody() != null) || (request.getMethod() != NetworkRequest.Method.GET && request.getParams() != null)){
                int totalBytesRead = 0;
                int bytesRead = 0;
                int contentLength = 0;

                byte[] body = request.getBody();
                List<NameValuePair> params = request.getParams();

                ByteArrayInputStream bais = null;
                if(body != null){
                    contentLength = body.length;
                    bais = new ByteArrayInputStream(body);
                }else if(params.size() > 0){
                    byte[] data = NetworkUtils.buildURLParamsEncoded(params, NetworkRequest.CHARSET).getBytes(NetworkRequest.CHARSET);
                    contentLength = data.length;
                    bais = new ByteArrayInputStream(data);
                }

                // Nous allons écrire dans le body de la connection selon les parametres body ou params
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(contentLength);

                
                OutputStream os = connection.getOutputStream();
                byte[] buffer = new byte[1024];
                while((bytesRead = bais.read(buffer)) != -1){
                    os.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    // Possibilité de dispatcher le pourcentage d'écriture
                    //totalBytesRead contentLength
                    
                    //float ratio = totalBytesRead / contentLength; 
                }
            }


			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

            int status = connection.getResponseCode();
            int contentLength = connection.getContentLength();

            
            mNetworkTask.setResponseCode(status);
            
            if(status < 300){
                responseInputStream = connection.getInputStream();
                response =  getBytes(responseInputStream, 1024 * 2);

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                mNetworkTask.setResponse(response);
                mNetworkTask.handleState(COMPLETED);
            }else{
                responseInputStream = connection.getErrorStream();
                response =  getBytes(responseInputStream, 1024 * 2);

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                
                String test = new String(response);
                mNetworkTask.setResponse(response);
                mNetworkTask.handleState(FAILED);
            }
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(connection != null){
				connection.disconnect();
			}
			
			if(responseInputStream != null){
				try {
                    responseInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(response == null){
				mNetworkTask.handleState(FAILED);
			}
			
			mNetworkTask.setCurrentThread(null);
			Thread.interrupted();
		}
	}
	
	
	public static byte[] getBytes(InputStream is, int bufferSize) throws IOException {

		int len;
		int size = bufferSize;
		byte[] buf;

		if (is instanceof ByteArrayInputStream) {
			size = is.available();
			buf = new byte[size];
			len = is.read(buf, 0, size);
		} else {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			buf = new byte[size];
			while ((len = is.read(buf, 0, size)) != -1)
				bos.write(buf, 0, len);
			buf = bos.toByteArray();
		}
		return buf;
	}



}
