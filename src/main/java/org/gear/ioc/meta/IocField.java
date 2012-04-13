package org.gear.ioc.meta;

/**
 * 描述了一个对象的字段，两个属性分别表示字段名，和字段值
 * 
 * @see org.gear.ioc.meta.IocValue
 */
public class IocField {

	private String name;
	private IocValue value;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public IocValue getValue() {
		return value;
	}
	public void setValue(IocValue value) {
		this.value = value;
	}
}
