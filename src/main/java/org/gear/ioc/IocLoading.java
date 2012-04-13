package org.gear.ioc;

import static org.gear.ioc.Iocs.isIocObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gear.ioc.meta.IocField;
import org.gear.ioc.meta.IocObject;
import org.gear.ioc.meta.IocValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IocLoading {

	private static final Logger log = LoggerFactory.getLogger(IocLoading.class);
	
	private Set<String> suppportedTypes;
	
	public IocLoading(Set<String> supportedTypes) {
		this.suppportedTypes = supportedTypes;
	}
	
	private static ObjectLoadException E(Throwable e, String fmt, Object... args) {
		return new ObjectLoadException(String.format(fmt, args), e);
	}
	
	public IocObject map2iobj(Map<String, Object> map) throws ObjectLoadException {
		final IocObject iobj = new IocObject();
		if(!isIocObject(map)) {
			for(Entry<String, Object> en : map.entrySet()) {
				IocField ifld = new IocField();
				ifld.setName(en.getKey());
				ifld.setValue(object2value(en.getValue()));
				iobj.addField(ifld);
			}
			if(log.isWarnEnabled()) {
				// TODO replace iobj to Json.toJson(iobj)
				log.warn("Using *Declared* ioc-define (without type or events)!!! Pls use Standard Ioc-Define!!" +
						" Bean will define as:\n"+iobj);
			}
		} else {
			Object v = map.get("type");
			// type
			// TODO waiting to implement Lang & Strings
			/*
			try {
				String typeName = (String)v;
				if(!Strings.isBlank(typeName)) {
					iobj.setType(Lang.loadClass(typeName));
				}
			}catch(Exception e) {
				throw E(e, "Wrong type name: '%s'", v);
			}
			*/
		}
		return iobj;
	}
	
	@SuppressWarnings("unchecked")
	IocValue object2value(Object obj) {
		IocValue iv = new IocValue();
		// null
		if(null == obj) {
			iv.setType(null);
			return iv;
		} 
		// IocValue
		else if(obj instanceof IocValue) {
			return (IocValue)obj;
		}
		// map
		else if(obj instanceof Map<?, ?>) {
			Map<String, Object> map = (Map<String, Object>) obj;
			if(map.size() == 1) {
				Entry<String, ?> en = map.entrySet().iterator().next();
				String key = en.getKey();
				// support this type or not?
				if(suppportedTypes.contains(key)) {
					iv.setType(key);
					iv.setValue(en.getValue());
					return iv;
				}
			}
			// inner
			if(isIocObject(map)) {
				iv.setType(IocValue.TYPE_INNER);
				try {
					iv.setValue(map2iobj(map));
				}catch(ObjectLoadException e) {
					// TODO replace to Lang.makeThrow
					throw new RuntimeException(e);
				}
				
				return iv;
			}
			// normal map
			Map<String, IocValue> newmap = new HashMap<String, IocValue>();
			for(Entry<String, Object> en : map.entrySet()) {
				IocValue v = object2value(en.getValue());
				newmap.put(en.getKey(), v);
			}
			iv.setType(IocValue.TYPE_NORMAL);
			iv.setValue(newmap);
			return iv;
		}
		// array
		else if(obj.getClass().isArray()) {
			Object[] array = (Object[])obj;
			IocValue[] ivs = new IocValue[array.length];
			for(int i = 0; i < ivs.length; i++) {
				ivs[i] = object2value(ivs[i]);
			}
			iv.setType(IocValue.TYPE_NORMAL);
			iv.setValue(ivs);
			// TODO nutz bug ?
			return iv;
		}
		// collection
		else if(obj instanceof Collection<?>) {
			// TODO wait for implement Mirror
			/*
			try {
				Collection<IocValue> values = (Collection<IocValue>) Mirror.me(obj).born();
				Iterator<?> it = ((Collection<?>) obj).iterator();
				while(it.hasNext()) {
					Object o = it.next();
					IocValue v = object2value(o);
					values.add(v);
				}
				iv.setType(IocValue.TYPE_NORMAL);
				iv.setValue(values);
				return iv;
			}catch(Exception e) {
				// TODO replace to Lang.makeThrow...
				throw new RuntimeException(e);
			}
			*/
		}
		// normal
		iv.setType(IocValue.TYPE_NORMAL);
		iv.setValue(obj);
		return iv;
	}
}
