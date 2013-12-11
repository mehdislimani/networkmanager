package com.ineatconseil.networkmanager;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;

public class NetworkRequest {

    public static final String CHARSET = "UTF-8";

	public enum Method {
		GET(HttpGet.METHOD_NAME), 
		POST(HttpPost.METHOD_NAME), 
		PUT(HttpPut.METHOD_NAME), 
		DELETE(HttpDelete.METHOD_NAME);

		private String mMethodName;

		Method(String methodName) {
			mMethodName = methodName;
		}

		public String getValue() {
			return mMethodName;
		}
	}

    private static final int DEFAULT_TRY_COUNT = 1;

    private long id;
	private Method mMethod = Method.GET;
	private String mUrl;
	private List<NameValuePair> mHeaders;
	private List<NameValuePair> mParams;
	private byte[] mBody;
	private boolean mAcceptAllCertificate;

	public NetworkRequest(String url) {
        id = System.currentTimeMillis();
		mUrl = url;
		initImmutableList();
	}

	public NetworkRequest(String url, Method method) {
        id = System.currentTimeMillis();
		mUrl = url;
		mMethod = method;
		initImmutableList();
	}
	
	public long getId() {
		return id;
	}

	private void initImmutableList() {
		mHeaders = new LinkedList<NameValuePair>();
		mParams = new LinkedList<NameValuePair>();
	}

	public Method getMethod() {
		return mMethod;
	}

	public void setMethod(Method method) {
		mMethod = method;
	}

	public String getUrl() {
		return mUrl.toString();
	}

	public void setUrl(String url) {
		mUrl = url;
	}
	
	public void replaceDynamicURLParams(String dynamicTag, String value){
		if(mUrl == null){
			return;
		}
		mUrl = mUrl.replace("#{"+dynamicTag+"}#", value);
	}

	public List<NameValuePair> getHeaders() {
		return mHeaders;
	}

	public void addHeaders(List<NameValuePair> headers) {
		mHeaders.addAll(headers);
	}

	public void addHeader(NameValuePair header) {
		mHeaders.add(header);
	}
	
	public void addHeader(String key, String value){
		addParam(new BasicNameValuePair(key, value));
	}
	
	public void addAuthorization(String username, String password){
		String encoded = Base64.encodeBytes(new String(username+":"+password).getBytes());
		NameValuePair authorization = new BasicNameValuePair("Authorization", "Basic "+encoded);
		addHeader(authorization);
	}

	public List<NameValuePair> getParams() {
		return mParams;
	}

	public void addParams(List<NameValuePair> params) {
		mParams.addAll(params);
	}

	public void addParam(NameValuePair param) {
		mParams.add(param);
	}
	
	public void addParam(String key, String value){
		addParam(new BasicNameValuePair(key, value));
	}

	public byte[] getBody() {
		return mBody;
	}

	public void setBody(byte[] body) {
		mBody = body;
	}
	
	public String getMethodType(){
		return mMethod.getValue();
	}
	
	public void setAcceptAllCertificate(boolean acceptAllCertificate) {
		mAcceptAllCertificate = acceptAllCertificate;
	}
	
	public boolean isAcceptAllCertificate() {
		return mAcceptAllCertificate;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof NetworkRequest)){
			return false;
		}
		
		return ((NetworkRequest)o).getId() == id;
	}

}
