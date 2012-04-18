package org.gear.lang.inject;

import java.lang.reflect.Method;

import org.gear.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InjectBySetter implements Injecting {
	
	private static final Logger log = LoggerFactory.getLogger(InjectBySetter.class);
	
	private Method setter;
	private Class<?> valueType;
	
	public InjectBySetter(Method setter) {
		this.setter = setter;
		valueType = setter.getParameterTypes()[0];
	}

	@Override
	public void inject(Object obj, Object value) {
		Object v = null;
		try {
			// TODO waiting for implement Castors.me()
			// v = Castors.me().castTo(value, valueType);
			setter.invoke(obj, v);
		}catch(Exception e) {
			if(log.isInfoEnabled())
				log.info("Fail to value by setter", e);
			throw Lang.makeThrow("Fail to set '%s'[ %s ] by setter %s. '%s' because [%s]: %s",
								value,
								v,
								setter.getDeclaringClass().getName(),
								setter.getName(),
								Lang.unwrapThrow(e),
								Lang.unwrapThrow(e).getMessage());
		}
	}

}
