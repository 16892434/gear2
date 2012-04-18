package org.gear.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gear.castor.FailToCastObjectException;
import org.gear.lang.born.BornContext;
import org.gear.lang.born.Borning;
import org.gear.lang.born.BorningException;
import org.gear.lang.born.Borns;
import org.gear.lang.eject.EjectByField;
import org.gear.lang.eject.EjectByGetter;
import org.gear.lang.eject.Ejecting;
import org.gear.lang.inject.InjectByField;
import org.gear.lang.inject.InjectBySetter;
import org.gear.lang.inject.Injecting;
import org.gear.util.Callback;
import org.gear.util.Callback3;
import org.gear.util.Strings;

public class Mirror<T> {

	private static class DefaultTypeExtractor implements TypeExtractor {

		@Override
		public Class<?>[] extract(Mirror<?> mirror) {
			Class<?> theType = mirror.getType();
			List<Class<?>> re = new ArrayList<Class<?>>(5);
			
			// 原生类型，增加其外覆类
			if(theType.isPrimitive()) {
				// TODO waiting for implement Mirror<T>
			}
			
			return null;
		}
		
	}
	
	private static final DefaultTypeExtractor defaultTypeExtractor = new DefaultTypeExtractor();
	
	/**
	 * 包裹一个类
	 * 
	 * @param classOfT
	 *            类
	 * @return Mirror
	 */
	public static <T> Mirror<T> me(Class<T> classOfT) {
		return null == classOfT ? null  
								: new Mirror<T>(classOfT).setTypeExtractor(defaultTypeExtractor);
	}
	
	/**
	 * 生成一个对象的 Mirror
	 * 
	 * @param obj
	 *            对象。
	 * @return Mirror， 如果 对象 null，则返回 null
	 */
	@SuppressWarnings("unchecked")
	public static <T> Mirror<T> me(T obj) {
		return null == obj ? null : (Mirror<T>)me(obj.getClass());
	}
	
	/**
	 * 包裹一个类，并设置自定义的类型提炼逻辑
	 * 
	 * @param classOfT
	 * @param typeExtractor
	 * @return Mirror
	 * @see org.gear.lang.TypeExtractor
	 */
	public static <T> Mirror<T> me(Class<T> classOfT, TypeExtractor typeExtractor) {
		return null == classOfT ? null 
								: new Mirror<T>(classOfT).setTypeExtractor(
										typeExtractor == null ? defaultTypeExtractor : typeExtractor);
	}
	
	/**
	 * 根据Type生成Mirror, 如果type是 {@link ParameterizedType} 类型的对象<br>
	 * 可以使用 getGenericsTypes() 方法取得它的泛型数组
	 * 
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Mirror<T> me(Type type) {
		if(null == type) {
			return null;
		}
		Mirror<T> mir = (Mirror<T>) Mirror.me(Lang.getTypeClass(type));
		mir.type = type;
		return mir;
	}
	
	private Class<T> klass;
	private Type type;
	private TypeExtractor typeExtractor;
	
	private Mirror(Class<T> classOfT) {
		this.klass = classOfT;
	}
	
	/**
	 * 设置自己的类型提炼逻辑
	 * 
	 * @param typeExtractor
	 * @return Mirror
	 * @see org.gear.lang.TypeExtractor
	 */
	public Mirror<T> setTypeExtractor(TypeExtractor typeExtractor) {
		this.typeExtractor = typeExtractor;
		return this;
	}
	
	public Class<T> getType() {
		return klass;
	}
	
	/**
	 * @return 对象提炼类型数组。从对象自身的类型到 Object，中间的继承关系中最有特点的几个类型
	 */
	public Class<?>[] extractTypes() {
		return typeExtractor.extract(this);
	}
	
	/**
	 * 根据名称获取一个 Getter。
	 * <p>
	 * 比如，你想获取 abc 的 getter ，那么优先查找 getAbc()，如果没有则查找isAbc()，最后才是查找 abc()。
	 * 
	 * @param fieldName
	 * @return 方法
	 * @throws NoSuchMethodException
	 *             没有找到 Getter
	 */
	public Method getGetter(String fieldName) throws NoSuchMethodException {
		String fn = Strings.capitalize(fieldName);
		String _get = "get" + fn;
		String _is = "is" + fn;
		for(Method method : klass.getMethods()) {
			if(method.getParameterTypes().length != 0)
				continue;
			if(_get.equals(method.getName()))
				return method;
			if(_is.equals(method.getName())) {
				if(!Mirror.me(method.getReturnType()).isBoolean())
					throw new NoSuchMethodException();
				return method;
			}
			if(fieldName.equals(method.getName()))
				return method;
		}
		throw Lang.makeThrow(NoSuchMethodException.class,
							"Fail to find getter for [%s] -> [%s]",
							klass.getName(),
							fieldName);
	}
	
	/**
	 * 根据给定的一个方法，判断其是 Getter 还是 Setter
	 * <p>
	 * 对于回调会接受三个参数
	 * 
	 * <pre>
	 * callback(虚字段名, getter, setter)
	 * </pre>
	 * 
	 * 回调都会给一个参数，表示这个方法对应的虚拟字段名。所谓"虚拟字段"，就比如
	 * <ul>
	 * <li>如果是 setAbc : 那么就是 "abc"
	 * <li>如果是 getAbc : 那么就是 "abc"
	 * <li>如果是 isAbc : 那么就是 "abc"
	 * </ul>
	 * 而 getter 或者 setter 参数如果为 null，则表示本函数未发现对应的 getter|setter
	 * 
	 * @param method
	 *            方法对象
	 * @param callback
	 *            回调, 如果为 null，则无视
	 * @param whenError
	 *            如果本方法即不是 Getter 也不是 Setter 的回调, 如果为 null，则无视
	 */
	public static void evalGetterSetter(Method method,
										Callback3<String, Method, Method> callback,
										Callback<Method> whenError) {
		String name = method.getName();
		Method getter = null;
		Method setter = null;
		
		// 是getter
		if(name.startsWith("get") && method.getParameterTypes().length == 0) {
			name = Strings.lowerFirst(name.substring(4));
			getter = method;
			// 寻找setter
			try {
				setter = method.getDeclaringClass().getMethod("set" + Strings.capitalize(name),
																method.getReturnType());
			}catch(Exception e) {}
		}
		// 布尔的getter
		else if(name.startsWith("is")
				&& Mirror.me(method.getReturnType().getClass()).isBoolean()
				&& method.getParameterTypes().length == 0) {
			name = Strings.lowerFirst(name.substring(3));
			getter = method;
			// 寻找setter
			try {
				setter = method.getDeclaringClass().getMethod("set" + Strings.capitalize(name), 
																method.getReturnType());
			}catch(Exception e) {}
		}
		// 是setter
		else if(name.startsWith("set") && method.getParameterTypes().length == 1) {
			name = Strings.lowerFirst(name.substring(4));
			setter = method;
			// 寻找getter
			try {
				getter = method.getDeclaringClass().getMethod("get" + Strings.capitalize(name));
			}catch(Exception e) {}
		}
		// 既不是getter也不是setter
		else {
			if(null != whenError)
				whenError.invoke(method);
			return;
		}
		// 最后回调
		if(null != callback)
			callback.invoke(name, getter, setter);
	}
	
	/**
	 * 根据给定的一个方法，判断其是 Getter 还是 Setter，根据情况不同，调用不同的回调。
	 * 
	 * @param method
	 *            方法对象
	 * @param errmsgFormat
	 *            如果本方法即不是 Getter 也不是 Setter 的回调, 则根据这个消息模板抛出一个运行时异常。 这个字符串格式是个
	 *            Java 的字符串模板，接受两个参数，第一个是方法名，第二个是所在类名
	 * @param callback
	 *            回调, 如果为 null，则无视
	 */
	public static void evalGetterSetter(final Method method,
										final String errmsgFormat,
										Callback3<String, Method, Method> callback) {
		evalGetterSetter(method, callback, new Callback<Method>() {
			public void invoke(Method method) {
				throw Lang.makeThrow(errmsgFormat, method.getName(), method.getDeclaringClass().getName());
			}
		});
	}
	
	/**
	 * 根据字段获取一个 Getter。
	 * <p>
	 * 比如，你想获取 abc 的 getter ，那么优先查找 getAbc()，如果 没有，则查找 abc()。
	 * 
	 * @param field
	 * @return 方法
	 * @throws NoSuchMethodException
	 *             没有找到 Getter
	 */
	public Method getGetter(Field field) throws NoSuchMethodException {
		return getGetter(field.getName());
	}
	
	/**
	 * 根据一个字段获取 Setter
	 * <p>
	 * 比如，你想获取 abc 的 setter ，那么优先查找 setAbc(T abc)，如果 没有，则查找 abc(T abc)。
	 * 
	 * @param field
	 *            字段
	 * @return 方法
	 * @throws NoSuchMethodException
	 *             没找到 Setter
	 */
	public Method getSetter(Field field) throws NoSuchMethodException {
		return getSetter(field.getName(), field.getType());
	}
	
	/**
	 * 根据一个字段名和字段类型获取 Setter
	 * 
	 * @param fieldName
	 *            字段名
	 * @param paramType
	 *            字段类型
	 * @return 方法
	 * @throws NoSuchMethodException
	 *             没找到 Setter
	 */
	public Method getSetter(String fieldName, Class<?> paramType) throws NoSuchMethodException {
		// TODO need to read
		try {
			String setterName = "set" + Strings.capitalize(fieldName);
			try {
				return klass.getMethod(setterName, paramType);
			}catch(Throwable e) {
				try {
					return klass.getMethod(fieldName, paramType);
				}catch(Throwable tw) {
					Mirror<?> type = Mirror.me(paramType);
					for(Method method : klass.getMethods()) {
						if(method.getParameterTypes().length == 1) {
							if(method.getName().equals(setterName)
									|| method.getName().equals(fieldName)) {
								if(null == paramType
										|| type.canCastToDirectly(method.getParameterTypes()[0]))
									return method;
							}
						}
					}
					// 可能是包装类
					if(!paramType.isPrimitive()) {
						Class<?> p = unWrapper();
						if(null != p)
							return getSetter(fieldName, p);
					}
					throw new RuntimeException();
				}
			}
		}catch(Throwable e) {
			throw Lang.makeThrow(NoSuchMethodException.class,
								"Fail to find setter for [%s] -> [%s]",
								klass.getName(),
								fieldName,
								paramType.getName());
		}
	}
	
	/**
	 * 根据一个字段名，获取一组有可能成为 Setter 函数
	 * 
	 * @param fieldName
	 * @return 函数数组
	 */
	public Method[] findSetters(String fieldName) {
		String mName = "set" + Strings.capitalize(fieldName);
		List<Method> ms = new ArrayList<Method>();
		for(Method m : this.klass.getMethods()) {
			if(!Modifier.isStatic(m.getModifiers())
					&& m.getParameterTypes().length == 1
					&& m.getName().equals(mName)) {
				ms.add(m);
			}
		}
		return ms.toArray(new Method[ms.size()]);
	}
	
	/**
	 * 获取一个字段。这个字段可以是当前类型或者其父类的私有字段。
	 * 
	 * @param name
	 *            字段名
	 * @return 字段
	 * @throws NoSuchFieldException
	 */
	public Field getField(String name) throws NoSuchFieldException {
		Class<?> cc = klass;
		while(null != cc && cc != Object.class) {
			try {
				return cc.getDeclaredField(name);
			}catch(NoSuchFieldException e) {
				cc = cc.getSuperclass();
			}
		}
		throw new NoSuchFieldException(String.format("Can NOT find field [%s] in class [%s] and it's parents classes",
													name,
													klass.getName()));
	}
	
	/**
	 * 获取一个字段。这个字段必须声明特殊的注解，第一遇到的对象会被返回
	 * 
	 * @param ann
	 *            注解
	 * @return 字段
	 * @throws NoSuchFieldException
	 */
	public <AT extends Annotation> Field getField(Class<AT> ann) throws NoSuchFieldException {
		for(Field field : this.getFields()) {
			if(field.isAnnotationPresent(ann))
				return field;
		}
		throw new NoSuchFieldException(String.format("Can NOT find field [@%s] in class [%s] and it's parents classes",
													ann.getName(),
													klass.getName()));
	}
	
	/**
	 * 获取一组声明了特殊注解的字段
	 * 
	 * @param ann
	 *            注解类型
	 * @return 字段数组
	 */
	public <AT extends Annotation> Field[] getFields(Class<AT> ann) {
		List<Field> fields = new LinkedList<Field>();
		for(Field f : this.getFields()) {
			if(f.isAnnotationPresent(ann))
				fields.add(f);
		}
		return fields.toArray(new Field[fields.size()]);
	}
	
	/**
	 * 获得当前类以及所有父类的所有的属性，包括私有属性。 <br>
	 * 但是父类不包括 Object 类，并且，如果子类的属性如果与父类重名，将会将其覆盖
	 * 
	 * @return 属性列表
	 */
	public Field[] getFields() {
		return _getFields(true, false, true, true);
	}
	
	/**
	 * 获得所有的静态变量属性
	 * 
	 * @param noFinal
	 *            是否包括 final 修饰符的字段
	 * 
	 * @return 字段列表
	 */
	public Field[] getStaticField(boolean noFinal) {
		return _getFields(false, true, noFinal, true);
	}
	
	private Field[] _getFields(boolean noStatic, boolean noMember, boolean noFinal, boolean noInner) {
		Class<?> cc = klass;
		Map<String, Field> map = new LinkedHashMap<String, Field>();
		while(null != cc && cc != Object.class) {
			Field[] fs = cc.getDeclaredFields();
			for(int i = 0; i < fs.length; i++) {
				Field f = fs[i];
				int m = f.getModifiers();
				if(noStatic && Modifier.isStatic(m))
					continue;
				if(noFinal && Modifier.isFinal(m))
					continue;
				if(noInner && f.getName().startsWith("this$"))
					continue;
				if(noMember && !Modifier.isStatic(m))
					continue;
				if(map.containsKey(fs[i].getName()))
					continue;
				
				map.put(fs[i].getName(), fs[i]);
			}
			cc = cc.getSuperclass();
		}
		return map.values().toArray(new Field[map.size()]);
	}
	
	/**
	 * 向父类递归查找某一个运行时注解
	 * 
	 * @param <A>
	 *            注解类型参数
	 * @param annType
	 *            注解类型
	 * @return 注解
	 */
	public <A extends Annotation> A getAnnotation(Class<A> annType) {
		Class<?> cc = klass;
		A ann;
		do {
			ann = cc.getAnnotation(annType);
			cc = cc.getSuperclass();
		}while(null == ann && cc != Object.class);
		return ann;
	}
	
	/**
	 * 取得当前类型的泛型数组
	 * 
	 * @return
	 */
	public Type[] getGenericsTypes() {
		if(type instanceof ParameterizedType) {
			return Lang.getGenericsTypes(type);
		}
		return null;
	}
	
	/**
	 * 取得当前类型的指定泛型
	 * 
	 * @param index
	 * @return
	 */
	public Type getGenericsType(int index) {
		Type[] ts = getGenericsTypes();
		return ts == null ? null : (ts.length < index ? null : ts[index]);
	}
	
	/**
	 * 获取本类型所有的方法，包括私有方法。不包括 Object 的方法
	 */
	public Method[] getMethods() {
		Class<?> cc = klass;
		List<Method> list = new LinkedList<Method>();
		while(null != cc && cc != Object.class) {
			Method[] ms = cc.getDeclaredMethods();
			for(int i = 0; i < ms.length; i++) {
				list.add(ms[i]);
			}
			cc = cc.getSuperclass();
		}
		return list.toArray(new Method[list.size()]);
	}
	
	/**
	 * 获取当前对象，所有的方法，包括私有方法。递归查找至自己某一个父类为止 。
	 * <p>
	 * 并且这个按照名称，消除重复的方法。子类方法优先
	 * 
	 * @param top
	 *            截至的父类
	 * @return 方法数组
	 */
	public Method[] getAllDeclaredMethods(Class<?> top) {
		Class<?> cc = klass;
		Map<String, Method> map = new LinkedHashMap<String, Method>();
		while(null != cc && cc != Object.class) {
			Method[] fs = cc.getDeclaredMethods();
			for(int i = 0; i < fs.length; i++) {
				String key = fs[i].getName() + Mirror.getParamDescriptor(fs[i].getParameterTypes());
				if(!map.containsKey(key))
					map.put(key, fs[i]);
			}
			cc = cc.getSuperclass() == top ? null : cc.getSuperclass();
		}
		return map.values().toArray(new Method[map.size()]);
	}
	
	/**
	 * @param parameterTypes
	 *            函数的参数类型数组
	 * @return 参数的描述符
	 */
	public static String getParamDescriptor(Class<?>[] parameterTypes) {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for(Class<?> pt : parameterTypes) {
			sb.append(getTypeDescriptor(pt));
		}
		sb.append(')');
		return sb.toString();
	}
	
	/**
	 * @param klass
	 *            类型
	 * @return 获得一个类型的描述符
	 */
	public static String getTypeDescriptor(Class<?> klass) {
		if(klass.isPrimitive()) {
			if(klass == void.class)
				return "V";
			else if(klass == int.class)
				return "I";
			else if(klass == long.class)
				return "J";
			else if(klass == byte.class)
				return "B";
			else if(klass == short.class)
				return "S";
			else if(klass == float.class)
				return "F";
			else if(klass == double.class)
				return "D";
			else if(klass == char.class)
				return "C";
			else 
				return "Z";
		}
		StringBuilder sb = new StringBuilder();
		if(klass.isArray()) {
			return sb.append('[').append(getTypeDescriptor(klass.getComponentType())).toString();
		}
		return sb.append('L').append(Mirror.getPath(klass)).append(';').toString();
	}
	
	/**
	 * @param klass
	 *            类型
	 * @return 一个类型的包路径
	 */
	public static String getPath(Class<?> c) {
		return c.getName().replace(".", "/");
	}
	
	/**
	 * 相当于 getAllDeclaredMethods(Object.class)
	 * 
	 * @return 方法数组
	 */
	public Method[] getAllDeclaredMethodsWithoutTop() {
		return getAllDeclaredMethods(Object.class);
	}
	
	/**
	 * @return 所有静态方法
	 */
	public Method[] getStaticMethods() {
		List<Method> list = new LinkedList<Method>();
		for(Method m : klass.getMethods()) {
			if(Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()))
				list.add(m);
		}
		return list.toArray(new Method[list.size()]);
	}
	
	private static RuntimeException makeSetValueException(Class<?> type,
															String name,
															Object value,
															Exception e) {
		if(e instanceof FailToSetValueException) {
			return (FailToSetValueException)e;
		}
		return new FailToSetValueException(String.format("Fail to set value [%s] to [%s]->[%s] because '%s'",
														value,
														type.getName(),
														name,
														e.getMessage()),
											e);
	}
	
	/**
	 * 为对象的一个字段设值。 优先调用对象的 setter，如果没有，直接设置字段的值
	 * 
	 * @param obj
	 *            对象
	 * @param field
	 *            字段
	 * @param value
	 *            值。如果为 null，字符和数字字段，都会设成 0
	 * @throws FailToSetValueException
	 */
	public void setValue(Object obj, Field field, Object value) throws FailToSetValueException {
		if(!field.isAccessible())
			field.setAccessible(true);
		Class<?> ft = field.getType();
		// 非null值，进行转换
		if(null != value) {
			try {
				// TODO waiting for implement Castors.me().castTo()(value, field.getType())
				// value = Castors.me().castTo(value, field.getType());
			}catch(FailToCastObjectException e) {
				throw makeSetValueException(obj.getClass(), field.getName(), value, e);
			}
		}
		// 原生类型，转换为默认值
		else if(ft.isPrimitive()) {
			if(boolean.class == ft) {
				value = false;
			} else if(char.class == ft) {
				value = (char)0;
			} else {
				value = (byte)0;
			}
		}
		try {
			this.getSetter(field).invoke(obj, value);
		}catch(Exception e1) {
			try {
				field.set(obj, value);
			}catch(Exception e2) {
				throw makeSetValueException(obj.getClass(), field.getName(), value, e2);
			}
		}
	}
	
	/**
	 * 为对象的一个字段设值。优先调用 setter 方法。
	 * 
	 * @param obj
	 *            对象
	 * @param fieldName
	 *            字段名
	 * @param value
	 *            值
	 * @throws FailToSetValueException
	 */
	public void setValue(Object obj, String fieldName, Object value) throws FailToSetValueException {
		if(null == value) {
			try {
				setValue(obj, this.getField(fieldName), value);
			}catch(Exception e) {
				throw makeSetValueException(obj.getClass(), fieldName, value, e);
			}
		} else {
			try {
				this.getSetter(fieldName, value.getClass()).invoke(obj, value);
			}catch(Exception e) {
				try {
					setValue(obj, this.getField(fieldName), value);
				}catch(Exception ex) {
					throw makeSetValueException(obj.getClass(), fieldName, value, ex);
				}
			}
		}
	}
	
	private static RuntimeException makeGetValueException(Class<?> type, String name, Throwable e) {
		return new FailToGetValueException(String.format("Fail to get value for [%s]->[%s]",
														type.getName(),
														name), e);
	}
	
	/**
	 * 不调用 getter，直接获得字段的值
	 * 
	 * @param obj
	 *            对象
	 * @param f
	 *            字段
	 * @return 字段的值。
	 * @throws FailToGetValueException
	 */
	public Object getValue(Object obj, Field field) throws FailToGetValueException {
		if(!field.isAccessible())
			field.setAccessible(true);
		try {
			return field.get(obj);
		}catch(Exception e) {
			throw makeGetValueException(obj.getClass(), field.getName(), e);
		}
	}
	
	/**
	 * 优先通过 getter 获取字段值，如果没有，则直接获取字段值
	 * 
	 * @param obj
	 *            对象
	 * @param name
	 *            字段名
	 * @return 字段值
	 * @throws FailToGetValueException
	 *             既没发现 getter，又没有字段
	 */
	public Object getValue(Object obj, String name) throws FailToGetValueException {
		try {
			return this.getGetter(name).invoke(obj);
		}catch(Exception e) {
			try {
				return getValue(obj, this.getField(name));
			}catch(Exception ex) {
				throw makeGetValueException(obj.getClass(), name, ex);
			}
		}
	}
	
	/**
	 * @return 获得外覆类
	 * 
	 * @throws RuntimeException
	 *             如果当前类型不是原生类型，则抛出
	 */
	public Class<?> getWrapperClass() {
		if(!klass.isPrimitive()) {
			if(this.isPrimitiveNumber() || this.is(Boolean.class) || this.is(Character.class))
				return klass;
			throw Lang.makeThrow("Class '%s' should be a primitive class", klass.getName());
		}
		if(is(int.class))
			return Integer.class;
		if(is(char.class))
			return Character.class;
		if(is(boolean.class))
			return Boolean.class;
		if(is(long.class))
			return Long.class;
		if(is(float.class))
			return Float.class;
		if(is(byte.class))
			return Byte.class;
		if(is(short.class))
			return Short.class;
		if(is(double.class))
			return Double.class;
		
		throw Lang.makeThrow("Class [%s] has no wrapper class!", klass.getName());
	}
	
	/**
	 * @return 获得外覆类，如果没有外覆类，则返回自身的类型
	 */
	public Class<?> getWrapper() {
		if(klass.isPrimitive())
			return getWrapperClass();
		return klass;
	}
	
	/**
	 * @return 如果当前类为内部类，则返回其外部类。否则返回 null
	 */
	public Class<?> getOuterClass() {
		if(Modifier.isStatic(klass.getModifiers()))
			return null;
		String name = klass.getName();
		int pos = name.indexOf("$");
		if(pos == -1)
			return null;
		name = name.substring(0, pos);
		try {
			return Lang.loadClass(name);
		}catch(ClassNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * 获取对象构建器
	 * 
	 * @param args
	 *            构造函数参数
	 * @return 当前对象的构建方式。
	 * 
	 * @throws BorningException
	 *             当没有发现合适的 Borning 时抛出
	 * 
	 * @see org.gear.lang.born.Borning
	 */
	public Borning<T> getBorning(Object... args) throws BorningException {
		BornContext<T> bc = Borns.eval(klass, args);
		if(null == bc)
			throw new BorningException(klass, args);
		
		return bc.getBorning();
	}
	
	/**
	 * 获取对象构建器
	 * 
	 * @param argTypes
	 *            构造函数参数类型数组
	 * @return 当前对象构建方式
	 * 
	 * @throws BorningException
	 *             当没有发现合适的 Borning 时抛出
	 */
	public Borning<T> getBorningByArgTypes(Class<?>... argTypes) throws BorningException {
		BornContext<T> bc = Borns.evalByArgTypes(klass, argTypes);
		if(null == bc)
			throw new BorningException(klass, argTypes);
		return bc.getBorning();
	}
	
	/**
	 * 根据构造函数参数，创建一个对象。
	 * 
	 * @param args
	 *            构造函数参数
	 * @return 新对象
	 */
	public T born(Object... args) {
		return Borns.eval(klass, args).doBorn();
	}
	
	private static boolean doMatchMethodParamsType(Class<?>[] paramTypes, Class<?>[] methodArgTypes) {
		if(paramTypes.length == 0 && methodArgTypes.length == 0)
			return true;
		if(paramTypes.length == methodArgTypes.length) {
			for(int i = 0; i < paramTypes.length; i++)
				if(!Mirror.me(paramTypes[i]).canCastToDirectly(methodArgTypes[i]))
					return false;
			return true;
		} 
		// 可变参数
		else if(paramTypes.length + 1 == methodArgTypes.length) {
			if(!methodArgTypes[paramTypes.length].isArray())
				return false;
			for(int i = 0; i < paramTypes.length; i++)
				if(!Mirror.me(paramTypes[i]).canCastToDirectly(methodArgTypes[i]))
					return false;
			return true;
		}
		return false;
	}
	
	/**
	 * 根据函数名称和参数，返回一个函数调用方式
	 * 
	 * @param methodName
	 *            函数名
	 * @param args
	 *            参数
	 * @return 函数调用方式
	 */
	public Invoking getInvoking(String methodName, Object... args) {
		return new Invoking(klass, methodName, args);
	}
	
	/**
	 * 根据字段名，得出一个字段注入方式。优先用 Setter
	 * 
	 * @param fieldName
	 *            字段名
	 * @return 注入方式。
	 */
	public Injecting getInjecting(String fieldName) {
		Method[] sss = this.findSetters(fieldName);
		if(sss.length == 1)
			return new InjectBySetter(sss[0]);
		else
			try {
				Field field = this.getField(fieldName);
				try {
					return new InjectBySetter(this.getSetter(field));
				}catch(NoSuchMethodException e) {
					return new InjectByField(field);
				}
			}catch(NoSuchFieldException ex) {
				throw Lang.wrapThrow(ex);
			}
	}
	
	/**
	 * 根据字段名获得一个字段输入方式。优先用 Getter
	 * 
	 * @param fieldName
	 *            字段名
	 * @return 输出方式
	 */
	public Ejecting getEjecting(String fieldName) {
		try {
			return new EjectByGetter(getGetter(fieldName));
		}catch(NoSuchMethodException e) {
			try {
				Field field = this.getField(fieldName);
				try {
					return new EjectByGetter(getGetter(field));
				}catch(NoSuchMethodException e1) {
					return new EjectByField(field);
				}
			}catch(NoSuchFieldException ex) {
				throw Lang.wrapThrow(ex);
			}
		}
	}
	
	/**
	 * 调用对象的一个方法
	 * 
	 * @param obj
	 *            对象
	 * @param methodName
	 *            方法名
	 * @param args
	 *            参数
	 * @return 调用结果
	 */
	public Object invoke(Object obj, String methodName, Object...args) {
		return getInvoking(methodName, args).invoke(obj);
	}
	
	/**
	 * 查找一个方法。匹配的很宽泛
	 * 
	 * @param name
	 *            方法名
	 * @param paramTypes
	 *            参数类型列表
	 * @return 方法
	 * @throws NoSuchMethodException
	 */
	public Method findMethod(String name, Class<?>...paramTypes) throws NoSuchMethodException {
		try {
			return klass.getMethod(name, paramTypes);
		}catch(NoSuchMethodException e) {
			for(Method m : klass.getMethods()) {
				if(m.getName().equals(name)) {
					if(doMatchMethodParamsType(paramTypes, m.getParameterTypes()))
						return m;
				}
			}
		}
		throw new NoSuchMethodException(String.format("Fail to find Method %s->%s with params: \n%s",
													klass.getName(),
													name,
													// TODO waiting for implement Castors.me().castToString(paramTypes)
													// Castors.me().castToString(paramTypes)));
													null));
	}
	
	/**
	 * 根据名称和参数个数，查找一组方法
	 * 
	 * @param name
	 *            方法名
	 * @param argNumber
	 *            参数个数
	 * @return 方法数组
	 */
	public Method[] findMethods(String name, int argNumber) {
		List<Method> methods = new LinkedList<Method>();
		for(Method m : klass.getMethods()) {
			if(m.getName().equals(name))
				if(argNumber < 0)
					methods.add(m);
				else if(m.getParameterTypes().length == argNumber)
					methods.add(m);
		}
		return methods.toArray(new Method[methods.size()]);
	}
	
	/**
	 * 根据返回值类型，以及参数类型，查找第一个匹配的方法
	 * 
	 * @param returnType
	 *            返回值类型
	 * @param paramTypes
	 *            参数个数
	 * @return 方法
	 * @throws NoSuchMethodException
	 */
	public Method findMethod(Class<?> returnType, Class<?>...paramTypes) throws NoSuchMethodException {
		for(Method m : klass.getMethods()) {
			if(returnType == m.getReturnType())
				if(paramTypes.length == m.getParameterTypes().length) {
					boolean noThisOne = false;
					for(int i = 0; i < paramTypes.length; i++) {
						if(paramTypes[i] != m.getParameterTypes()[i]) {
							noThisOne = true;
							break;
						}
					}
					if(!noThisOne)
						return m;
				}
		}
		throw new NoSuchMethodException(String.format("Can not find method in [%s] with return type '%s' and arguments \n'%s'!",
													klass.getName(),
													returnType.getName(),
													// TODO waiting for implement Castors.me().castToString(paramTypes)
													// Castors.me().castToString(paramTypes)));
													null));
	}
	
	/**
	 * 一个方法的参数类型同一个给定的参数数组是否可以匹配
	 * 
	 * @param methodParamTypes
	 *            参数类型列表
	 * @param args
	 *            参数
	 * @return 匹配类型
	 * 
	 * @see org.gear.lang.MatchType
	 */
	public static MatchType matchParamTypes(Class<?>[] methodParamTypes, Object...args) {
		return matchParamTypes(methodParamTypes, evalToTypes(args));
	}
	
	/**
	 * 将一组对象，变成一组类型
	 * 
	 * @param args
	 *            对象数组
	 * @return 类型数组
	 */
	public static Class<?>[] evalToTypes(Object...args) {
		Class<?>[] types = new Class[args.length];
		int i = 0;
		for(Object obj : args) {
			types[i++] = null == obj ? Object.class : obj.getClass();
		}
		return types;
	}
	
	/**
	 * 匹配一个函数声明的参数类型数组和一个调用参数数组
	 * 
	 * @param paramTypes
	 *            函数声明参数数组
	 * @param argTypes
	 *            调用参数数组
	 * @return 匹配类型
	 * 
	 * @see org.gear.lang.MatchType
	 */
	public static MatchType matchParamTypes(Class<?>[] paramTypes, Class<?>[] argTypes) {
		int len = null == argTypes ? 0 : argTypes.length;
		if(len == 0 && paramTypes.length == 0) {
			return MatchType.YES;
		}
		if(paramTypes.length == len) {
			for(int i = 0; i < paramTypes.length; i++) {
				if(!Mirror.me(argTypes[i]).canCastToDirectly(paramTypes[i]))
					return MatchType.NO;
			}
			return MatchType.YES;
		} else if( len + 1 == paramTypes.length) {
			if(!paramTypes[len].isArray())
				return MatchType.NO;
			for(int i = 0; i < len; i++) {
				if(!Mirror.me(argTypes[i]).canCastToDirectly(paramTypes[i]))
					return MatchType.NO;
			}
			return MatchType.LACK;
		}
		return MatchType.NO;
	}
	
	/**
	 * @param type
	 *            目标类型
	 * @return 判断当前对象是否能直接转换到目标类型，而不产生异常
	 */
	public boolean canCastToDirectly(Class<?> type) {
		if(klass == type || type.isAssignableFrom(klass))
			return true;
		if(klass.isPrimitive() && type.isPrimitive()) {
			if(this.isPrimitiveNumber() && Mirror.me(type).isPrimitiveNumber())
				return true;
		}
		try {
			return Mirror.me(type).getWrapperClass() == this.getWrapperClass();
		}catch(Exception e) {}
		return false;
	}
	
	/**
	 * 判断当前对象是否为一个类型。精确匹配，即使是父类和接口，也不相等
	 * 
	 * @param type
	 *            类型
	 * @return 是否相等
	 */
	public boolean is(Class<?> type) {
		return null != type && klass == type;
	}
	
	/**
	 * 判断当前对象是否为一个类型。精确匹配，即使是父类和接口，也不相等
	 * 
	 * @param className
	 *            类型名称
	 * @return 是否相等
	 */
	public boolean is(String className) {
		return klass.getName().equals(className);
	}
	
	/**
	 * @param type
	 *            类型或接口名
	 * @return 当前对象是否为一个类型的子类，或者一个接口的实现类
	 */
	public boolean isOf(Class<?> type) {
		return type.isAssignableFrom(type);
	}
	
	/**
	 * @return 当前对象是否为字符串
	 */
	public boolean isString() {
		return is(String.class);
	}
	
	/**
	 * @return 当前对象是否为CharSequence的子类
	 */
	public boolean isStringLike() {
		return CharSequence.class.isAssignableFrom(klass);
	}
	
	/**
	 * @return 当前对象是否为字符
	 */
	public boolean isChar() {
		return is(char.class) || is(Character.class);
	}
	
	/**
	 * @return 当前对象是否为枚举
	 */
	public boolean isEnum() {
		return klass.isEnum();
	}
	
	/**
	 * @return 当前对象是否为布尔
	 */
	public boolean isBoolean() {
		return is(boolean.class) || is(Boolean.class);
	}
	
	/**
	 * @return 当前对象是否为浮点
	 */
	public boolean isFloat() {
		return is(float.class) || is(Float.class);
	}
	
	/**
	 * @return 当前对象是否为双精度浮点
	 */
	public boolean isDouble() {
		return is(double.class) || is(Double.class);
	}
	
	/**
	 * @return 当前对象是否为整型
	 */
	public boolean isInt() {
		return is(int.class) || is(Integer.class);
	}
	
	/**
	 * @return 当前对象是否为整数（包括 int, long, short, byte）
	 */
	public boolean isIntLike() {
		return isInt() || isLong() || isShort() || isByte() || is(BigDecimal.class);
	}
	
	/**
	 * @return 当前类型是不是接口
	 */
	public boolean isInterface() {
		return null == klass ? null : klass.isInterface();
	}
	
	/**
	 * @return 当前对象是否为小数 (float, dobule)
	 */
	public boolean isDecimal() {
		return isFloat() || isDouble();
	}
	
	/**
	 * @return 当前对象是否为长整型
	 */
	public boolean isLong() {
		return is(long.class) || is(Long.class);
	}
	
	/**
	 * @return 当前对象是否为短整型
	 */
	public boolean isShort() {
		return is(short.class) || is(Short.class);
	}
	
	/**
	 * @return 当前对象是否为字节型
	 */
	public boolean isByte() {
		return is(byte.class) || is(Byte.class);
	}
	
	/**
	 * @param type
	 *            类型
	 * @return 否为一个对象的外覆类
	 */
	public boolean isWrapperOf(Class<?> type) {
		try {
			return Mirror.me(type).getWrapperClass() == klass;
		}catch(Exception e) {}
		return false;
	}
	
	/**
	 * @return 当前对象是否为原生的数字类型 （即不包括 boolean 和 char）
	 */
	public boolean isPrimitiveNumber() {
		return isInt() || isLong() || isFloat() || isDouble() || isByte() || isShort();
	}
	
	/**
	 * 如果不是容器，也不是 POJO，那么它必然是个 Obj
	 * 
	 * @return true or false
	 */
	public boolean isObj() {
		return isContainer() || isPojo();
	}
	
	/**
	 * 判断当前类型是否为POJO。 除了下面的类型，其他均为 POJO
	 * <ul>
	 * <li>原生以及所有包裹类
	 * <li>类字符串
	 * <li>类日期
	 * <li>非容器
	 * </ul>
	 * 
	 * @return true or false
	 */
	public boolean isPojo() {
		if(klass.isPrimitive())
			return false;
		if(this.isStringLike() || this.isDateTimeLike())
			return false;
		if(this.isPrimitiveNumber() || this.isBoolean() || this.isChar())
			return false;
		return !isContainer();
	}
	
	/**
	 * 判断当前类型是否为容器，包括 Map，Collection, 以及数组
	 * 
	 * @return true of false
	 */
	public boolean isContainer() {
		return isColl() || isMap();
	}
	
	/**
	 * 判断当前类型是否为数组
	 * 
	 * @return true of false
	 */
	public boolean isArray() {
		return klass.isArray();
	}
	
	/**
	 * 判断当前类型是否为 Collection
	 * 
	 * @return true of false
	 */
	public boolean isCollection() {
		return isOf(Collection.class);
	}
	
	/**
	 * @return 当前类型是否是数组或者集合
	 */
	public boolean isColl() {
		return isArray() || isCollection();
	}
	
	/**
	 * 判断当前类型是否为 Map
	 * 
	 * @return true of false
	 */
	public boolean isMap() {
		return isOf(Map.class);
	}
	
	/**
	 * @return 当前对象是否为数字
	 */
	public boolean isNumber() {
		return Number.class.isAssignableFrom(klass)
				|| klass.isPrimitive()
				&& !is(boolean.class)
				&& !is(char.class);
	}
	
	/**
	 * @return 当前对象是否在表示日期或时间
	 */
	public boolean isDateTimeLike() {
		return Calendar.class.isAssignableFrom(klass)
				|| java.util.Date.class.isAssignableFrom(klass)
				|| java.sql.Date.class.isAssignableFrom(klass)
				|| java.sql.Time.class.isAssignableFrom(klass);
	}
	
	public String toString() {
		return klass.getName();
	}
	
	/**
	 * 根据函数参数类型数组的最后一个类型（一定是数组，表示变参），为最后一个变参生成一个空数组
	 * 
	 * @param pts
	 *            函数参数类型列表
	 * @return 变参空数组
	 */
	public static Object[] blankArrayArg(Class<?>[] pts) {
		return (Object[]) Array.newInstance(pts[pts.length - 1].getComponentType(), 0);
	}
	
	/**
	 * 获取一个类的泛型参数数组，如果这个类没有泛型参数，返回 null
	 */
	public static Type[] getTypeParams(Class<?> klass) {
		// TODO 这个实现会导致泛形丢失，只能取得申明类型
		if(klass == null || "java.lang.Object".equals(klass.getName()))
			return null;
		// 父类
		Type superclass = klass.getGenericSuperclass();
		if(null != superclass && superclass instanceof ParameterizedType)
			return ((ParameterizedType)superclass).getActualTypeArguments();
		// 接口
		Type[] interfaces = klass.getGenericInterfaces();
		for(Type inf : interfaces) {
			if(inf instanceof ParameterizedType) {
				return ((ParameterizedType)inf).getActualTypeArguments();
			}
		}
		return getTypeParams(klass.getSuperclass());
	}
	
	private static final Pattern PTN = Pattern.compile("(<)(.+)(>)");
	
	/**
	 * 获取一个字段的泛型参数数组，如果这个字段没有泛型，返回空数组
	 * 
	 * @param f
	 *            字段
	 * @return 泛型参数数组
	 */
	public static Class<?>[] getGenericsTypes(Field f) {
		String gts = f.toGenericString();
		Matcher m = PTN.matcher(gts);
		if(m.find()) {
			String s = m.group(2);
			String[] ss = Strings.splitIgnoreBlank(s);
			if(ss.length > 0) {
				Class<?>[] re = new Class<?>[ss.length];
				try {
					for(int i = 0; i < ss.length; i++) {
						String className = ss[i];
						if(className.length() > 0 && className.charAt(0) == '?') {
							re[i] = Object.class;
						} else {
							int pos = className.indexOf('<');
							if(pos < 0)
								re[i] = Lang.loadClass(className);
							else 
								re[i] = Lang.loadClass(className.substring(0, pos));
						}
					}
					return re;
				}catch(ClassNotFoundException e) {
					throw Lang.wrapThrow(e);
				}
			}
		}
		return new Class<?>[0];
	}
	
	// TODO waiting for implement ...
	
	public Class<?> unWrapper() {
		return TypeMapping2.get(klass);
	}
	
	private static final Map<Class<?>, Class<?>> TypeMapping2 = new HashMap<Class<?>, Class<?>>();
	static {
		TypeMapping2.put(Short.class, short.class);
		TypeMapping2.put(Integer.class, int.class);
		TypeMapping2.put(Long.class, long.class);
		TypeMapping2.put(Double.class, double.class);
		TypeMapping2.put(Float.class, float.class);
		TypeMapping2.put(Byte.class, byte.class);
		TypeMapping2.put(Character.class, char.class);
		TypeMapping2.put(Boolean.class, boolean.class);
	}
}
