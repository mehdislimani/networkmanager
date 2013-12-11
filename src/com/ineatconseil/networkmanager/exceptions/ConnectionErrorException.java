package com.ineatconseil.networkmanager.exceptions;

public class ConnectionErrorException extends Exception{

	private static final long serialVersionUID = 6555994278795264384L;

	public ConnectionErrorException() {
		super();
	}

	public ConnectionErrorException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ConnectionErrorException(String detailMessage) {
		super(detailMessage);
	}

	public ConnectionErrorException(Throwable throwable) {
		super(throwable);
	}
	
	

}
