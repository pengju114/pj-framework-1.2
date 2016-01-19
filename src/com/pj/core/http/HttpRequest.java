package com.pj.core.http;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

import com.pj.core.BaseApplication;
import com.pj.core.R;
import com.pj.core.http.HttpResult;
import com.pj.core.http.Parameter;
import com.pj.core.res.AppConfig;
import com.pj.core.utilities.HttpUtility;
import com.pj.core.utilities.StringUtility;
import com.pj.core.utilities.URLUtility;



/**
 * http请求类
 * 2012-03-28 17:43
 * @author 陆振文[PENGJU]
 *
 */
public class HttpRequest extends AsyncTask<Void,Float,HttpResult>{
	public static final String 		METHOD_GET ="GET";
	public static final String 		METHOD_POST="POST";
	
	/**
	 * 要求返回的数据类型为字节流(InputStream)
	 */ 
	public static final int 		EXPECTED_STREAM=1<<1;
	/**
	 * 要求返回的数据类型为字符串(String)
	 */
	public static final int 		EXPECTED_STRING=1<<2;
	/**
	 * 要求返回的数据类型为TreeDataWrapper
	 */
	public static final int 		EXPECTED_DATAWRAPPER=1<<3;
	
	/**
	 * 服务器返回的数据为文本
	 */
	public static final int 		RESPONSE_TEXT=1<<4;
	/**
	 * 服务器返回的数据为XML数据
	 */
	public static final int 		RESPONSE_XML=1<<5;
	/**
	 * 服务器返回的数据为JSON数据
	 */
	public static final int 		RESPONSE_JSON=1<<6;
	
	//全局id
	private static int 				GLOBAL_ID=Integer.MIN_VALUE/2;
	
	
	//参数对象,用来封装发送给服务器的数据
	private Parameter 				parameter;
	private Parameter				headers;
	private String 					url;
	
	//默认编码为UTF-8
	private String 					charset;
	//请求唯一ID
	private int 					uniqueID;
	
	private int 					requestCode;
	private boolean 				multipart;
	private String 					method;
	private Object 					extraData;
	/**
	 * 拿到数据后要解析成的目标数据类型
	 */
	private int 					expectedDataFormat;
	/**
	 * 指明服务器返回的数据类型
	 */
	private int						responseDataFormat;
	
	private HttpRequestBase         nativeRequest;
	
	private HttpRequestListener		httpRequestListener;
	
	
	private boolean                 sessionPersistent;
	
	public HttpRequest(String url,int requestCode){
		this.url=url;
		this.requestCode=requestCode;
		
		parameter=new Parameter();
		headers=new Parameter();
		uniqueID=GLOBAL_ID++;
		setDefaults();
	}
	
	public void setDefaults(){
		parameter.removeAll();
		headers.removeAll();
		charset="UTF-8";
		multipart=false;
		method=METHOD_POST;
		extraData=null;
		expectedDataFormat=EXPECTED_DATAWRAPPER;
		responseDataFormat=RESPONSE_JSON;
		sessionPersistent = true;
	}
	
	public String getUrl() {
		return url;
	}
	
	public boolean isMultipart() {
		return multipart;
	}
	public void setMultipart(boolean multipart) {
		this.multipart = multipart;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public Object getExtraData() {
		return extraData;
	}
	public void setExtraData(Object extraData) {
		this.extraData = extraData;
	}
	
	/**
	 * 是否维护会话
	 * @param sessionPersistent
	 */
	public void setSessionPersistent(boolean sessionPersistent) {
		this.sessionPersistent = sessionPersistent;
	}
	
	public boolean isSessionPersistent() {
		return sessionPersistent;
	}
	
	/**
	 * 设置要把服务器返回的数据解析成哪种数据类型
	 * @param responseDataType 可用的值有<br>
	 * {@link HttpRequest#EXPRCTED_DATAWRAPPER}(默认)<br>
	 * {@link HttpRequest#EXPRCTED_STRING}<br>
	 * {@link HttpRequest#EXPRCTED_STREAM}
	 */
	public void setExpectedDataFormat(int expectedDataFormat) {
		this.expectedDataFormat = expectedDataFormat;
	}
	
	/**
	 * 要解析成哪种目标数据类型
	 * @return
	 */
	public int getExpectedDataFormat() {
		return expectedDataFormat;
	}
	
	/**
	 * 服务器返回的数据类型
	 * @return
	 */
	public int getResponseDataFormat() {
		return responseDataFormat;
	}
	
	/**
	 * 指明服务器将要返回的数据类型
	 * @param responseDataFormat 可用的值有<br>
	 * {@link HttpRequest#RESPONSE_JSON}(默认)<br>
	 * {@link HttpRequest#RESPONSE_XML}<br>
	 * {@link HttpRequest#RESPONSE_TEXT}
	 */
	public void setResponseDataFormat(int responseDataFormat) {
		this.responseDataFormat = responseDataFormat;
	}
	
	/**
	 * 默认UTF-8编码
	 * @return
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * 设置成功返回true
	 * @param charset
	 * @return
	 */
	public boolean setCharset(String charset) {
		//如果系统支持此编码
		if (Charset.isSupported(charset)) {
			this.charset=charset;
			return true;
		}
		return false;
	}
	
	public void setHttpRequestListener(HttpRequestListener httpRequestListener) {
		this.httpRequestListener = httpRequestListener;
	}
	
	public HttpRequestListener getHttpRequestListener() {
		return httpRequestListener;
	}
	
	/**
	 * 获取请求码，用户设置
	 * @return
	 */
	public int getRequestCode() {
		return requestCode;
	}
	
	/**
	 * 获取代表该请求的唯一ID
	 * @return
	 */
	public int getUniqueID() {
		return uniqueID;
	}
	
	/**
	 * 获取链接服务器时的URL，如果是get方法将会包含所有参数
	 * @return
	 */
	public String getRequestUrl() {
		return URLUtility.appendParameter(getUrl(), parameter);
	}
	
	
	/**
	 * 添加一个参数,即使存在该参数也不会覆盖原来的
	 * @param key
	 * @param value
	 */
	public void addParameter(String key,Object value) {
		parameter.addParameter(key, value);
	}
	
	/**
	 * 添加多个参数,即使存在该参数也不会覆盖原来的
	 * @param map
	 */
	public void addParameter(Map<String, Object> params) {
		parameter.addParameter(params);
	}
	
	/**
	 * 设置一个参数,覆盖旧的(如果存在)
	 * @param key
	 * @param value
	 */
	public void setParameter(String key,Object value) {
		parameter.setParameter(key, value);
	}
	
	/**
	 * 设置多个参数,覆盖旧的(如果存在)
	 * @param map
	 */
	public void setParameter(Map<String, Object> params) {
		parameter.setParameter(params);
	}
	
	/**
	 * 删除一个参数
	 * @param key
	 */
	public void removeParameter(String key) {
		parameter.removeParameter(key);
	}
	
	/**
	 * 替换所有参数
	 * @param newParameter
	 */
	public void replaceParameter(Parameter newParameter){
		if (newParameter!=null) {
			parameter=newParameter;
		}
	}
	
	private void addRequestHeader(org.apache.http.HttpRequest request) {
		if (request!=null) {
			if (isSessionPersistent()) {
				headers.removeParameter("Cookie");
				headers.addParameter("Cookie", "JSESSIONID="+BaseApplication.getInstance().getSessionId());
			}
			for (Entry<String, LinkedList<? extends Object>> h : headers.getParameterEntrys()) {
				for (Object v : h.getValue()) {
					request.addHeader(h.getKey(), StringUtility.toString(v));
				}
			}
		}
	}
	
	/**
	 * 添加一个HTTP头
	 * @param name
	 * @param value
	 */
	public void addHeader(String name,String value) {
		headers.addParameter(name, value);
	}
	/**
	 * 设置一个HTTP头
	 * @param name
	 * @param value
	 */
	public void setHeader(String name,String value) {
		headers.setParameter(name, value);
	}
	
	public void setHeader(Map<String, String> headerMap) {
		headers.setParameter(headerMap);
	}
	
	public void addHeader(Map<String, String> headerMap) {
		if (headerMap!=null) {
			for (Entry<String, String> h : headerMap.entrySet()) {
				addHeader(h.getKey(), h.getValue());
			}
		}
	}
	
	public void clearHeader() {
		headers.removeAll();
	}
	
	private String getString(int stringRes){
		return BaseApplication.getInstance().getString(stringRes);
	}
	
	/**
	 * 开始异步访问网络
	 */
	public void startAsynchronousRequest() {
		execute(null,null);
	}
	
	/**
	 * 同步访问网络
	 * @return 根据 ResponseDataType 返回对应的数据类型
	 * @throws HttpException
	 */
	public HttpResult startSynchronousRequest() throws HttpException,IOException{
		HttpEntity result=null;
		Parameter  responseHeader=new Parameter();
		if (isMultipart()) {
			setMethod(METHOD_POST);
			HttpEntity requestEntity=wrapFileParam();
			result=sendPostRequest(requestEntity, responseHeader);
		}else {
			if (METHOD_POST.equalsIgnoreCase(getMethod())) {
				HttpEntity requestEntity=wrapStringParam();
				result=sendPostRequest(requestEntity, responseHeader);
			}else {
				result=sendGetRequest(responseHeader);
			}
		}
		
		if (result==null) {
			throw new HttpException(getString(R.string.c_msg_http_request_error));
		}
		
		Object responseData=null;
		if (getExpectedDataFormat()==EXPECTED_STRING) {
			responseData=EntityUtils.toString(result, getCharset());
		}else if (getExpectedDataFormat()==EXPECTED_STREAM) {
			responseData=result.getContent();
		}else {
			if (getResponseDataFormat()==RESPONSE_JSON) {
				responseData=HttpUtility.parseJSON(EntityUtils.toString(result, getCharset()));
			}else if (getResponseDataFormat()==RESPONSE_XML) {
				responseData=HttpUtility.parseXML(result.getContent());
			}else {
				throw new HttpException(BaseApplication.getInstance().getResources().getString(R.string.c_msg_http_illegal_data_format, "DATA_DATAWRAPPER"));
			}
		}
		
		// 获取session id
		if (responseHeader!=null) {
			for (String key : responseHeader.getParameterNames()) {
				if ("Set-Cookie".equalsIgnoreCase(key)) {
					Object[] cookie = responseHeader.getParameterValues(key);
					String sessionString = null;
					for (Object object : cookie) {
						sessionString = StringUtility.toString(object);
						int index = sessionString.indexOf("JSESSIONID=");
						if (index==-1) {
							index = sessionString.indexOf("jsessionid=");
						}
						if (index!=-1) {
							String id = sessionString.substring(index+11);
							if (id!=null) {
								index = id.indexOf(";");
								if (index>-1) {
									id = id.substring(0, index);
								}
							}
							BaseApplication.getInstance().setSessionId(id);
							break;
						}
					}
					break;
				}
			}
		}
		
		HttpResult httpResult=new HttpResult(responseData, responseHeader);
		if (getExpectedDataFormat()!=EXPECTED_DATAWRAPPER) {
			httpResult.statusCode=HttpResult.HTTP_OK;
		}
		
		return httpResult;
	}
	
	
	/**
	 * 提取请求返回的头部信息
	 * @param response
	 */
	private void extractHeader(HttpResponse response,Parameter responseHeaderReceiver) {
		// TODO Auto-generated method stub
		if (response!=null && responseHeaderReceiver!=null) {
			Header[] hs=response.getAllHeaders();
			if (hs!=null) {
				for (Header header : hs) {
					responseHeaderReceiver.addParameter(header.getName(), header.getValue());
				}
			}
		}
	}
	
	/**
	 * 以post的方式发送请求
	 * @param  postEntity 发送的数据包
	 * @return 请求结果体
	 * @throws HttpException
	 */
	private HttpEntity sendPostRequest(HttpEntity postEntity,Parameter responseHeaderReceiver) throws HttpException{
		URL urlPath=null;
		try {
			urlPath=new URL(url);
		} catch (Exception e) {
			// TODO: handle exception
			throw new HttpException(e.getMessage(), e);
		}
		//创建要请求的目标服务器对象
		HttpHost host = new HttpHost(urlPath.getHost(), urlPath.getPort(), urlPath.getProtocol());
		//请求方法
		StringBuilder path=new StringBuilder(urlPath.getPath());
		if (urlPath.getQuery()!=null) {
			path.append('?').append(urlPath.getQuery());
		}
		HttpPost post = new HttpPost(path.toString());
		nativeRequest=post;
		//请求客户端
		CloseableHttpClient client = HttpClients.createDefault();
		Integer connTimeout=AppConfig.getConfig(AppConfig.CONF_HTTP_CONN_TIMEOUT, AppConfig.VALUE_HTTP_CONN_TIMEOUT);
		Integer socketTimeout=AppConfig.getConfig(AppConfig.CONF_HTTP_SO_TIMEOUT,  AppConfig.VALUE_HTTP_SO_TIMEOUT);

		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connTimeout).setSocketTimeout(socketTimeout).setConnectionRequestTimeout(connTimeout).build();
		
		post.setConfig(requestConfig);
		
		//请求结果
		CloseableHttpResponse response=null;
		HttpEntity resultEntity=null;
		//添加头
		addRequestHeader(post);
		post.setEntity(postEntity);
		
		try {
			//发起请求
			response=client.execute(host,post);
			
			if (response.getStatusLine().getStatusCode()!=200) {
				throw new Exception(getString(R.string.c_msg_http_request_error)+":"+response.getStatusLine().getReasonPhrase());
			}
			
			resultEntity=response.getEntity();
			
			extractHeader(response,responseHeaderReceiver);
		} catch (Exception e) {
			// TODO: handle exception
			//Log.e(e.getMessage(), e.getStackTrace().toString());
			if ( (e instanceof InterruptedException) || (e instanceof SocketTimeoutException)) {
				throw new HttpException(getString(R.string.c_msg_http_conn_timeout), e);
			}else {
				throw new HttpException(e.getMessage(), e);
			}
		}
		
		post=null;
		client=null;
		response=null;
		return resultEntity;
	}
	
	private HttpEntity sendGetRequest(Parameter responseHeaderReceiver) throws HttpException{
		//请求客户端
		URL urlPath=null;
		try {
			urlPath=new URL(getRequestUrl());
		} catch (Exception e) {
			// TODO: handle exception
			throw new HttpException(e.getMessage(), e);
		}
		//创建要请求的目标服务器对象
		HttpHost host = new HttpHost(urlPath.getHost(), urlPath.getPort(), urlPath.getProtocol());
		
		//请求方法
		StringBuilder path=new StringBuilder(urlPath.getPath());
		if (urlPath.getQuery()!=null) {
			path.append('?').append(urlPath.getQuery());
		}
		HttpGet get = new HttpGet(path.toString());
		nativeRequest =get;
		//请求客户端
		CloseableHttpClient client=HttpClients.createDefault();
		Integer connTimeout=AppConfig.getConfig(AppConfig.CONF_HTTP_CONN_TIMEOUT,  AppConfig.VALUE_HTTP_CONN_TIMEOUT);
		Integer socketTimeout=AppConfig.getConfig(AppConfig.CONF_HTTP_SO_TIMEOUT,  AppConfig.VALUE_HTTP_SO_TIMEOUT);

		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connTimeout).setConnectTimeout(connTimeout).setSocketTimeout(socketTimeout).build();
		
		get.setConfig(requestConfig);
		
		//请求结果
		HttpResponse response=null;
		HttpEntity resultEntity=null;
		//添加头
		addRequestHeader(get);
		try {
			//发起请求
			response=client.execute(host,get);
			
			if (response.getStatusLine().getStatusCode()!=200) {
				throw new Exception(getString(R.string.c_msg_http_request_error)+":"+response.getStatusLine().getReasonPhrase());
			}
			
			resultEntity=response.getEntity();
			extractHeader(response,responseHeaderReceiver);
		} catch (Exception e) {
			// TODO: handle exception
			if ( (e instanceof InterruptedException) || (e instanceof SocketTimeoutException)) {
				throw new HttpException(getString(R.string.c_msg_http_conn_timeout), e);
			}else {
				throw new HttpException(e.getMessage(), e);
			}
		}
		
		client=null;
		response=null;
		return resultEntity;
	}
	
		
	private HttpEntity wrapStringParam() throws HttpException{
		//要发送的参数列表,每个参数对用NameValuePair表示
		LinkedList<NameValuePair> pairs=new LinkedList<NameValuePair>();
		//把所有参数以NameValuePair对象放到参数列表中
		for (Map.Entry<String, LinkedList<? extends Object>> entry : parameter.getParameterEntrys()) {
			for (Object value : entry.getValue()) {
				BasicNameValuePair pair=new BasicNameValuePair(entry.getKey(), String.valueOf(value));
				pairs.add(pair);
			}
		}
		//将参数编码
		try {
			return new UrlEncodedFormEntity(pairs,charset);
		} catch (Exception e) {
			// TODO: handle exception
			throw new HttpException(e.getMessage(), e);
		}
	}
	
	private HttpEntity wrapFileParam() throws HttpException{
		MultipartEntityBuilder entityBuilder=MultipartEntityBuilder.create();
		Charset encodeCharset=Charset.forName(charset);
		entityBuilder.setCharset(encodeCharset);
		try {
			//把所有参数以内容提的形式放到参数实体中
			for (Map.Entry<String, LinkedList<? extends Object>> entry : parameter.getParameterEntrys()) {
				for (Object value : entry.getValue()) {
					if (value instanceof File) {
						File file = (File) value;
						entityBuilder.addBinaryBody(entry.getKey(), file);
					} else {
						entityBuilder.addTextBody(entry.getKey(), String.valueOf(value));
					}
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
			throw new HttpException(e.getMessage(), e);
		}
		
		return entityBuilder.build();
	}
	
	public void cancelRequest(){
		if (nativeRequest!=null) {
			try {
				nativeRequest.abort();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.cancel(true);
	}
	
	
	
	/**
	 * 异步方法,不属于UI线程,在这里执行异步请求
	 */
	@Override
	protected HttpResult doInBackground(Void... params) {
		// TODO Auto-generated method stub
		HttpResult result=null;
		try {
			result=startSynchronousRequest();
		} catch (Exception e) {
			// TODO: handle exception
			result=new HttpResult(null, null,e.getMessage());
		}
		return result;
	}
	
	/**
	 * 返回到UI线程,异步执行已经完毕
	 */
	protected void onPostExecute(HttpResult result) { 
		super.onPostExecute(result);
		//调用接收器的回调方法
		if (getHttpRequestListener()!=null) {
			getHttpRequestListener().onHttpResponse(this, result);
		}
    }
	
	/**
	 * 在执行异步调用前执行,属于UI线程
	 */
	protected void onPreExecute () {
		super.onPreExecute();
		if (getHttpRequestListener()!=null) {
			getHttpRequestListener().beforeHttpRequest(this);
		}
	}
	
	protected void onProgressUpdate (Float... values){
		super.onProgressUpdate(values);
	}
	
	protected void onCancelled(){
		super.onCancelled();
		if (getHttpRequestListener()!=null) {
			getHttpRequestListener().onHttpRequestCancelled(this);
		}
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format(Locale.getDefault(),"http request[url=%s; parameter=%s; header=%s]", getRequestUrl(),parameter,headers);
	}
	
	public interface HttpRequestListener {
		
		/**
		 * 在HTTP请求之前调用,属于UI线程
		 */
		public void beforeHttpRequest(HttpRequest request);
		/**
		 * 当HTTP请求完成后调用,属于UI线程
		 * @param result
		 */
		public void onHttpResponse(HttpRequest request,HttpResult result);
		/**
		 * 在请求取消之后调用，属于UI线程
		 * @param request
		 */
		public void onHttpRequestCancelled(HttpRequest request);
	}
}
