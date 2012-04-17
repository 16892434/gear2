package org.gear.lang;

/**
 * 类型提炼器。针对一个类型，提炼出一组最能反应其特征的类型
 * 
 */
public interface TypeExtractor {

	Class<?>[] extract(Mirror<?> mirror);
}
