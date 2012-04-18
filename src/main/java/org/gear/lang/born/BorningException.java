package org.gear.lang.born;

import org.gear.lang.Lang;

@SuppressWarnings("serial")
public class BorningException extends RuntimeException {
	
	public BorningException(Class<?> type, Object[] args) {
		this(new RuntimeException("Don't know how to born it!"), type, args);
	}
	
	public BorningException(Throwable e, Class<?> type, Object[] args) {
		super(makeMessage(e, type, args), e);
	}
	
	private static String makeMessage(Throwable e, Class<?> type, Object[] args) {
		StringBuilder sb = new StringBuilder();
		String name = null == type ? "unknow" : type.getName();
		sb.append("Fail to born '").append(name).append("\'");
		if(null != args && args.length > 0) {
			sb.append("\n by args: [");
			for(Object argType : args) {
				sb.append("\n @(").append(argType).append(')');
			}
			sb.append("]");
		}
		if(null != e)
			sb.append(" because:\n").append(getExceptionMessage(e));
		return null;
	}

	private static Object getExceptionMessage(Throwable e) {
		return Lang.unwrapThrow(e).getMessage();
	}

}
