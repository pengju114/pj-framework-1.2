package com.pj.core.utilities;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.pj.core.BaseApplication;
import com.pj.core.annotation.MethodIdentifier;
import com.pj.core.managers.LogManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class AppUtility {
	private static ConnectivityManager manager;
	
	private static HashMap<String, String> MIME_MAP;
	
	public static int getAppVersionCode(){
		try {
			Context ctx=BaseApplication.getInstance();
			return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return -1;
	}
	
	public static String getAppVersionName(){
		try {
			Context ctx=BaseApplication.getInstance();
			return ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return StringUtility.EMPTY_STRING;
	}
	
	public static boolean isNetworkAvailable() {
		if (manager==null) {
			manager=(ConnectivityManager) BaseApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		NetworkInfo networkInfo=manager.getActiveNetworkInfo();
		if (networkInfo==null || !networkInfo.isAvailable()) {
			return false;
		}
		return true;
	}
	
	public static boolean hideInputSoft(View trigger) {
		InputMethodManager inputMethodManager=(InputMethodManager) trigger.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		return inputMethodManager.hideSoftInputFromWindow(trigger.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	public static boolean showInputSoft(View trigger) {
		InputMethodManager inputMethodManager=(InputMethodManager) trigger.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		return inputMethodManager.showSoftInput(trigger, InputMethodManager.SHOW_FORCED);
	}
	
	
	@SuppressWarnings("unchecked")
	public static String getFileMimeType(File file) {
		String mimeString = "application/octet-stream";
		String ext = file.getName();
		int dotIdx=ext.lastIndexOf(".");
		if (dotIdx>-1) {
			ext = ext.substring(dotIdx + 1);
			if (MIME_MAP==null) {
				synchronized (AppUtility.class) {
					if (MIME_MAP==null) {
						try {
							InputStream orgnStream=AppUtility.class.getResourceAsStream("mime.map");
							ObjectInputStream oi=new ObjectInputStream(orgnStream);
							MIME_MAP=(HashMap<String, String>) oi.readObject();
							oi.close();
						} catch (Exception e) {
							LogManager.trace(e);
						}
					}
				}
			}
			if (MIME_MAP!=null) {
				mimeString = MIME_MAP.get(ext.toLowerCase());
			}
		}
		
		return StringUtility.ensure(mimeString);
	}
	
	/**
	 * 获取所有安装的安装包
	 * PENGJU
	 * 2012-11-22 上午10:25:13
	 * @return
	 */
	public static List<PackageInfo> getInstalledPackages(){
		return getInstalledPackages(null);
	}
	
	/**
	 * 获取所有安装的安装包,并且包前缀以指定前缀开头的
	 * PENGJU
	 * 2012-11-22 上午10:25:55
	 * @param pkgPrefix
	 * @return
	 */
	public static List<PackageInfo> getInstalledPackages(String pkgPrefix){
		PackageManager packageManager=BaseApplication.getInstance().getPackageManager();
		List<PackageInfo> packageInfos=packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		
		List<PackageInfo> rs=null;
		
		if (StringUtility.isEmpty(pkgPrefix)) {
			rs=packageInfos;
		}else {
			rs=new LinkedList<PackageInfo>();
			for (PackageInfo packageInfo : packageInfos) {
				if (!StringUtility.isEmpty(packageInfo.packageName) && packageInfo.packageName.startsWith(pkgPrefix)) {
					rs.add(packageInfo);
				}
			}
		}
		
		return rs;
	}
	
	/**
	 * 获取所有系统安装的安装包,并且包前缀以指定前缀开头的
	 * PENGJU
	 * 2012-11-22 上午10:25:55
	 * @param pkgPrefix
	 * @return
	 */
	public static List<PackageInfo> getSystemPackages(String pkgPrefix){
		List<PackageInfo> packageInfos=getInstalledPackages();
		
		List<PackageInfo> rs=new LinkedList<PackageInfo>();
		for (PackageInfo packageInfo : packageInfos) {
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
				if (StringUtility.isEmpty(pkgPrefix)) {
					rs.add(packageInfo);
				}else if (packageInfo.packageName.startsWith(pkgPrefix)) {
					rs.add(packageInfo);
				}
			}
		}
		
		return rs;
	}
	/**
	 * 获取所有系统安装的安装包
	 * PENGJU
	 * 2012-11-22 上午10:25:55
	 * @param pkgPrefix
	 * @return
	 */
	public static List<PackageInfo> getSystemPackages(){
		return getSystemPackages(null);
	}
	
	/**
	 * 获取所有非系统安装的安装包,并且包前缀以指定前缀开头的
	 * PENGJU
	 * 2012-11-22 上午10:25:55
	 * @param pkgPrefix
	 * @return
	 */
	public static List<PackageInfo> getNonSystemPackages(String pkgPrefix){
		List<PackageInfo> packageInfos=getInstalledPackages();
		
		List<PackageInfo> rs=new LinkedList<PackageInfo>();
		for (PackageInfo packageInfo : packageInfos) {
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				if (StringUtility.isEmpty(pkgPrefix)) {
					rs.add(packageInfo);
				}else if (packageInfo.packageName.startsWith(pkgPrefix)) {
					rs.add(packageInfo);
				}
			}
		}
		
		return rs;
	}
	
	/**
	 * 获取所有非系统安装的安装包
	 * PENGJU
	 * 2012-11-22 上午10:25:55
	 * @param pkgPrefix
	 * @return
	 */
	public static List<PackageInfo> getNonSystemPackages(){
		return getNonSystemPackages(null);
	}
	
	
	/**
	 * 非阻塞震动,需要震动权限 android.permission.VIBRATE
	 * PENGJU
	 * 2012-11-19 上午10:10:13
	 * @param msec 震动时长 毫秒
	 */
	public static void vibrate(long msec){
		Vibrator vibrator=(Vibrator) BaseApplication.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(msec);
	}
	/**
	 * 非阻塞震动,需要震动权限 android.permission.VIBRATE
	 * PENGJU
	 * 2012-11-19 上午10:15:41
	 * @param pattern 震动格式 [wait,on,wait,on...] 第一个值为等待毫秒数，第二个为震动时长...以此类推
	 * @param repeat -1不重复，非-1为从pattern的指定下标开始重复
	 */
	public static void vibrate(long[] pattern,boolean repeat){
		Vibrator vibrator=(Vibrator) BaseApplication.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(pattern,repeat?0:-1);
	}
	
	/**
	 * 删除文件,如果是文件夹就迭代删除全部
	 * PENGJU
	 * 2012-12-11 下午1:08:56
	 * @param dir
	 */
	public static void iterateDelete(File dir) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            File[] fs=dir.listFiles();
            if (fs!=null) {
                for (File file : fs) {
                    if (file.isFile()) {
                            file.delete();
                    }else {
                            iterateDelete(file);
                    }
                }
            }
        }
        dir.delete();
    }
	
	
	public static void installApk(File apkFile){
		if (apkFile!=null && apkFile.exists()) {
			Intent intent = new Intent(Intent.ACTION_VIEW);  
			intent.setDataAndType(Uri.fromFile(apkFile),"application/vnd.android.package-archive");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			BaseApplication.getInstance().startActivity(intent);
		}
	}
	
	public static void uninstallApk(String packageName) {
		if (!StringUtility.isEmpty(packageName)) {
			String prefix="package:";
			if (!packageName.startsWith(prefix)) {
				packageName=prefix+packageName;
			}
			try {
				Uri uri=Uri.parse(packageName);
				Intent uninstallIntent=new Intent(Intent.ACTION_DELETE, uri);
				uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				BaseApplication.getInstance().startActivity(uninstallIntent);
			} catch (Exception e) {
				LogManager.trace(e);
			}
		}
	}
	
	public static String getPhoneBrand() {
		return Build.BRAND;
	}
	public static String getPhoneModel() {
		return Build.MODEL;
	}
	public static String getDeviceName() {
		return Build.DEVICE;
	}
	public static String getDeviceId() {
		return Build.ID;
	}
	public static String getPhoneManufacturer() {
		return Build.MANUFACTURER;
	}
	
	public static String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}
	public static int getAndroidSDKVersion() {
		return Build.VERSION.SDK_INT;
	}
	
	/**
     * 获取网络连接类型
     * @return 值为<br>
     * {@link ConnectivityManager#TYPE_MOBILE}(2G/3G)<br>
     * {@link ConnectivityManager#TYPE_WIFI}(WIFI)<br>
     * {@link ConnectivityManager#TYPE_WIMAX}<br> 
     * {@link ConnectivityManager#TYPE_ETHERNET}<br>
     * {@link ConnectivityManager#TYPE_BLUETOOTH}<br>
     * 更所类型在{@link ConnectivityManager}中定义<br>
     * -1 表示无网络连接
     */
	public static int getNetworkType(){
		if (manager==null) {
			manager=(ConnectivityManager) BaseApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		NetworkInfo info=manager.getActiveNetworkInfo();
		if (info!=null && info.isAvailable()) {
			return info.getType();
		}
		
		return -1;
	}
	
	/**
	 * 调用指定对象或类的方法
	 * lzw
	 * 2014年5月29日 下午10:47:27
	 * @param target     目标对象或类
	 * @param name		 方法名字
	 * @param arguments  调用方法时的参数值，null表示无参数方法，值的类型一定要对应上方法参数类型
	 * @return			 执行方法后返回的值，方法无返回则返回null
	 * @throws Exception
	 */
	public static Object invokeMethod(Object target,String name,Object...arguments) throws Exception{
		
	
		Method method = findMethod(target, name, arguments);
		Object val    = null;
		
		if (method!=null) {
			val = method.invoke(target, arguments);
		}else {
			throw new UnsupportedOperationException("method \""+name+"\" with "+(arguments==null?0:arguments.length)+" argument(s) not found!");
		}
		
		
		return val;
	}
	
	
	@SuppressWarnings({ "rawtypes" })
	public static Method findMethod(Object target,String name,Object... arguments) {
		
		Class[] argumentTypes = null;
		if (arguments!=null) {
			argumentTypes = new Class[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				argumentTypes[i] = arguments[i]==null?null:arguments[i].getClass();
			}
		}
		
		return findMethod(target, name, argumentTypes, arguments);
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Method findMethod(Object target,String name,Class[] argumentTypes, Object... arguments){
		
		Class clazz = (target instanceof Class)?(Class)target:target.getClass();
		
		Method method = null;
		
		try {
			method = clazz.getDeclaredMethod(name, argumentTypes);
		} catch (Exception e) {
			// TODO: handle exception
			try {
				method = clazz.getMethod(name, argumentTypes);
			} catch (Exception e2) {
				// TODO: handle exception
				method = filterMethod(clazz.getDeclaredMethods(), name, argumentTypes);
				if (method==null) {
					method = filterMethod(clazz.getMethods(), name, argumentTypes);
				}
			}
		}
		
		if (method!=null) {
			method.setAccessible(true);
		}
		
		return method;
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Method filterMethod(Method[] mds,String name ,Class[]argumentTypes){
		int targetArgumentLength = argumentTypes==null?0:argumentTypes.length;
		
		Method target = null;
		
		for (int i = 0; i < mds.length; i++) {
			Method tmp = mds[i];
			Class[] argTypes = tmp.getParameterTypes();
			int argLen = argTypes==null?0:argTypes.length;
			if (argLen==targetArgumentLength && tmp.getName().equals(name)) {
				if (argLen==0) {
					target = tmp;
					break;
				}else {
					int c = 0;
					for (int j = 0; j < argLen; j++) {
						if (argumentTypes[j]==null || argTypes[j].isAssignableFrom(argumentTypes[j])) {
							c++;
						}
					}
					if (c==argLen) {
						target = tmp;
						break;
					}
				}
			}
		}
		
		return target;
	}
	
	
	@SuppressWarnings({ "rawtypes" })
	public static Method findMethodById(Object target,int methodId,Object... arguments) {
		
		Class clazz = (target instanceof Class)?(Class)target:target.getClass();
		
		Class[] argumentTypes = null;
		if (arguments!=null) {
			argumentTypes = new Class[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				argumentTypes[i] = arguments[i]==null?null:arguments[i].getClass();
			}
		}
		
		Method method = null;
		
		method = filterMethodById(clazz.getDeclaredMethods(), methodId, argumentTypes);
		if (method==null) {
			method = filterMethodById(clazz.getMethods(), methodId, argumentTypes);
		}
		
		if (method!=null) {
			method.setAccessible(true);
		}
		
		return method;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Method filterMethodById(Method[] mds,int methodId ,Class[]argumentTypes){
		int targetArgumentLength = argumentTypes==null?0:argumentTypes.length;
		
		Method target = null;
		
		for (int i = 0; i < mds.length; i++) {
			Method tmp = mds[i];
			Class[] argTypes = tmp.getParameterTypes();
			int argLen = argTypes==null?0:argTypes.length;
			if (argLen==targetArgumentLength && tmp.isAnnotationPresent(MethodIdentifier.class)) {
				int id = tmp.getAnnotation(MethodIdentifier.class).methodId();
				
				if (methodId != id) {
					continue;
				}
				
				if (argLen==0) {
					target = tmp;
					break;
				}else {
					int c = 0;
					for (int j = 0; j < argLen; j++) {
						if (argumentTypes[j]==null || argTypes[j].isAssignableFrom(argumentTypes[j])) {
							c++;
						}
					}
					if (c==argLen) {
						target = tmp;
						break;
					}
				}
			}
		}
		
		return target;
	}
	
	
	/**
	 * 调用指定对象或类的方法
	 * lzw
	 * 2014年5月29日 下午10:47:27
	 * @param target     目标对象或类
	 * @param methodId	 方法ID
	 * @param arguments  调用方法时的参数值，null表示无参数方法，值的类型一定要对应上方法参数类型
	 * @return			 执行方法后返回的值，方法无返回则返回null
	 * @throws Exception
	 */
	public static Object invokeMethodById(Object target,int methodId,Object...arguments) throws Exception{
		
	
		Method method = findMethodById(target, methodId, arguments);
		Object val    = null;
		
		if (method!=null) {
			val = method.invoke(target, arguments);
		}else {
			throw new UnsupportedOperationException("method which id is \""+methodId+"\" with "+(arguments==null?0:arguments.length)+" argument(s) not found!");
		}
		
		
		return val;
	}
}
