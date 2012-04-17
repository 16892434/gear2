package org.gear.util;

/**
 * 带一个参数的通用回调接口
 * 
 * @param <T>
 */
public interface Callback<T> {
	void invoke(T obj);
}
