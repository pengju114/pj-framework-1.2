package com.pj.core.datamodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.pj.core.utilities.ConvertUtility;
import com.pj.core.utilities.StringUtility;

/**
 * 数据包装器类。数据包装器是基本的数据模型，扩展于Map
 * 在程序中尽量使用包装器，包装器提供了基本的数据装转换方法
 * @author 陆振文[pengju]
 * 2012-10-18 下午1:35:21
 * email: pengju114@163.com
 */
public class DataWrapper extends HashMap<String, Object> implements Cacheable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7900630258174086874L;

	
	/**
	 * 获取int类型数据，不是int类型将转换成int类型
	 * 陆振文
	 * 2014年3月6日 下午10:12:13
	 * @param key
	 * @return
	 */
	public Integer getInt(String key) {
		// TODO Auto-generated method stub
		return getDouble(key).intValue();
	}

	/**
	 * 获取Float类型数据，不是Float类型将转换成Float类型
	 * 陆振文
	 * 2014年3月6日 下午10:12:13
	 * @param key
	 * @return
	 */
	public Float getFloat(String key) {
		// TODO Auto-generated method stub
		return getDouble(key).floatValue();
	}

	/**
	 * 获取Double类型数据，不是Double类型将转换成Double类型
	 * 陆振文
	 * 2014年3月6日 下午10:12:13
	 * @param key
	 * @return
	 */
	public Double getDouble(String key) {
		// TODO Auto-generated method stub
		return ConvertUtility.parseDouble(getString(key));
	}

	/**
	 * 获取Long类型数据，不是Long类型将转换成Long类型
	 * 陆振文
	 * 2014年3月6日 下午10:12:13
	 * @param key
	 * @return
	 */
	public Long getLong(String key) {
		// TODO Auto-generated method stub
		return getDouble(key).longValue();
	}


	/**
	 * 获取String类型数据，不是String类型将转换成String类型
	 * 陆振文
	 * 2014年3月6日 下午10:12:13
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		// TODO Auto-generated method stub
		Object valObject=get(key);
		return StringUtility.toString(valObject);
	}

	@Override
	public Object getObject(String key) {
		// TODO Auto-generated method stub
		return get(key);
	}

	@Override
	public void setObject(String key, Object value) {
		// TODO Auto-generated method stub
		put(key, value);
	}
	
	/**
	 * 相当于getObject,不过顺带帮你类型转换罢了
	 * 陆振文
	 * 2014年3月6日 下午10:15:57
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T easyGetObject(String key){
		return (T) get(key);
	}
	
	/**
	 * 获取一个List,非List类型将返回null
	 * 陆振文
	 * 2014年3月6日 下午10:16:35
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getList(String key){
		Object object=get(key);
		
		if (object instanceof List) {
			return (List<T>) object;
		}
		
		return null;
	}
	
	/**
	 * 获取指定类型的对象，如果根据key获取的是List类型则取第一个对象(即使这个也是List)返回
	 * 陆振文
	 * 2014年3月6日 下午10:17:02
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getObjectAndIgnoreList(String key){
		Object object=get(key);
		if (object instanceof Collection) {
			Collection<Object> list=(Collection<Object>) object;
			object=null;
			if (list!=null && list.size()>0) {
				object=list.iterator().next();
			}
		}
		
		return (T) object;
	}
}
