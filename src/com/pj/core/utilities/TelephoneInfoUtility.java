package com.pj.core.utilities;

import com.pj.core.BaseApplication;

import android.content.Context;
import android.telephony.TelephonyManager;

public class TelephoneInfoUtility {

	/**
	 * 获取本机号码
	 */
	public static String getPhoneNumber(){
		TelephonyManager manager=(TelephonyManager) BaseApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getLine1Number();
	}
	
	/**
	 * 获取提供商名称,获取失败返回null
	 * 需要加入权限<uses-permission android:name="android.permission.READ_PHONE_STATE"/> 
	 * @return
	 */
	public static String getProviderName() {
		String name=null;
		TelephonyManager manager=(TelephonyManager) BaseApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		String imsi=manager.getSubscriberId();
		//IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。
		if (!StringUtility.isEmpty(imsi)) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
				name="中国移动";
			}else if (imsi.startsWith("46001")) {
				name="中国联通";
			}else if (imsi.startsWith("46003")) {
				name="中国电信";
			}
		}
		return name;
	}
	
	public static String getProviderId() {
		TelephonyManager manager=(TelephonyManager) BaseApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		return manager.getSubscriberId();
	}
	
	
}
