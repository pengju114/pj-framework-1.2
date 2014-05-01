package com.pj.core.utilities;

import java.net.URLEncoder;

import org.apache.http.protocol.HTTP;

import com.pj.core.http.Parameter;

/**
 * URL辅助类
 *@author 陆振文[PENGJU]
 *时间:2012-5-3 上午9:49:04
 */
public class URLUtility {
	public static final String DEFAULT_SESSION_ID_NAME="jsessionid";
	
	/**
	 * 相当于HttpServletResponse的encodeURL方法,在链接后面加入会话ID维护会话状态.
	 * @param url 目标链接地址
	 * @param sessionId 服务器端的session ID
	 * @return 处理后的URL
	 */
	public static String encodeURL(String url,String sessionId) {
		if (!StringUtility.isEmpty(url)) {
			int queryIndex=url.indexOf("?");
			StringBuilder baseUrl=new StringBuilder();
			String queryString=StringUtility.EMPTY_STRING;
			if (queryIndex>-1) {
				baseUrl.append(url.substring(0, queryIndex));
				queryString=url.substring(queryIndex);
			}else {
				baseUrl.append(url);
			}
			
			queryIndex=baseUrl.indexOf(";");
			
			if (queryIndex>-1) {
				baseUrl.delete(queryIndex, baseUrl.length());
			}
			
			baseUrl.append(';').append(DEFAULT_SESSION_ID_NAME).append('=').append(sessionId);
			baseUrl.append(queryString);
			
			return baseUrl.toString();
		}
		return StringUtility.EMPTY_STRING;
	}
	
	public static String appendParameter(String url,String name,String... values) {
		StringBuilder builder=new StringBuilder(url);
		
		if (builder.lastIndexOf("?")>0) {
			if ((builder.lastIndexOf("?")==builder.length()-1) || (builder.lastIndexOf("&")==builder.length()-1)) {
			}else {
				builder.append('&');
			}
		}else {
			builder.append('?');
		}
		
		
		if (values!=null) {
			for (String string : values) {
				try {
					string=URLEncoder.encode(string, HTTP.UTF_8);
				} catch (Exception e) {}
				builder.append(name).append('=').append(string).append('&');
			}
		}
		
		if (builder.charAt(builder.length()-1)=='&') {
			builder.deleteCharAt(builder.length()-1);
		}
		
		return builder.toString();
	}
	
	public static String appendParameter(String url,Parameter parameter) {
		StringBuilder builder=new StringBuilder(url);
		
		if (builder.lastIndexOf("?")>0) {
			if ((builder.lastIndexOf("?")==builder.length()-1) || (builder.lastIndexOf("&")==builder.length()-1)) {
			}else {
				builder.append('&');
			}
		}else {
			builder.append('?');
		}
		
		builder.append(parameter);
		if (builder.charAt(builder.length()-1)=='&') {
			builder.deleteCharAt(builder.length()-1);
		}
		
		return builder.toString();
	}
}
