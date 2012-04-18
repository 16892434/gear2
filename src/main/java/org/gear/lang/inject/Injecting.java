package org.gear.lang.inject;

/**
 * 抽象注入接口
 * <p>
 * 封装了通过 setter 以及 field 两种方式设置值的区别
 * 
 */
public interface Injecting {

	void inject(Object obj, Object value);
}
