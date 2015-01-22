package com.pj.core.utilities;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import com.pj.core.AsyncExecutor;
import com.pj.core.managers.LogManager;

public class ThreadUtility {
	
	private ThreadUtility(){
		
	}
	
	/**
	 * 异步执行准备消息
	 */
	private static final int MSG_HANDLE_ASYNC_PREPARE = 0xEF00AA01;
	/**
	 * 异步执行完成消息
	 */
	private static final int MSG_HANDLE_ASYNC_COMPLETE = 0xEF00AA02;
	
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
	
	
	/** 异步线程处理器 */
	public static final Handler ASYNC_THREAD_HANDLER;
	
	private static final SparseArray<AsyncExecutorWrapper<?>> ASYNC_EXECUTOR_ARRAY=new SparseArray<AsyncExecutorWrapper<?>>();
	
	static{
		HandlerThread thread=new HandlerThread(ThreadUtility.class.getName());
		thread.start();
		ASYNC_THREAD_HANDLER=new Handler(thread.getLooper());
	}
	
	private static ExecutorService threadPool = null;
	
	
	/**********************线程执行*********************/
	
	/**
	 * 执行异步任务
	 * 任务会放到任务列表等待执行，如果前面还有任务未完成则会继续等待
	 * PENGJU
	 * 2014年3月17日 下午10:21:18
	 * @param executor
	 */
	public static <T> void execute(AsyncExecutor<T> executor){
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
	public static <T> void execute(AsyncExecutor<T> executor,long delay){
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
	public static void cancelExecute(AsyncExecutor<?> executor){
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
	public static void executeMethodInBackground(Object target,int methodId,Object... arguments) {
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
	public static void executeMethodInBackground(long delay,Object target,int methodId,Object... arguments) {
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
	public static void executeMethodInMainThread(Object target,int methodId,Object... arguments) {
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
	public static void executeMethodInMainThread(long delay,Object target,int methodId,Object... arguments) {
		ExecuteMethodRunnable emr = new ExecuteMethodRunnable(delay, target, methodId, arguments, true);
		getThreadPool().execute(emr);
	}
	
	
	
	public static ExecutorService getThreadPool() {
		if (threadPool==null) {
			synchronized (ThreadUtility.class) {
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
	
	
	
	/**********************线程执行结束******************/
	
	/**    消息处理部分    **/
	public static void postMessage(int msgId, Object data, MessageListener listener) {
		postMessage(msgId, data, 0, listener);
	}
	
	public static void postMessage(int msgId, Object data, long delayMillis,MessageListener listener) {
		Message message = obtainHandlerMessage(msgId, data, listener);
		UI_THREAD_HANDLER.sendMessageDelayed(message, delayMillis);
	}
	
	public static Message obtainHandlerMessage(int msgId,Object data,MessageListener listener) {
		Message message=new Message();
		HandlerMessage handlerMessage=new HandlerMessage();
		handlerMessage.listener=listener;
		handlerMessage.messageData=data;
		
		message.what=msgId;
		message.obj=handlerMessage;
		
		return message;
	}
	
	public static void removeMessage(int msgId) {
		UI_THREAD_HANDLER.removeMessages(msgId);
	}
	
	public static void release(){
		if (threadPool != null) {
			threadPool.shutdownNow();
			threadPool = null;
		}
		
		ASYNC_EXECUTOR_ARRAY.clear();
	}
	
	private static class HandlerMessage{
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
	private static class AsyncExecutorWrapper<T> implements Runnable,MessageListener{
		
		private AsyncExecutor<T> executor;
		public AsyncExecutorWrapper(AsyncExecutor<T> executor){
			this.executor=executor;
		}
		
		private boolean shouldCancel(){
			if (executor.isExecuteCancel()) {
				ASYNC_EXECUTOR_ARRAY.remove(executor.hashCode());
				return true;
			}
			return false;
		}

		/**
		 * 回到了主线程
		 */
		
		@Override
		@SuppressWarnings("unchecked")
		public void handleMessage(int messageId, Object data) {
			// TODO Auto-generated method stub
			
			if (shouldCancel()) {
				return;// do nothing
			}
			
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
			
			if (shouldCancel()) {
				return;
			}
			
			T v=null;
			try {
				v=executor.execute();
			} catch (Exception e) {
				LogManager.trace(e);
			} finally{
				if (!shouldCancel()) {
					postMessage(MSG_HANDLE_ASYNC_COMPLETE, v, AsyncExecutorWrapper.this);
					ASYNC_EXECUTOR_ARRAY.remove(executor.hashCode());
				}
			}
		}
	}
	
	
	private static class ExecuteMethodRunnable implements Runnable,MessageListener{
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
				LogManager.e(ThreadUtility.class.getSimpleName(), "execute method "+method, e);
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
	
	
	
	
	/**
	 * 监听UI线程的消息
	 * @author 陆振文[pengju]
	 * 2012-11-22 上午10:52:32
	 * email: pengju114@163.com
	 */
	public static interface MessageListener {
		public void handleMessage(int messageId, Object data);
	}
}
