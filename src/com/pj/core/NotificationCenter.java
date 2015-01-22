package com.pj.core;

import java.util.HashMap;
import java.util.HashSet;

import com.pj.core.utilities.ThreadUtility;

import android.util.SparseArray;

/**
 * 通知中心
 * @author luzhenwen
 *
 */
public final class NotificationCenter {
	
	private static NotificationCenter defaultCenter = null;
	
	
	private SparseArray<HashMap<NotificationListener,Object>> notificationListeners;//notificationID->[listener->sender]
	
	
	private NotificationCenter(){
		notificationListeners = new SparseArray<HashMap<NotificationListener,Object>>();
	}
	
	
	public static NotificationCenter getDefaultCenter() {
		
		if (defaultCenter == null) {
			synchronized (NotificationCenter.class) {
				if (defaultCenter == null) {
					defaultCenter = new NotificationCenter();
				}
			}
		}
		
		return defaultCenter;
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
		NotificationMessageHandler handler=new NotificationMessageHandler(sender, notificationId, data);
		ThreadUtility.postMessage(Integer.MIN_VALUE, null, delay, handler);
	}
	
	/**
	 * 删除所有注册的应用程序级的通知监听器
	 * 在一个对象不再需要监听任何通知的时候记得调用此方法
	 * 否则对象会一直保留
	 * 2013-6-8 下午11:26:13
	 * @param listener
	 */
	public void removeNotificationListener(NotificationListener listener) {
		synchronized (this) {
			for (int i = 0; i < notificationListeners.size(); i++) {
				int key=notificationListeners.keyAt(i);
				notificationListeners.get(key).remove(listener);
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
	public void removeNotificationListener(NotificationListener listener,int... notificationIds) {
		removeNotificationListener(listener, null, notificationIds);
	}
	
	/**
	 * 删除一个或多个应用程序级的通知监听器,当添加监听器的sender和传进来的sender相同时才会删除，null则删除所有指定notificationId并且添加时sender为null的通知
	 * 在一个对象不再需要监听指定类型的通知的时候记得调用此方法
	 * @param listener
	 * @param sender 
	 * @param notificationIds
	 */
	public void removeNotificationListener(NotificationListener listener,Object sender,int... notificationIds) {
		synchronized (this) {
			if (notificationIds==null) {
				notificationIds=new int[]{NotificationListener.NOTIFICATION_ALL};
			}
			for (int i : notificationIds) {
				HashMap<NotificationListener, Object> ls=notificationListeners.get(i);
				if (ls!=null) {
					HashSet<NotificationListener> keys=new HashSet<NotificationListener>();
					keys.addAll(ls.keySet());
					for (NotificationListener l : keys) {
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
	public void addNotificationListener(NotificationListener listener) {
		addNotificationListener(listener,null, NotificationListener.NOTIFICATION_ALL);
	}
	
	/**
	 * 添加一个类别为监听所有由sender发出的通知的应用程序级的通知监听器
	 * 在一个对象不再需要监听通知的时候记得调用removeNotificationListener
	 * @param listener
	 * @param sender 
	 */
	public void addNotificationListener(NotificationListener listener,Object sender) {
		addNotificationListener(listener, sender,NotificationListener.NOTIFICATION_ALL);
	}
	
	
	/**
	 * 添加一个监听指定类别的并且通知是由sender发出的应用程序级别的监听器,notificationIds为null则监听所有通知
	 * @param listener
	 * @param sender null则监听所有对象发出的通知
	 * @param notificationIds
	 */
	public void addNotificationListener(NotificationListener listener,Object sender,int... notificationIds) {
		synchronized (this) {
			if (notificationIds==null) {
				notificationIds=new int[]{NotificationListener.NOTIFICATION_ALL};
			}
			for (int i : notificationIds) {
				HashMap<NotificationListener, Object> ls=notificationListeners.get(i);
				if (ls==null) {
					ls=new HashMap<NotificationListener, Object>();
					notificationListeners.put(i, ls);
				}
				ls.put(listener, sender);
			}
		}
	}
	
	
	
	

	
	
	private class NotificationMessageHandler implements ThreadUtility.MessageListener{
		private Object sender;
		private int    notificationId;
		private Object data;
		
		public NotificationMessageHandler(Object sender,int id,Object data){
			this.sender=sender;
			this.notificationId=id;
			this.data=data;
		}
		
		@Override
		public void handleMessage(int messageId, Object _data) {
			// TODO Auto-generated method stub
			synchronized (NotificationCenter.this) {
				int[] ids=new int[]{notificationId,NotificationListener.NOTIFICATION_ALL};
				for (int i : ids) {
					HashMap<NotificationListener, Object> ls=notificationListeners.get(i);
					if (ls!=null) {
						for (NotificationListener listener : ls.keySet()) {
							Object specifySender=ls.get(listener);
							if (specifySender==null) {//如果监听器不指定发送者,则监听所有通知
								listener.onReceivedNotification(sender, notificationId, data);
							}else if (sender==specifySender) {
								listener.onReceivedNotification(sender, notificationId, data);
							}
						}
					}
				}
			}
		}
		
	}
	
	
	
	/**
	 * 应用程序级别的通知监听器
	 * @author 陆振文[PENGJU]
	 * 2013-6-8 下午11:15:48
	 * email: pengju114@163.com
	 */
	public static interface NotificationListener {
		/**
		 * 此类别的通知类别为监听所有通知，而不特定是哪一类通知
		 */
		int NOTIFICATION_ALL = Integer.MIN_VALUE>>8;
		
		/**
		 * 当收到应用程序通知时调用
		 * 2013-6-8 下午11:19:01
		 * @param sender 通知的发送者
		 * @param notificationId 通知ID
		 * @param data 通知附带的数据
		 */
		public void onReceivedNotification(Object sender,int notificationId,Object data);
	}
}
