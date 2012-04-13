package org.gear.ioc.impl;

import org.gear.ioc.IocContext;
import org.gear.ioc.IocException;
import org.gear.ioc.IocExtend;
import org.gear.ioc.IocLoader;
import org.gear.ioc.ValueProxyMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GearIoc implements IocExtend {
	
	// TODO replace Logger to Log
	private static final Logger log = LoggerFactory.getLogger(GearIoc.class);
	private static final Object lock_get = new Object();
	private static final String DEF_SCOPE = "app";
	
	/**
	 * 读取配置文件的 Loader
	 */
	private IocLoader loader;

	@Override
	public <T> T get(Class<T> type, String name) throws IocException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T get(Class<T> type) throws IocException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean has(String name) throws IocException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void depose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> T get(Class<T> type, String name, IocContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IocContext getIocContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addValueProxyMaker(ValueProxyMaker vpm) {
		// TODO Auto-generated method stub
		
	}

}
