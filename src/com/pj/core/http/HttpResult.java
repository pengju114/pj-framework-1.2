package com.pj.core.http;

import java.io.Serializable;
import java.util.List;

import com.pj.core.datamodel.DataWrapper;
import com.pj.core.utilities.ConvertUtility;
import com.pj.core.utilities.StringUtility;


/**
 * HTTP请求结果类
 * @author 陆振文[PENGJU]
 * 2012-8-8 下午1:32:35
 * email:pengju114@163.com
 */
public class HttpResult implements Serializable{
	private static final long serialVersionUID = 7257126864687851074L;
	/**
	 * 请求成功状态码
	 */
	public static final int HTTP_OK					=0; 
	public static final int HTTP_NOT_LOGIN			=-1;
	public static final int HTTP_ERROR				=-9;
	
	private static final String KEY_HEADER		    ="header";
	private static final String KEY_RESULT		    ="result";
	
	
	private static final String KEY_STATUS_CODE		  ="statusCode";
	private static final String KEY_STATUS_TEXT		  ="statusText";
	private static final String KEY_TOTAL_RESULT_COUNT="totalResults";
	private static final String KEY_CURRENT_PAGE	  ="pageNumber";
	private static final String KEY_PAGE_COUNT		  ="pageCount";
	
	private Object	  responseData;
	private Parameter responseHeader;
	
	int 			statusCode;
	String  		statusText;	
	
	
	public HttpResult(Object respData,Parameter responseHeader){
		this(respData, responseHeader, null);
	}
	
	public HttpResult(Object respData,Parameter responseHeader,String statusText){
		this.responseData=respData;
		this.responseHeader=responseHeader;
		this.statusText=statusText;
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		statusCode=HTTP_ERROR;
		if (responseData!=null && (responseData instanceof DataWrapper)) {
			
			DataWrapper wrapper=(DataWrapper) responseData;
			DataWrapper header = wrapper.getObjectAndIgnoreList(KEY_HEADER);
			if (header!=null) {
				statusCode=ConvertUtility.parseInt(header.getString(KEY_STATUS_CODE), statusCode);
				if (!StringUtility.isEmpty(header.getString(KEY_STATUS_TEXT))) {
					statusText=header.getString(KEY_STATUS_TEXT);
				}
			}
		}
	}
	
	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusText() {
		return statusText;
	}

	/**
	 * 获取服务器返回的数据
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getResponseData() {
		try {
			return (T)responseData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<DataWrapper> getDataList(){
		if (responseData!=null && (responseData instanceof DataWrapper)) {
			DataWrapper wrapper=(DataWrapper) responseData;
			return wrapper.getList(KEY_RESULT);
		}
		return null;
	}
	
	public Parameter getResponseHeader() {
		return responseHeader;
	}
	
	public int getPageCount() {
		return getIntValue(KEY_PAGE_COUNT);
	}
	public int getCurrentPage() {
		return getIntValue(KEY_CURRENT_PAGE);
	}
	public int getTotalResultsCount() {
		return getIntValue(KEY_TOTAL_RESULT_COUNT);
	}
	
	private int getIntValue(String key){
		if (responseData!=null && (responseData instanceof DataWrapper)) {
			DataWrapper wrapper=(DataWrapper) responseData;
			DataWrapper header = wrapper.getObjectAndIgnoreList(KEY_HEADER);
			return header.getInt(key);
		}
		return -1;
	}
}
