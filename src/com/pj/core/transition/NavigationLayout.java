package com.pj.core.transition;


import java.util.HashSet;

import com.pj.core.viewholders.ViewHolder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;

public class NavigationLayout extends FrameLayout {
	private static final int CACHE_KEY=com.pj.core.R.drawable.ic_launcher;
	
	public static final int EVENT_ANIMATION_START	=0;
	public static final int EVENT_ANIMATION_FINISH	=1;
	
	public static final int EVENT_VIEW_ATTACHED		=2;
	public static final int EVENT_VIEW_DETACHED		=3;
	
	public static final int TYPE_ADD	=10;
	public static final int TYPE_REMOVE =11;
	
	private HashSet<AnimationHandler> animatingHandlers;
	private HashSet<View> dirtyViews;
	
	private long duration=500;
	private Interpolator showInterpolator=new AccelerateDecelerateInterpolator();
	private Interpolator hideInterpolator=showInterpolator;
	private Interpolator fadeInInterpolator=showInterpolator;
	private Interpolator fadeOutInterpolator=showInterpolator;

	
	
	private NavigationListener navigationListener;
	
	public NavigationLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
	
	public NavigationLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}
	public NavigationLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}
	
	protected void init() {
		// TODO Auto-generated method stub
		animatingHandlers=new HashSet<NavigationLayout.AnimationHandler>();
		dirtyViews=new HashSet<View>();
	}
	
	@Override
	public void addView(View child, int index,android.view.ViewGroup.LayoutParams params) {
		// TODO Auto-generated method stub
		super.addView(child, index, params);
		handleEvent(EVENT_VIEW_ATTACHED, TYPE_ADD,child);
	}
	
	@Override
	public void removeAllViews() {
		// TODO Auto-generated method stub
		View[] children=new View[getChildCount()];
		for (int i = 0; i < children.length; i++) {
			children[i]=getChildAt(i);
		}
		super.removeAllViews();
		for (View view : children) {
			handleEvent(EVENT_VIEW_DETACHED, TYPE_REMOVE,view);
		}
	}
	
	@Override
	public void removeView(View view) {
		// TODO Auto-generated method stub
		super.removeView(view);
		handleEvent(EVENT_VIEW_DETACHED, TYPE_REMOVE,view);
	}
	@Override
	public void removeViewAt(int index) {
		// TODO Auto-generated method stub
		View v=getChildAt(index);
		super.removeViewAt(index);
		handleEvent(EVENT_VIEW_DETACHED, TYPE_REMOVE,v);
	}
	
	@Override
	public void removeViews(int start, int count) {
		// TODO Auto-generated method stub
		View[] childrenViews=new View[count];
		for (int i = 0; i < childrenViews.length; i++) {
			childrenViews[i]=getChildAt(start+i);
		}
		super.removeViews(start, count);
		for (View view : childrenViews) {
			handleEvent(EVENT_VIEW_DETACHED, TYPE_REMOVE,view);
		}
	}
	
	/*********************动画部分*********************/
	
	public void addView(int animationType,View child) {
		// TODO Auto-generated method stub
		animateView(TYPE_ADD, animationType, child,true);
	}
	
	
	public void addView(int animationType,View child, int index) {
		// TODO Auto-generated method stub
		animateView(TYPE_ADD, animationType, child,true);
	}
	
	
	public void addView(int animationType,View child, android.view.ViewGroup.LayoutParams params) {
		// TODO Auto-generated method stub
		animateView(TYPE_ADD, animationType, child,true);
	}
	
	public void addView(int animationType,View child, int width, int height) {
		// TODO Auto-generated method stub
		animateView(TYPE_ADD, animationType, child,true);
	}
	
	public void addView(int animationType,ViewHolder holder) {
		addView(animationType, holder.getView());
	}
	
	
	public void addView(int animationType,ViewHolder holder, android.view.ViewGroup.LayoutParams params) {
		// TODO Auto-generated method stub
		addView(animationType, holder.getView(), params);
	}
	
	public void setView(int showAnimation,int hideAnimation,View view) {
		removeAllViews(hideAnimation);
		addView(showAnimation, view);
	}
	
	public void setView(int animationType,View view) {
		removeAllViews(AnimationFactory.getMatchAnimation(animationType));
		addView(animationType, view);
	}
	public void setView(int showAnimation,int hideAnimation,ViewHolder holder) {
		setView(showAnimation, hideAnimation, holder.getView());
	}
	
	public void setView(int animationType,ViewHolder holder) {
		setView(animationType, holder.getView());
	}
	
	public void removeAllViews(int animationType) {
		while (getChildCount()>1) {
			removeViewAt(0);
		}
		if (getChildCount()>0) {
			animateView(TYPE_REMOVE, animationType, getChildAt(0),false);
		}
	}
	
	
	public void removeView(int animationType,View view) {
		// TODO Auto-generated method stub
		if (view.getParent()!=null) {
			animateView(TYPE_REMOVE, animationType, view,false);
		}
	}
	
	public void removeView(int animationType,ViewHolder holder) {
		// TODO Auto-generated method stub
		removeView(animationType, holder.getView());
	}
	
	public void removeViewAt(int animationType,int index) {
		// TODO Auto-generated method stub
		removeView(animationType, getChildAt(index));
	}
	
	public void animateView(int type,int animationType,View target,boolean isShow) {
		if (target!=null) {
			Animation animation=AnimationFactory.getAnimation(animationType);
			if (animation==null) {
				throw new UnsupportedOperationException("不支持的动画类型");
			}
			initAnimation(animation,isShow);
			AnimationHandler animationHandler=new AnimationHandler(type ,target,animation);
			animation.setAnimationListener(animationHandler);
			
			//有可能不会调用onAnimationFinish
			animatingHandlers.remove(target.getTag(CACHE_KEY));
			target.setTag(CACHE_KEY, null);
			
			//直接忽视正在执行的动画(如果有)并且正在执行的动画不会回调事件监听函数
			animationHandler.run();
		}
	}
	
	
	private void initAnimation(Animation animation,boolean isShow) {
		// TODO Auto-generated method stub
		animation.setFillAfter(true);
		animation.setDuration(duration);
		
		if (animation instanceof AnimationSet) {
			AnimationSet set=(AnimationSet) animation;
			for (Animation a : set.getAnimations()) {
				if (a instanceof AlphaAnimation) {
					a.setInterpolator(isShow?fadeInInterpolator:fadeOutInterpolator);
				}else {
					a.setInterpolator(isShow?showInterpolator:hideInterpolator);
				}
			}
		}else{
			if (animation instanceof AlphaAnimation) {
				animation.setInterpolator(isShow?fadeInInterpolator:fadeOutInterpolator);
			}else {
				animation.setInterpolator(isShow?showInterpolator:hideInterpolator);
			}
		}
	}

	

	protected void handleEvent(int event,int type,View target) {
		if (event==EVENT_ANIMATION_START) {
			if (getFocusedChild()==target) {
				target.clearFocus();
			}
			if (type==TYPE_ADD) {
				onViewShowStart(target);
			}else {
				onViewHideStart(target);
			}
		}else if (event==EVENT_ANIMATION_FINISH) {
			if (getFocusedChild()==target) {
				target.clearFocus();
			}
			if (type==TYPE_REMOVE) {
				onViewHideFinish(target);
			}else {
				onViewShowFinish(target);
			}
		}else if (event==EVENT_VIEW_ATTACHED) {
			onViewAttached(target);
		}else if (event==EVENT_VIEW_DETACHED) {
			onViewDetached(target);
		}
	}
	
	protected void onViewShowStart(View target) {
		if (navigationListener!=null) {
			navigationListener.onShowAnimationStart(this, target);
		}
	}
	protected void onViewHideStart(View target) {
		if (navigationListener!=null) {
			navigationListener.onHideAnimationStart(this, target);
		}
	}
	protected void onViewShowFinish(View target) {
		if (navigationListener!=null) {
			navigationListener.onShowAnimationFinish(this, target);
		}
	}
	protected void onViewHideFinish(View target) {
		if (navigationListener!=null) {
			navigationListener.onHideAnimationFinish(this, target);
		}
	}
	protected void onViewAttached(View target) {
		if (navigationListener!=null) {
			navigationListener.onViewAttached(this, target);
		}
	}
	protected void onViewDetached(View target) {
		if (navigationListener!=null) {
			navigationListener.onViewDetached(this, target);
		}
	}
	
	private void onAnimationFinish(AnimationHandler handler){
		if (handler.actionType==TYPE_REMOVE) {
			dirtyViews.add(handler.animatedView);
		}else {
			dirtyViews.remove(handler.animatedView);
		}
		
		
		if (animatingHandlers.size()==0) {
			
			if (navigationListener!=null) {
				navigationListener.onAllAnimationFinish(this);
			}
			
			for (View view : dirtyViews) {
				removeView(view);
			}
			dirtyViews.clear();
		}
	}
	
	public void stopAllAnimation() {
		for (AnimationHandler handler : animatingHandlers) {
			handler.animatedView.clearAnimation();
		}
	}
	
	public NavigationListener getNavigationListener() {
		return navigationListener;
	}

	public void setNavigationListener(NavigationListener navigationListener) {
		this.navigationListener = navigationListener;
	}
	
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public Interpolator getShowInterpolator() {
		return showInterpolator;
	}
	public void setShowInterpolator(Interpolator showInterpolator) {
		if (showInterpolator!=null) {
			this.showInterpolator=showInterpolator;
		}
	}
	public Interpolator getHideInterpolator() {
		return hideInterpolator;
	}
	public void setHideInterpolator(Interpolator hideInterpolator) {
		if (hideInterpolator!=null) {
			this.hideInterpolator = hideInterpolator;
		}
	}
	public Interpolator getFadeInInterpolator() {
		return fadeInInterpolator;
	}
	public Interpolator getFadeOutInterpolator() {
		return fadeOutInterpolator;
	}
	public void setFadeInInterpolator(Interpolator fadeInInterpolator) {
		if (fadeInInterpolator!=null) {
			this.fadeInInterpolator = fadeInInterpolator;
		}
	}
	public void setFadeOutInterpolator(Interpolator fadeOutInterpolator) {
		if (fadeOutInterpolator!=null) {
			this.fadeOutInterpolator = fadeOutInterpolator;
		}
	}

	private class AnimationHandler implements AnimationListener,Runnable{
		private int actionType;
		private View animatedView;
		
		private Animation animation;
		
		public AnimationHandler(int actionType,View target,Animation animation){
			this.actionType=actionType;
			this.animatedView=target;
			this.animation=animation;
		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			animatingHandlers.add(this);
			animatedView.setTag(CACHE_KEY, this);
			handleEvent(EVENT_ANIMATION_START,actionType, animatedView);
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			animatedView.setTag(CACHE_KEY, null);
			handleEvent(EVENT_ANIMATION_FINISH, actionType, animatedView);
			animatingHandlers.remove(this);
			onAnimationFinish(this);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (actionType==TYPE_ADD && animatedView.getParent()==null) {
				addView(animatedView);
			}
			animatedView.startAnimation(animation);
		}
	}
	
	public interface NavigationListener{
		public void onShowAnimationStart(NavigationLayout navigationLayout,View view);
		public void onShowAnimationFinish(NavigationLayout navigationLayout,View view);
		public void onHideAnimationStart(NavigationLayout navigationLayout,View view);
		public void onHideAnimationFinish(NavigationLayout navigationLayout,View view);
		public void onViewAttached(NavigationLayout navigationLayout,View view);
		public void onViewDetached(NavigationLayout navigationLayout,View view);
		public void onAllAnimationFinish(NavigationLayout navigationLayout);
	}
}
