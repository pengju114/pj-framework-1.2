package com.pj.core;

/**
 * 应用程序级别的通知监听器
 * @author 陆振文[PENGJU]
 * 2013-6-8 下午11:15:48
 * email: pengju114@163.com
 */
public interface ApplicationNotificationListener {
	/**
	 * 此类别的通知类别为监听所有通知，而不特定是哪一类通知
	 */
	int NOTIFICATION_ALL=Integer.MIN_VALUE>>8;
	/**
	 * 当收到应用程序通知时调用
	 * 2013-6-8 下午11:19:01
	 * @param sender 通知的发送者
	 * @param notificationId 通知ID
	 * @param data 通知附带的数据
	 */
	public void onReceivedApplicationNotification(Object sender,int notificationId,Object data);
}
