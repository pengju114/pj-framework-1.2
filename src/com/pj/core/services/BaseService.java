package com.pj.core.services;


import com.pj.core.BaseActivity;
import com.pj.core.BaseApplication;
import com.pj.core.utilities.ThreadUtility;

import android.app.Service;
import android.widget.Toast;

/**
 * 所有服务的基类
 * @author 陆振文[PENGJU]
 * 2012-12-3 下午4:32:10
 * email: pengju114@163.com
 */
public abstract class BaseService extends Service implements com.pj.core.utilities.ThreadUtility.MessageListener{
	private static final int MSG_SHOWTIP=BaseActivity.nextUniqueInt();
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		BaseApplication.getInstance().addService(this);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		BaseApplication.getInstance().removeService(this);
		super.onDestroy();
	}
	
	public void handleMessage(int id,Object data) {
		if (id==MSG_SHOWTIP) {
			Toast.makeText(getBaseContext(), String.valueOf(data), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void postMessage(int msgId, Object data) {
		ThreadUtility.postMessage(msgId, data, this);
	}

	public void postMessage(int msgId, Object data, long delayMillis) {
		ThreadUtility.postMessage(msgId, data, delayMillis, this);
	}
	
	public void showTip(Object msg) {
		postMessage(MSG_SHOWTIP, msg);
	}
	
	public void showTip(int resid) {
		postMessage(MSG_SHOWTIP, getBaseContext().getResources().getString(resid));
	}
}
