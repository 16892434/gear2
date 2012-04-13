package org.gear.ioc;

import org.gear.ioc.meta.IocObject;

/**
 * 这个接口封装了对象注入逻辑。生成对象代理。
 * 
 * @author uncle.zy
 * 
 * @see org.gear.ioc.ObjectProxy
 */
public interface ObjectMaker {

	/**
	 * 根据 IocObject 制作一个对象代理，如果对象是 singleton，则保存在上下文环境中
	 */
	ObjectProxy make(IocMaking ing, IocObject obj);
}
