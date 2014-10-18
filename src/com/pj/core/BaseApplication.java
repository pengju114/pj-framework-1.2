package com.pj.core;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pj.core.http.HttpImage;
import com.pj.core.managers.LogManager;
import com.pj.core.managers.TaskManager;
import com.pj.core.res.AppConfig;
import com.pj.core.res.Constants;
import com.pj.core.services.BaseService;
import com.pj.core.utilities.AppUtility;
import com.pj.core.utilities.StringUtility;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.SparseArray;
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
	public static final int NOTIFICATION_NETWORK_STATE_CHANGE=8088;
	
	/**
	 * 异步执行准备消息
	 */
	private static final int MSG_HANDLE_ASYNC_PREPARE=BaseActivity.nextUniqueInt();
	/**
	 * 异步执行完成消息
	 */
	private static final int MSG_HANDLE_ASYNC_COMPLETE=BaseActivity.nextUniqueInt();
	
	/** UI线程消息处理器 */
	public static final Handler UI_THREAD_HANDLER = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.obj instanceof HandlerMessage) {
				HandlerMessage handlerMessage=(HandlerMessage) msg.obj;
				if (handlerMessage.listener!=null) {
					handlerMessage.listener.handleMessage(msg.what, handlerMessage.messageData);
				}
			}
		}
	};
	
	/** 网络状态变化监听器 */
	private static final BroadcastReceiver NETWORK_STATE_RECEIVER=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				LogManager.i(application.getClass().getSimpleName(), "network state change,type=%d", AppUtility.getNetworkType());
				application.sendNotification(application, NOTIFICATION_NETWORK_STATE_CHANGE, AppUtility.getNetworkType());
			}
		}
	};
	
	
	/** 异步线程处理器 */
	public static final Handler ASYNC_THREAD_HANDLER;
	private static final SparseArray<AsyncExecutorWrapper<?>> ASYNC_EXECUTOR_ARRAY=new SparseArray<BaseApplication.AsyncExecutorWrapper<?>>();
	static{
		HandlerThread thread=new HandlerThread(BaseApplication.class.getName());
		thread.start();
		ASYNC_THREAD_HANDLER=new Handler(thread.getLooper());
	}
	
	private SparseArray<HashMap<ApplicationNotificationListener,Object>> applicationNotificationListeners;//notificationID->[listener->sender]
	
	private static BaseApplication   application;
	private ExecutorService 		 threadPool;
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
	};
	
	public static BaseApplication getInstance() {
		return application;
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
	
	public ExecutorService getThreadPool() {
		if (threadPool==null) {
			synchronized (this) {
				if (threadPool==null) {
					int size = Runtime.getRuntime().availableProcessors()*2;
					if (size>5) {
						size = 5;
					}
					threadPool = Executors.newFixedThreadPool(size);
				}
			}
		}
		return threadPool;
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
		applicationNotificationListeners=new SparseArray<HashMap<ApplicationNotificationListener,Object>>();
		init();
		registerNetworkStateReciever();
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
			asyncExecute(exitExecutor,detectDelay);
		}
	}
	
	/**
	 * 发送应用程序通知,可在线程里调用
	 * 2013-6-8 下午11:21:56
	 * @param sender 发送者
	 * @param notificationId 通知ID
	 * @param data 通知附带的数据
	 */
	public void sendNotification(Object sender,int notificationId,Object data) {
		sendNotification(sender, notificationId, data, 0);
	}
	/**
	 * 等待指定时间后发送应用程序通知，可在线程里调用
	 * 2013-6-8 下午11:23:17
	 * @param sender  发送者
	 * @param notificationId 通知ID
	 * @param data 通知附带的数据
	 * @param delay 等待的时间
	 */
	public void sendNotification(Object sender,int notificationId,Object data,long delay) {
		ApplicationNotificationMessageHandler handler=new ApplicationNotificationMessageHandler(sender, notificationId, data);
		postMessage(Integer.MIN_VALUE, null, delay, handler);
	}
	
	/**
	 * 删除所有注册的应用程序级的通知监听器
	 * 在一个对象不再需要监听任何通知的时候记得调用此方法
	 * 否则对象会一直保留
	 * 2013-6-8 下午11:26:13
	 * @param listener
	 */
	public void removeNotificationListener(ApplicationNotificationListener listener) {
		synchronized (this) {
			for (int i = 0; i < applicationNotificationListeners.size(); i++) {
				int key=applicationNotificationListeners.keyAt(i);
				applicationNotificationListeners.get(key).remove(listener);
			}
		}
	}
	/**
	 * 删除一个或多个应用程序级的通知监听器
	 * 在一个对象不再需要监听指定类型的通知的时候记得调用此方法
	 * 否则对象会一直保留
	 * 2013-6-8 下午11:26:13
	 * @param listener
	 * @param notificationIds 为null则删除类别为NOTIFICATION_ALL的监听器
	 */
	public void removeNotificationListener(ApplicationNotificationListener listener,int... notificationIds) {
		removeNotificationListener(listener, null, notificationIds);
	}
	
	/**
	 * 删除一个或多个应用程序级的通知监听器,当添加监听器的sender和传进来的sender相同时才会删除，null则删除所有指定notificationId并且添加时sender为null的通知
	 * 在一个对象不再需要监听指定类型的通知的时候记得调用此方法
	 * @param listener
	 * @param sender 
	 * @param notificationIds
	 */
	public void removeNotificationListener(ApplicationNotificationListener listener,Object sender,int... notificationIds) {
		synchronized (this) {
			if (notificationIds==null) {
				notificationIds=new int[]{ApplicationNotificationListener.NOTIFICATION_ALL};
			}
			for (int i : notificationIds) {
				HashMap<ApplicationNotificationListener, Object> ls=applicationNotificationListeners.get(i);
				if (ls!=null) {
					HashSet<ApplicationNotificationListener> keys=new HashSet<ApplicationNotificationListener>();
					keys.addAll(ls.keySet());
					for (ApplicationNotificationListener l : keys) {
						if (ls.get(l)==sender && l==listener) {
							ls.remove(l);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 添加一个类别为监听所有通知的应用程序级的通知监听器
	 * 在一个对象不再需要监听通知的时候记得调用removeNotificationListener
	 * 2013-6-8 下午11:28:05
	 * @param listener
	 */
	public void addNotificationListener(ApplicationNotificationListener listener) {
		addNotificationListener(listener,null, ApplicationNotificationListener.NOTIFICATION_ALL);
	}
	
	/**
	 * 添加一个类别为监听所有由sender发出的通知的应用程序级的通知监听器
	 * 在一个对象不再需要监听通知的时候记得调用removeNotificationListener
	 * @param listener
	 * @param sender 
	 */
	public void addNotificationListener(ApplicationNotificationListener listener,Object sender) {
		addNotificationListener(listener, sender,ApplicationNotificationListener.NOTIFICATION_ALL);
	}
	
	/**
	 * 添加一个监听指定类别的应用程序级别的监听器,notificationIds为null则监听所有通知
	 * @param listener
	 * @param notificationIds
	 */
	public void addNotificationListener(ApplicationNotificationListener listener,int... notificationIds) {
		addNotificationListener(listener, null, notificationIds);
	}
	
	/**
	 * 添加一个监听指定类别的并且通知是由sender发出的应用程序级别的监听器,notificationIds为null则监听所有通知
	 * @param listener
	 * @param sender null则监听所有对象发出的通知
	 * @param notificationIds
	 */
	public void addNotificationListener(ApplicationNotificationListener listener,Object sender,int... notificationIds) {
		synchronized (this) {
			if (notificationIds==null) {
				notificationIds=new int[]{ApplicationNotificationListener.NOTIFICATION_ALL};
			}
			for (int i : notificationIds) {
				HashMap<ApplicationNotificationListener, Object> ls=applicationNotificationListeners.get(i);
				if (ls==null) {
					ls=new HashMap<ApplicationNotificationListener, Object>();
					applicationNotificationListeners.put(i, ls);
				}
				ls.put(listener, sender);
			}
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
		getInstance().applicationNotificationListeners.clear();
		
		application.unregisterNetworkStateReciever();
		
		if (application.threadPool!=null) {
			application.threadPool.shutdownNow();
			application.threadPool = null;
		}
	}
	
	protected void onExit(){
		
	}
	
/**********************线程执行*********************/
	
	/**
	 * 执行异步任务
	 * 任务会放到任务列表等待执行，如果前面还有任务未完成则会继续等待
	 * PENGJU
	 * 2014年3月17日 下午10:21:18
	 * @param executor
	 */
	public <T> void asyncExecute(AsyncExecutor<T> executor){
		AsyncExecutorWrapper<T> action=new AsyncExecutorWrapper<T>(executor);
		ASYNC_EXECUTOR_ARRAY.put(executor.hashCode(), action);
		postMessage(MSG_HANDLE_ASYNC_PREPARE, null, action);
	}
	/**
	 * 延迟delay毫秒后执行异步任务
	 * 任务等待delay毫秒后会放到任务列表等待执行，如果前面还有任务未完成则会继续等待
	 * PENGJU
	 * 2013-1-20 上午10:39:01
	 * @param executor
	 * @param delay
	 */
	public <T> void asyncExecute(AsyncExecutor<T> executor,long delay){
		AsyncExecutorWrapper<T> action=new AsyncExecutorWrapper<T>(executor);
		ASYNC_EXECUTOR_ARRAY.put(executor.hashCode(), action);
		postMessage(MSG_HANDLE_ASYNC_PREPARE, null, delay, action);
	}
	
	/**
	 * 取消队列中一个正在等待执行的异步任务
	 * PENGJU
	 * 2014年3月17日 下午10:21:45
	 * @param executor
	 */
	public void cancelAsyncExecute(AsyncExecutor<?> executor){
		removeMessages(MSG_HANDLE_ASYNC_PREPARE);
		AsyncExecutorWrapper<?> action=ASYNC_EXECUTOR_ARRAY.get(executor.hashCode());
		ASYNC_EXECUTOR_ARRAY.remove(executor.hashCode());
		if (action!=null) {
			ASYNC_THREAD_HANDLER.removeCallbacks(action);
		}
	}
	
	
	
	
	/**
	 * 在线程池中执行指定对象或类的方法
	 * 要执行的方法必须是用注解{@link com.pj.core.annotation.MethodIdentifier}}标明ID的方法
	 * lzw
	 * 2014年5月29日 下午11:06:59
	 * @param target		指定对象或类
	 * @param methodId	    方法ID
	 * @param arguments		参数值，null为无参数
	 */
	public void executeMethodInBackground(Object target,int methodId,Object... arguments) {
		executeMethodInBackground(0, target, methodId, arguments);
	}
	
	/**
	 * 在线程池中执行指定对象或类的方法
	 * 要执行的方法必须是用注解{@link com.pj.core.annotation.MethodIdentifier}}标明ID的方法
	 * lzw
	 * 2014年5月29日 下午11:06:59
	 * @param delay			等待delay毫秒后执行
	 * @param target		指定对象或类
	 * @param methodId	    方法ID
	 * @param arguments		参数值，null为无参数
	 */
	public void executeMethodInBackground(long delay,Object target,int methodId,Object... arguments) {
		ExecuteMethodRunnable emr = new ExecuteMethodRunnable(delay, target, methodId, arguments, false);
		getThreadPool().execute(emr);
	}
	
	/**
	 * 在主线程中执行指定对象或类的方法
	 * 要执行的方法必须是用注解{@link com.pj.core.annotation.MethodIdentifier}}标明ID的方法
	 * lzw
	 * 2014年5月29日 下午11:06:59
	 * @param target		指定对象或类
	 * @param methodId	    方法ID
	 * @param arguments		参数值，null为无参数
	 */
	public void executeMethodInMainThread(Object target,int methodId,Object... arguments) {
		executeMethodInMainThread(0, target, methodId, arguments);
	}
	/**
	 * 在主线程中执行指定对象或类的方法
	 * 要执行的方法必须是用注解{@link com.pj.core.annotation.MethodIdentifier}}标明ID的方法
	 * lzw
	 * 2014年5月29日 下午11:06:59
	 * @param delay			等待delay毫秒后执行
	 * @param target		指定对象或类
	 * @param methodId	    方法ID
	 * @param arguments		参数值，null为无参数
	 */
	public void executeMethodInMainThread(long delay,Object target,int methodId,Object... arguments) {
		ExecuteMethodRunnable emr = new ExecuteMethodRunnable(delay, target, methodId, arguments, true);
		getThreadPool().execute(emr);
	}
	
	/**********************线程执行结束******************/
	
	/**    消息处理部分    **/
	public void postMessage(int msgId, Object data, MessageListener listener) {
		postMessage(msgId, data, 0, listener);
	}
	
	public void postMessage(int msgId, Object data, long delayMillis,MessageListener listener) {
		Message message = obtainHandlerMessage(msgId, data, listener);
		if (delayMillis>0) {
			UI_THREAD_HANDLER.sendMessageDelayed(message, delayMillis);
		}else {
			UI_THREAD_HANDLER.sendMessage(message);
		}
	}
	
	public Message obtainHandlerMessage(int msgId,Object data,MessageListener listener) {
		Message message=new Message();
		HandlerMessage handlerMessage=new HandlerMessage();
		handlerMessage.listener=listener;
		handlerMessage.messageData=data;
		
		message.what=msgId;
		message.obj=handlerMessage;
		
		return message;
	}
	
	public void removeMessages(int msgId) {
		UI_THREAD_HANDLER.removeMessages(msgId);
	}
	
	public class HandlerMessage{
		public MessageListener listener;
		public Object messageData;
	}
	
	/**
	 * 异步任务执行器
	 * framework
	 * @author PENGJU
	 * 2013-1-20 上午10:22:29
	 * email: pengju114@163.com
	 */
	private class AsyncExecutorWrapper<T> implements Runnable,MessageListener{
		
		private AsyncExecutor<T> executor;
		public AsyncExecutorWrapper(AsyncExecutor<T> executor){
			this.executor=executor;
		}

		/**
		 * 回到了主线程
		 */
		
		@Override
		@SuppressWarnings("unchecked")
		public void handleMessage(int messageId, Object data) {
			// TODO Auto-generated method stub
			if (messageId==MSG_HANDLE_ASYNC_PREPARE) {
				executor.executePrepare();
				//执行完准备操作就开始异步任务
				ASYNC_THREAD_HANDLER.post(AsyncExecutorWrapper.this);
			} else if (messageId==MSG_HANDLE_ASYNC_COMPLETE) {
				T v=(T) data;
				executor.executeComplete(v);
			}
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			T v=null;
			try {
				v=executor.execute();
			} catch (Exception e) {
				LogManager.trace(e);
			} finally{
				
				postMessage(MSG_HANDLE_ASYNC_COMPLETE, v, AsyncExecutorWrapper.this);
				ASYNC_EXECUTOR_ARRAY.remove(executor.hashCode());
			}
		}
	}
	
	private class ApplicationNotificationMessageHandler implements MessageListener{
		private Object sender;
		private int    notificationId;
		private Object data;
		
		public ApplicationNotificationMessageHandler(Object sender,int id,Object data){
			this.sender=sender;
			this.notificationId=id;
			this.data=data;
		}
		
		@Override
		public void handleMessage(int messageId, Object _data) {
			// TODO Auto-generated method stub
			synchronized (BaseApplication.this) {
				int[] ids=new int[]{notificationId,ApplicationNotificationListener.NOTIFICATION_ALL};
				for (int i : ids) {
					HashMap<ApplicationNotificationListener, Object> ls=applicationNotificationListeners.get(i);
					if (ls!=null) {
						for (ApplicationNotificationListener listener : ls.keySet()) {
							Object specifySender=ls.get(listener);
							if (specifySender==null) {//如果监听器不指定发送者,则监听所有通知
								listener.onReceivedApplicationNotification(sender, notificationId, data);
							}else if (sender==specifySender) {
								listener.onReceivedApplicationNotification(sender, notificationId, data);
							}
						}
					}
				}
			}
		}
		
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
	
	
	
	private class ExecuteMethodRunnable implements Runnable,MessageListener{
		private long 		delay;
		private Object 		target;
		private int 		method;
		private Object[]    arguments;
		private boolean     executeInMainThread;
		
		public ExecuteMethodRunnable(
				long 		delay,
				Object 		target,
				int 		method,
				Object[]    arguments,
				boolean     executeInMainThread ){
			this.delay 		= delay;
			this.target 	= target;
			this.method		= method;
			this.arguments	= arguments;
			this.executeInMainThread = executeInMainThread;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				if (delay>0) {
					Thread.sleep(delay);
				}
				
				if (executeInMainThread) {
					Method md = AppUtility.findMethodById(target, method, arguments);
					if (md != null) {
						postMessage(0, md, this);
					}else {
						throw new UnsupportedOperationException("method:"+method+" not fount");
					}
				}else {
					AppUtility.invokeMethodById(target, method, arguments);
				}
			} catch (Exception e) {
				// TODO: handle exception
				LogManager.e(BaseApplication.this.getClass().getSimpleName(), "execute method "+method, e);
			}
		}

		@Override
		public void handleMessage(int messageId, Object data) {
			// TODO Auto-generated method stub
			Method md = (Method) data;
			if (md!=null) {
				try {
					md.invoke(target, arguments);
				} catch (Exception e) {
					// TODO: handle exception
					LogManager.trace(e);
				}
			}
		}
	}
}
