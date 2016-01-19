package com.pj.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.pj.core.NotificationCenter.NotificationListener;
import com.pj.core.http.HttpImage;
import com.pj.core.managers.LogManager;
import com.pj.core.managers.TaskManager;
import com.pj.core.res.AppConfig;
import com.pj.core.res.Constants;
import com.pj.core.services.BaseService;
import com.pj.core.utilities.AppUtility;
import com.pj.core.utilities.StringUtility;
import com.pj.core.utilities.ThreadUtility;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
/**
 * 应用的基类，不建议直接修改，可以继承扩展
 * framework
 * @author 陆振文[pengju]
 * 2012-10-17 下午5:48:19
 */
public class BaseApplication extends Application {
	/**
	 * 网络状态变化通知，发送者将会是 {@link BaseApplication},附带的数据是网络类型(-1表示连接不可用)
	 */
	public static final int NOTIFICATION_NETWORK_STATE_CHANGE    = 0xCFBC0001;
	
	
	/**
	 * 当APP回到前台时发送此通知(当前活动的APP，即当前显示的是此APP的一个活动，仅发送一回)
	 */
	public static final int NOTIFICATION_APPLICATION_ON_ACTIVE   = 0xFF00ED01;
	/**
	 * 当APP退回后台时发送此通知(APP由活动状态变为非活动状态，即此APP的所有活动都已经处于stop状态了)
	 */
	public static final int NOTIFICATION_APPLICATION_ON_INACTIVE = 0xFF00ED02;
	
	
	
	/** 网络状态变化监听器 */
	private static final BroadcastReceiver NETWORK_STATE_RECEIVER=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				LogManager.i(application.getClass().getSimpleName(), "network state change,type=%d", AppUtility.getNetworkType());
				NotificationCenter.getDefaultCenter().sendNotification(application, NOTIFICATION_NETWORK_STATE_CHANGE, AppUtility.getNetworkType());
			}
		}
	};
	
	
	private int activeActivityCount = 0;
	private NotificationListener activityStateNotificationListener = new NotificationListener() {
		
		
		
		@Override
		public void onReceivedNotification(Object sender, int notificationId, Object data) {
			// TODO Auto-generated method stub
			if (sender instanceof BaseActivity) {
				if (notificationId == BaseActivity.NOTIFICATION_ACTIVITY_START) {
					activeActivityCount++;
					if (activeActivityCount == 1) {// 第一个活动可见即认为APP被激活
						NotificationCenter.getDefaultCenter().sendNotification(application, NOTIFICATION_APPLICATION_ON_ACTIVE, null);
					}
				}else if (notificationId == BaseActivity.NOTIFICATION_ACTIVITY_STOP) {
					activeActivityCount--;
					if (activeActivityCount == 0) {// 没有活动状态的活动了
						NotificationCenter.getDefaultCenter().sendNotification(application, NOTIFICATION_APPLICATION_ON_INACTIVE, null);
					}
				}
			}
		}
	};
	
	
	
	private static BaseApplication   application;
	private TaskManager    			 taskManager;
	private Map<Object, Object> 	 session;
	private Map<Activity, Object> 	 activitiesMap;
	private Map<BaseService, Object> serviceMap;
	private final int 				 detectDelay=500;
	
	private AsyncExecutor<Boolean> exitExecutor=new AsyncExecutor<Boolean>() {
		
		public void executePrepare(){}
		
		@Override
		public void executeComplete(Boolean value) {
			// TODO Auto-generated method stub
			if (value.booleanValue() && activitiesMap.size()<1) {
				exit();
				System.exit(0);
			}
		}
		@Override
		public Boolean execute() {
			// TODO Auto-generated method stub
			if (activitiesMap.size()<1) {
				//调用system.exit不会调用onTerminate,自动调用
				clearCache();//耗时操作，在线程里执行;
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}

		@Override
		public boolean isExecuteCancel() {
			// TODO Auto-generated method stub
			return false;
		}
		
	};
	
	
	public boolean isApplicationActive(){
		return activeActivityCount>0;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends BaseApplication> T getInstance() {
		return (T) application;
	}
	
	/**
	 * 获取任务管理器
	 * @param create 不存在是否创建
	 * @return
	 */
	public TaskManager getTaskManager(boolean create){
		if (taskManager==null && create) {
			taskManager=new TaskManager(AppConfig.getConfig(AppConfig.CONF_TASK_SIZE, AppConfig.VALUE_TASK_SIZE));
		}
		return taskManager;
	}
	
	
	/**
	 * 启动应用程序
	 */
	public void onCreate() {
		super.onCreate();
		application=this;
		session=new HashMap<Object, Object>();
		activitiesMap=new HashMap<Activity, Object>();
		serviceMap=new HashMap<BaseService, Object>();
		init();
		registerNetworkStateReciever();
		
		NotificationCenter.getDefaultCenter().addNotificationListener(activityStateNotificationListener,null, BaseActivity.NOTIFICATION_ACTIVITY_START,BaseActivity.NOTIFICATION_ACTIVITY_STOP);
	}

	private void registerNetworkStateReciever(){
		IntentFilter intentFilter=new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		try {
			registerReceiver(NETWORK_STATE_RECEIVER, intentFilter);
		} catch (Exception e) {
		}
	}
	private void unregisterNetworkStateReciever(){
		try {
			unregisterReceiver(NETWORK_STATE_RECEIVER);
		} catch (Exception e) {
		}
	}
	
	private String init() {
		return genSessionId();
	}
	
	private String genSessionId() {
		//生成一个会话ID
		String uuid=java.util.UUID.randomUUID().toString();
		uuid=uuid.replaceAll("\\-+", StringUtility.EMPTY_STRING);
		addSessionData(Constants.Keys.SESSION_ID, uuid);
		return uuid;
	}
	
	public String getSessionId() {
		String sid=(String) getSessionData(Constants.Keys.SESSION_ID);
		if (StringUtility.isEmpty(sid)) {//会话过期
			return init();
		}
		return sid;
	}
	
	public void setSessionId(String sessionId) {
		addSessionData(Constants.Keys.SESSION_ID, sessionId);
	}
	
	
	/**
	 * 终止应用时调用,但如果是被内核终止则不调用
	 */
	public void onTerminate() {
		clearCache();
		exit();
		super.onTerminate();
	}
	
	private void clearCache(){
		clearNotSDCardCache();
		clearSDCardCache();
	}
	
	/**
	 * 内存不足时调用
	 */
	public void onLowMemory() {
		super.onLowMemory();
	}
	
	/**
	 * 清空内部缓存
	 * PENGJU
	 * 2012-12-11 下午12:01:05
	 */
	protected void clearNotSDCardCache() {
		String dirName=AppConfig.getConfig(AppConfig.CONF_APP_DIR, AppConfig.VALUE_APP_DIR);
		File dir=new File(getCacheDir(), dirName);
		AppUtility.iterateDelete(dir);
	}
	
	protected void clearSDCardCache() {
		int duration=AppConfig.getConfig(AppConfig.CONF_APP_CACHE_DURATION, AppConfig.VALUE_APP_CACHE_DURATION);
		HttpImage.FileCache.clearTmpFile(duration);
	}
	
	public Object getSessionData(Object key) {
		return session.get(key);
	}
	public void addSessionData(Object key,Object value) {
		session.put(key, value);
	}
	public void removeSessionData(Object key) {
		session.remove(key);
	}
	
	public void addActivity(Activity activity) {
		activitiesMap.put(activity, null);
	}
	public void removeActivity(Activity activity) {
		activitiesMap.remove(activity);
		boolean exit=AppConfig.getConfig(AppConfig.CONF_EXIT_APP_ON_ALL_ACTIVITIES_DESTROYED,AppConfig.VALUE_EXIT_APP_ON_ALL_ACTIVITIES_DESTROYED);
		
		if (exit && activitiesMap.size()<1) {
			ThreadUtility.execute(exitExecutor,detectDelay);
		}
	}
	
	
	
	
	
	public void addService(BaseService service) {
		serviceMap.put(service, null);
	}
	public void removeService(BaseService service) {
		serviceMap.remove(service);
	}
	public BaseService findService (Class<? extends BaseService> clazz) {
		for (BaseService service : serviceMap.keySet()) {
			if (service.getClass().equals(clazz)) {
				return service;
			}
		}
		return null;
	}
	
	public static SharedPreferences getSharedPreferences(){
		Context context=getInstance().getApplicationContext();
		SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(context);
		return preferences;
	}
	
	public static void exit() {
		//shutdown service first
		getInstance().onExit();
		
		if (getInstance().taskManager!=null) {
			boolean save=AppConfig.getConfig(AppConfig.CONF_TASK_SAVE_STATE_WHEN_EXIT, AppConfig.VALUE_TASK_SAVE_STATE);
			getInstance().taskManager.stop(save);
			getInstance().taskManager.exit();
		}
		
		boolean shouldStopService=AppConfig.getConfig(AppConfig.CONF_STOP_ALL_SERVICE_ON_EXIT_APP, AppConfig.VALUE_STOP_ALL_SERVICE_ON_EXIT_APP);
		if (shouldStopService) {
			for (BaseService s : getInstance().serviceMap.keySet()) {
				try {
					s.stopSelf();
				} catch (Exception e) {LogManager.trace(e);}
			}
		}
		
		//close all activities
		for (Activity activity : getInstance().activitiesMap.keySet()) {
			if (activity!=null && !activity.isFinishing()) {
				activity.finish();
			}
		}
		
		getInstance().serviceMap.clear();
		getInstance().activitiesMap.clear();
		getInstance().session.clear();
		
		application.unregisterNetworkStateReciever();
		
		ThreadUtility.release();
		
		NotificationCenter.getDefaultCenter().removeNotificationListener(application.activityStateNotificationListener);
	}
	
	protected void onExit(){
		
	}
	
	
	/**
	 * 保存一个短状态对象，该对象被访问一次就会被移除
	 * lzw
	 * 2014年3月6日 下午11:59:07
	 * @param key
	 * @param object
	 */
	public void putTransientObject(int key,Object object){
		session.put(String.valueOf(key), object);
	}
	
	/**
	 * 获取一个短状态对象，该对象被访问一次就会被移除
	 * lzw
	 * 2014年3月6日 下午11:59:07
	 * @param key
	 * @param object
	 */
	public Object getTransientObject(int key){
		return session.remove(String.valueOf(key));
	}
}
