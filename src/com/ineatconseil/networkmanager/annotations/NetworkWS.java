package com.ineatconseil.networkmanager.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.ineatconseil.networkmanager.NetworkRequest.Method;

@Retention(RetentionPolicy.RUNTIME)
public @interface NetworkWS {

	String url();
	Method method() default Method.GET;
	boolean accepteAllCertificate() default false;
}
