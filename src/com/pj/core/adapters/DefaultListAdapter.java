package com.pj.core.adapters;

import java.lang.reflect.Constructor;

import java.util.HashSet;
import java.util.List;

import com.pj.core.BaseActivity;
import com.pj.core.managers.LogManager;
import com.pj.core.viewholders.ViewHolder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * 默认列表适配器
 * @author 陆振文[pengju]
 * @param <T>
 *
 */
public class DefaultListAdapter<T> extends ArrayAdapter<T>{
	private Class<? extends ViewHolder> itemHolderClass;
	private int resourceIdUseForCache;
	private ViewHolder parent;
	private HashSet<ViewHolder> cacheHolders;

	/**
	 * 
	 * @param parent 列表视图持有器
	 * @param resourceIdUseForCache 资源ID，用来缓存重用列表项
	 * @param itemHolderClass 列表项视图持有器，该视图持有器必须实现基类的构造方法
	 * @param objects 数据列表
	 */
	public DefaultListAdapter(ViewHolder parent, int resourceIdUseForCache,Class<? extends ViewHolder> itemHolderClass,List<T> objects) {
		super(parent.getActivity(), resourceIdUseForCache, objects);
		// TODO Auto-generated constructor stub
		this.parent=parent;
		this.itemHolderClass=itemHolderClass;
		this.resourceIdUseForCache=resourceIdUseForCache;
		cacheHolders=new HashSet<ViewHolder>();
	}
	
	public View getView(int pos,View convertView,ViewGroup group) {
		ViewHolder item=null;
		try {
			if (convertView==null) {
				Constructor<? extends ViewHolder> constructor=itemHolderClass.getConstructor(BaseActivity.class);
				item=constructor.newInstance(this.parent.getActivity());
				item.getView().setTag(resourceIdUseForCache,item);
			}else {
				item=(ViewHolder) convertView.getTag(resourceIdUseForCache);
				if (item==null) {
					Constructor<? extends ViewHolder> constructor=itemHolderClass.getConstructor(BaseActivity.class,View.class);
					item=constructor.newInstance(this.parent.getActivity(),convertView);
					item.getView().setTag(resourceIdUseForCache,item);
				}
			}
		} catch (Exception e) {
			LogManager.trace(e);
		}
		
		if (item!=null) {
			item.setParent(this.parent);
			cacheHolders.add(item);
		}
		
		onAssignData(item, pos);
		
		return item.getView();
	}
	
	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		cacheHolders.clear();
		super.notifyDataSetChanged();
	}
	
	protected void onAssignData(ViewHolder item,int pos) {
		
		T info=getItem(pos);
		item.setData(info);
	}
	
	public HashSet<ViewHolder> getCacheHolders() {
		return cacheHolders;
	}
}
