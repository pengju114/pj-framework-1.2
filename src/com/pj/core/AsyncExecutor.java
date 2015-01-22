package com.pj.core;

/**
 * 异步任务接口
 * framework
 * @author 陆振文[pengju]
 * 2013-1-20 上午10:10:13
 * email: pengju114@163.com
 */
public interface AsyncExecutor<T> {
	
	/**
	 * 在调用 {@link #execute()}之前调用
	 * 属于UI线程
	 * PENGJU
	 * 2014年3月17日 下午10:04:50
	 */
	public void executePrepare();
	/**
	 * 异步线程调用
	 * PENGJU
	 * 2013-1-20 上午10:12:58
	 * @return 结果
	 */
	public T execute();
	/**
	 * 异步线程调用结束后调用
	 * 属于UI线程
	 * PENGJU
	 * 2013-1-20 上午10:14:15
	 * @param value 异步调用返回的结果
	 */
	public void executeComplete(T value);
	
	/**
	 * 是否取消执行，取消并非实在意义上取消，而是在执行过程不再对此executor抛送事件
	 * @return
	 */
	public boolean isExecuteCancel();
}
