package com.pj.core.viewholders;

import java.util.LinkedHashMap;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabWidget;

import com.pj.core.BaseActivity;
import com.pj.core.R;
import com.pj.core.utilities.DimensionUtility;

public class TabViewHolder extends ViewHolder implements OnTabChangeListener, TabContentFactory {
	
	private TabHost tabHost;
	private LinkedHashMap<String,ViewHolder> tabPages;
	
	private String currentTabId;
	
	private int tabIndicatorGap;
	
	private OnTabChangeListener onTabChangeListener;

	public TabViewHolder(BaseActivity activity) {
		super(activity);
		// TODO Auto-generated constructor stub
		setLayoutResource(R.layout.c_tab_view);
	}

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
		addTab(tabId, tabIndicatorText, null, tabViewHolder);
	}
	
	public void addTab(String tabId,String tabIndicatorText,Drawable indicatorIcon,ViewHolder tabViewHolder) {
		// TODO Auto-generated method stub
		if (tabPages.containsKey(tabId)) {
			throw new IllegalArgumentException("ID为"+tabId+"的页签已存在");
		}
		
		View tabIndicator=getActivity().defaultInflater().inflate(R.layout.c_tab_indicator, null);
		TextView textView=(TextView) tabIndicator.findViewById(R.id.c_label_tab_indicator);
		textView.setText(tabIndicatorText);
		
		ImageView imageView=(ImageView) tabIndicator.findViewById(R.id.c_img_tab_indicator);
		
		if (indicatorIcon!=null) {
			imageView.setImageDrawable(indicatorIcon);
		}else {
			imageView.setVisibility(View.GONE);
		}
		addTab(tabId, tabIndicator, tabViewHolder);
	}
	
	protected void applyTabIndicatorGap(TabWidget tabWidget) {
		if (tabWidget.getChildCount()>0) {
			int c=tabWidget.getChildCount();
			if (c>1) {
				c-=1;//最后一个不处理
				for (int i = 0; i < c; i++) {
					View tabView=tabWidget.getChildTabViewAt(i);
					LinearLayout.LayoutParams params=(LinearLayout.LayoutParams) tabView.getLayoutParams();
					params.setMargins(params.leftMargin, params.topMargin, tabIndicatorGap, params.bottomMargin);
				}
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
	public void onTabChanged(String tabId) {
		// TODO Auto-generated method stub
		ViewHolder oldHolder=tabPages.get(currentTabId);
		currentTabId=tabId;
		ViewHolder currentHolder=tabPages.get(currentTabId);
		
		if (oldHolder!=null) {
			oldHolder.onDeselected();
		}
		if (currentHolder!=null) {
			currentHolder.onSelected();
		}
		if (onTabChangeListener!=null) {
			onTabChangeListener.onTabChanged(tabId);
		}
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
