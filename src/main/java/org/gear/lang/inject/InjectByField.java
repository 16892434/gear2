package org.gear.lang.inject;

import java.lang.reflect.Field;

import org.gear.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InjectByField implements Injecting {

	private static final Logger log = LoggerFactory.getLogger(InjectByField.class);
	
	private Field field;
	
	public InjectByField(Field field) {
		this.field = field;
		this.field.setAccessible(true);
	}
	
	@Override
	public void inject(Object obj, Object value) {
		Object v = null;
		try {
			// TODO waiting for implement Castors.me().castTo(value, field.getType())
			// v = Castors.me().castTo(value, field.getType());
			field.set(obj, v);
		}catch(Exception e) {
			if(log.isInfoEnabled())
				log.info("Fail to value by field", e);
			throw Lang.makeThrow("Fail to set '%s'[ %s ] to field %s.'%s' because [%s]: %s",
								value,
								v,
								field.getDeclaringClass().getName(),
								field.getName(),
								Lang.unwrapThrow(e),
								Lang.unwrapThrow(e).getMessage());
		}
	}

}
