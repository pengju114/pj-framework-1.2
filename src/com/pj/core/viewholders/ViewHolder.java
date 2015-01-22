package com.pj.core.viewholders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint.FontMetrics;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pj.core.AsyncExecutor;
import com.pj.core.BaseActivity;
import com.pj.core.BaseApplication;
import com.pj.core.NotificationCenter;
import com.pj.core.NotificationCenter.NotificationListener;
import com.pj.core.R;
import com.pj.core.dialog.BaseDialog;
import com.pj.core.dialog.HolderDialog;
import com.pj.core.dialog.HolderPopupWindow;
import com.pj.core.managers.LogManager;
import com.pj.core.transition.AnimationFactory;
import com.pj.core.transition.Rotate3dAnimation;
import com.pj.core.ui.GobackArrowDrawable;
import com.pj.core.utilities.AppUtility;
import com.pj.core.utilities.DimensionUtility;
import com.pj.core.utilities.ThreadUtility;
import com.pj.core.utilities.ThreadUtility.MessageListener;

/**
 * 视图持有器，用来管理视图，代表页面中的一个视图模块
 * 包括实现该视图模块的事件响应和网络访问等等
 * 在实现中要指定该视图持有器所代表的视图
 * 比如指定对应的布局文件，对应的视图等等
 * @author 陆振文[PENGJU]
 * 2012-10-19 下午4:54:25
 * email: pengju114@163.com
 */
public abstract class ViewHolder implements MessageListener{
	
	protected static final short MODE_PLAIN_VIEW =0x0F01;
	protected static final short MODE_MODULE_VIEW=0x0F02;
	protected static final short MODE_FLAG_FLIP  =0x0010;
	
	public static final String   EXTRA_HOLDER_CLASS=ViewHolder.class.getPackage().getName();
	
	/** 启动活动时不需返回结果 */
	public static final int      NO_RESULT         =-1;
	
	private static final int     MSG_DISMISS_MENU=BaseActivity.nextUniqueInt();
	
	public static final int ACTIVITY_CREATE			=BaseActivity.NOTIFICATION_ACTIVITY_CREATE;
	public static final int ACTIVITY_RESTORE_STATE	=BaseActivity.NOTIFICATION_ACTIVITY_RESTORE_STATE;
	public static final int ACTIVITY_START			=BaseActivity.NOTIFICATION_ACTIVITY_START;
	public static final int ACTIVITY_RESUME			=BaseActivity.NOTIFICATION_ACTIVITY_RESUME;
	public static final int ACTIVITY_SAVE_STATE		=BaseActivity.NOTIFICATION_ACTIVITY_SAVE_STATE;
	public static final int ACTIVITY_PAUSE			=BaseActivity.NOTIFICATION_ACTIVITY_PAUSE;
	public static final int ACTIVITY_STOP			=BaseActivity.NOTIFICATION_ACTIVITY_STOP;
	public static final int ACTIVITY_RESTART		=BaseActivity.NOTIFICATION_ACTIVITY_RESTART;
	public static final int ACTIVITY_DESTROY		=BaseActivity.NOTIFICATION_ACTIVITY_DESTROY;
	public static final int ACTIVITY_RESULT			=BaseActivity.NOTIFICATION_ACTIVITY_RESULT;
	
	/**
	 * 网络状态变化通知，发送者将会是 {@link BaseApplication},附带的数据是网络类型(-1表示连接不可用)
	 */
	public static final int NOTIFICATION_NETWORK_STATE_CHANGE		=BaseApplication.NOTIFICATION_NETWORK_STATE_CHANGE;
	
	private final NotificationListener activityNotificationListener=new NotificationListener() {
		@Override
		public void onReceivedNotification(Object sender,
				int notificationId, Object data) {
			if (sender==ViewHolder.this.getActivity()) {
				if (notificationId==ACTIVITY_RESULT) {
					Bundle bundle=(Bundle) data;
					ViewHolder.this.onActivityResult(bundle.getInt("requestCode"), bundle.getInt("resultCode"),(Intent)bundle.getParcelable("data"));
				}else {
					ViewHolder.this.onActivityStateChange(notificationId,(Bundle)data);
				}
			}
		}
	};
	
	private boolean 								 duplicateParentState;
	private HashSet<ViewHolderStateListener> 		 stateListeners;
	
	private SparseArray<HolderViewAnimationListener> animatingArray;//只保留最新的一个动画
	private int startedAnimationCount;
	private int finishedAnimationCount;
	private int animationDuration;
	
	private BaseActivity           activity;
	private HashSet<ViewHolder>    notifyHolders;
	private ArrayList<ViewHolder>  childrenHolders;
	
	/** 父holder,如果有的话 **/
	private ViewHolder parent;
	private short      mode;
	
	//导航部分
	private NavigationBar navigationBar;
	NavigationViewHolder  navigationViewHolder;
	
	public BaseDialog 	  attachedDialog;
	public PopupWindow 	  attachedPopupWindow;
	private boolean    	  viewDidAppeared;
	
	
	/**
	 * 根视图
	 * 该视图持有器所持有的根视图
	 * 即该视图持有器就代表这个视图
	 * 调用getView得到的就是这个视图
	 * 视图持有器只是对视图进行包装
	 */
	private View holderView;
	
	public ViewHolder(BaseActivity activity){
		this(activity, null);
	}
	
	public ViewHolder(BaseActivity activity,View view/*根视图*/){
		privateInitialize(activity, view);
	}
	
	public ViewHolder(ViewHolder mParent){
		this(mParent, null);
	}
	
	public ViewHolder(ViewHolder mParent,View rootView){
		setParent(mParent);
		privateInitialize(mParent.getActivity(), rootView);
	}
	
	private void privateInitialize(BaseActivity activity,View root){
		this.activity			= activity;
		notifyHolders			= new HashSet<ViewHolder>();
		childrenHolders			= new ArrayList<ViewHolder>(6);
		animatingArray			= new SparseArray<ViewHolder.HolderViewAnimationListener>();
		startedAnimationCount	= 0;
		finishedAnimationCount	= 0;
		animationDuration		= 400;
		mode					= MODE_PLAIN_VIEW;
		duplicateParentState	= true;
		viewDidAppeared  		= false;
		stateListeners  		= new HashSet<ViewHolder.ViewHolderStateListener>(3);
		//在所有东西都设置好的时候才调用子类的初始化函数
		initialize(activity,root);
		setView(root);
	}
		
	/**
	 * 在构造函数中设置根视图之前调用
	 * PENGJU
	 * 2013-1-5 下午10:20:07
	 */
	protected void initialize(BaseActivity activity,View view) {
		// TODO Auto-generated method stub
		
	}
	public void setDuplicateParentState(boolean duplicateParentState) {
		this.duplicateParentState = duplicateParentState;
	}
	public boolean isDuplicateParentState() {
		return duplicateParentState;
	}
	
	public void addViewHolderStateListener(ViewHolderStateListener listener) {
		// TODO Auto-generated method stub
		stateListeners.add(listener);
	}
	
	public void removeViewHolderStateListener(ViewHolderStateListener listener) {
		// TODO Auto-generated method stub
		stateListeners.remove(listener);
	}
	
	protected void clearViewHolderStateListener(){
		stateListeners.clear();
	}

	/**
	 * 获取上下文活动
	 * PENGJU
	 * 2012-12-28 下午9:41:41
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends BaseActivity> T getActivity(){
		return (T) activity;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends BaseApplication> T getApplication(){
		return (T) BaseApplication.getInstance();
	}
	
	/**
	 * 获取该视图持有器所持有的视图,即根视图
	 * 在调用此方法之前要先初始化
	 * setLayoutResource 或者 setView
	 * 也可以在构造时直接指定根视图
	 * PENGJU
	 * 2012-12-28 下午9:42:54
	 * @return
	 */
	public View getView(){
		return holderView;
	}
	
	/**
	 * 设置根视图的视图布局文件
	 * 调用此方法后就会调用onApplyView通知将要使用指定的视图作为根视图
	 * 建议在构造函数中进行初始化
	 * PENGJU
	 * 2012-12-28 下午9:44:47
	 * @param layout
	 */
	public void setLayoutResource(int layout) {
		setView(getActivity().defaultInflater().inflate(layout, null));
	}
	
	/**
	 * 直接设置根视图
	 * 调用此方法后就会调用onApplyView通知将要使用指定的视图作为根视图
	 * PENGJU
	 * 2012-12-28 下午9:49:56
	 * @param holderView
	 */
	public void setView(View holderView) {
		this.holderView=holderView;
		if (holderView!=null) {
			onApplyView(holderView);
		}
	}
	
	/**
	 * 根据ID查找视图，相当于getView().findViewById(id)
	 * PENGJU
	 * 2012-12-28 下午11:15:02
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends View> T findViewById(int id) {
		View selfView=getView();
		if (selfView!=null) {
			View v=selfView.findViewById(id);
			if (v==null) {
				if (selfView.getId()==id) {
					v=selfView;
				}
			}
			return (T)v;
		}
		return (T)selfView;
	}
	
	/**
	 * 在设置或者更新根视图时调用
	 * 用来进行初始化，比如添加事件监听器，设置视图初始数据等等
	 * PENGJU
	 * 2012-12-28 下午9:50:17
	 * @param view
	 */
	protected abstract void onApplyView(View view);

	public NavigationBar getNavigationBar() {
		if (navigationBar==null) {
			setNavigationBar(new NavigationBar());
		}
		return navigationBar;
	}
	public void setNavigationBar(NavigationBar navigationBar) {
		this.navigationBar = navigationBar;
	}
	
	protected void setMode(short mode) {
		this.mode = mode;
	}
	
	public short getMode() {
		return mode;
	}
	
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
	
	/**
	 * 从另一个视图持有器中申请一个视图充当当前视图
	 * 将会触发相应的holder生命周期方法
	 * @param holder 指定的视图持有器,将用来当做当前持有器的父持有器
	 * @param viewId 要申请的视图ID
	 */
	public void applyViewOnHolder(ViewHolder holder,int viewId){
		applyViewOnHolder(holder, holder.findViewById(viewId));
	}
	
	/**
	 * 从另一个视图持有器中申请一个视图充当当前视图
	 * 将会触发相应的holder生命周期方法
	 * @param holder 指定的视图持有器,将用来当做当前持有器的父持有器
	 * @param view   要申请的视图
	 */
	public void applyViewOnHolder(ViewHolder holder,View view){
		setParent(holder);
		setView(view);
		
		holder.childrenHolders.add(this);
		holder.onChildAttach(this, false);
	}
	
	/**
	 * 获取动画添加/删除视图时的默认动画时长
	 * @return
	 */
	public int getAnimationDuration() {
		return animationDuration;
	}
	
	/**
	 * 设置动画添加/删除视图时的默认动画时长
	 * @param animationDuration
	 */
	public void setAnimationDuration(int animationDuration) {
		this.animationDuration = animationDuration;
	}
	
	/**
	 * 在每次动画进行前都会调用这个来进行初始化设置
	 * @param animation
	 */
	protected Animation initAnimations(Animation animation){
		animation.setFillAfter(true);
		animation.setDuration(getAnimationDuration());
		
		return animation;
	}
	
	public void addChild(ViewHolder holder) {
		// TODO Auto-generated method stub
		addChild(holder, null);
	}
	public void addChild(ViewHolder holder,LayoutParams params) {
		// TODO Auto-generated method stub
		addChild(getView(), holder, params, false);
	}
	
	public void addChild(int containerID,ViewHolder holder) {
		addChild(containerID, holder,null);
	}
	public void addChild(ViewGroup container,ViewHolder holder) {
		addChild(container, holder,null,false);
	}
	
	public void addChild(int containerID,ViewHolder holder,LayoutParams param) {
		
		addChild(containerID, holder, param, false);
	}
	
	private void addChild(int containerID,ViewHolder holder,LayoutParams param,boolean animated) {
		if (getView()!=null && holder.getView()!=null) {
			addChild(findViewById(containerID), holder, param, animated);
		}
	}
	
	private void addChild(View group,ViewHolder holder,LayoutParams param,boolean animated) {
		if (group!=null && holder.getView()!=null) {
			
			View targetView=group;
			if (targetView instanceof ViewGroup) {
				//先在holder的父holder中删除
				if (holder.getParent()!=null) {
					holder.getParent().removeChild(holder);
				}
				//有可能没删除掉,没有设置holder的父holder
				ViewGroup ctr=(ViewGroup) targetView;
				if (holder.getView().getParent()!=null) {
					ViewParent p=holder.getView().getParent();
					if (p instanceof ViewGroup) {
						((ViewGroup)p).removeView(holder.getView());
						onChildDettach(holder, false);
					}
				}
				if (param==null) {
					ctr.addView(holder.getView());
				}else {
					ctr.addView(holder.getView(),param);
				}
				
				holder.setParent(this);
				childrenHolders.add(holder);
				
				onChildAttach(holder,animated);
			}
		}
	}
	
	public void addChild(Animation animation,ViewHolder holder){
		addChild(getView(), holder, null, true);
		//添加成功
		startAddAnimation(animation,holder);
	}
	
	public void addChild(Animation animation,ViewHolder holder,LayoutParams params){
		addChild(getView(), holder, params, true);
		//添加成功
		startAddAnimation(animation,holder);
	}
	
	public void addChild(Animation animation,ViewGroup container,ViewHolder holder){
		addChild(container, holder, null, true);
		//添加成功
		startAddAnimation(animation,holder);
	}

	public void addChild(int animationType,int containerID,ViewHolder holder){
		addChild(animationType, containerID, holder, null);
	}
	
	public void addChild(int animationType,int containerID,ViewHolder holder,LayoutParams param){
		Animation animation=AnimationFactory.getAnimation(animationType);
		addChild(animation, containerID, holder, param);
	}
	public void addChild(Animation animation,int containerID,ViewHolder holder){
		addChild(animation, containerID, holder, null);
	}
	public void addChild(Animation animation,int containerID,ViewHolder holder,LayoutParams param){
		//先加到视图上
		addChild(containerID, holder, param,true);
		startAddAnimation(animation, holder);
	}
	
	/**
	 * 为指定ID的布局添加一个视图
	 * @param containerId 布局容器ID
	 * @param layout	  视图布局文件
	 * @return            布局文件的根视图 如果ID为containerId的布局不存在则返回null
	 */
	public View addLayout(int containerId,int layout){
		ViewGroup group = findViewById(containerId);
		if (group != null) {
			View view = getLayoutInflater().inflate(layout, group, false);
			
			group.addView(view);
			
			return view;
		}
		
		return null;
	}
	
	private void startAddAnimation(Animation animation, ViewHolder holder) {
		// TODO Auto-generated method stub
		//添加成功
		if (holder!=null && holder.getParent()==this) {
			initAnimations(animation);
			HolderViewAnimationListener listener=new HolderViewAnimationListener(holder, animation, TRANSITION_APPEAR);
			listener.postAnimation();
		}
	}
	
	public void removeChild(ViewHolder holder){
		removeChild(holder, false);
	}
	
	private void removeChild(ViewHolder holder,boolean animated){
		if (holder!=null) {
			if (holder.getParent()==this) {
				holder.setParent(null);
			}
			
			childrenHolders.remove(holder);
			
			//从视图中删除
			if (holder.getView()!=null && (holder.getView().getParent() instanceof ViewGroup)) {
				((ViewGroup)holder.getView().getParent()).removeView(holder.getView());
			}
			onChildDettach(holder,animated);
		}
	}
	
	public void removeChild(int animationType,ViewHolder holder){
		removeChild(AnimationFactory.getAnimation(animationType), holder);
	}
	public void removeChild(Animation animation,ViewHolder holder){
		if (animation==null) {
			removeChild(holder,false);
		}else {
			initAnimations(animation);
			HolderViewAnimationListener  listener=new HolderViewAnimationListener(holder, animation, TRANSITION_DISAPPEAR);
			listener.postAnimation();
		}
	}
	
	public List<ViewHolder> getChildHolders() {
		return Collections.unmodifiableList(childrenHolders);
	}
	
	public void clearChildrenHolders(boolean animate){
		for (ViewHolder h : childrenHolders) {
			removeChild(h,animate);
		}
		childrenHolders.clear();
	}
	
	public static final void bindNotificationListener(ViewHolder holder){
		NotificationCenter.getDefaultCenter().addNotificationListener(
				holder.activityNotificationListener, 
				holder.getActivity(),
				ACTIVITY_CREATE,
				ACTIVITY_RESTORE_STATE,
				ACTIVITY_START,
				ACTIVITY_RESUME,
				ACTIVITY_SAVE_STATE,
				ACTIVITY_PAUSE,
				ACTIVITY_STOP,
				ACTIVITY_RESTART,
				ACTIVITY_DESTROY,
				ACTIVITY_RESULT
				);
	}
	
	public static final void unbindNotificationListener(ViewHolder holder){
		NotificationCenter.getDefaultCenter().removeNotificationListener(
				holder.activityNotificationListener,
				holder.getActivity(), 
				ACTIVITY_CREATE,
				ACTIVITY_RESTORE_STATE,
				ACTIVITY_START,
				ACTIVITY_RESUME,
				ACTIVITY_SAVE_STATE,
				ACTIVITY_PAUSE,
				ACTIVITY_STOP,
				ACTIVITY_RESTART,
				ACTIVITY_DESTROY,
				ACTIVITY_RESULT
				);
	}
	
	private void onChildAttach(ViewHolder holder,boolean animated){
		
		dispathAttached(holder);
		
		//如果非动画则调用生命周期函数
		if (!animated) {
			dispathWillAppear(holder, animated);
			dispathDidAppear(holder, animated);
		}
	}
	private void onChildDettach(ViewHolder holder,boolean animated) {
		
		if (!animated) {
			dispathWillDisappear(holder, animated);
			dispathDidDisappear(holder, animated);
		}
		dispathDettached(holder);
	}
	
	
	/**
	 * 该视图被选中时触发
	 * lzw
	 * 2013-5-13 上午11:32:39
	 */
	public void onSelected() {
		LogManager.i(getClass().getSimpleName(),"onSelected");
	}
	/**
	 * 该视图被取消选中时触发
	 * lzw
	 * 2013-5-13 上午11:32:39
	 */
	public void onDeselected() {
		LogManager.i(getClass().getSimpleName(),"onDeselected");
	}

	/**
	 * 添加视图持有器通知监听器
	 * 多个视图之间可以通过这种方式来交互
	 * 比如有一个内容视图持有器(contentViewHolder)
	 * contentViewHolder下有导航视图持有器(navViewHolder)和页面视图持有器(pageViewHolder)
	 * contentViewHolder可以监听pageViewHolder发出的通知，比如监听到点击事件就切换到新的页面等等
	 * 实现监听其他视图，首先把自己添加到想要监听的视图持有器中
	 * 其次要在目标持有器中要发出通知,那么监听器才会收到通知
	 * 拿内容视图持有器和页面视图持有器来说
	 * 1、contentViewHolder要监听pageViewHolder
	 * 2、pageViewHolder.addNotifyListener(contentViewHolder)//表示contentViewHolder要监听pageViewHolder发出的通知
	 * 3、pageViewHolder里面在点击按钮时调用sendNotify就可以向监听自己的监听器发出通知
	 * 
	 * PENGJU
	 * 2012-12-28 下午9:53:21
	 * @param listener
	 */
	public final void addNotifyListener(ViewHolder listener) {
		if (listener!=null && listener!=this) {
			synchronized (notifyHolders) {
				notifyHolders.add(listener);
			}
		}
	}
	
	/**
	 * 删除一个通知监听器
	 * PENGJU
	 * 2012-12-28 下午10:06:23
	 * @param listener
	 */
	public final void removeNotifyListener(ViewHolder listener) {
		if (listener!=null) {
			synchronized (notifyHolders) {
				notifyHolders.remove(listener);
			}
		}
	}
	
	/**
	 * 清空监听自己的所有监听器
	 * PENGJU
	 * 2012-12-28 下午10:06:44
	 */
	public void clearNotifyListeners() {
		synchronized (notifyHolders) {
			notifyHolders.clear();
		}
	}
	
	/**
	 * 收到通知
	 * 在这里不能调用sendNotify
	 * 但是可以dispatchNotify
	 * PENGJU
	 * 2012-10-24 下午2:30:11
	 * @param notifyId
	 * @param object
	 */
	protected void onNotify(int notifyId,Object object) {
		
	}
	
	/**
	 * 向该Viewholder发送通知并传递给所有监听器
	 * 该方法会触发该ViewHolder的所有通知监听器和自身
	 * 比如一个ViewHolder要监听另一个ViewHolder收到的通知并作出反应可将它加到监听器列表中
	 * PENGJU
	 * 2012-10-24 下午2:30:11
	 * @param notifyId
	 * @param dispatch 是否分发给子组件
	 * @param wrapper
	 */
	public void sendNotify(int notifyId,boolean dispatch,Object object) {
		onNotify(notifyId, object);
		if (dispatch) {
			dispatchNotify(notifyId, object);
		}
	}
	
	protected final void dispatchNotify(int notifyId,Object object){
		synchronized (notifyHolders) {
			for (ViewHolder listener : notifyHolders) {
				listener.onNotify(notifyId, object);
			}
		}
	}
	
	/**
	 * 设置视图数据
	 * 在DefaultListAdapter中使用
	 * lzw
	 * 2013-5-12 下午9:50:56
	 * @param wrapper
	 */
	public void setItem(int index, Object data){}
	
	/**
	 * 向UI线程发送消息
	 * 这里直接通过活动的handler发送消息
	 * 通过重载handleMessage在UI线程中进行处理
	 * PENGJU
	 * 2012-12-28 下午9:36:43
	 * @param msgId 消息ID
	 * @param data 消息附带的数据
	 */
	public void postMessage(int msgId, Object data) {
		getActivity().postMessage(msgId, data,this);
	}

	/**
	 * 向UI线程发送消息
	 * 这里直接通过活动的handler发送消息
	 * 通过重载handleMessage在UI线程中进行处理
	 * PENGJU
	 * 2012-12-28 下午9:38:16
	 * @param msgId 消息ID
	 * @param data 消息附带的数据
	 * @param delayMillis 等待的毫秒数，消息将等待指定毫秒后发送
	 */
	public void postMessage(int msgId, Object data, long delayMillis) {
		getActivity().postMessage(msgId, data, delayMillis,this);
	}
	
	public void removeMessages(int msgId) {
		getActivity().removeMessages(msgId);
	}
	
	/**
	 * 获取postMessage发送的消息
	 * 属于UI线程
	 */
	public void handleMessage(int id, Object data){
		if (id==MSG_DISMISS_MENU) {
			dismissMenu();
		}
	}
	
	
	/****************活动状态*******************/
	
	/**
	 * 监听活动的生命周期,以便进行响应
	 * PENGJU
	 * 2012-12-28 下午9:52:53
	 * @param state
	 */
	protected void onActivityStateChange(int state,Bundle bundle){
		LogManager.i(getClass().getSimpleName(),"onActivityStateChange state=%d",state);
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogManager.i(getClass().getSimpleName(),"onActivityResult requestCode=%d resultCode=%d data=%s",requestCode,resultCode,data);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean handled=false;
		List<ViewHolder> childrenList = childrenHolders;
		int lastIndex = childrenList.size()-1;
		while (lastIndex>-1 && !handled) {
			ViewHolder h = childrenList.get(lastIndex--);
			handled=(handled||h.onKeyDown(keyCode, event));
		}
		
		if (!handled && (keyCode==KeyEvent.KEYCODE_BACK && (getMode()&MODE_MODULE_VIEW)==MODE_MODULE_VIEW)) {
			if ((getMode()&MODE_FLAG_FLIP)==MODE_FLAG_FLIP) {
				closeModuleViewByFlipping();
			}else {
				closeModuleView(true);
			}
			handled = true;
		}
		
		return handled;
	}
	
	
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean handled=false;
		List<ViewHolder> childrenList = childrenHolders;
		int lastIndex = childrenList.size()-1;
		while (lastIndex>-1 && !handled) {
			ViewHolder h = childrenList.get(lastIndex--);
			handled=(handled||h.onKeyLongPress(keyCode, event));
		}
		return handled;
	}
	
	
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean handled=false;
		
		List<ViewHolder> childrenList = childrenHolders;
		int lastIndex = childrenList.size()-1;
		while (lastIndex>-1 && !handled) {
			ViewHolder h = childrenList.get(lastIndex--);
			handled=(handled||h.onKeyMultiple(keyCode, repeatCount, event));
		}
		return handled;
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean handled=false;
		List<ViewHolder> childrenList = childrenHolders;
		int lastIndex = childrenList.size()-1;
		while (lastIndex>-1 && !handled) {
			ViewHolder h = childrenList.get(lastIndex--);
			handled=(handled||h.onKeyUp(keyCode, event));
		}
		return handled;
	}
	
	/***************活动状态结束***************/
	
	/*************** holder生命周期 ***************/
	
	public final void dispathWillAppear(ViewHolder holder,boolean anim){
		holder.onViewWillAppear(anim);
		
		for (ViewHolder h : holder.childrenHolders) {
			if (h.isDuplicateParentState()) {
				h.dispathWillAppear(h, anim);
			}
		}
		
		for (ViewHolderStateListener listener : holder.stateListeners) {
			listener.onHolderWillAppear(holder, anim);
		}
	}
	public final void dispathWillDisappear(ViewHolder holder,boolean anim){
		holder.onViewWillDisappear(anim);
		holder.clearFocus();
		
		for (ViewHolder h : holder.childrenHolders) {
			if (h.isDuplicateParentState()) {
				h.dispathWillDisappear(h, anim);
			}
		}
		for (ViewHolderStateListener listener : holder.stateListeners) {
			listener.onHolderWillDisappear(holder, anim);
		}
	}
	public final void dispathDidAppear(ViewHolder holder,boolean anim){
		
		if (!holder.viewDidAppeared) {
			//第一次显示
			holder.viewDidAppeared = true;
			holder.onViewDidAppearAtFirstTime(anim);
		}
		
		holder.onViewDidAppear(anim);
		
		// 这里一定要这样调，软键盘才会出来，不晓得为啥
		holder.focus();
		holder.clearFocus();
		holder.focus();
		holder.clearFocus();
		
		for (ViewHolder h : holder.childrenHolders) {
			if (h.isDuplicateParentState()) {
				h.dispathDidAppear(h, anim);
			}
		}
		
		for (ViewHolderStateListener listener : holder.stateListeners) {
			listener.onHolderDidAppear(holder, anim);
		}
	}
	public final void dispathDidDisappear(ViewHolder holder,boolean anim){
		holder.onViewDidDisappear(anim);
		
		for (ViewHolder h : holder.childrenHolders) {
			if (h.isDuplicateParentState()) {
				h.dispathDidDisappear(h, anim);
			}
		}
		
		for (ViewHolderStateListener listener : holder.stateListeners) {
			listener.onHolderDidDisappear(holder, anim);
		}
	}
	public final void dispathAttached(ViewHolder holder){
		holder.onViewAttached();
		bindNotificationListener(holder);
		
		for (ViewHolder h : holder.childrenHolders) {
			if (h.isDuplicateParentState()) {
				h.dispathAttached(h);
			}
		}
		
		for (ViewHolderStateListener listener : holder.stateListeners) {
			listener.onHolderAttached(holder);
		}
	}
	public final void dispathDettached(ViewHolder holder){
		unbindNotificationListener(holder);
		holder.onViewDetached();
		
		for (ViewHolder h : holder.childrenHolders) {
			if (h.isDuplicateParentState()) {
				h.dispathDettached(h);
			}
		}
		
		
		for (ViewHolderStateListener listener : holder.stateListeners) {
			listener.onHolderDetached(holder);
		}
	}
	
	/**
	 * 视图将要显示
	 * @param animated 是否动画显示
	 */
	public void onViewWillAppear(boolean animated){
		LogManager.i(getClass().getSimpleName(),"onViewWillAppear(%s)",String.valueOf(animated));
	}
	
	/**
	 * 视图将要隐藏
	 * @param animated 是否动画
	 */
	public void onViewWillDisappear(boolean animated){
		
		LogManager.i(getClass().getSimpleName(),"onViewWillDisappear(%s)",String.valueOf(animated));
	}
	/**
	 * 视图完成显示
	 * @param animated
	 */
	public void onViewDidAppear(boolean animated){
		
		LogManager.i(getClass().getSimpleName(),"onViewDidAppear(%s)",String.valueOf(animated));
	}
	/**
	 * 视图完成隐藏
	 * @param animated
	 */
	public void onViewDidDisappear(boolean animated){
		LogManager.i(getClass().getSimpleName(),"onViewDidDisappear(%s)",String.valueOf(animated));
	}
	/**
	 * 视图添加到其他视图上
	 */
	public void onViewAttached(){
		LogManager.i(getClass().getSimpleName(),"onViewAttached");
	}
	/**
	 * 视图从其他视图脱离
	 */
	public void onViewDetached(){
		LogManager.i(getClass().getSimpleName(),"onViewDetached");
	}
	
	public void clearFocus() {
		//取消所有焦点
		if (getView() instanceof ViewGroup) {
			ViewGroup g=(ViewGroup) getView();
			View c=g.getFocusedChild();
			if (c!=null) {
				if (c instanceof TextView) {
					AppUtility.hideInputSoft(c);
				}
				c.clearFocus();
			}
		}
		getView().clearFocus();
	}
	
	public void focus() {
		getView().requestFocus();
	}
	
	
	public ViewHolder getParent() {
		return parent;
	}
	public void setParent(ViewHolder parent) {
		this.parent = parent;
	}
	
	
	public void showAsModuleView(boolean animate) {
		if (getActivity().getState()==ACTIVITY_DESTROY) {
			return;
		}
		if (!animate) {
			showAsModuleView((Animation)null);
		}else {
			Animation animation=AnimationFactory.getAnimation(AnimationFactory.ANIM_T_BOTTOM_IN);
			animation.setInterpolator(new DecelerateInterpolator(1.3f));
			showAsModuleView(animation);
		}
	}
	
	public void closeModuleView(boolean animate) {
		if (getActivity().getState()==ACTIVITY_DESTROY) {
			return;
		}
		if (!animate) {
			closeModuleView((Animation)null);
		}else {
			Animation animation = AnimationFactory.getAnimation(AnimationFactory.ANIM_T_BOTTOM_OUT);
			animation.setInterpolator(new DecelerateInterpolator(1.3f));
			closeModuleView(animation);
		}
	}
	
	public void showAsModuleView(int animationType) {
		showAsModuleView(AnimationFactory.getAnimation(animationType));
	}
	
	public void closeModuleView(int animationType) {
		closeModuleView(AnimationFactory.getAnimation(animationType));
	}
	
	
	public void showAsModuleViewByFlipping(){
		if (getActivity().getState()==ACTIVITY_DESTROY) {
			return;
		}
		
		if ((getMode()&MODE_MODULE_VIEW)==MODE_MODULE_VIEW) {//已经在显示了
			return;
		}
		setMode((short)(MODE_MODULE_VIEW | MODE_FLAG_FLIP));
		
		final ViewHolder root = getActivity().getRootViewHolder();
		ViewHolder       tmp  = null;
		
		List<ViewHolder> childrenList = root.childrenHolders;
		int lastIndex = childrenList.size()-1;
		if (lastIndex>-1) {
			tmp = childrenList.get(lastIndex);
		}
		
		if (tmp==null) {
			return;
		}
		
		final ViewHolder target = tmp;
		
		final float zVal = 0.7f;
		final int   dur  = getAnimationDuration();
		
		Rotate3dAnimation animation=new Rotate3dAnimation(0, -90, 0.5f, 0.5f, zVal, true);
		animation.setInterpolator(new AccelerateInterpolator());
		animation.setDuration(dur);
		animation.setFillAfter(false);
		
		AnimationListener listener=new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				dispathWillDisappear(target, true);
			}
			public void onAnimationRepeat(Animation animation) {}
			
			@SuppressWarnings("deprecation")
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				target.getView().setVisibility(View.GONE);
				dispathDidDisappear(target, true);
				
				final View view = ViewHolder.this.getView();
				view.setVisibility(View.VISIBLE);
				view.setClickable(true);//挡掉所有点击,有可能会转发给后面的View
				
				Rotate3dAnimation showAnimation=new Rotate3dAnimation(90, 0, 0.5f, 0.5f, zVal, false);
				showAnimation.setInterpolator(new DecelerateInterpolator());
				showAnimation.setDuration(dur);
				showAnimation.setFillAfter(false);
				
				ViewGroup.LayoutParams params=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
				root.addChild(showAnimation, ViewHolder.this,params);
			}
		};
		
		animation.setAnimationListener(listener);
		enhanceAnimation(animation, target.getView());
		target.getView().startAnimation(animation);
	}
	
	public void closeModuleViewByFlipping(){
		if (getActivity().getState()==ACTIVITY_DESTROY) {
			return;
		}
		
		if ((getMode()&MODE_MODULE_VIEW)==MODE_MODULE_VIEW) {//在显示
			setMode(MODE_PLAIN_VIEW);
			
			final ViewHolder root = getActivity().getRootViewHolder();
			ViewHolder       tmp  = null;
			
			List<ViewHolder> childrenList = root.childrenHolders;
			int lastIndex = childrenList.size()-2;//跳过自己
			if (lastIndex>-1) {
				tmp = childrenList.get(lastIndex);
			}
			
			if (tmp==null) {
				return;
			}
			
			final ViewHolder target = tmp;
			
			final float zVal = 0.7f;
			final int   dur  = getAnimationDuration();
			
			Rotate3dAnimation animation=new Rotate3dAnimation(0, 90, 0.5f, 0.5f, zVal, true);
			animation.setInterpolator(new AccelerateInterpolator());
			animation.setDuration(dur);
			
			ViewHolderStateAdapter adapter=new ViewHolderStateAdapter(){
				@Override
				public void onHolderDetached(ViewHolder holder) {
					// TODO Auto-generated method stub
					super.onHolderDetached(holder);
					holder.removeViewHolderStateListener(this);
					
					Rotate3dAnimation showBackAnimation=new Rotate3dAnimation(-90, 0, 0.5f, 0.5f, zVal, false);
					showBackAnimation.setInterpolator(new DecelerateInterpolator());
					showBackAnimation.setDuration(dur);
					showBackAnimation.setFillAfter(false);
					
					AnimationListener showBackListener=new AnimationListener() {
						
						public void onAnimationStart(Animation animation) {
							// TODO Auto-generated method stub
							dispathWillAppear(target, true);
						}
						
						public void onAnimationRepeat(Animation animation) {}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							// TODO Auto-generated method stub
							dispathDidAppear(target, true);
						}
					};
					
					showBackAnimation.setAnimationListener(showBackListener);
					target.getView().setVisibility(View.VISIBLE);
					enhanceAnimation(showBackAnimation, target.getView());
					target.getView().startAnimation(showBackAnimation);
				}
			};
			
			ViewHolder.this.addViewHolderStateListener(adapter);
			root.removeChild(animation, ViewHolder.this);
		}
	}
	
	public void showAsModuleView(Animation animation) {
		if (getActivity().getState()==ACTIVITY_DESTROY) {
			return;
		}
		
		if ((getMode()&MODE_MODULE_VIEW)==MODE_MODULE_VIEW) {//已经在显示了
			return;
		}
		setMode(MODE_MODULE_VIEW);
		getView().setClickable(true);//挡掉所有点击,有可能会转发给后面的View
		
		boolean shouldAnimated = (animation != null);
		
		// 通知所有holder视图将不可见
		for (ViewHolder c : getActivity().getRootViewHolder().getChildHolders()) {
			c.dispathWillDisappear(c, shouldAnimated);
		}
		
		final ViewHolderStateAdapter adapter = new ViewHolderStateAdapter(){
			@Override
			public void onHolderDidAppear(ViewHolder holder, boolean animated) {
				// TODO Auto-generated method stub
				super.onHolderDidAppear(holder, animated);
				for (ViewHolder c : getActivity().getRootViewHolder().getChildHolders()) {
					if (c!=holder) {
						c.dispathDidDisappear(c, animated);
					}
				}
				holder.removeViewHolderStateListener(this);
			}
		};
		this.addViewHolderStateListener(adapter);
		
		if (!shouldAnimated) {
			getActivity().getRootViewHolder().addChild(this);
		}else {
			initAnimations(animation);
			getActivity().getRootViewHolder().addChild(animation, this);
		}
	}
	
	public void closeModuleView(Animation animation) {
		if (getActivity().getState()==ACTIVITY_DESTROY) {
			return;
		}
		
		if ((getMode()&MODE_MODULE_VIEW)==MODE_MODULE_VIEW) {//在显示
			setMode(MODE_PLAIN_VIEW);
			
			final boolean shouldAnimated = (animation != null);
			for (ViewHolder c : getActivity().getRootViewHolder().getChildHolders()) {
				if (c!=this) {
					c.dispathWillAppear(c, shouldAnimated);
				}
			}
			
			final ViewHolderStateAdapter adapter = new ViewHolderStateAdapter(){
				@Override
				public void onHolderDetached(ViewHolder holder) {
					// TODO Auto-generated method stub
					super.onHolderDetached(holder);
					for (ViewHolder c : getActivity().getRootViewHolder().getChildHolders()) {
						if (c!=holder) {
							c.dispathDidAppear(c, shouldAnimated);
						}
					}
					holder.removeViewHolderStateListener(this);
				}
			};
			this.addViewHolderStateListener(adapter);
			
			if (!shouldAnimated) {
				getActivity().getRootViewHolder().removeChild(this);
			}else {
				initAnimations(animation);
				getActivity().getRootViewHolder().removeChild(animation, this);
			}
		}
	}
	
	
	/*************************动画监听器部分*************************/
	public final static int TRANSITION_APPEAR	=1<<3;
	public final static int TRANSITION_DISAPPEAR=1<<4;
	
	private class HolderViewAnimationListener implements AnimationListener{
		
		private ViewHolder holder;
		private Animation  animation;
		
		private int transitionType;
		
		public HolderViewAnimationListener(ViewHolder holder,Animation animation,int type){
			this.holder=holder;
			this.animation=animation;
			this.transitionType=type;
		}
		
		public void postAnimation() {
			this.animation.setAnimationListener(this);
			this.holder.getView().clearAnimation();
			enhanceAnimation(animation, holder.getView());
			this.holder.getView().startAnimation(this.animation);
		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			//保存最新动画
			animatingArray.put(holder.getView().hashCode(), this);
			startedAnimationCount++;
			
			//回调
			if (transitionType==TRANSITION_APPEAR) {
				dispathWillAppear(holder, true);
			}else {
				dispathWillDisappear(holder, true);
			}
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			if (transitionType==TRANSITION_DISAPPEAR) {
				dispathDidDisappear(holder, true);
			}else {
				dispathDidAppear(holder, true);
			}
			
			finishedAnimationCount++;
			
			//所有动画完成
			if (startedAnimationCount==finishedAnimationCount) {
				for (int i = 0; i < animatingArray.size(); i++) {
					HolderViewAnimationListener l=animatingArray.get(animatingArray.keyAt(i));
					if (l.transitionType==TRANSITION_DISAPPEAR) {
						removeChild(l.holder,true);
					}
				}
				
				animatingArray.clear();
				startedAnimationCount=0;
				finishedAnimationCount=0;
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public NavigationViewHolder getNavigationViewHolder() {
		return navigationViewHolder;
	}
	
	/**
	 * 导航栏
	 * @author 陆振文[PENGJU]
	 * @email pengju114@163.com
	 */
	public class NavigationBar{
		
		private View navigationLeftView;
		private View navigationRightView;
		private View navigationCenterView;
		
		private String   title;
		private TextView titleView;
		
		private String   gobackText;
		
		private boolean  hideGobackButton = false;
		Button           defaultGobackButton;
		/**
		 * 获取导航栏视图
		 * @return
		 */
		public View getNavigationCenterView() {
			return navigationCenterView;
		}
		public void setNavigationCenterView(View navigationCenterView) {
			this.navigationCenterView = navigationCenterView;
		}
		public void setNavigationCenterView(int layout) {
			setNavigationCenterView(getActivity().defaultInflater().inflate(layout, null));
		}
		public void setNavigationCenterView(View navigationCenterView,View.OnClickListener clickListener) {
			setNavigationCenterView(navigationCenterView);
			if (getNavigationCenterView()!=null) {
				getNavigationCenterView().setOnClickListener(clickListener);
			}
		}
		public void setNavigationCenterView(int layout,View.OnClickListener clickListener) {
			setNavigationCenterView(getActivity().defaultInflater().inflate(layout, null),clickListener);
		}
		
		public void setNavigationCenterView(View navigationCenterView,View.OnClickListener clickListener,int... viewIds) {
			setNavigationCenterView(navigationCenterView);
			if (navigationCenterView!=null && viewIds!=null) {
				for (int i : viewIds) {
					View v=navigationCenterView.findViewById(i);
					if (v!=null) {
						v.setOnClickListener(clickListener);
					}
				}
			}
		}
		public void setNavigationCenterView(int layout,View.OnClickListener clickListener,int... viewIds) {
			setNavigationCenterView(getActivity().defaultInflater().inflate(layout, null),clickListener,viewIds);
		}
		
		public View getNavigationLeftView() {
			return navigationLeftView;
		}
		public void setNavigationLeftView(View navigationLeftView) {
			this.navigationLeftView = navigationLeftView;
		}
		public void setNavigationLeftView(int layout) {
			setNavigationLeftView(getActivity().defaultInflater().inflate(layout, null));;
		}
		
		public void setNavigationLeftView(View navigationLeftView,View.OnClickListener listener) {
			setNavigationLeftView(navigationLeftView);
			if (navigationLeftView!=null) {
				navigationLeftView.setOnClickListener(listener);
			}
		}
		public void setNavigationLeftView(int layout,View.OnClickListener listener) {
			setNavigationLeftView(layout);
			if (getNavigationLeftView()!=null) {
				getNavigationLeftView().setOnClickListener(listener);
			}
		}
		
		public void setNavigationLeftView(View navigationLeftView,View.OnClickListener listener,int... viewIds) {
			setNavigationLeftView(navigationLeftView);
			if (navigationLeftView!=null && viewIds!=null) {
				for (int i : viewIds) {
					View c=navigationLeftView.findViewById(i);
					if (c!=null) {
						c.setOnClickListener(listener);
					}
				}
			}
		}
		public void setNavigationLeftView(int layout,View.OnClickListener listener,int... viewIds) {
			setNavigationLeftView(getActivity().defaultInflater().inflate(layout, null), listener, viewIds);
		}
		
		
		public View getNavigationRightView() {
			return navigationRightView;
		}
		public void setNavigationRightView(View navigationRightView) {
			this.navigationRightView = navigationRightView;
		}
		public void setNavigationRightView(int layout) {
			setNavigationRightView(getActivity().defaultInflater().inflate(layout, null));
		}
		public void setNavigationRightView(View navigationRightView,View.OnClickListener listener) {
			this.navigationRightView = navigationRightView;
			if (navigationRightView!=null) {
				navigationRightView.setOnClickListener(listener);
			}
		}
		public void setNavigationRightView(int layout,View.OnClickListener listener) {
			setNavigationRightView(getActivity().defaultInflater().inflate(layout, null),listener);
		}
		public void setNavigationRightView(View navigationRightView,View.OnClickListener listener,int... viewIds) {
			setNavigationRightView(navigationRightView);
			if (navigationRightView!=null && viewIds !=null) {
				for (int i : viewIds) {
					View c=navigationRightView.findViewById(i);
					if (c!=null) {
						c.setOnClickListener(listener);
					}
				}
			}
		}
		public void setNavigationRightView(int layout,View.OnClickListener listener,int... viewIds) {
			setNavigationRightView(getActivity().defaultInflater().inflate(layout, null),listener,viewIds);
		}
		
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
			getTitleView().setText(title);
		}
		public void setTitle(int stringResId) {
			getTitleView().setText(stringResId);
			this.title=getTitleView().getText().toString();
		}
		
		public TextView getTitleView() {
			if (titleView==null) {
				titleView=new TextView(getActivity());
				titleView.setSingleLine(true);
				titleView.setTextColor(ViewHolder.NavigationBarParams.TitleTextColor);
				titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, ViewHolder.NavigationBarParams.TitleTextSize);
				titleView.setEllipsize(TruncateAt.MIDDLE);
			}
			return titleView;
		}
		
		public void setGobackText(String gobackText) {
			this.gobackText = gobackText;
		}
		public String getGobackText() {
			return gobackText;
		}
		
		public void setHideGobackButton(boolean hideGobackButton) {
			this.hideGobackButton = hideGobackButton;
		}
		public boolean isHideGobackButton() {
			return hideGobackButton;
		}
		
		@SuppressWarnings("deprecation")
		public Button getDefaultGobackButton() {
			if (defaultGobackButton==null) {
				defaultGobackButton=new Button(getActivity());
				defaultGobackButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, ViewHolder.NavigationBarParams.GobackButtonTextSize);
				
				
				int[] color = new int[]{
						ViewHolder.NavigationBarParams.GobackButtonPlainTextColor,
						ViewHolder.NavigationBarParams.GobackButtonPressedTextColor};
				
				int[][] states = new int[][]{
						{-android.R.attr.state_pressed},
						{android.R.attr.state_pressed}
				};
				ColorStateList textColorStateList = new ColorStateList(states, color);
				
				defaultGobackButton.setTextColor(textColorStateList);
				if (ViewHolder.NavigationBarParams.GobackButtonBackgroundResource != 0) {
					defaultGobackButton.setBackgroundResource(ViewHolder.NavigationBarParams.GobackButtonBackgroundResource);
				}else {
					FontMetrics metrics = defaultGobackButton.getPaint().getFontMetrics();
					
					GobackArrowDrawable drawable = new GobackArrowDrawable(ViewHolder.NavigationBarParams.GobackButtonPlainTextColor,metrics.descent - metrics.ascent);
					
					drawable.setStrokeWidth(DimensionUtility.dp2px(2));
					defaultGobackButton.setBackgroundDrawable(drawable);
				}
				
				defaultGobackButton.setMaxHeight(DimensionUtility.dp2px(ViewHolder.NavigationBarParams.GobackButtonMaxHeight));
				defaultGobackButton.setMinHeight(DimensionUtility.dp2px(ViewHolder.NavigationBarParams.GobackButtonMinHeight));
				defaultGobackButton.setPadding(
						DimensionUtility.dp2px(ViewHolder.NavigationBarParams.GobackButtonPaddingLeft), 
						DimensionUtility.dp2px(ViewHolder.NavigationBarParams.GobackButtonPaddingTop), 
						DimensionUtility.dp2px(ViewHolder.NavigationBarParams.GobackButtonPaddingRight), 
						DimensionUtility.dp2px(ViewHolder.NavigationBarParams.GobackButtonPaddingBottom));
			}
			return defaultGobackButton;
		}
		
		public Button newNavigationBarItem(String text , View.OnClickListener listener){
			Button button = new Button(getActivity());
			button.setText(text);
			button.setOnClickListener(listener);
			
			button.setTextSize(TypedValue.COMPLEX_UNIT_SP, ViewHolder.NavigationBarParams.ItemButtonTextSize);
			
			int[] color = new int[]{
					ViewHolder.NavigationBarParams.ItemButtonPlainTextColor,
					ViewHolder.NavigationBarParams.ItemButtonPressedTextColor};
			
			int[][] states = new int[][]{
					{-android.R.attr.state_pressed},
					{android.R.attr.state_pressed}
			};
			
			ColorStateList textColorStateList = new ColorStateList(states, color);
			
			button.setTextColor(textColorStateList);
			if (ViewHolder.NavigationBarParams.ItemButtonBackgroundResource != 0) {
				button.setBackgroundResource(ViewHolder.NavigationBarParams.ItemButtonBackgroundResource);
			}else {
				button.setBackgroundColor(ViewHolder.NavigationBarParams.ItemButtonBackgroundColor);
			}
			
			button.setMaxHeight(DimensionUtility.dp2px(ViewHolder.NavigationBarParams.ItemButtonMaxHeight));
			button.setMinHeight(DimensionUtility.dp2px(ViewHolder.NavigationBarParams.ItemButtonMinHeight));
			button.setPadding(
					DimensionUtility.dp2px(ViewHolder.NavigationBarParams.ItemButtonPaddingLeft), 
					DimensionUtility.dp2px(ViewHolder.NavigationBarParams.ItemButtonPaddingTop), 
					DimensionUtility.dp2px(ViewHolder.NavigationBarParams.ItemButtonPaddingRight), 
					DimensionUtility.dp2px(ViewHolder.NavigationBarParams.ItemButtonPaddingBottom));
			return button;
		}
		
		
		/**
		 * 将导航栏的组件加到相对布局中
		 */
		public void attachToRelativeLayout(RelativeLayout layout){
			NavigationBar bar=this;
			View leftView=bar.getNavigationLeftView();
			
			View centerView=bar.getNavigationCenterView();
			if (centerView==null) {
				centerView=bar.getTitleView();
			}
			
			View rightView=bar.getNavigationRightView();
			
			if (leftView!=null) {
				RelativeLayout.LayoutParams leftItemParams=getNavigationLeftItemLayoutParams();
				forceAddView(layout, leftView, leftItemParams);
			}
			
			RelativeLayout.LayoutParams centerItemParams=getNavigationCenterItemLayoutParams();
			forceAddView(layout, centerView, centerItemParams);
			
			if (rightView!=null) {
				
				RelativeLayout.LayoutParams rightItemParams=getNavigationRightItemLayoutParams();
				forceAddView(layout, rightView, rightItemParams);
			}
		}
		
		protected RelativeLayout.LayoutParams getNavigationLeftItemLayoutParams(){
			RelativeLayout.LayoutParams leftItemParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			leftItemParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			leftItemParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			return leftItemParams;
		}
		
		protected RelativeLayout.LayoutParams getNavigationCenterItemLayoutParams(){
			RelativeLayout.LayoutParams centerItemParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			centerItemParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
			return centerItemParams;
		}
		protected RelativeLayout.LayoutParams getNavigationRightItemLayoutParams(){
			RelativeLayout.LayoutParams rightItemParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			
			rightItemParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
			rightItemParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			return rightItemParams;
		}
	}
	
	/**
	 * 将一个ViewHolder显示在一个空的活动中，可以配置一个空的活动，给多个holder使用，就可以免去很多配置活动的步骤<br>
	 * 调用此方法前必须先配置好一个公用的活动，跟着重写{@link ViewHolder#getShareActivityAction()}方法返回公用活动的action<br>
	 * @param holderClass 实现了{@link ViewHolder#ViewHolder(BaseActivity)}构造函数的试图持有器
	 * @param requestCode 返回结果时需要用到的请求码，{@link ViewHolder#NO_RESULT}表示不需要返回结果；即指明是不是调用{@link android.app.Activity#startActivityForResult(Intent, int)}
	 * @param param       要传送的参数，可以通过{@link android.app.Activity#getIntent()}.getExtras()获取
	 */
	public void showHolderInShareActivity(Class<? extends ViewHolder> holderClass,int requestCode,Bundle param) {
		Intent intent=new Intent(getShareActivityAction());
		intent.putExtra(EXTRA_HOLDER_CLASS, holderClass);
		if (param!=null) {
			intent.putExtras(param);
		}
		
		if (requestCode!=NO_RESULT) {
			getActivity().startActivityForResult(intent, requestCode);
		}else {
			getActivity().startActivity(intent);
		}
	}
	
	/**
	 * 获取共享活动action
	 * @return
	 */
	protected String getShareActivityAction(){
		return null;
	}
	
	
	public final void forceAddView(ViewGroup group,View child,ViewGroup.LayoutParams params){
		if (group!=null && child!=null) {
			if (child.getParent()!=null && (child.getParent() instanceof ViewGroup)) {
				((ViewGroup)child.getParent()).removeView(child);
			}
			
			if (params!=null) {
				group.addView(child, params);
			}else {
				group.addView(child);
			}
		}
	}
	
	
	/**
	 * 
	 * lzw
	 * 2014年5月28日 下午11:08:50
	 * @return 显示的对话框，当活动已销毁则返回null
	 */
	public BaseDialog showInDialog() {
		if (getActivity().getState()==ACTIVITY_DESTROY) {
			return null;
		}
		HolderDialog dialog=new HolderDialog(this);
		dialog.show();
		return dialog;
	}
	
	public void dismissDialog(){
		if (attachedDialog!=null) {
			attachedDialog.dismiss();
		}
	}
	
	public void cancelDialog(){
		if (attachedDialog!=null) {
			attachedDialog.cancel();
		}
	}
	
	/**
	 * 
	 * lzw
	 * 2014年5月28日 下午11:09:37
	 * @return 显示的视图，当活动已销毁则返回null
	 */
	public PopupWindow showAsBottomMenu() {
		if (getActivity().getState()==ACTIVITY_DESTROY) {
			return null;
		}
		HolderPopupWindow popupWindow=new HolderPopupWindow(this);
		popupWindow.setAnimationStyle(R.style.c_bottom_menu_animstyle);
		popupWindow.showAtLocation(getActivity().getRootViewHolder().getView(), Gravity.BOTTOM, 0, 0);
		return popupWindow;
	}
	public void dismissBottomMenu(){
		postMessage(MSG_DISMISS_MENU, null, 200);
	}
	
	private void dismissMenu(){
		if (attachedPopupWindow!=null) {
			attachedPopupWindow.dismiss();
		}
	}
	
	public Resources getResource(){
		return getActivity().defaultResources();
	}
	
	public String getString(int stringId) {
		// TODO Auto-generated method stub
		return getResource().getString(stringId);
	}
	
	public String getString(int stringId,Object... args) {
		// TODO Auto-generated method stub
		return getResource().getString(stringId, args);
	}
	
	public LayoutInflater getLayoutInflater() {
		return getActivity().defaultInflater();
	}
	
	public void setText(int resId,CharSequence text){
		TextView textView=findViewById(resId);
		if (textView!=null) {
			textView.setText(text);
		}
	}
	
	public String getText(int resId){
		TextView textView=findViewById(resId);
		if (textView!=null) {
			return textView.getText().toString();
		}
		return null;
	}
	
	/**
	 * 当第一次可见时（也即第一次触发{@link #onViewDidAppear(boolean)}时）调用一次，而且仅调用一次。
	 * 后面即使多次触发{@link #onViewDidAppear(boolean)}也不会再次调用此方法
	 * lzw
	 * 2014年3月23日 下午10:38:27
	 */
	protected void onViewDidAppearAtFirstTime(boolean animated){
		LogManager.i(getClass().getSimpleName(),"onViewDidAppearAtFirstTime");
	}
	
	public static interface ViewHolderStateListener{
		public void onHolderAttached(ViewHolder holder);
		public void onHolderDetached(ViewHolder holder);
		
		public void onHolderWillAppear(ViewHolder holder, boolean animated);
		public void onHolderDidAppear(ViewHolder holder, boolean animated);
		
		public void onHolderWillDisappear(ViewHolder holder, boolean animated);
		public void onHolderDidDisappear(ViewHolder holder, boolean animated);
	}
	
	public static class ViewHolderStateAdapter implements ViewHolderStateListener{

		public void onHolderAttached(ViewHolder holder) {}

		public void onHolderDetached(ViewHolder holder) {}

		public void onHolderWillAppear(ViewHolder holder, boolean animated) {}

		public void onHolderDidAppear(ViewHolder holder, boolean animated) {}

		public void onHolderWillDisappear(ViewHolder holder, boolean animated) {}

		public void onHolderDidDisappear(ViewHolder holder, boolean animated) {}
		
	}
	
	protected void enhanceAnimation(Animation animation,View target) {
		if (target instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) target;
			group.setAnimationCacheEnabled(true);
			group.setPersistentDrawingCache(group.getPersistentDrawingCache()|ViewGroup.PERSISTENT_ANIMATION_CACHE);
		}
	}
	
	
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
	 * @param methodName	方法名
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
	 * @param methodId	   方法ID
	 * @param arguments		参数值，null为无参数
	 */
	public void executeMethodInMainThread(long delay,Object target,int methodId,Object... arguments) {
		ThreadUtility.executeMethodInMainThread(delay, target, methodId, arguments);
	}
	
	/**********************线程执行结束******************/
	
	
	/*******************  导航栏组件配置参数部分  ********************/
	public static class NavigationBarParams{
		/** 返回按钮字体大小，单位为SP */
		public static int GobackButtonTextSize = 16;
		/** 返回按钮正常状态字体颜色 */
		public static int GobackButtonPlainTextColor = Color.parseColor("#3D6AC6");
		/** 返回按钮按下状态字体颜色 */
		public static int GobackButtonPressedTextColor = Color.parseColor("#663D6AC6");
		/** 返回按钮背景 */
		public static int GobackButtonBackgroundResource = 0;
		/** 返回按钮背景颜色,如果设置了 {@link #GobackButtonBackgroundResource }则忽略此颜色 */
		public static int GobackButtonBackgroundColor = Color.TRANSPARENT;
		
		/** 返回按钮padding，单位为dip */
		public static int GobackButtonPaddingLeft = 12;
		/** 返回按钮padding，单位为dip */
		public static int GobackButtonPaddingTop = 1;
		/** 返回按钮padding，单位为dip */
		public static int GobackButtonPaddingRight = 6;
		/** 返回按钮padding，单位为dip */
		public static int GobackButtonPaddingBottom = 1;
		/** 返回按钮最大高度，单位为dip */
		public static int GobackButtonMaxHeight = 32;
		/** 返回按钮最小高度，单位为dip */
		public static int GobackButtonMinHeight = 16;
		
		
		/** 标题字体大小，单位为SP */
		public static int TitleTextSize = 18;
		/** 标题字体颜色 */
		public static int TitleTextColor = Color.BLACK;
		
		
		/** 导航栏普通按钮字体大小，单位为SP */
		public static int ItemButtonTextSize = 16;
		/** 导航栏普通按钮正常状态字体颜色 */
		public static int ItemButtonPlainTextColor = GobackButtonPlainTextColor;
		/** 导航栏普通按钮按下状态字体颜色 */
		public static int ItemButtonPressedTextColor = GobackButtonPressedTextColor;
		/** 导航栏普通按钮背景 */
		public static int ItemButtonBackgroundResource = 0;
		/** 导航栏普通按钮背景颜色，如已设置了 {@link #ItemButtonBackgroundResource}则忽略此颜色 */
		public static int ItemButtonBackgroundColor = Color.TRANSPARENT;
		
		/** 导航栏普通按钮padding，单位为dip */
		public static int ItemButtonPaddingLeft = 6;
		/** 导航栏普通按钮padding，单位为dip */
		public static int ItemButtonPaddingTop = 1;
		/** 导航栏普通按钮padding，单位为dip */
		public static int ItemButtonPaddingRight = 6;
		/** 导航栏普通按钮padding，单位为dip */
		public static int ItemButtonPaddingBottom = 1;
		
		/** 导航栏普通按钮最大高度，单位为dip */
		public static int ItemButtonMaxHeight = 32;
		/** 导航栏普通按钮最小高度，单位为dip */
		public static int ItemButtonMinHeight = 16;
	}
}
