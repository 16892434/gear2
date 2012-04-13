package org.gear.ioc;

public class ObjectLoadException extends Exception {

	private static final long serialVersionUID = 930600302242715910L;

	public ObjectLoadException(String message) {
		this(message, null);
	}
	
	public ObjectLoadException(String message, Throwable cause) {
		super(message, cause);
	}
}
