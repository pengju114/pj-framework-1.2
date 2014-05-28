package com.pj.core.managers;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import com.pj.core.BaseApplication;
import com.pj.core.res.AppConfig;
import android.util.Log;
import android.util.SparseArray;


/**
 * 日志输出控制器,考虑到性能问题，在发布时应禁用控制台日志输出
 * @author 陆振文[PENGJU]
 * 2012-7-29 上午10:03:57
 */
public class LogManager {
	private static boolean logenable;
	
	private static String APP_PACKAGE;
	
	private static String TAG = "LogManager";
	
	static{
		logenable=AppConfig.getConfig(AppConfig.CONF_LOG_ENABLE, AppConfig.VALUE_LOG_ENABLE);
	}
	
	public static void log(int level , String tag , Object message, Throwable e) {
		if (logenable) {
			if (APP_PACKAGE==null) {
				APP_PACKAGE = BaseApplication.getInstance().getPackageName();
			}
			String appTag = String.format("[%s]%s", APP_PACKAGE,tag==null?TAG:tag);
			String msg = String.valueOf(message);
			if (e!=null) {
				msg = msg + "\n" + Log.getStackTraceString(e);
			}
			Log.println(level, appTag, msg);
		}
	}
	
	public static void log(int level , String tag , Throwable e, String format,Object... args) {
		if (logenable) {
			log(level, tag, String.format(format, args), e);
		}
	}
	
	public static void i(Object message){
		i(TAG, message);
	}
	public static void e(Object message,Throwable e){
		e(TAG, message,e);
	}
	public static void w(Object message){
		w(TAG, message);
	}
	public static void v(Object message){
		v(TAG, message);
	}
	public static void d(Object message){
		d(TAG, message);
	}
	
	public static void i(String tag,Object message){
		log(Log.INFO, tag, message, null);
	}
	public static void e(String tag,Object message,Throwable e){
		log(Log.ERROR, tag, message, e);	
	}
	public static void w(String tag,Object message){
		log(Log.WARN, tag, message, null);
	}
	public static void v(String tag,Object message){
		log(Log.VERBOSE, tag, message, null);
	}
	public static void d(String tag,Object message){
		log(Log.DEBUG, tag, message, null);
	}
	
	public static void i(String tag,String format ,Object... args){
		log(Log.INFO, tag, null, format, args);
	}
	public static void e(String tag,Throwable e, String format ,Object... args){
		log(Log.ERROR, tag, e, format, args);
	}
	public static void w(String tag,String format ,Object... args){
		log(Log.WARN, tag, null, format, args);
	}
	public static void v(String tag,String format ,Object... args){
		log(Log.VERBOSE, tag, null, format, args);
	}
	public static void d(String tag,String format ,Object... args){
		log(Log.DEBUG, tag, null, format, args);
	}
	
	public static void trace(Throwable e) {
		log(Log.ERROR, TAG, "", e);
	}

	
	public static boolean isLogEnable() {
		return logenable;
	}
	
	
	
	/**
	 * 读取本应用 日志信息 ，需要 读日志权限
	 * @param baos
	 * @param levels
	 * @return
	 */
	
	public static boolean readApplicationLogs(ByteArrayOutputStream baos,int...levels) {
		//"logcat *:e *:w | grep \"("+mPID+")\""
		if (levels==null) {
			throw new IllegalArgumentException("argument levels should not be null");
		}
		StringBuilder sb = new StringBuilder();
		
		SparseArray<String> pArray = new SparseArray<String>(6);
		pArray.put(Log.DEBUG, "*:D");
		pArray.put(Log.ERROR, "*:E");
		pArray.put(Log.INFO, "*:I");
		pArray.put(Log.VERBOSE, "*:V");
		pArray.put(Log.WARN, "*:W");
		pArray.put(Log.ASSERT, "*:A");
		
		
		for (int i = 0; i < levels.length; i++) {
			String c = pArray.get(levels[i]); 
			if (c!=null) {
				sb.append(c).append(' ');
			}
		}
		
		i(TAG);
		
		String cmd = String.format("logcat -d %s| grep \"\\[%s\\]\"", sb,APP_PACKAGE);
		return readApplicationLogs(baos, cmd);
	}
	
	public static boolean readApplicationLogs(ByteArrayOutputStream baos,String command) {
		if (command==null) {
			throw new IllegalArgumentException("argument command should not be null");
		}
		
		LogManager.i("~~~~~~~获取logcat~~~~~~",command);
		
		try {
			Process p= Runtime.getRuntime().exec(command);
			InputStream is = p.getInputStream();
			byte[] buff = new byte[1024*4];
			int rd = -1;
			while ((rd=is.read(buff))!=-1) {
				baos.write(buff, 0, rd);
			}
			
			try {
				is.close();
				p.destroy();
			} catch (Exception e) {
				// TODO: handle exception
			}
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			trace(e);
		}
		
		return false;
	}
}
