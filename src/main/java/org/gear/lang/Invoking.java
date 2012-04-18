package org.gear.lang;

import java.lang.reflect.Method;

/**
 * 函数调用方式
 * 
 */
public class Invoking {

	private static abstract class Invoker {
		
		protected Method method;
		
		public Invoker(Method method) {
			this.method = method;
		}
		
		abstract Object invoke(Object obj) throws Exception;
	}
	
	private static class DefaultInvoker extends Invoker {
		
		private Object[] args;
		
		public DefaultInvoker(Method method, Object[] args) {
			super(method);
			this.args = args;
		}
		
		@Override
		Object invoke(Object obj) throws Exception {
			return method.invoke(obj, args);
		}
	}
	
	private static class DynamicArgsInvoker extends Invoker {
		
		private Object args;
		
		public DynamicArgsInvoker(Method method, Object args) {
			super(method);
			this.args = args;
		}
		
		@Override
		Object invoke(Object obj) throws Exception {
			return method.invoke(obj, args);
		}
	}
	
	private static class NullArgInvoker extends Invoker {
		
		public NullArgInvoker(Method method) {
			super(method);
		}
		
		@Override
		Object invoke(Object obj) throws Exception {
			return method.invoke(obj);
		}
	}
	
	private String msg;
	private Invoker invoker;
	
	public Invoking(Class<?> type, String methodName, Object...args) {
		// TODO waiting for implement ...
	}
	
	public Object invoke(Object obj) {
		try {
			return invoker.invoke(obj);
		}catch(Throwable e) {
			throw new InvokingException(msg, Lang.unwrapThrow(e));
		}
	}
}
