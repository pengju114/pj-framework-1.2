package com.pj.core;

/**
 * 监听UI线程的消息
 * @author 陆振文[pengju]
 * 2012-11-22 上午10:52:32
 * email: pengju114@163.com
 */
public interface MessageListener {
	public void handleMessage(int messageId, Object data);
}
