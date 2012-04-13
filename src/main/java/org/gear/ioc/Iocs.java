package org.gear.ioc;

import java.util.Map;
import java.util.Map.Entry;

public abstract class Iocs {

	private static final String OBJFIELDS = "^(type|scope|singleton|fields|args|events)$";
	
	public static boolean isIocObject(Map<String, ?> map) {
		for(Entry<String, ?> en : map.entrySet()) {
			if(!en.getKey().matches(OBJFIELDS)) 
				return false;
		}
		return true;
	}
}
