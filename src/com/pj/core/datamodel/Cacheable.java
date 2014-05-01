package com.pj.core.datamodel;

/**
 * 可缓存接口
 * @author 陆振文[PENGJU]
 * 2012-10-19 下午2:24:02
 * email: pengju114@163.com
 */
public interface Cacheable {
	public void setObject(String key,Object value);
	public Object getObject(String key);
}
