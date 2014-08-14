package com.pj.core.viewholders;


import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;

import com.pj.core.BaseActivity;
import com.pj.core.R;
import com.pj.core.http.HttpRequest;
import com.pj.core.http.HttpRequest.HttpRequestListener;
import com.pj.core.http.HttpResult;
import com.pj.core.utilities.AppUtility;
import com.pj.core.utilities.StringUtility;

/**
 * 在网络调用中 activity在stop时必须通知 HttpViewHolder
 * 否则在stop时没停止网络请求会引起崩溃
 * @author 陆振文[PENGJU]
 * 2012-10-22 下午4:36:51
 * email: pengju114@163.com
 */
public abstract class HttpViewHolder extends ViewHolder implements HttpRequestListener{
	
	private SparseArray<HttpRequest> requestArray;
	private int requestCount=0;

	public HttpViewHolder(BaseActivity activity) {
		super(activity);
		// TODO Auto-generated constructor stub
		requestArray=new SparseArray<HttpRequest>();
	}
	
	public HttpViewHolder(BaseActivity activity,View initView) {
		super(activity,initView);
		// TODO Auto-generated constructor stub
		requestArray=new SparseArray<HttpRequest>();
	}
	
	public HttpViewHolder(ViewHolder mParent) {
		// TODO Auto-generated constructor stub
		super(mParent);
		requestArray=new SparseArray<HttpRequest>();
	}
	public HttpViewHolder(ViewHolder mParent,View root){
		super(mParent, root);
		requestArray=new SparseArray<HttpRequest>();
	}
	
	@Override
	protected void onActivityStateChange(int state, Bundle bundle) {
		// TODO Auto-generated method stub
		super.onActivityStateChange(state, bundle);
		if (state==ACTIVITY_STOP) {
			stopAllAsyncRequest();
		}
	}
	
	/**
	 * 停止所有网络访问
	 * PENGJU
	 * 2012-10-23 上午10:33:49
	 */
	protected void stopAllAsyncRequest(){
		for (int i=0;i<requestArray.size();i++) {
			HttpRequest req=requestArray.valueAt(i);
			if (req!=null && !req.isCancelled()) {
				req.cancelRequest();
			}
		}
		requestArray.clear();
		requestCount=0;
	}


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
	/**
	 * 网络不可用时调用
	 * PENGJU
	 * 2012-11-29 下午1:49:09
	 */
	protected void onNetworkNotAvailable() {
		getActivity().showTip(R.string.c_msg_network_not_available);
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
			onHttpFail(result==null?getActivity().defaultResources().getString(R.string.c_msg_http_request_fail):result.getStatusText());
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
		getActivity().showTip(msg);
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
	
	protected void requestStateChange(int reqCount){
		if (reqCount>0) {
			getActivity().showExecutingDialog(0, null, null);
		}else {
			getActivity().closeExecutingDialog();
		}
	}
	
	@Override
	public void onViewWillDisappear(boolean animated) {
		// TODO Auto-generated method stub
		stopAllAsyncRequest();
		super.onViewWillDisappear(animated);
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
