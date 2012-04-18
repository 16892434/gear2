package org.gear.lang.eject;

import java.lang.reflect.Field;

import org.gear.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EjectByField implements Ejecting {

	private static final Logger log = LoggerFactory.getLogger(EjectByField.class);
	
	private Field field;
	
	public EjectByField(Field field) {
		this.field = field;
		this.field.setAccessible(true);
	}
	
	@Override
	public Object eject(Object obj) {
		try {
			return null == obj ? null : field.get(obj);
		}catch(Exception e) {
			if(log.isInfoEnabled())
				log.info("Fail to value by getter", e);
			throw Lang.makeThrow("Fail to get field %s.'%s' because [%s]: %s",
								field.getDeclaringClass().getName(),
								field.getName(),
								Lang.unwrapThrow(e),
								Lang.unwrapThrow(e).getMessage());
		}
	}

}
