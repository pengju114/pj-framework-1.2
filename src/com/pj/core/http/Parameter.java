package com.pj.core.http;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.pj.core.utilities.StringUtility;

/**
 * 参数类
 *@author 陆振文[PENGJU]
 *时间:2012-4-27 下午01:52:23
 */
public class Parameter implements Serializable{
	private static final long serialVersionUID = -7841258209137760683L;
	
	private Map<String, LinkedList<? extends Object>> parameters;
	public Parameter(){
		parameters=new HashMap<String, LinkedList<? extends Object>>();
	}
	public Parameter(String queryString){
		this();
		appendParameters(queryString);
	}
	
	/**
	 * 设置参数
	 * PENGJU
	 * 2012-11-13 下午3:13:22
	 * @param parameters 查询字符串
	 */
	public void appendParameters(String parameters) {
		if (!StringUtility.isEmpty(parameters)) {
			String[] params=parameters.split("&");
			int index=0;
			String eq="=";
			
			for (String string : params) {
				index=string.indexOf(eq);
				if (index>-1) {
					addParameter(string.substring(0, index), string.substring(index+1));
				}
			}
		}
	}
	/**
	 * 添加一个参数,即使存在该参数也不会覆盖原来的
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void addParameter(String key,Object value) {
		LinkedList<Object> list=(LinkedList<Object>) getDataList(key);
		list.add(value);
	}
	
	/**
	 * 添加多个参数,即使存在该参数也不会覆盖原来的
	 * @param map
	 */
	public void addParameter(Map<String, ? extends Object> params) {
		if (params!=null) {
			for (Map.Entry<String, ? extends Object> entry : params.entrySet()) {
				addParameter(entry.getKey(), entry.getValue());
			}
		}
	}
	
	/**
	 * 把一个parameter里面的参数附加到当前参数对象
	 * PENGJU
	 * 2012-12-5 上午10:30:51
	 * @param parameter
	 */
	public void concat(Parameter parameter) {
		if (parameter!=null) {
			for (Entry<String, LinkedList<? extends Object>> e : parameter.getParameterEntrys()) {
				for (Object v : e.getValue()) {
					addParameter(e.getKey(), v);
				}
			}
		}
	}
	
	/**
	 * 设置一个参数,覆盖旧的(如果存在)
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public  void setParameter(String key,Object value) {
		LinkedList<Object> list=(LinkedList<Object>)getDataList(key);
		list.clear();
		list.add(value);
	}
	
	/**
	 * 设置多个参数,覆盖旧的(如果存在)
	 * @param map
	 */
	public void setParameter(Map<String, ? extends Object> params) {
		if (params!=null) {
			for (Map.Entry<String, ? extends Object> entry : params.entrySet()) {
				setParameter(entry.getKey(), entry.getValue());
			}
		}
	}
	
	/**
	 * 删除一个参数
	 * @param key
	 */
	public void removeParameter(String key) {
		parameters.remove(key);
	}
	
	private LinkedList<? extends Object> getDataList(String key) {
		LinkedList<? extends Object> list=parameters.get(key);
		if (list==null) {
			list=new LinkedList<Object>();
			parameters.put(key, list);
		}
		return list;
	}
	
	public Object getParameter(String key) {
		LinkedList<? extends Object> list=parameters.get(key);
		if (list!=null && list.size()>0) {
			return list.getFirst();
		}
		return null;
	}
	
	public Object[] getParameterValues(String key) {
		LinkedList<? extends Object> list=parameters.get(key);
		if (list!=null && list.size()>0) {
			return list.toArray();
		}
		return null;
	}
	
	public String[] getParameterNames() {
		return parameters.keySet().toArray(new String[0]);
	}


	public Set<Entry<String, LinkedList<? extends Object>>> getParameterEntrys() {
		return parameters.entrySet();
	}
	
	public void removeAll() {
		parameters.clear();
	}
	
	public String toString() {
		StringBuilder builder=new StringBuilder();
		char eq='=';
		char and='&';
		for (Entry<String, LinkedList<? extends Object>> entry : parameters.entrySet()) {
			for (Object val : entry.getValue()) {
				builder.append(entry.getKey()).append(eq).append(val).append(and);
			}
		}
		
		if (builder.length()>0) {
			builder.deleteCharAt(builder.length()-1);
		}
		
		return builder.toString();
	}
}
