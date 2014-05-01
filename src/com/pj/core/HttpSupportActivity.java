package com.pj.core;

import com.pj.core.http.HttpRequest;
import com.pj.core.http.HttpRequest.HttpRequestListener;
import com.pj.core.http.HttpResult;
import com.pj.core.utilities.AppUtility;
import com.pj.core.utilities.StringUtility;

import android.util.SparseArray;

/**
 * 提供Http支持
 * @author 陆振文[PENGJU]
 *
 */
public class HttpSupportActivity extends BaseActivity implements HttpRequestListener{
	private SparseArray<HttpRequest> requestArray=new SparseArray<HttpRequest>();
	private int requestCount=0;
	
	
	/**
	 * 发起网络请求
	 */
	public final void asyncRequest(HttpRequest request) {
		// TODO Auto-generated method stub
		if (AppUtility.isNetworkAvailable()) {
			request.setHttpRequestListener(this);
			requestArray.put(request.getUniqueID(), request);
			request.startAsynchronousRequest();
		}else {
			onNetworkNotAvailable();
		}
	}
	
	
	protected void onNetworkNotAvailable() {
		// TODO Auto-generated method stub
		showTip(R.string.c_msg_network_not_available);
	}
	
	/**
	 * 判断网络请求是否成功返回，失败或发生错误时提示
	 * PENGJU
	 * 2012-10-23 上午10:39:41
	 * @param result
	 * @return
	 */
	public boolean isHttpSuccessAndNotify(HttpResult result){
		boolean ok=false;
		if (result!=null && result.getStatusCode()==HttpResult.HTTP_OK) {
			ok=true;
		}else {
			onHttpFail(result==null?defaultResources().getString(R.string.c_msg_http_request_fail):result.getStatusText());
		}
		
		return ok;
	}
	/**
	 * 网络请求失败时调用
	 * PENGJU
	 * 2012-11-29 下午1:51:36
	 * @param msg 失败原因,可能为null
	 */
	protected void onHttpFail(String msg) {
		showTip(msg);
	}


	@Override
	public void beforeHttpRequest(HttpRequest request) {
		// TODO Auto-generated method stub
		requestStateChange(++requestCount);
	}
	@Override
	public void onHttpResponse(HttpRequest request, HttpResult result) {
		// TODO Auto-generated method stub
		requestStateChange(--requestCount);
		requestArray.remove(request.getUniqueID());
	}
	@Override
	public void onHttpRequestCancelled(HttpRequest request) {
		// TODO Auto-generated method stub
		requestStateChange(--requestCount);
		requestArray.remove(request.getUniqueID());
	}
	
	
	/**
	 * 停止所有网络访问
	 * PENGJU
	 * 2012-10-23 上午10:33:49
	 */
	protected void stopAllAsyncRequest(){
		for (int i=0;i<requestArray.size();i++) {
			HttpRequest loader=requestArray.valueAt(i);
			if (loader!=null && !loader.isCancelled()) {
				loader.cancelRequest();
			}
		}
		requestArray.clear();
		requestCount=0;
	}
	
	protected void requestStateChange(int reqCount){
		if (reqCount>0) {
			showExecutingDialog(-1, null, null);
		}else {
			closeExecutingDialog();
		}
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	
	public HttpRequest makeRequest(int requestCode,String action,Object extraObject,Object... parameterKeyValuePares){
		HttpRequest request=new HttpRequest(getUrlByAction(action), requestCode);
		request.setExtraData(extraObject);
		if (parameterKeyValuePares!=null) {
			int c=parameterKeyValuePares.length/2;
			for (int i = 0; i < c; i++) {
				int startIndex=i*2;
				String key=StringUtility.toString(parameterKeyValuePares[startIndex]);
				Object value=parameterKeyValuePares[startIndex+1];
				if (StringUtility.isEmpty(key) || value==null) {
					continue;
				}
				
				request.addParameter(key, value);
			}
		}
		request.setResponseDataFormat(HttpRequest.FORMAT_XML);
		request.setResponseDataType(HttpRequest.DATA_DATAWRAPPER);
		return request;
	}


	/**
	 * 重写此方法
	 * @param action
	 * @return
	 */
	public String getUrlByAction(String action) {
		// TODO Auto-generated method stub
		return ("www.baidu.com/"+action);
	}
}
