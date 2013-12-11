package com.ineatconseil.networkmanager.exceptions;

public class AirplaneException extends Exception{

	private static final long serialVersionUID = -6345479500711581546L;

	public AirplaneException() {
		super();
	}

	public AirplaneException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public AirplaneException(String detailMessage) {
		super(detailMessage);
	}

	public AirplaneException(Throwable throwable) {
		super(throwable);
	}
	
	

}
