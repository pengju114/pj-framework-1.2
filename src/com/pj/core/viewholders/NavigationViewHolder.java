package com.pj.core.viewholders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.pj.core.BaseActivity;
import com.pj.core.managers.LogManager;
import com.pj.core.transition.AnimationFactory;
import com.pj.core.utilities.DimensionUtility;
import com.pj.core.utilities.StringUtility;

import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * 导航视图持有器
 * pj-framework-1.2
 * @author lzw
 * 2014年3月23日 下午4:13:53
 * email: pengju114@163.com
 */
public class NavigationViewHolder extends ViewHolder{
	
	private RelativeLayout navigationBarLayout;
	private FrameLayout    navigationContentLayout;
	
	private boolean animating;
	private List<ViewHolder> pageHolders;
	private HashSet<View> dirtySet;

	private Interpolator  alphaInInterpolator  = new AccelerateInterpolator(1.4f);
	private Interpolator  alphaOutInterpolator = new DecelerateInterpolator(2.0f);
	
	private Interpolator  contentInterpolator  = new AccelerateDecelerateInterpolator();
	
	
	private int animationCount;
	private NavigationAnimationListener animationCounterListener=new NavigationAnimationListener();
	private View.OnClickListener gobackClickListener=new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			NavigationViewHolder.this.pop(true);
		}
	};

	public NavigationViewHolder(ViewHolder firstViewHolder) {
		super(firstViewHolder.getActivity());
		// TODO Auto-generated constructor stub
		setView(generateView());
		
		firstViewHolder.setDuplicateParentState(false);
		push(firstViewHolder, false);
	}
	
	
	
	
	@SuppressWarnings("deprecation")
	private View generateView() {
		// TODO Auto-generated method stub
		RelativeLayout rootLayout = new RelativeLayout(getActivity());
		if (NavigationViewHolder.NavigationViewParams.NavigationBackgroundResource != 0) {
			rootLayout.setBackgroundResource(NavigationViewHolder.NavigationViewParams.NavigationBackgroundResource);
		}else {
			rootLayout.setBackgroundColor(NavigationViewHolder.NavigationViewParams.NavigationBackgroundColor);
		}
		rootLayout.setPadding(
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationPaddingLeft), 
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationPaddingTop), 
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationPaddingRight), 
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationPaddingBottom)
				);
		
		int navBarHeight = DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationBarHeight);
		navigationBarLayout = new RelativeLayout(getActivity());
		if (NavigationViewHolder.NavigationViewParams.NavigationBarBackgroundResource != 0) {
			navigationBarLayout.setBackgroundResource(NavigationViewHolder.NavigationViewParams.NavigationBarBackgroundResource);
		}else {
			navigationBarLayout.setBackgroundColor(NavigationViewHolder.NavigationViewParams.NavigationBarBackgroundColor);
		}
		navigationBarLayout.setPadding(
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationBarPaddingLeft), 
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationBarPaddingTop), 
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationBarPaddingRight), 
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationBarPaddingBottom)
				);
		
		RelativeLayout.LayoutParams navBarParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, navBarHeight);
		navBarParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		navBarParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		
		
		navigationContentLayout = new FrameLayout(getActivity());
		if (NavigationViewHolder.NavigationViewParams.NavigationContentBackgroundResource != 0) {
			navigationContentLayout.setBackgroundResource(NavigationViewHolder.NavigationViewParams.NavigationContentBackgroundResource);
		}else {
			navigationContentLayout.setBackgroundColor(NavigationViewHolder.NavigationViewParams.NavigationContentBackgroundColor);
		}
		
		navigationContentLayout.setPadding(
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationContentPaddingLeft), 
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationContentPaddingTop), 
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationContentPaddingRight), 
				DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationContentPaddingBottom)
				);
		
		RelativeLayout.LayoutParams navContentParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		navContentParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		navContentParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		navContentParams.topMargin = navBarHeight + DimensionUtility.dp2px(NavigationViewHolder.NavigationViewParams.NavigationBarAndContentGap);
		
		navigationBarLayout.setClipToPadding(false);
		navigationContentLayout.setClipToPadding(false);
		
		rootLayout.addView(navigationContentLayout, navContentParams);
		rootLayout.addView(navigationBarLayout, navBarParams);
		
		return rootLayout;
	}




	public RelativeLayout getNavigationBarView() {
		// TODO Auto-generated method stub
		return navigationBarLayout;
	}
	
	public FrameLayout getNavigationContentView() {
		return navigationContentLayout;
	}
	
	public ViewHolder getCurrentTopHolder(){
		return getTop();
	}
	
	@Override
	protected void initialize(BaseActivity activity, View view) {
		
		animating=false;
		pageHolders=Collections.synchronizedList(new ArrayList<ViewHolder>(8));
		dirtySet=new HashSet<View>();
		
		super.initialize(activity, view);
	}

	@Override
	protected void onApplyView(View view) {
		// TODO Auto-generated method stub
	}
	
	
	public void postAnimation(View view,Animation animation,boolean removedWhenFinish){
		if (view!=null && animation!=null) {
			if (removedWhenFinish) {
				dirtySet.add(view);
			}
			view.clearAnimation();
			enhanceAnimation(animation, view);
			view.startAnimation(animation);
		}
	}
	
	private ViewHolder getTop() {
		// TODO Auto-generated method stub
		if (pageHolders.size()>0) {
			return pageHolders.get(pageHolders.size()-1);
		}
		return null;
	}
	
	
	public boolean push(ViewHolder holder,boolean animate) {
		if (animating || pageHolders.contains(holder)) {
			return false;
		}
		ViewHolder topViewHolder=getTop();
		push(topViewHolder, holder, animate);
		
		return true;
	}
	
	protected void push(ViewHolder topViewHolder,ViewHolder holder,boolean animate){
		onTransitionStart();
		
		holder.navigationViewHolder=this;
		pageHolders.add(holder);
		
		NavigationBar bar=holder.getNavigationBar();
		View leftView=bar.getNavigationLeftView();
		
		if (leftView==null && topViewHolder!=null && !bar.isHideGobackButton()) {
			Button back=bar.getDefaultGobackButton();
			if (StringUtility.isEmpty(bar.getGobackText())) {
				back.setText(topViewHolder.getNavigationBar().getTitle());
			}else {
				back.setText(bar.getGobackText());
			}
			
			back.setOnClickListener(gobackClickListener);
			leftView=back;
		}
		
		View centerView=bar.getNavigationCenterView();
		if (centerView==null) {
			centerView=bar.getTitleView();
		}
		
		View rightView=bar.getNavigationRightView();
		
		if (!animate) {
			navigationBarLayout.removeAllViews();
		}
		
		if (leftView!=null) {
			RelativeLayout.LayoutParams leftItemParams=bar.getNavigationLeftItemLayoutParams();
			//先度量试图尺寸，否则getMeasuredWidth会返回0
			leftView.measure(leftItemParams.width, leftItemParams.height);
			forceAddView(navigationBarLayout, leftView, leftItemParams);
		}
		
		RelativeLayout.LayoutParams centerItemParams=bar.getNavigationCenterItemLayoutParams();
		centerView.measure(centerItemParams.width, centerItemParams.height);
		forceAddView(navigationBarLayout, centerView, centerItemParams);
		
		if (rightView!=null) {
			
			RelativeLayout.LayoutParams rightItemParams=bar.getNavigationRightItemLayoutParams();
			forceAddView(navigationBarLayout, rightView, rightItemParams);
		}
		
		
		if (animate) {
			
			if (topViewHolder!=null) {
				NavigationBar preBar=topViewHolder.getNavigationBar();
				View preLeftItemView=preBar.getNavigationLeftView();
				if (preLeftItemView==null && preBar.defaultGobackButton!=null) {
					preLeftItemView=preBar.getDefaultGobackButton();
				}
				
				View preCenterView=preBar.getNavigationCenterView();
				if (preCenterView==null) {
					preCenterView=preBar.getTitleView();
				}
				
				View preRightView=preBar.getNavigationRightView();
				
				
				if (preLeftItemView!=null) {
					postAnimation(preLeftItemView, getLeftItemPushoutAnimation(preLeftItemView), true);
				}
				
				if (preCenterView!=null) {
					postAnimation(preCenterView, getCenterItemPushoutAnimation(preCenterView), true);
				}
				
				if (preRightView!=null) {
					postAnimation(preRightView, getRightItemPushoutAnimation(preRightView), true);
				}
			}
			
			if (leftView!=null) {
				postAnimation(leftView, getLeftItemPushAnimation(leftView), false);
			}
			
			//centerView是肯定有的
			postAnimation(centerView, getCenterItemPushAnimation(centerView), false);
			
			if (rightView!=null) {
				postAnimation(rightView, getRightItemPushAnimation(rightView), false);
			}
		}
		
		if (topViewHolder!=null) {
			if (animate) {
				removeChild(getContentPushoutAnimation(topViewHolder), topViewHolder);
			}else {
				removeChild(topViewHolder);
			}
		}
		if (animate) {
			addChild(getContentPushAnimation(holder),navigationContentLayout, holder);
		}else {
			addChild(navigationContentLayout,holder);
			onTransitionEnd();
		}
	}
	
	
	protected int getNavigationBarWidth(){
		return navigationBarLayout.getMeasuredWidth();
	}

	private Animation getLeftItemPushAnimation(View leftView) {
		// TODO Auto-generated method stub
		AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);
		alphaAnimation.setInterpolator(alphaInInterpolator);
		
		int Xtype=Animation.ABSOLUTE;
		int Ytype=Animation.RELATIVE_TO_SELF;
		float fromXValue=(getNavigationBarWidth()-leftView.getMeasuredWidth())*0.5f-navigationBarLayout.getPaddingLeft();
		TranslateAnimation translateAnimation=new TranslateAnimation(Xtype, fromXValue, Xtype, 0, Ytype, 0, Ytype, 0);
		translateAnimation.setInterpolator(contentInterpolator);
		
		AnimationSet animationSet=new AnimationSet(false);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		return initAnimations(animationSet);
	}
	
	private Animation getCenterItemPushAnimation(View centerView) {
		// TODO Auto-generated method stub
		AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);
		
		int Xtype=Animation.RELATIVE_TO_PARENT;
		int Ytype=Animation.RELATIVE_TO_SELF;
		float fromXValue=0.6f;
		float toXValue=0f;
		TranslateAnimation translateAnimation=new TranslateAnimation(Xtype, fromXValue, Xtype, toXValue, Ytype, 0, Ytype, 0);
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		return initAnimations(animationSet);
	}
	
	private Animation getRightItemPushAnimation(View rightView) {
		// TODO Auto-generated method stub
		AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);
		initAnimations(alphaAnimation);
		return alphaAnimation;
	}
	
	private Animation getContentPushAnimation(ViewHolder holder) {
		return initAnimations(AnimationFactory.getAnimation(AnimationFactory.ANIM_T_RIGHT_IN));
	}


	private Animation getContentPushoutAnimation(ViewHolder topViewHolder) {
		return initAnimations(AnimationFactory.getAnimation(AnimationFactory.ANIM_T_LEFT_OUT));
	}
	
	private Animation getContentPopinAnimation(ViewHolder holder) {
		return initAnimations(AnimationFactory.getAnimation(AnimationFactory.ANIM_T_LEFT_IN));
	}


	private Animation getContentPopoutAnimation(ViewHolder topViewHolder) {
		return initAnimations(AnimationFactory.getAnimation(AnimationFactory.ANIM_T_RIGHT_OUT));
	}

	
	private Animation getLeftItemPushoutAnimation(View leftItemView) {
		// TODO Auto-generated method stub
		AlphaAnimation alphaAnimation=new AlphaAnimation(1, 0);
		alphaAnimation.setInterpolator(alphaOutInterpolator);
		
		int Xtype=Animation.ABSOLUTE;
		int Ytype=Animation.RELATIVE_TO_SELF;
		float toXValue=navigationBarLayout.getPaddingLeft()-leftItemView.getMeasuredWidth()-DimensionUtility.dp2px(30);;
		float fromXValue=0;
		TranslateAnimation translateAnimation=new TranslateAnimation(Xtype, fromXValue, Xtype, toXValue, Ytype, 0, Ytype, 0);
		translateAnimation.setInterpolator(contentInterpolator);
		
		AnimationSet animationSet=new AnimationSet(false);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		return initAnimations(animationSet);
	}

	private Animation getRightItemPushoutAnimation(View preRightView) {
		// TODO Auto-generated method stub
		return initAnimations(AnimationFactory.getAnimation(AnimationFactory.ANIM_F_FADE_OUT));
	}

	private Animation getCenterItemPushoutAnimation(View centerView) {
		// TODO Auto-generated method stub
		AlphaAnimation alphaAnimation=new AlphaAnimation(1, 0);
		alphaAnimation.setInterpolator(alphaOutInterpolator);
		
		int Xtype=Animation.ABSOLUTE;
		int Ytype=Animation.RELATIVE_TO_SELF;
		
		float toXValue=navigationBarLayout.getPaddingLeft()-(getNavigationBarWidth()-centerView.getMeasuredWidth())*0.5f;
		float fromXValue=0;
		TranslateAnimation translateAnimation=new TranslateAnimation(Xtype, fromXValue, Xtype, toXValue, Ytype, 0, Ytype,0);
		translateAnimation.setInterpolator(contentInterpolator);
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		return initAnimations(animationSet);
	}
	

	public boolean pop(boolean animate){
		if (animating || pageHolders.size()<2) {
			return false;
		}
		onTransitionStart();
		
		ViewHolder currTopHolder=getTop();
		ViewHolder gettingShowHolder=pageHolders.get(pageHolders.size()-2);
		pageHolders.remove(pageHolders.size()-1);
				
		View gsLeftItem=gettingShowHolder.getNavigationBar().getNavigationLeftView();
		if (gsLeftItem==null && gettingShowHolder.getNavigationBar().defaultGobackButton!=null) {
			gsLeftItem=gettingShowHolder.getNavigationBar().getDefaultGobackButton();
		}
		
		View gsCenterItem=gettingShowHolder.getNavigationBar().getNavigationCenterView();
		if (gsCenterItem==null) {
			gsCenterItem=gettingShowHolder.getNavigationBar().getTitleView();
		}
		
		View gsRightItem=gettingShowHolder.getNavigationBar().getNavigationRightView();
		
		if (!animate) {
			navigationBarLayout.removeAllViews();
		}
		
		if (gsLeftItem!=null) {
			forceAddView(navigationBarLayout, gsLeftItem, gettingShowHolder.getNavigationBar().getNavigationLeftItemLayoutParams());
		}
		forceAddView(navigationBarLayout, gsCenterItem, gettingShowHolder.getNavigationBar().getNavigationCenterItemLayoutParams());
		
		if (gsRightItem!=null) {
			forceAddView(navigationBarLayout, gsRightItem, gettingShowHolder.getNavigationBar().getNavigationRightItemLayoutParams());
		}
		
		if (animate) {
			View currLeftItem=currTopHolder.getNavigationBar().getNavigationLeftView();
			if (currLeftItem==null && currTopHolder.getNavigationBar().defaultGobackButton!=null) {
				currLeftItem=currTopHolder.getNavigationBar().getDefaultGobackButton();
			}
			
			View currCenterItem=currTopHolder.getNavigationBar().getNavigationCenterView();
			if (currCenterItem==null) {
				currCenterItem=currTopHolder.getNavigationBar().getTitleView();
			}
			
			View currRightItem=currTopHolder.getNavigationBar().getNavigationRightView();
			
			if (currLeftItem!=null) {
				postAnimation(currLeftItem, getLeftItemPopoutAnimation(currLeftItem), true);
			}
			
			if (currCenterItem!=null) {
				postAnimation(currCenterItem, getCenterItemPopoutAnimation(currCenterItem), true);
			}
			
			if (currRightItem!=null) {
				postAnimation(currRightItem, getRightItemPopoutAnimation(currRightItem), true);
			}
			
			//上一个holder
			if (gsLeftItem!=null) {
				postAnimation(gsLeftItem, getLeftItemPopinAnimation(gsLeftItem), false);
			}
			
			if (gsCenterItem!=null) {
				postAnimation(gsCenterItem, getCenterItemPopinAnimation(gsCenterItem), false);
			}
			
			if (gsRightItem!=null) {
				postAnimation(gsRightItem, getRightItemPopinAnimation(gsRightItem), false);
			}
		}
		//内容部分
		if (animate) {
			removeChild(getContentPopoutAnimation(currTopHolder), currTopHolder);
			addChild(getContentPopinAnimation(gettingShowHolder), navigationContentLayout, gettingShowHolder);
		}else {
			removeChild(currTopHolder);
			addChild(navigationContentLayout, gettingShowHolder);
			onTransitionEnd();
		}
		
		currTopHolder.navigationViewHolder=null;
		return true;
	}
	
	public boolean pop(int count,boolean animate) {
		if (animating) {
			return false;
		}
		
		count--;
		if (count>=(pageHolders.size()-1)) {
			count=pageHolders.size()-2;
		}
		
		for (int i = 0; i < count; i++) {
			ViewHolder holder=pageHolders.remove(pageHolders.size()-2);
			holder.navigationViewHolder=null;
		}
		
		return pop(animate);
	}
	
	public boolean popTo(ViewHolder holder,boolean animate) {
		if (animating) {
			return false;
		}
		
		int index=pageHolders.indexOf(holder);
		if (index<0) {
			return false;
		}
		return popToIndex(index, animate);
	}
	
	public boolean popTo(Class<? extends ViewHolder> clazz,boolean animate) {
		if (animating) {
			return false;
		}
		for (int i = 0; i < pageHolders.size(); i++) {
			ViewHolder holder=pageHolders.get(i);
			if (holder.getClass().equals(clazz)) {
				return popToIndex(i, animate);
			}
		}
		return false;
	}
	
	public boolean setHolder(ViewHolder holder,boolean animate){
		if (animating || pageHolders.contains(holder)) {
			 return false;
		}
		ViewHolder topViewHolder = getTop();
		
		for (ViewHolder h : pageHolders) {
			h.navigationViewHolder = null;
		}
		pageHolders.clear();
		
		holder.getNavigationBar().setHideGobackButton(true);
		push(topViewHolder, holder, animate);
		return true;
	}
	
	
	private boolean popToIndex(int index,boolean animate){
		int pos=pageHolders.size()-2;
		while (pos>index) {
			ViewHolder holder=pageHolders.remove(pos--);
			holder.navigationViewHolder.navigationViewHolder=null;
		}
		return pop(animate);
	}
	
	
	protected Animation getLeftItemPopoutAnimation(View leftView){
		Animation alphaAnimation=new AlphaAnimation(1, 0);
		
		int xtype=Animation.ABSOLUTE;
		int ytype=Animation.RELATIVE_TO_SELF;
		
		float toXValue=(getNavigationBarWidth()-leftView.getMeasuredWidth())*0.5f-navigationBarLayout.getPaddingLeft();
		Animation transAnimation=new TranslateAnimation(xtype, 0, xtype, toXValue, ytype, 0, ytype, 0);
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(transAnimation);
		
		return initAnimations(animationSet);
	}
	
	protected Animation getCenterItemPopoutAnimation(View centerItem){
		AlphaAnimation alphaAnimation=new AlphaAnimation(1, 0);
		int Xtype=Animation.RELATIVE_TO_PARENT;
		int Ytype=Animation.RELATIVE_TO_SELF;
		float fromXValue=0f;
		float toXValue=0.6f;
		TranslateAnimation translateAnimation=new TranslateAnimation(Xtype, fromXValue, Xtype, toXValue, Ytype, 0, Ytype, 0);
		
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		return initAnimations(animationSet);
	}
	
	protected Animation getRightItemPopoutAnimation(View rightItem){
		return initAnimations(new AlphaAnimation(1, 0));
	}
	
	protected Animation getLeftItemPopinAnimation(View leftItem){
		if (leftItem.getMeasuredWidth()==0 && leftItem.getLayoutParams()!=null) {
			leftItem.measure(leftItem.getLayoutParams().width, leftItem.getLayoutParams().height);
		}
		
		AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);
		
		int xtype=Animation.ABSOLUTE;
		int ytype=Animation.RELATIVE_TO_SELF;
		
		float fromXValue=navigationBarLayout.getPaddingLeft()-leftItem.getMeasuredWidth()-DimensionUtility.dp2px(30);
		float toXValue  =0;
		
		TranslateAnimation translateAnimation=new TranslateAnimation(xtype, fromXValue, xtype, toXValue, ytype, 0, ytype, 0);
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		
		return initAnimations(animationSet);
	}
	
	protected Animation getCenterItemPopinAnimation(View centerView){
		if (centerView.getMeasuredWidth()==0 && centerView.getLayoutParams()!=null) {
			centerView.measure(centerView.getLayoutParams().width, centerView.getLayoutParams().height);
		}
		
		AlphaAnimation alphaAnimation=new AlphaAnimation(0, 1);
		
		int xtype=Animation.ABSOLUTE;
		int ytype=Animation.RELATIVE_TO_SELF;
		
		float fromXValue=navigationBarLayout.getPaddingLeft()-(getNavigationBarWidth()-centerView.getMeasuredWidth())*0.5f;
		float toXValue  =0;
		
		TranslateAnimation translateAnimation=new TranslateAnimation(xtype, fromXValue, xtype, toXValue, ytype, 0, ytype, 0);
		
		AnimationSet animationSet=new AnimationSet(true);
		animationSet.addAnimation(alphaAnimation);
		animationSet.addAnimation(translateAnimation);
		
		return initAnimations(animationSet);
	}
	
	protected Animation getRightItemPopinAnimation(View rightItem){
		return initAnimations(new AlphaAnimation(0, 1));
	}
	
	@Override
	protected Animation initAnimations(Animation animation) {
		// TODO Auto-generated method stub
		animation.setAnimationListener(animationCounterListener);
		return super.initAnimations(animation);
	}
	
	private void onTransitionStart(){
		animating=true;
		LogManager.i(getClass().getSimpleName(), "onTransitionStart");
	}
	
	private void onTransitionEnd(){
		LogManager.i(getClass().getSimpleName(), dirtySet);
		for (View view : dirtySet) {
			if (view!=null) {
				view.clearAnimation();
				navigationBarLayout.removeView(view);
			}
		}
		dirtySet.clear();
		animating=false;
		
		if (pageHolders.size()==1) {
			getCurrentTopHolder().setDuplicateParentState(true);
		}
		LogManager.i(getClass().getSimpleName(), "onTransitionEnd");
	}
	
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		 boolean h=super.onKeyDown(keyCode, event);
		
		if (!h && keyCode==KeyEvent.KEYCODE_BACK) {
			return animating?true:pop(true);
		}
		return h;
	}
	
	private class NavigationAnimationListener implements AnimationListener{
		
		public NavigationAnimationListener(){
		}

		@Override
		public void onAnimationStart(Animation animation) {
			// TODO Auto-generated method stub
			NavigationViewHolder.this.animationCount++;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			// TODO Auto-generated method stub
			NavigationViewHolder.this.animationCount--;
			if (NavigationViewHolder.this.animationCount<=0) {
				onTransitionEnd();
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public static class NavigationViewParams{
		/** 导航栏高度 ,单位为dip */
		public static int NavigationBarHeight = 44;
		/**  导航栏背景  */
		public static int NavigationBarBackgroundResource = 0;
		/**  导航栏背景颜色,若已设置{@link #NavigationBarBackgroundResource}则忽略此颜色  */
		public static int NavigationBarBackgroundColor = Color.parseColor("#F3F3F3");
		
		/** 导航栏padding，单位为dip */
		public static int NavigationBarPaddingLeft = 12;
		/** 导航栏padding，单位为dip */
		public static int NavigationBarPaddingTop = 0;
		/** 导航栏padding，单位为dip */
		public static int NavigationBarPaddingRight = 12;
		/** 导航栏padding，单位为dip */
		public static int NavigationBarPaddingBottom = 0;
		
		
		
		
		/**  导航内容容器背景  */
		public static int NavigationContentBackgroundResource = 0;
		/**  导航内容容器背景颜色,若已设置{@link #NavigationContentBackgroundResource}则忽略此颜色  */
		public static int NavigationContentBackgroundColor = Color.LTGRAY;
		
		/** 导航内容容器padding，单位为dip */
		public static int NavigationContentPaddingLeft = 0;
		/** 导航内容容器padding，单位为dip */
		public static int NavigationContentPaddingTop = 0;
		/** 导航内容容器padding，单位为dip */
		public static int NavigationContentPaddingRight = 0;
		/** 导航内容容器padding，单位为dip */
		public static int NavigationContentPaddingBottom = 0;
		
		
		/**  导航视图（导航栏和导航内容容器的父视图）背景  */
		public static int NavigationBackgroundResource = 0;
		/**  导航视图（导航栏和导航内容容器的父视图）背景颜色,若已设置{@link #NavigationBackgroundResource}则忽略此颜色  */
		public static int NavigationBackgroundColor = Color.WHITE;
		
		/** 导航视图（导航栏和导航内容容器的父视图）padding，单位为dip */
		public static int NavigationPaddingLeft = 0;
		/** 导航视图（导航栏和导航内容容器的父视图）padding，单位为dip */
		public static int NavigationPaddingTop = 0;
		/** 导航视图（导航栏和导航内容容器的父视图）padding，单位为dip */
		public static int NavigationPaddingRight = 0;
		/** 导航视图（导航栏和导航内容容器的父视图）padding，单位为dip */
		public static int NavigationPaddingBottom = 0;
		
		/** 导航栏和导航内容容器的间隔，默认0，负数则内容容器嵌入导航栏下面，单位为dip */
		public static int NavigationBarAndContentGap = 0;
	}
}