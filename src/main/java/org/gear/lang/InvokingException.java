package org.gear.lang;

@SuppressWarnings("serial")
public class InvokingException extends RuntimeException {
	
	public InvokingException(String format, Object...args) {
		super(String.format(format, args));
	}
	
	public InvokingException(String msg, Throwable cause) {
		super(String.format(msg, cause.getMessage()), cause);
	}

}
