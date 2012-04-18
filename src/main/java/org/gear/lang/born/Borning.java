package org.gear.lang.born;

/**
 * 对象抽象创建方式
 * 
 * @param <T>
 */
public interface Borning<T> {
	T born(Object[] args);
}
