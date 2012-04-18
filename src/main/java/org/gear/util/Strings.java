package org.gear.util;

import java.util.LinkedList;
import java.util.List;

public abstract class Strings {

	/**
	 * 将字符串首字母大写
	 * 
	 * @param s
	 *            字符串
	 * @return 首字母大写后的新字符串
	 */
	public static String capitalize(CharSequence s) {
		if(s == null)
			return null;
		if(s.length() == 0)
			return "";
		int len = s.length();
		char char0 = s.charAt(0);
		if(Character.isUpperCase(char0))
			return s.toString();
		return new StringBuilder(len).append(Character.toUpperCase(char0))
					.append(s.subSequence(1, len)).toString();
	}

	/**
	 * 将字符串首字母小写
	 * 
	 * @param s
	 *            字符串
	 * @return 首字母小写后的新字符串
	 */
	public static String lowerFirst(CharSequence s) {
		if(null == s)
			return null;
		int len = s.length();
		if(len == 0)
			return "";
		char c = s.charAt(0);
		if(Character.isLowerCase(c)) {
			return s.toString();
		}
		return new StringBuilder(len).append(Character.toLowerCase(c))
									.append(s.subSequence(1, len))
									.toString();
	}

	/**
	 * 将字符串按半角逗号，拆分成数组，空元素将被忽略
	 * 
	 * @param s
	 *            字符串
	 * @return 字符串数组
	 */
	public static String[] splitIgnoreBlank(String s) {
		return Strings.splitIgnoreBlank(s, ",");
	}
	
	/**
	 * 根据一个正则式，将字符串拆分成数组，空元素将被忽略
	 * 
	 * @param s
	 *            字符串
	 * @param regex
	 *            正则式
	 * @return 字符串数组
	 */
	public static String[] splitIgnoreBlank(String s, String regex) {
		if(null == s)
			return null;
		String[] ss = s.split(regex);
		List<String> list = new LinkedList<String>();
		for(String st : ss) {
			if(isBlank(st))
				continue;
			list.add(trim(st));
		}
		return list.toArray(new String[list.size()]);
	}
	
	/**
	 * @param cs
	 *            字符串
	 * @return 是不是为空白字符串
	 */
	public static boolean isBlank(CharSequence cs) {
		if(null == cs)
			return true;
		int len = cs.length();
		for(int i = 0; i < len; i++) {
			if(!(Character.isWhitespace(cs.charAt(i))))
				return false;
		}
		return true;
	}
	
	/**
	 * 去掉字符串前后空白
	 * 
	 * @param cs
	 *            字符串
	 * @return 新字符串
	 */
	public static String trim(CharSequence cs) {
		if(null == cs)
			return null;
		if(cs instanceof String)
			return ((String)cs).trim();
		int len = cs.length();
		if(len == 0)
			return cs.toString();
		int l = 0;
		int last = len - 1;
		int r = last;
		for(; l < len; l++) {
			if(!Character.isWhitespace(cs.charAt(l)))
				break;
		}
		for(; r > l; r--) {
			if(!Character.isWhitespace(cs.charAt(r)))
				break;
		}
		if(l > r)
			return "";
		else if(l == 0 && r == last)
			return cs.toString();
		return cs.subSequence(l, r+1).toString();
	}
	
}
