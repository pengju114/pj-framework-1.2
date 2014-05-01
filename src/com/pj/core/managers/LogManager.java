package com.pj.core.managers;


import com.pj.core.res.AppConfig;
import android.util.Log;


/**
 * 日志输出控制器,考虑到性能问题，在发布时应禁用控制台日志输出
 * @author 陆振文[PENGJU]
 * 2012-7-29 上午10:03:57
 */
public class LogManager {
	private static boolean logenable;
	static{
		logenable=AppConfig.getConfig(AppConfig.CONF_LOG_ENABLE, AppConfig.VALUE_LOG_ENABLE);
	}
	
	public static void log(String title,Object val) {
		if (logenable) {
			if (title==null) {
				log(val);
			}else {
				Log.i(title, String.valueOf(val));
			}
		}
	}
	public static void log(Object val) {
		if (logenable) {
			Log.i(LogManager.class.getSimpleName(), String.valueOf(val));
		}
	}
	public static void trace(Throwable e){
		if (logenable) {
			e.printStackTrace();
		}
	}
	public static void trace(String msg,Throwable e){
		if (logenable) {
			Log.e("Error", msg, e);
		}
	}
	
	public static void log(String title,String format,Object... args) {
		if (isLogEnable()) {
			if (title==null) {
				title=LogManager.class.getSimpleName();
			}
			Log.i(title, String.format(format, args));
		}
	}
	
	public static boolean isLogEnable() {
		return logenable;
	}
}
