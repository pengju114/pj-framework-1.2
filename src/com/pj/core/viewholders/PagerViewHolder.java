package com.pj.core.viewholders;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

import com.pj.core.BaseActivity;
import com.pj.core.adapters.ViewPagerAdapter;

public class PagerViewHolder extends ViewHolder implements OnPageChangeListener {
	
	private ViewPager 		 viewPager;
	private List<ViewHolder> pages;
	private ViewPagerAdapter pagerAdapter;
	
	private int previousIndex=0;
	private int currentIndex=-1;
	
	
	private OnPageChangeListener onPageChangeListener;

	public PagerViewHolder(BaseActivity activity) {
		super(activity);
		// TODO Auto-generated constructor stub
		
		ViewPager pager=new ViewPager(activity);
		setView(pager);
	}

	public PagerViewHolder(BaseActivity activity, View viewPager) {
		super(activity, viewPager);
		// TODO Auto-generated constructor stub
	}
	
	public PagerViewHolder(ViewHolder mParent) {
		// TODO Auto-generated constructor stub
		super(mParent);
	}
	
	public PagerViewHolder(ViewHolder mParent,View root){
		super(mParent, root);
	}
	
	@Override
	protected void initialize(BaseActivity activity,View view) {
		// TODO Auto-generated method stub
		super.initialize(activity,view);
		pages=new ArrayList<ViewHolder>();
		pagerAdapter=new ViewPagerAdapter(pages);
	}

	@Override
	protected void onApplyView(View view) {
		// TODO Auto-generated method stub
		viewPager=(ViewPager) view;
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(this);
	}
	
	public void addPage(ViewHolder holder) {
		// TODO Auto-generated method stub
		if (!pages.contains(holder) && holder!=null) {
			pages.add(holder);
			holder.setParent(this);
			pagerAdapter.notifyDataSetChanged();
		}
	}
	
	public ViewPager getViewPager() {
		return viewPager;
	}
	
	public List<ViewHolder> getPages() {
		return pages;
	}
	
	public int getCurrentIndex() {
		return currentIndex;
	}
	public int getPreviousIndex() {
		return previousIndex;
	}
	
	public void setOnPageChangeListener( OnPageChangeListener onPageChangeListener) {
		this.onPageChangeListener = onPageChangeListener;
	}
	
	
	@Override
	protected void onActivityStateChange(int state,Bundle bundle) {
		// TODO Auto-generated method stub
		super.onActivityStateChange(state,bundle);
		for (ViewHolder holder : pages) {
			holder.onActivityStateChange(state,bundle);
		}
	}
	
	@Override
	public void sendNotify(int notifyId, boolean dispatch, Object object) {
		// TODO Auto-generated method stub
		super.sendNotify(notifyId,dispatch, object);
		if (dispatch) {
			for (ViewHolder holder : pages) {
				holder.sendNotify(notifyId,dispatch, object);
			}
		}
	}

	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		if (onPageChangeListener!=null) {
			onPageChangeListener.onPageScrollStateChanged(arg0);
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		if (onPageChangeListener!=null) {
			onPageChangeListener.onPageScrolled(arg0, arg1, arg2);
		}
	}

	@Override
	public void onPageSelected(int index) {
		// TODO Auto-generated method stub
		previousIndex=currentIndex;
		currentIndex=index;
		
		ViewHolder preViewHolder=getPageHolder(previousIndex);
		ViewHolder nxtViewHolder=getPageHolder(currentIndex);
		
		if (preViewHolder!=null) {
			preViewHolder.onDeselected();
		}
		if (nxtViewHolder!=null) {
			nxtViewHolder.onSelected();
		}
		
		if (onPageChangeListener!=null) {
			onPageChangeListener.onPageSelected(index);
		}
	}
	
	public ViewHolder getPageHolder(int index) {
		if (index>-1 && index<pages.size()) {
			return pages.get(index);
		}
		return null;
	}
}
