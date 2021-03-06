package com.pj.core.viewholders;

import java.util.LinkedHashMap;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;

import com.pj.core.BaseActivity;
import com.pj.core.ui.TabItemBackroundDrawable;
import com.pj.core.utilities.DimensionUtility;

public class TabViewHolder extends ViewHolder implements OnTabChangeListener, TabContentFactory {
	
	private TabHost tabHost;
	private LinkedHashMap<String,ViewHolder> tabPages;
	
	private String currentTabId;
	
	private int tabIndicatorGap;
	
	private OnTabChangeListener onTabChangeListener;


	public TabViewHolder(BaseActivity activity, View view) {
		super(activity, view);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void initialize(BaseActivity activity,View view) {
		// TODO Auto-generated method stub
		super.initialize(activity,view);
		tabPages=new LinkedHashMap<String, ViewHolder>();
	}

	@Override
	protected void onApplyView(View view) {
		// TODO Auto-generated method stub
		tabHost=(TabHost) view;
		tabHost.setup();
		tabHost.setOnTabChangedListener(this);
	}

	public void setOnTabChangeListener(OnTabChangeListener onTabChangeListener) {
		this.onTabChangeListener = onTabChangeListener;
	}
	
	public void addTab(String tabId,View tabIndicatorView,ViewHolder tabViewHolder) {
		// TODO Auto-generated method stub
		if (tabPages.containsKey(tabId)) {
			throw new IllegalArgumentException("ID为"+tabId+"的页签已存在");
		}
		tabViewHolder.setParent(this);
		tabPages.put(tabId, tabViewHolder);
		tabHost.addTab(tabHost.newTabSpec(tabId).setIndicator(tabIndicatorView).setContent(this));
		applyTabIndicatorGap(tabHost.getTabWidget());
	}
	
	public void addTab(String tabId,String tabIndicatorText,ViewHolder tabViewHolder) {
		// TODO Auto-generated method stub
		StateListDrawable drawable = new StateListDrawable();
		TabItemBackroundDrawable plain = new TabItemBackroundDrawable();
		TabItemBackroundDrawable focus = new TabItemBackroundDrawable();
		plain.setTintEndColor(Color.TRANSPARENT);
		drawable.addState(new int[]{android.R.attr.state_pressed}, focus);
		drawable.addState(new int[]{android.R.attr.state_selected}, focus);
		drawable.addState(new int[]{}, plain);
		addTab(tabId, tabIndicatorText, drawable, tabViewHolder);
	}
	
	public void addTab(String tabId,String tabIndicatorText,Drawable indicatorIcon,ViewHolder tabViewHolder) {
		// TODO Auto-generated method stub
		if (tabPages.containsKey(tabId)) {
			throw new IllegalArgumentException("ID为"+tabId+"的页签已存在");
		}
		
		tabViewHolder.setParent(this);
		tabPages.put(tabId, tabViewHolder);
		
		if (indicatorIcon!=null) {
			tabHost.addTab(tabHost.newTabSpec(tabId).setIndicator(tabIndicatorText,indicatorIcon).setContent(this));
		}else {
			tabHost.addTab(tabHost.newTabSpec(tabId).setIndicator(tabIndicatorText).setContent(this));
		}
		applyTabIndicatorGap(tabHost.getTabWidget());
	}
	
	protected void applyTabIndicatorGap(TabWidget tabWidget) {
		if (tabWidget.getChildCount()>0) {
			int c=tabWidget.getChildCount();
			if (c>1) {
				tabWidget.setWeightSum(c);
				c-=1;//最后一个不处理
				for (int i = 0; i < c; i++) {
					View tabView=tabWidget.getChildTabViewAt(i);
					LinearLayout.LayoutParams params=(LinearLayout.LayoutParams) tabView.getLayoutParams();
					params.weight = 1;
					params.setMargins(params.leftMargin, params.topMargin, tabIndicatorGap, params.bottomMargin);
				}
				
				View tabView=tabWidget.getChildTabViewAt(c);
				LinearLayout.LayoutParams params=(LinearLayout.LayoutParams) tabView.getLayoutParams();
				params.weight = 1;
			}
		}
	}
	
	public void setTabIndicatorGap(int px) {
		this.tabIndicatorGap = px;
		applyTabIndicatorGap(tabHost.getTabWidget());
	}
	
	public void setTabIndicatorGapWithDp(int dp) {
		setTabIndicatorGap( DimensionUtility.dp2px(dp));
	}
	
	public TabHost getTabHost() {
		return tabHost;
	}
	
	public LinkedHashMap<String, ViewHolder> getTabPages() {
		return tabPages;
	}
	
	public String getCurrentTabId() {
		return currentTabId;
	}
	
	public ViewHolder getCurrentTabPage(){
		return tabPages.get(currentTabId);
	}
	
	@Override
	public void onViewWillAppear(boolean animated) {
		// TODO Auto-generated method stub
		super.onViewWillAppear(animated);
		for (ViewHolder v : tabPages.values()) {
			v.navigationViewHolder = navigationViewHolder;
		}
	}
	
	@Override
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		ViewHolder oldHolder=tabPages.get(currentTabId);
		currentTabId=tabId;
		ViewHolder currentHolder=tabPages.get(currentTabId);
		
		if (oldHolder!=null) {
			oldHolder.onDeselected();
		}
		if (currentHolder!=null) {
			currentHolder.navigationViewHolder = getNavigationViewHolder();
			currentHolder.onSelected();
		}
		if (onTabChangeListener!=null) {
			onTabChangeListener.onTabChanged(tabId);
		}
		
		changeTitle(currentHolder);
		changeNavigationBarItems(currentHolder);
	}
	
	protected void changeTitle(ViewHolder viewHolder){
		if (viewHolder != null) {
			getNavigationBar().setTitle(viewHolder.getNavigationBar().getTitle());
		}else {
			getNavigationBar().setTitle("");
		}
	}
	
	protected void changeNavigationBarItems(ViewHolder viewHolder){
	}

	@Override
	public View createTabContent(String tag) {
		// TODO Auto-generated method stub
		return tabPages.get(tag).getView();
	}

	
	@Override
	protected void onActivityStateChange(int state,Bundle bundle) {
		// TODO Auto-generated method stub
		super.onActivityStateChange(state,bundle);
		for (ViewHolder holder : tabPages.values()) {
			holder.onActivityStateChange(state,bundle);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean handle = false;
		ViewHolder current = getCurrentTabPage();
		if (current!=null) {
			handle = current.onKeyDown(keyCode, event);
		}
		return handle?handle:super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean handle = false;
		ViewHolder current = getCurrentTabPage();
		if (current!=null) {
			handle = current.onKeyLongPress(keyCode, event);
		}
		return handle?handle:super.onKeyLongPress(keyCode, event);
	}
	
	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean handle = false;
		ViewHolder current = getCurrentTabPage();
		if (current!=null) {
			handle = current.onKeyMultiple(keyCode,repeatCount, event);
		}
		return handle?handle:super.onKeyMultiple(keyCode, repeatCount ,event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		boolean handle = false;
		ViewHolder current = getCurrentTabPage();
		if (current!=null) {
			handle = current.onKeyUp(keyCode, event);
		}
		return handle?handle:super.onKeyUp(keyCode, event);
	}
	
	@Override
	public void sendNotify(int notifyId, boolean dispatch, Object object) {
		// TODO Auto-generated method stub
		super.sendNotify(notifyId, dispatch, object);
		if (dispatch) {
			for (ViewHolder holder : tabPages.values()) {
				holder.sendNotify(notifyId, dispatch, object);
			}
		}
	}
}
