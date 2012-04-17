package org.gear.lang;

@SuppressWarnings("serial")
public class FailToCastObjectException extends Exception {

	public FailToCastObjectException(String message) {
		super(message);
	}
	
	public FailToCastObjectException(String message, Throwable e) {
		super(message, e);
	}
}
