package org.gear.castor;

public class FailToCastObjectException extends RuntimeException {

	private static final long serialVersionUID = 7483399854923975725L;

	public FailToCastObjectException(String message) {
		super(message);
	}
	
	public FailToCastObjectException(String message, Throwable cause) {
		super(message, cause);
	}
}
