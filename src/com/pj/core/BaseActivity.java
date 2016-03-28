package com.pj.core;


import java.lang.reflect.Constructor;

import com.pj.core.datamodel.DataWrapper;
import com.pj.core.dialog.MessageDialog;
import com.pj.core.dialog.ExecutingDialog;
import com.pj.core.dialog.InputDialog;
import com.pj.core.dialog.ProgressDialog;
import com.pj.core.dialog.CacheableDialog;
import com.pj.core.dialog.DialogListener;
import com.pj.core.managers.LogManager;
import com.pj.core.ui.SystemBarTintManager;
import com.pj.core.utilities.StringUtility;
import com.pj.core.utilities.ThreadUtility;
import com.pj.core.utilities.ThreadUtility.MessageListener;
import com.pj.core.viewholders.ViewHolder;

import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 所有活动的基类
 * framework
 * @author 陆振文[pengju]
 * 2012-10-17 下午4:49:27
 */
public class BaseActivity extends Activity implements MessageListener{
	
	private static int      UNIQUE_INT								=100;
	
	public static final int NOTIFICATION_ACTIVITY_CREATE			=0xFF0E00A1;
	public static final int NOTIFICATION_ACTIVITY_RESTORE_STATE		=0xFF0E00A2;
	public static final int NOTIFICATION_ACTIVITY_START				=0xFF0E00A3;
	public static final int NOTIFICATION_ACTIVITY_RESUME			=0xFF0E00A4;
	public static final int NOTIFICATION_ACTIVITY_SAVE_STATE		=0xFF0E00A5;
	public static final int NOTIFICATION_ACTIVITY_PAUSE				=0xFF0E00A6;
	public static final int NOTIFICATION_ACTIVITY_STOP				=0xFF0E00A7;
	public static final int NOTIFICATION_ACTIVITY_RESTART			=0xFF0E00A8;
	public static final int NOTIFICATION_ACTIVITY_DESTROY			=0xFF0E00A9;
	
	public static final int NOTIFICATION_ACTIVITY_RESULT			=0xFF0E00AA;
	
	/**
	 * 网络状态变化通知，发送者将会是 {@link BaseApplication},附带的数据是网络类型(-1表示连接不可用)
	 */
	public static final int NOTIFICATION_NETWORK_STATE_CHANGE		=BaseApplication.NOTIFICATION_NETWORK_STATE_CHANGE;
	
	
	private static final int MSG_DLG_TIP		=nextUniqueInt();//显示浮动消息
	private static final int MSG_DLG_MESSAGE	=nextUniqueInt();//显示提示信息
	private static final int MSG_DLG_CONFIRM	=nextUniqueInt();//显示确认信息
	private static final int MSG_DLG_PROGRESS	=nextUniqueInt();//显示状态信息
	private static final int MSG_DLG_EXECUTING	=nextUniqueInt();//显示正在执行消息
	private static final int MSG_DLG_INPUT		=nextUniqueInt();//显示输入对话框
	
	private static final int REQ_EXIT			=nextUniqueInt();
	
	protected CacheableDialog messageDialog;
	protected CacheableDialog confirmDialog;
	protected CacheableDialog executingDialog;
	protected CacheableDialog progressDialog;
	protected CacheableDialog inputDialog;

	protected LayoutInflater  inflater;
	protected Resources 	  resources;
	private   ViewHolder 	  rootViewHolder;
	private   int             state;
	

	/***********************活动生命周期**********************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//都是无标题活动
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (shouldEnableBarTint()) {
			enableBarTint();
		}
		inflater = super.getLayoutInflater();
		resources= super.getResources();
		
		//入栈
		BaseApplication.getInstance().addActivity(this);
		
		rootViewHolder=new DecorViewHolder(this);
		//将视图挂到窗口上,记得要super.setContentView,因为此方法被重写了
		super.setContentView(rootViewHolder.getView());
		
		Intent fromIntent=getIntent();
		if (fromIntent!=null) {
			@SuppressWarnings("unchecked")
			Class<ViewHolder> initHolderClass=(Class<ViewHolder>) fromIntent.getSerializableExtra(ViewHolder.EXTRA_HOLDER_CLASS);
			if (initHolderClass!=null) {
				try {
					Constructor<ViewHolder> constructor=initHolderClass.getConstructor(BaseActivity.class);
					ViewHolder initHolder=constructor.newInstance(this);
					setContentView(initHolder);
				} catch (Exception e) {
					LogManager.trace(e);
				}
			}
		}
		
		activityStateChange(NOTIFICATION_ACTIVITY_CREATE,savedInstanceState);
	}
	
	/**
	 * 是否允许浸入式状态栏和导航栏,默认开启。
	 * @return
	 */
	protected boolean shouldEnableBarTint(){
		return true;
	}
	
	@SuppressLint("InlinedApi")
	public void enableBarTint(){
		if (Build.VERSION.SDK_INT >= 19) {
			//透明状态栏  
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);  
			//透明导航栏  
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		SystemBarTintManager manager = new SystemBarTintManager(this);
		manager.setStatusBarTintEnabled(true);
		manager.setNavigationBarTintEnabled(true);
	}
	
	public void setSystemBarTintColor(int color) {
		SystemBarTintManager manager = new SystemBarTintManager(this);
		manager.setTintColor(color);
		manager.setStatusBarTintColor(color);
		manager.setNavigationBarTintColor(color);
	}
	
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		activityStateChange(NOTIFICATION_ACTIVITY_RESTORE_STATE,savedInstanceState);
		super.onRestoreInstanceState(savedInstanceState);
	}
	protected void onStart(){
		activityStateChange(NOTIFICATION_ACTIVITY_START,null);
		super.onStart();
	}
	protected void onResume(){
		activityStateChange(NOTIFICATION_ACTIVITY_RESUME,null);
		super.onResume();
	}
	protected void onSaveInstanceState(Bundle outState){
		activityStateChange(NOTIFICATION_ACTIVITY_SAVE_STATE,outState);
		super.onSaveInstanceState(outState);
	}
	protected void onPause(){
		activityStateChange(NOTIFICATION_ACTIVITY_PAUSE,null);
		super.onPause();
	}
	protected void onStop(){
		activityStateChange(NOTIFICATION_ACTIVITY_STOP,null);
		super.onStop();
	}
	protected void onRestart(){
		activityStateChange(NOTIFICATION_ACTIVITY_RESTART,null);
		super.onRestart();
	}
	protected void onDestroy() {
		BaseApplication.getInstance().removeActivity(this);
		
		closeDialogs(messageDialog,confirmDialog,progressDialog,executingDialog,inputDialog);
		messageDialog=null;
		confirmDialog=null;
		progressDialog=null;
		executingDialog=null;
		inputDialog=null;
		activityStateChange(NOTIFICATION_ACTIVITY_DESTROY,null);
		super.onDestroy();
	}
	
	
	public void closeDialogs(CacheableDialog... dialogs) {
		// TODO Auto-generated method stub
		for (CacheableDialog cacheableDialog : dialogs) {
			if (cacheableDialog!=null && cacheableDialog.isShowing()) {
				cacheableDialog.cancel();
			}
		}
	}
	/***********************活动生命周期结束******************/
	public Resources defaultResources() {
		// TODO Auto-generated method stub
		return resources;
	}
	
	public LayoutInflater defaultInflater() {
		// TODO Auto-generated method stub
		return inflater;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Bundle bundle=new Bundle(3);
		bundle.putInt("requestCode", requestCode);
		bundle.putInt("resultCode", resultCode);
		bundle.putParcelable("data", data);
		NotificationCenter.getDefaultCenter().sendNotification(this, NOTIFICATION_ACTIVITY_RESULT, bundle);
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	/**
	 * 设置根视图
	 * PENGJU
	 * 2012-11-21 上午9:53:15
	 * @param viewHolder
	 */
	public void setContentView(ViewHolder viewHolder) {
		rootViewHolder.addChild(viewHolder);
	}
	
	@Override
	public void setContentView(int layoutResID) {
		// TODO Auto-generated method stub
		ChildHolder h=new ChildHolder(rootViewHolder,layoutResID);
		rootViewHolder.addChild(h);
	}
	
	@Override
	public void setContentView(View view) {
		// TODO Auto-generated method stub
		ChildHolder h=new ChildHolder(rootViewHolder, view);
		rootViewHolder.addChild(h);
	}
	
	@Override
	public void setContentView(View view, LayoutParams params) {
		// TODO Auto-generated method stub
		ChildHolder h=new ChildHolder(rootViewHolder, view);
		rootViewHolder.addChild(h,params);
	}
	
	public ViewHolder getRootViewHolder() {
		return rootViewHolder;
	}
	
	/****************ViewHolder监听状态*****************/
	/**
	 * 活动生命周期改变时调用
	 * 2012-10-23 下午2:36:07
	 * @param state
	 */
	protected void activityStateChange(int state,Bundle bundle){
		this.state = state;
		
		NotificationCenter.getDefaultCenter().sendNotification(this, state, bundle);
		
		if (state==NOTIFICATION_ACTIVITY_CREATE) {
			rootViewHolder.dispathAttached(rootViewHolder);
			rootViewHolder.dispathWillAppear(rootViewHolder, false);
			rootViewHolder.dispathDidAppear(rootViewHolder, false);
		}else if (state==NOTIFICATION_ACTIVITY_DESTROY) {
			rootViewHolder.dispathWillDisappear(rootViewHolder, false);
			rootViewHolder.dispathDidDisappear(rootViewHolder, false);
			rootViewHolder.dispathDettached(rootViewHolder);
		}
	}
	
	public int getState() {
		return state;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (rootViewHolder.onKeyDown(keyCode, event)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (rootViewHolder.onKeyLongPress(keyCode, event)) {
			return true;
		}
		return super.onKeyLongPress(keyCode, event);
	}
	
	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		// TODO Auto-generated method stub
		if (rootViewHolder.onKeyMultiple(keyCode, repeatCount, event)) {
			return true;
		}
		return super.onKeyMultiple(keyCode, repeatCount, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (rootViewHolder.onKeyUp(keyCode, event)) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	/****************ViewHolder监听状态结束*************/

	
	/**********************消息*************************/
	public void postMessage(int msgId, Object data) {
		postMessage(msgId, data, this);
	}

	public void postMessage(int msgId, Object data, long delayMillis) {
		postMessage(msgId, data, delayMillis, this);
	}
	
	public void postMessage(int msgId, Object data, MessageListener listener) {
		postMessage(msgId, data, 0, listener);
	}
	
	public void postMessage(int msgId, Object data, long delayMillis,MessageListener listener) {
		ThreadUtility.postMessage(msgId, data, delayMillis, listener);
	}

	public void removeMessages(int msgId) {
		ThreadUtility.removeMessage(msgId);
	}
	
	/**
	 * 在子活动中记得 super.handleMessage[{@link BaseActivity#handleMessage(int, Object)}}]
	 */
	public void handleMessage(int messageId, Object data) {
		if (messageId==MSG_DLG_TIP) {
			callShowTip(String.valueOf(data));
		}else if (messageId==MSG_DLG_MESSAGE) {
			if (getState()!=NOTIFICATION_ACTIVITY_DESTROY) {
				DataWrapper wrapper=(DataWrapper)data;
				getMessageDialog(wrapper).show();
			}
		}else if (messageId==MSG_DLG_CONFIRM) {
			if (getState()!=NOTIFICATION_ACTIVITY_DESTROY){
				DataWrapper wrapper=(DataWrapper)data;
				getConfirmDialog(wrapper).show();
			}
		}else if (messageId==MSG_DLG_EXECUTING) {
			if (getState()!=NOTIFICATION_ACTIVITY_DESTROY){
				DataWrapper wrapper=(DataWrapper)data;
				getExecutingDialog(wrapper).show();
			}
		}else if (messageId==MSG_DLG_PROGRESS) {
			if (getState()!=NOTIFICATION_ACTIVITY_DESTROY){
				DataWrapper wrapper=(DataWrapper)data;
				getProgressDialog(wrapper).show();
			}
		}else if (messageId==MSG_DLG_INPUT) {
			if (getState()!=NOTIFICATION_ACTIVITY_DESTROY){
				DataWrapper wrapper=(DataWrapper) data;
				getInputDialog(wrapper).show();
			}
		}
	}

	
	/**********************消息结束*********************/
	
	
	
	/**********************线程执行*********************/
	
	/**
	 * 执行异步任务
	 * 任务会放到任务列表等待执行，如果前面还有任务未完成则会继续等待
	 * @author PENGJU
	 * @param executor
	 */
	public <T> void execute(AsyncExecutor<T> executor){
		ThreadUtility.execute(executor);
	}
	/**
	 * 延迟delay毫秒后执行异步任务
	 * 任务等待delay毫秒后会放到任务列表等待执行，如果前面还有任务未完成则会继续等待
	 * PENGJU
	 * @param executor
	 * @param delay
	 */
	public <T> void execute(AsyncExecutor<T> executor,long delay){
		ThreadUtility.execute(executor, delay);
	}
	
	/**
	 * 取消队列中一个正在等待执行的异步任务
	 * PENGJU
	 * @param executor
	 */
	public void cancelExecute(AsyncExecutor<?> executor){
		ThreadUtility.cancelExecute(executor);
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
		executeMethodInBackground(0,target, methodId, arguments);
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
		ThreadUtility.executeMethodInBackground(delay, target, methodId, arguments);
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
		ThreadUtility.executeMethodInMainThread(delay, target, methodId, arguments);
	}
	
	/**********************线程执行结束******************/
	
	
	
	
	/************************显示信息************************/
	
	protected void callShowTip(String tip) {
		// TODO Auto-generated method stub
		Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
	}

	/** 实现显示信息对话框 */
	protected  CacheableDialog getMessageDialog(DataWrapper wrapper){
		if (messageDialog==null) {
			messageDialog=new MessageDialog(this);
		}
		messageDialog.setDialogData(wrapper);
		return messageDialog;
	}
	/** 实现确认信息对话框 */
	protected  CacheableDialog getConfirmDialog(DataWrapper wrapper){
		if (confirmDialog==null) {
			confirmDialog=new MessageDialog(this);
		}
		confirmDialog.setDialogData(wrapper);
		return confirmDialog;
	}
	/** 实现正在执行信息对话框 */
	protected  CacheableDialog getExecutingDialog(DataWrapper wrapper){
		if (executingDialog==null) {
			executingDialog=new ExecutingDialog(this);
		}
		executingDialog.setDialogData(wrapper);
		return executingDialog;
	}
	/** 实现状态信息对话框 */
	protected  CacheableDialog getProgressDialog(DataWrapper wrapper){
		if (progressDialog==null) {
			progressDialog=new ProgressDialog(this);
		}
		progressDialog.setDialogData(wrapper);
		return progressDialog;
	}
	
	protected CacheableDialog getInputDialog(DataWrapper wrapper) {
		if (inputDialog==null) {
			inputDialog=new InputDialog(this);
		}
		inputDialog.setDialogData(wrapper);
		return inputDialog;
	}

	/**
	 * 显示浮动消息,可在线程中调用
	 * @param msg
	 */
	public void showTip(Object msg) {
		postMessage(MSG_DLG_TIP, msg);
	}
	/**
	 * 显示浮动消息,可在线程中调用
	 * @param resId
	 */
	public void showTip(int resId) {
		postMessage(MSG_DLG_TIP, resources.getString(resId));
	}	
	
	/**
	 * 显示状态对话框,可在线程中调用
	 * lzw
	 * 2013-4-2 下午2:39:32
	 * @param msg 消息提示，可为null
	 * @param listener 回调监听器，可为null
	 */
	public void showProgressDialog(int requestCode,String msg,DialogListener listener) {
		if (getState()!=NOTIFICATION_ACTIVITY_DESTROY){
			showProgressDialog(requestCode,msg,null,listener);
		}
	}
	/**
	 * 显示状态对话框,可在线程中调用
	 * lzw
	 * 2013-4-2 下午2:42:03
	 * @param msg 消息提示，可为null
	 * @param cacheData 附带数据
	 * @param listener 回调监听器，可为null
	 */
	public void showProgressDialog(int requestCode,String msg,Object cacheData,DialogListener listener) {
		if (getState()!=NOTIFICATION_ACTIVITY_DESTROY){
			DataWrapper wrapper=new DataWrapper();
			wrapper.setObject(CacheableDialog.KEY_MESSAGE, msg);
			wrapper.setObject(CacheableDialog.KEY_DATA, cacheData);
			wrapper.setObject(CacheableDialog.KEY_LISTENER, listener);
			wrapper.setObject(CacheableDialog.KEY_REQUEST_ID, requestCode);
			postMessage(MSG_DLG_PROGRESS, wrapper);
		}
	}
	
	/**
	 * 关闭状态对话框
	 * lzw
	 * 2014年3月20日 下午11:46:33
	 */
	public void closeProgressDialog(){
		if (progressDialog!=null && progressDialog.isShowing()) {
			progressDialog.cancel();
		}
	}
	
	
	/**
	 * 显示确认对话框
	 * lzw
	 * 2014年3月20日 下午11:47:16
	 * @param requestCode      请求码
	 * @param optionTitle      标题，可选
	 * @param msg              文本消息
	 * @param positiveBtnText  确定按钮文本,要是第一次调用时为空字符串则不显示按钮
	 * @param negtiveBtnText   取消按钮文本,要是第一次调用时为空字符串则不显示按钮
	 * @param listener
	 */
	public void showConfirmDialog(int requestCode,String optionTitle,String msg,String positiveBtnText,String negtiveBtnText,DialogListener listener) {
		showConfirmDialog(requestCode,optionTitle,msg,positiveBtnText,negtiveBtnText,null,listener);
	}
	
	/**
	 * 显示确认对话框
	 * lzw
	 * 2014年3月20日 下午11:50:53
	 * @param requestCode      请求码
	 * @param optionTitle      标题，可选
	 * @param msg              文本消息
	 * @param positiveBtnText  确定按钮文本,要是第一次调用时为空字符串则不显示按钮
	 * @param negtiveBtnText   取消按钮文本,要是第一次调用时为空字符串则不显示按钮
	 * @param cacheData
	 * @param listener
	 */
	public void showConfirmDialog(int requestCode,String optionTitle,String msg,String positiveBtnText,String negtiveBtnText,Object cacheData,DialogListener listener) {
		if (getState()!=NOTIFICATION_ACTIVITY_DESTROY){
			DataWrapper wrapper=new DataWrapper();
			wrapper.setObject(CacheableDialog.KEY_TITLE, optionTitle);
			wrapper.setObject(CacheableDialog.KEY_MESSAGE, msg);
			wrapper.setObject(CacheableDialog.KEY_DATA, cacheData);
			wrapper.setObject(CacheableDialog.KEY_LISTENER, listener);
			wrapper.setObject(CacheableDialog.KEY_POSITIVE_BUTTON_TEXT, positiveBtnText);
			wrapper.setObject(CacheableDialog.KEY_NEGATIVE_BUTTON_TEXT, negtiveBtnText);
			wrapper.setObject(CacheableDialog.KEY_REQUEST_ID, requestCode);
			postMessage(MSG_DLG_CONFIRM, wrapper);
		}
	}
		
	/**
	 * 显示正在执行对话框，无背景
	 * lzw
	 * 2014年3月20日 下午11:52:25
	 * @param requestCode
	 * @param listener
	 */
	public void showExecutingDialog(int requestCode,DialogListener listener) {
		showExecutingDialog( requestCode,null,listener);
	}
	/**
	 * 显示正在执行对话框，无背景
	 * lzw
	 * 2014年3月20日 下午11:53:59
	 * @param requestCode
	 * @param cacheData
	 * @param listener
	 */
	public void showExecutingDialog(int requestCode,Object cacheData,DialogListener listener) {
		if (getState()!=NOTIFICATION_ACTIVITY_DESTROY){
			DataWrapper wrapper=new DataWrapper();
			wrapper.setObject(CacheableDialog.KEY_DATA, cacheData);
			wrapper.setObject(CacheableDialog.KEY_LISTENER, listener);
			wrapper.setObject(CacheableDialog.KEY_REQUEST_ID, requestCode);
			postMessage(MSG_DLG_EXECUTING, wrapper);
		}
	}
	
	/**
	 * 关闭正在执行对话框
	 * lzw
	 * 2014年3月20日 下午11:54:29
	 */
	public void closeExecutingDialog(){
		if (executingDialog!=null && executingDialog.isShowing()) {
			executingDialog.cancel();
		}
	}
	
	/**
	 * 显示信息提示框
	 * lzw
	 * 2014年3月20日 下午11:55:15
	 * @param requestCode
	 * @param title
	 * @param msg
	 * @param positiveBtnText
	 * @param listener
	 */
	public void showMessageDialog(int requestCode,String title,String msg,String positiveBtnText,DialogListener listener) {
		showMessageDialog(requestCode,title,msg,positiveBtnText,null,listener);
	}
	
	public void showMessageDialog(int requestCode,String title,String msg,String positiveBtnText,Object cacheData,DialogListener listener) {
		if (getState()!=NOTIFICATION_ACTIVITY_DESTROY){
			DataWrapper wrapper=new DataWrapper();
			wrapper.setObject(CacheableDialog.KEY_TITLE, title);
			wrapper.setObject(CacheableDialog.KEY_MESSAGE, msg);
			wrapper.setObject(CacheableDialog.KEY_DATA, cacheData);
			wrapper.setObject(CacheableDialog.KEY_LISTENER, listener);
			wrapper.setObject(CacheableDialog.KEY_POSITIVE_BUTTON_TEXT, positiveBtnText);
			wrapper.setObject(CacheableDialog.KEY_REQUEST_ID, requestCode);
			
			postMessage(MSG_DLG_MESSAGE, wrapper);
		}
	}
	
	/**
	 * 输入框
	 * lzw
	 * 2014年5月28日 下午10:49:07
	 * @param requestCode
	 * @param title
	 * @param msg
	 * @param positiveBtnText
	 * @param negtiveBtnText
	 * @param defaultText
	 * @param hint
	 * @param listener
	 */
	public void showInputDialog(int requestCode, String title,String msg,String positiveBtnText,String negtiveBtnText,String defaultText,String hint,DialogListener listener) {
		showInputDialog(requestCode, EditorInfo.TYPE_CLASS_TEXT, title, msg, positiveBtnText,negtiveBtnText,defaultText, hint, null, listener);
	}
	
	public void showInputDialog(int requestCode,int inputType, String title,String msg,String positiveBtnText,String negtiveBtnText,String defaultText,String hint,Object cacheData,DialogListener listener) {
		if (getState()!=NOTIFICATION_ACTIVITY_DESTROY) {
			DataWrapper wrapper=new DataWrapper();
			wrapper.setObject(CacheableDialog.KEY_TITLE, title);
			wrapper.setObject(CacheableDialog.KEY_MESSAGE, msg);
			wrapper.setObject(CacheableDialog.KEY_DATA, cacheData);
			wrapper.setObject(CacheableDialog.KEY_LISTENER, listener);
			wrapper.setObject(CacheableDialog.KEY_POSITIVE_BUTTON_TEXT, positiveBtnText);
			wrapper.setObject(CacheableDialog.KEY_NEGATIVE_BUTTON_TEXT, negtiveBtnText);
			wrapper.setObject(CacheableDialog.KEY_REQUEST_ID, requestCode);
			
			wrapper.setObject(InputDialog.KEY_TEXT, defaultText);
			wrapper.setObject(InputDialog.KEY_HINT, hint);
			
			wrapper.setObject(InputDialog.KEY_INPUT_TYPE, inputType);
			postMessage(MSG_DLG_INPUT, wrapper);
		}
	}
	
	public void closeInputDialog(){
		if (inputDialog!=null && inputDialog.isShowing()) {
			inputDialog.cancel();
		}
	}

	/************************显示信息结束********************/
	
	/**
	 * 退出程序,关闭所有活动,但不关闭进程
	 * 要关闭进程 可在app.config中设置exit_app_on_all_activities_destroyed=true
	 * PENGJU
	 * 2012-10-19 下午3:52:53
	 */
	public void exit(){
		BaseApplication.exit();
	}
	
	/**
	 * 退出程序,关闭所有活动,但不关闭进程
	 * 要关闭进程 可在app.config中设置exit_app_on_all_activities_destroyed=true
	 * PENGJU
	 * 2012-10-19 下午3:53:08
	 * @param confirmTip 推出前确认信息
	 */
	public void exit(String confirmTip){
		if (!StringUtility.isEmpty(confirmTip)) {
			showConfirmDialog(REQ_EXIT,null,confirmTip,null, null, new DialogListener() {
				
				@Override
				public void onDialogClose(int req, CacheableDialog dialog, int triggerbtn,
						Object cacheData) {
					// TODO Auto-generated method stub
					if (triggerbtn==DialogListener.BTN_OK && req==REQ_EXIT) {
						BaseActivity.this.exit();
					}
				}
			});
		} else {
			exit();
		}
	}
	
	
	public static final int nextUniqueInt() {
		return UNIQUE_INT++;
	}

	/***********************会话数据*********************/
	public void addSessionData(Object key,Object val) {
		BaseApplication.getInstance().addSessionData(key, val);
	}
	public Object getSessionData(Object key) {
		return BaseApplication.getInstance().getSessionData(key);
	}
	public String getSessionId() {
		return BaseApplication.getInstance().getSessionId();
	}
	public void removeSessionData(Object key) {
		BaseApplication.getInstance().removeSessionData(key);
	}
	/***********************会话数据结束*********************/
	
	/**
	 * 给当前视图内给指定ID的视图注册OnClickListener监听器
	 * @param listener
	 * @param viewIds 视图ID（一个或多个）
	 */
	public void assignClickListener(View.OnClickListener listener,int... viewIds){
		if (viewIds!=null) {
			for (int i : viewIds) {
				View v=findViewById(i);
				if (v!=null) {
					v.setOnClickListener(listener);
				}
			}
		}
	}
	
	public class DecorViewHolder extends ViewHolder{
		public DecorViewHolder(BaseActivity activity) {
			super(activity,new FrameLayout(activity));
			// TODO Auto-generated constructor stub
		}
		@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
		@Override
		protected void onApplyView(View view) {
			if (shouldEnableBarTint() && android.os.Build.VERSION.SDK_INT >= 19) {
				view.setFitsSystemWindows(true);
			}
			// TODO Auto-generated method stub
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setFocusable(true);
			enhanceAnimation(null, view);
			view.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
			FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT, Gravity.LEFT|Gravity.TOP);
			view.setLayoutParams(p);
		}
	}
	
	private class ChildHolder extends ViewHolder{

		public ChildHolder(ViewHolder mParent, int layout) {
			super(mParent);
			// TODO Auto-generated constructor stub
			setLayoutResource(layout);
		}
		
		public ChildHolder(ViewHolder mParent, View view) {
			super(mParent, view);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onApplyView(View view) {
			// TODO Auto-generated method stub
			
		}
	}
}
