package org.gear.util;

/**
 * 带三个参数的通用回调接口
 * 
 * @param <T1>
 * @param <T2>
 * @param <T3>
 */
public interface Callback3<T1, T2, T3> {
	void invoke(T1 arg1, T2 arg2, T3 arg3);
}
