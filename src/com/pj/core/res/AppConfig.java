package com.pj.core.res;

import java.io.InputStream;
import java.util.Properties;

import com.pj.core.utilities.ConvertUtility;
import com.pj.core.utilities.StringUtility;


/**
 * 项目配置类,配置可在app.config文件中配置
 * 放在源文件根目录下,没有则使用默认配置
 * @author 陆振文[PENGJU]
 * 2012-11-29 上午11:04:09
 * email: pengju114@163.com
 */
public class AppConfig {
	private static final String CONFIG_FILE_NAME		="app.config";
	
	/** 项目文件目录名 */
	public static final String CONF_APP_DIR				="app_dir";
	/** 项目图片缓存目录名 */
	public static final String CONF_APP_CACHE_DIR		="app_cache_dir";
	/** 项目下载目录名 */
	public static final String CONF_APP_DOWNLOADS_DIR	="app_downloads_dir";
	/** 项目图片缓存时长（单位 MS） */
	public static final String CONF_APP_CACHE_DURATION	="app_cache_duration";
	/** 是否启用日志，如果启用就会在控制台输出日志，建议发布时关闭 */
	public static final String CONF_LOG_ENABLE			="log_enable";
	
	/** 连接网络超时时长设置 */
	public static final String CONF_HTTP_CONN_TIMEOUT	="http_conn_timeout";
	/** 连接网络socket时长设置 */
	public static final String CONF_HTTP_SO_TIMEOUT		="http_so_timeout";
	
	/** 任务管理器线程池大小 */
	public static final String CONF_TASK_SIZE			="task_size";
	/** 任务管理器退出时保存任务状态，（需要实现{@link com.pj.core.managers.TaskManager.AsyncTask#saveState()}方法） */
	public static final String CONF_TASK_SAVE_STATE_WHEN_EXIT="save_state_when_taskmanager_exit";

	/** 当所有活动推出时是否结束APP进程 */
	public static final String CONF_EXIT_APP_ON_ALL_ACTIVITIES_DESTROYED="exit_app_on_all_activities_destroyed";
	
	/** 当退出应用时是否结束所有服务 */
	public static final String CONF_STOP_ALL_SERVICE_ON_EXIT_APP="stop_all_service_on_exit_app";
	
	
	public static final String  VALUE_APP_DIR			="frameworkDefault";
	public static final String  VALUE_CACHE_DIR			="cache";
	public static final String  VALUE_APP_DOWNLOADS_DIR	="downloads";
	public static final boolean VALUE_LOG_ENABLE		=false;
	public static final int 	VALUE_HTTP_CONN_TIMEOUT	=15000;//15 ms
	public static final int 	VALUE_HTTP_SO_TIMEOUT	=15000;//15 ms
	public static final boolean VALUE_EXIT_APP_ON_ALL_ACTIVITIES_DESTROYED=true;
	public static final boolean VALUE_STOP_ALL_SERVICE_ON_EXIT_APP=false;
	public static final int 	VALUE_APP_CACHE_DURATION=43200000;//12小时
	
	public static final int 	VALUE_TASK_SIZE			=3;
	public static final boolean	VALUE_TASK_SAVE_STATE   =true;
	
	private static final Properties configProperties;
	static{
		configProperties=getProperties();
	}
	
	private static Properties getProperties() {
		Properties properties=new Properties();
		
		//初始化
		properties.setProperty(CONF_APP_DIR, VALUE_APP_DIR);
		properties.setProperty(CONF_APP_CACHE_DIR, VALUE_CACHE_DIR);
		properties.setProperty(CONF_LOG_ENABLE, String.valueOf(VALUE_LOG_ENABLE));
		properties.setProperty(CONF_HTTP_CONN_TIMEOUT, String.valueOf(VALUE_HTTP_CONN_TIMEOUT));
		properties.setProperty(CONF_HTTP_SO_TIMEOUT, String.valueOf(VALUE_HTTP_SO_TIMEOUT));
		properties.setProperty(CONF_APP_CACHE_DURATION, String.valueOf(VALUE_APP_CACHE_DURATION));
		properties.setProperty(CONF_APP_DOWNLOADS_DIR, VALUE_APP_DOWNLOADS_DIR);
		
		try {
			InputStream inputStream=AppConfig.class.getResourceAsStream("/"+CONFIG_FILE_NAME);
			if (inputStream!=null) {
				properties.load(inputStream);
				inputStream.close();
			}else {
				throw new Exception("config file not found");
			}
		} catch (Exception e) {}
		
		return properties;
	}
	
	public static String getConfig(String key) {
		return configProperties.getProperty(key, StringUtility.EMPTY_STRING);
	}
	public static String getConfig(String key,String defaultValue) {
		return configProperties.getProperty(key, defaultValue);
	}
	
	public static int getConfig(String key,int defaultValue) {
		return ConvertUtility.parseInt(configProperties.getProperty(key), defaultValue);
	}
	public static float getConfig(String key,float defaultValue) {
		return ConvertUtility.parseFloat(configProperties.getProperty(key), defaultValue);
	}
	public static long getConfig(String key,long defaultValue) {
		return ConvertUtility.parseLong(configProperties.getProperty(key), defaultValue);
	}
	public static double getConfig(String key,double defaultValue) {
		return ConvertUtility.parseDouble(configProperties.getProperty(key), defaultValue);
	}
	public static boolean getConfig(String key,boolean defaultValue) {
		String prop=configProperties.getProperty(key);
		return prop==null?defaultValue:Boolean.valueOf(prop).booleanValue();
	}
}
