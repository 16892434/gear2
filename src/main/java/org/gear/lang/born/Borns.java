package org.gear.lang.born;

/**
 * 关于创建对象的一些帮助方法
 */
public abstract class Borns {

	/**
	 * 根据参数类型数组获取一个对象的构建信息
	 * 
	 * @param <T>
	 *            对象类型信息
	 * @param type
	 *            对象类型
	 * @param argTypes
	 *            构造参数类型数组
	 * @return 构建信息对象
	 */
	public static <T> BornContext<T> evalByArgTypes(Class<T> type, Class<?>... argTypes) {
		BornContext<T> re;
		if(null == argTypes || argTypes.length == 0) {
			re = evalWithoutArgs(type);
		} else {
			re = evalWithArgTypes(true, type, argTypes, null);
		}
		return re;
	}
	/**
	 * 根据参数类型数组获取一个对象的构建信息
	 * 
	 * @param <T>
	 *            对象类型信息
	 * @param type
	 *            对象类型
	 * @param args
	 *            构造参数数组
	 * @return 构建信息对象
	 */
	public static <T> BornContext<T> eval(Class<T> type, Object... args) {
		BornContext<T> re;
		if(null == args || args.length == 0) {
			re = evalWithoutArgs(type);
		} else {
			re = evalWithArgs(type, args);
		}
		return re;
	}
	
	/**
	 * 根据一个调用参数数组，获取一个对象的构建信息
	 * 
	 * @param <T>
	 *            对象类型信息
	 * @param type
	 *            对象类型
	 * @param args
	 *            参考构建参数
	 * @return 构建信息对象
	 */
	private static <T> BornContext<T> evalWithArgs(Class<T> type, Object... args) {
		// TODO wainting for implement Mirror.eval**
		return null;
	}
	
	/**
	 * 根据一个调用参数类型数组，获取一个对象的构建信息
	 * 
	 * @param <T>
	 *            对象类型信息
	 * @param accurate
	 *            是否需要精确匹配
	 * @param type
	 *            对象类型
	 * @param argTypes
	 *            参考参数类型数组
	 * @param dynaAry
	 *            参考参数类型信息是否是一个变参数组
	 * @return 构建信息对象
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static <T> BornContext<T> evalWithArgTypes(	boolean accurate,
														Class<T> type,
														Class<?>[] argTypes,
														Object dynaArg) {
		// TODO waiting for implement Mirror**
		return null;
	}
	
	private static boolean canBeCasted(Class<?>[] argTypes, Class<?>[] pts) {
		// TODO waiting for implement
		return false;
	}
	
	/**
	 * 为一个给定类，寻找一个不需要参数的构造方法
	 * 
	 * @param <T>
	 *            类
	 * @param type
	 *            类实例
	 * @return 构造信息
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static <T> BornContext<T> evalWithoutArgs(Class<T> type) {
		// TODO waiting for implement
		return null;
	}
	
}
