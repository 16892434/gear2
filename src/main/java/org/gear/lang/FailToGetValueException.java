package org.gear.lang;

@SuppressWarnings("serial")
public class FailToGetValueException extends RuntimeException {
	
	public FailToGetValueException(String message, Throwable e) {
		super(message, e);
	}

}
