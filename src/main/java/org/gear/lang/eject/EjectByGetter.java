package org.gear.lang.eject;

import java.lang.reflect.Method;

import org.gear.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EjectByGetter implements Ejecting {

	private static final Logger log = LoggerFactory.getLogger(EjectByGetter.class);
	
	private Method getter;
	
	public EjectByGetter(Method getter) {
		this.getter = getter;
	}
	
	@Override
	public Object eject(Object obj) {
		try {
			return null == obj ? null : getter.invoke(obj);
		}catch(Exception e) {
			if(log.isInfoEnabled())
				log.info("Fail to value by getter", e);
			throw Lang.makeThrow("Fail to invoke getter %s.'%s()' because [%s]: %s",
								getter.getDeclaringClass().getName(),
								getter.getName(),
								Lang.unwrapThrow(e),
								Lang.unwrapThrow(e).getMessage());
		}
	}

}
