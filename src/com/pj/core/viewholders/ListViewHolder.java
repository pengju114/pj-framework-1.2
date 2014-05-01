package com.pj.core.viewholders;

import java.util.ArrayList;
import java.util.Collection;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.pj.core.BaseActivity;
import com.pj.core.adapters.DefaultListAdapter;

public class ListViewHolder<T> extends ViewHolder implements OnScrollListener{
	
	private ListView listView;
	private ArrayList<T> dataList;
	private DefaultListAdapter<T> listAdapter;

	public ListViewHolder(BaseActivity activity, int resourceIdUseForCache,Class<? extends ViewHolder> itemHolderClass) {
		this(activity,new ListView(activity),resourceIdUseForCache,itemHolderClass);
	}

	public ListViewHolder(BaseActivity activity, View view, int resourceIdUseForCache,Class<? extends ViewHolder> itemHolderClass) {
		super(activity, null);
		// TODO Auto-generated constructor stub
		
		dataList=new ArrayList<T>();
		listAdapter=new DefaultListAdapter<T>(this, resourceIdUseForCache, itemHolderClass, dataList);
		setView(view);
	}
	
	public ListViewHolder(ViewHolder mParent,int resourceIdUseForCache,Class<? extends ViewHolder> itemHolderClass) {
		// TODO Auto-generated constructor stub
		this(mParent, new ListView(mParent.getActivity()),resourceIdUseForCache,itemHolderClass);
	}
	public ListViewHolder(ViewHolder mParent,View root,int resourceIdUseForCache,Class<? extends ViewHolder>itemHolderClass){
		super(mParent, root);
		dataList=new ArrayList<T>();
		listAdapter=new DefaultListAdapter<T>(this, resourceIdUseForCache, itemHolderClass, dataList);
		setView(root);
	}

	@Override
	protected void onApplyView(View view) {
		// TODO Auto-generated method stub
		listView=(ListView) view;
		listView.setAdapter(listAdapter);
		listView.setCacheColorHint(Color.TRANSPARENT);
	}

	public ListView getListView() {
		return listView;
	}
	
	public DefaultListAdapter<T> getListAdapter() {
		return listAdapter;
	}
	
	public ArrayList<T> getDataList() {
		return dataList;
	}
	
	public void addItem(T wrapper,boolean notifyChanged) {
		// TODO Auto-generated method stub
		dataList.add(wrapper);
		if (notifyChanged) {
			listAdapter.notifyDataSetChanged();
		}
	}
	public void addItems(Collection<? extends T> items,boolean notifyChanged) {
		// TODO Auto-generated method stub
		dataList.addAll(items);
		if (notifyChanged) {
			listAdapter.notifyDataSetChanged();
		}
	}
	
	
	@Override
	protected void onActivityStateChange(int state, Bundle bundle) {
		// TODO Auto-generated method stub
		super.onActivityStateChange(state, bundle);
		for (ViewHolder holder : listAdapter.getCacheHolders()) {
			holder.onActivityStateChange(state,bundle);
		}
	}
	
	@Override
	public void sendNotify(int notifyId, boolean dispatch ,Object object) {
		// TODO Auto-generated method stub
		super.sendNotify(notifyId,dispatch, object);
		if (dispatch) {
			for (ViewHolder holder : listAdapter.getCacheHolders()) {
				holder.sendNotify(notifyId,dispatch, object);
			}
		}
	}

	
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		//滚动到底部则加载下一页数据
		if (scrollState==OnScrollListener.SCROLL_STATE_IDLE && view.getLastVisiblePosition()==view.getCount()-1) {
			onScrollToBottom(this);
		}
	}
	
	protected void onScrollToBottom(ListViewHolder<T> listViewHolder) {
		
	}
}
