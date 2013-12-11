package com.ineatconseil.networkmanager;

import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Certificate {

	/*
	 * public static void accepteAllCertificate() { TrustManager[] trustAllCerts
	 * = new TrustManager[] { new X509TrustManager() {
	 * 
	 * public java.security.cert.X509Certificate[] getAcceptedIssuers() { return
	 * null; }
	 * 
	 * public void checkClientTrusted( java.security.cert.X509Certificate[]
	 * certs, String authType) { }
	 * 
	 * public void checkServerTrusted( java.security.cert.X509Certificate[]
	 * certs, String authType) { } } };
	 * 
	 * // Install the all-trusting trust manager try { SSLContext sc =
	 * SSLContext.getInstance("SSL"); sc.init(null, trustAllCerts, new
	 * java.security.SecureRandom());
	 * HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory()); }
	 * catch (Exception e) { e.printStackTrace(); }
	 * 
	 * // Set at true the HostnameVerifier HostnameVerifier hv = new
	 * HostnameVerifier() { public boolean verify(String urlHostName, SSLSession
	 * session) { // System.out.println("Warning: URL Host: "+urlHostName+" //
	 * vs. "+session.getPeerHost()); return true; } };
	 * 
	 * HttpsURLConnection.setDefaultHostnameVerifier(hv); }
	 */

	public static URL getSslURL(String url) throws Exception {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		// Create empty HostnameVerifier
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(hv);

		return new URL(url);
	}
}
