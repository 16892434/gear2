package org.gear.ioc;

import java.util.List;

import org.gear.ioc.aop.MirrorFactory;
import org.gear.ioc.meta.IocValue;

public class IocMaking {

	private String objectName;
	
	private ObjectMaker objectMaker;
	
	private Ioc ioc;
	
	private IocContext context;
	
	private List<ValueProxyMaker> vpms;
	
	private MirrorFactory mirrors;
	
	public IocMaking(
			Ioc ioc,
			MirrorFactory mirrors,
			IocContext context,
			ObjectMaker objectMaker,
			List<ValueProxyMaker> vpms,
			String objectName
			) {
		this.ioc = ioc;
		this.mirrors = mirrors;
		this.context = context;
		this.objectMaker = objectMaker;
		this.vpms = vpms;
		this.objectName = objectName;
	}

	public String getObjectName() {
		return objectName;
	}

	public ObjectMaker getObjectMaker() {
		return objectMaker;
	}

	public Ioc getIoc() {
		return ioc;
	}

	public IocContext getContext() {
		return context;
	}

	public MirrorFactory getMirrors() {
		return mirrors;
	}
	
	public IocMaking clone(String objectName) {
		return new IocMaking(ioc, mirrors, context, objectMaker, vpms, objectName);
	}
	
	public ValueProxy makeValue(IocValue iv) {
		for(ValueProxyMaker vpm : vpms) {
			ValueProxy vp = vpm.make(this, iv);
			if(null != vp)
				return vp;
		}
		
		// TODO replace Lang.makeThrow...
		throw new RuntimeException(
				String.format(
						"Unkown value {%s:%s} for object [%s]", 
						iv.getType(), 
						iv.getValue(), 
						objectName)
						);
	}
}
