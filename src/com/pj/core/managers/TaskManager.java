package com.pj.core.managers;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务管理器
 * 可以同时执行多个任务，任务太多会排队执行
 * 可以同时执行的任务个数由构造参数指定
 * @author 陆振文[PENGJU]
 * 2013-3-14 9:51:28
 * email: pengju114@163.com
 */
public class TaskManager {
	private static final int TASK_RECIEVE=0;
	private static final int TASK_START=1;
	private static final int TASK_FINISH=2;
	private static final int TASK_FINISH_ALL=3;
	private static final int TASK_CANCEL_ALL=4;
	private static final int TASK_ERROR=5;
	private static final int TASK_EXIT=6;
//	
//	private MessageListener managerEventListener=new MessageListener() {
//		
//		@Override
//		public void handleMessage(int messageId, Object data) {
//			// TODO Auto-generated method stub
//			MessageObjectHandler handler=(MessageObjectHandler) data;
//			TaskManager.this.stateChange(messageId, handler.asyncTask, handler.exception);
//		}
//	};
	
	
	//线程池
	private ExecutorService executorService;
	//等待执行的任务列表
	private LinkedList<AsyncTask> taskStack;
	//正在执行的任务
	private LinkedList<AsyncTask> runningTask;
	//监听器列表
	private LinkedList<TaskManagerListener> listeners;
	
	private final int threadSize;

	
	public TaskManager(int size){
		taskStack=new LinkedList<AsyncTask>();
		runningTask=new LinkedList<AsyncTask>();
		threadSize=size;
		executorService=Executors.newFixedThreadPool(threadSize);
		listeners=new LinkedList<TaskManagerListener>(); 
	}
	
	protected AsyncTask nextTask() {
		if (taskStack.size()>0) {
			synchronized (taskStack) {
				return taskStack.removeFirst();
			}
		}
		return null;
	}
	
	public void addTask(AsyncTask task) {
		synchronized (taskStack) {
			taskStack.add(task);
			LogManager.log(getClass().getSimpleName(),"收到任务[%s]",task);
			stateChange(TASK_RECIEVE, task, null);
			loop();
		}
	}

	private void loop() {
		// TODO Auto-generated method stub
		LogManager.log(getClass().getSimpleName()," before loop -> running:%d ; waiting:%d", runningTask.size(),taskStack.size());
		while (runningTask.size()<threadSize) {
			AsyncTask newTask=nextTask();
			if (newTask==null) {
				break;
			}
			synchronized (runningTask) {
				TaskWatcher watcher=new TaskWatcher(newTask);
				runningTask.add(watcher.getTarget());
				executorService.execute(watcher);
			}
		}
		LogManager.log(getClass().getSimpleName()," after loop -> running:%d ; waiting:%d", runningTask.size(),taskStack.size());
	}
	
	protected void onTaskFinish(AsyncTask task) {
		synchronized (runningTask) {
			runningTask.remove(task);
			stateChange(TASK_FINISH, task, null);
			LogManager.log(getClass().getSimpleName(),"任务[%s]执行完毕",task);
		}
		
		
		if (getRunningCount()<=0 && getWaitingCount()<=0) {
			//所有任务完成
			LogManager.log(getClass().getSimpleName(),"所有任务完成");
			stateChange(TASK_FINISH_ALL, null, null);
		}
		loop();
	}
	
	public boolean isInManager(AsyncTask task) {
		boolean isIn=taskStack.contains(task);
		return isIn?isIn:runningTask.contains(task);
	}
	
	public List<AsyncTask> getWaitingTasks() {
		return taskStack;
	}
	
	public List<AsyncTask> getRunningTasks() {
		return runningTask;
	}
	
	private class TaskWatcher implements Runnable{
		private AsyncTask asyncTask;
		
		public TaskWatcher(AsyncTask asyncTask){
			this.asyncTask=asyncTask;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			stateChange(TASK_START, getTarget(), null);
			try {
				asyncTask.onStart();
				try {
					asyncTask.execute();
				} catch (Exception e) {
					// TODO: handle exception
					asyncTask.onError(e);
					throw e;
				}finally{
					asyncTask.onFinish();
				}
			} catch (Exception e) {
				// TODO: handle exception
				LogManager.trace(e);
				stateChange(TASK_ERROR, getTarget(), e);
			}finally{
				onTaskFinish(asyncTask);
			}
		}
		
		public AsyncTask getTarget() {
			return asyncTask;
		}
	}
	
	public void stop(boolean save) {
		LogManager.log(getClass().getName(),"停止所有任务[保存状态%s]",save);
		stateChange(TASK_CANCEL_ALL, null, null);
		
		if (taskStack.size()>0) {
			synchronized (taskStack) {
				for (AsyncTask task : taskStack) {
					task.cancel();
					if (save) {
						task.saveState();
					}
				}
				taskStack.clear();
			}
		}
		
		if (runningTask.size()>0) {
			synchronized (runningTask) {
				for (AsyncTask task : runningTask) {
					task.cancel();
					if (save) {
						task.saveState();
					}
				}
				runningTask.clear();
			}
		}
	}
	
	public void exit(){
		stateChange(TASK_EXIT, null, null);
		stop(false);
		closeThreadPool();
	}
	
	private void closeThreadPool(){
		try {
			executorService.shutdownNow();
		} catch (Exception e) {
			LogManager.trace(e);
		}
	}
	
	public int getRunningCount() {
		return runningTask.size();
	}
	
	public int getWaitingCount() {
		return taskStack.size();
	}
	
	protected void finalize() throws Throwable{
		//stopAndSave();
		closeThreadPool();
		super.finalize();
	}
	
	public void addListener(TaskManagerListener listener) {
		if (listener!=null) {
			synchronized (this.listeners) {
				this.listeners.add(listener);
			}
		}
	}
	
	public void removeListener(TaskManagerListener listener) {
		synchronized (this.listeners) {
			listeners.remove(listener);
		}
	}
	
	public void clearListeners() {
		synchronized (listeners) {
			listeners.clear();
		}
	}
	
	public boolean removeWaiting(AsyncTask waiting) {
		synchronized (taskStack) {
			return taskStack.remove(waiting);
		}
	}
	
//	private void postMessage(int state,AsyncTask task,Exception exception){
//		MessageObjectHandler handler=new MessageObjectHandler();
//		handler.asyncTask=task;
//		handler.exception=exception;
//		
//		BaseApplication.getInstance().postMessage(state, handler, managerEventListener);
//	}
	
	private void stateChange(int state,AsyncTask asyncTask, Exception e){
		synchronized (listeners) {
			for (TaskManagerListener listener : listeners) {

				switch (state) {
				case TASK_RECIEVE:
					listener.onReceiveTask(this,asyncTask);
					break;
					
				case TASK_START:
					listener.onTaskStart(this,asyncTask);
					break;
					
				case TASK_FINISH:
					listener.onTaskFinish(this,asyncTask);
					break;
					
				case TASK_FINISH_ALL:
					listener.onAllTaskFinish(this);
					break;
					
				case TASK_CANCEL_ALL:
					listener.onCancelAll(this);
					break;
				case TASK_ERROR:
					listener.onTaskError(this,e,asyncTask);
					break;
				case TASK_EXIT:
					listener.onExit(this);
					break;
				default:
					break;
				}
			}
		}
	}
	
	/**
	 * 异步任务接口
	 * @author 陆振文[PENGJU]
	 *
	 */
	public interface AsyncTask {
		/**
		 * 执行任务主方法，抛出错误即马上停止
		 * @throws Exception
		 */
		public void execute() throws Exception;
		/**
		 * 取消任务执行
		 */
		public void cancel() ;
		public void saveState();
		public boolean isRunning();
		
		/**
		 * 任务执行结束。线程中调用，不管是否发生错误。
		 */
		public void onFinish();
		/**
		 * 任务开始，线程中调用
		 */
		public void onStart();
		/**
		 * 任务发生错误，线程中调用
		 * @param task
		 * @param e
		 */
		public void onError(Exception e);
	}
	
	/**
	 * 事件都在调用线程或者线程里调用
	 * @author 任务管理器监听器
	 *
	 */
	public interface TaskManagerListener {
		public void	onReceiveTask(TaskManager manager,AsyncTask task);
		public void	onTaskStart(TaskManager manager,AsyncTask task);
		public void	onTaskFinish(TaskManager manager,AsyncTask task);
		public void	onAllTaskFinish(TaskManager manager);
		public void	onCancelAll(TaskManager manager);
		public void	onTaskError(TaskManager manager,Exception e,AsyncTask task);
		public void onExit(TaskManager manager);
	}
	
//	private class MessageObjectHandler{
//		public AsyncTask asyncTask;
//		public Exception exception;
//	}
}
