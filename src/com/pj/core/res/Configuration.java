package com.pj.core.res;

import com.pj.core.BaseApplication;
import com.pj.core.utilities.StringUtility;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/**
 * 配置类
 * 每次设置前要begin,设置完要commit
 * @author 陆振文[PENGJU]
 * 2012-8-8 下午3:11:11
 * email:pengju114@163.com
 */
public class Configuration {
	private static SharedPreferences preferences;

	private static Editor editor;

	private synchronized static SharedPreferences getPreferences() {
		if (preferences == null) {
			preferences = BaseApplication.getSharedPreferences();
		}
		return preferences;
	}

	public static final void begin() {
		commit();
		editor = getPreferences().edit();
	}

	public static final void commit() {
		if (editor != null) {
			editor.commit();
			editor = null;
		}
	}

	private static void readyEditor() {
		if (editor == null) {
			begin();
		}
	}

	public static void put(String key, int val) {
		readyEditor();
		editor.putInt(key, val);
	}

	public static void put(String key, boolean val) {
		readyEditor();
		editor.putBoolean(key, val);
	}

	public static void put(String key, float val) {
		readyEditor();
		editor.putFloat(key, val);
	}

	public static void put(String key, long val) {
		readyEditor();
		editor.putLong(key, val);
	}

	public static void put(String key, String val) {
		readyEditor();
		editor.putString(key, val);
	}
	
	/**
	 * default 0
	 * @param key
	 * @return
	 */
	public static int getInt(String key){
		return getInt(key, 0);
	}
	
	/**
	 * default 0
	 * @param key
	 * @return
	 */
	public static float getFloat(String key){
		return getFloat(key, 0);
	}
	
	/**
	 * default false
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(String key){
		return getBoolean(key, false);
	}
	
	/**
	 * default 0
	 * @param key
	 * @return
	 */
	public static long getLong(String key){
		return getLong(key, 0);
	}
	
	/**
	 * default ""
	 * @param key
	 * @return
	 */
	public static String getString(String key){
		return getString(key, StringUtility.EMPTY_STRING);
	}

	/**
	 * default 0
	 * @param key
	 * @return
	 */
	public static int getInt(String key,int def){
		return getPreferences().getInt(key, def);
	}
	
	/**
	 * default 0
	 * @param key
	 * @return
	 */
	public static float getFloat(String key,float def){
		return getPreferences().getFloat(key, def);
	}
	
	/**
	 * default false
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(String key,boolean def){
		return getPreferences().getBoolean(key, def);
	}
	
	/**
	 * default 0
	 * @param key
	 * @return
	 */
	public static long getLong(String key,long def){
		return getPreferences().getLong(key, def);
	}
	
	/**
	 * default ""
	 * @param key
	 * @return
	 */
	public static String getString(String key,String def){
		return getPreferences().getString(key, def);
	}
}
