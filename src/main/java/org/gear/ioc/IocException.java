package org.gear.ioc;

public class IocException extends RuntimeException {

	private static final long serialVersionUID = 7578207685543630712L;
	
	public IocException(Throwable cause, String fmt, Object... args) {
		super(String.format(fmt, args), cause);
	}

}
