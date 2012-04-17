package org.gear.util;

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
	
}
