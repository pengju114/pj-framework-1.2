package com.pj.core.utilities;



import com.pj.core.datamodel.DataWrapper;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 视图辅助类,一般用于填充数据到组件中
 *@author 陆振文[PENGJU]
 *时间:2012-4-11 上午9:24:58
 */
public class ViewUtility {
	/**
	 * 把dataWrapper中的数据填充到container的子组件中
	 * columnKeys中第一个键对应的值放到container的第一个子组件中
	 * 第二个对应的放到第二个子组件中,以此类推
	 * 并且每个子组件是TextView或者是其子类
	 * @param container 组件容器
	 * @param columnKeys 键数组
	 * @param dataWrapper 数据包
	 */
	public static final void fill(ViewGroup container,String[] columnKeys,DataWrapper dataWrapper){
		if (container==null || columnKeys==null || dataWrapper==null) {
			return;
		}
		
		int columCount=Math.min(container.getChildCount(), columnKeys.length);
		for (int i = 0; i < columCount; i++) {
			TextView item=(TextView) container.getChildAt(i);
			String data=dataWrapper.getString(columnKeys[i]);
			item.setText(data);
		}
	}
	
	/**
	 * 把dataWrapper中的数据填充到container的子组件中
	 * columnKeys中第一个键对应的值放到container中id值为viewIds中第一个ID值的子组件中
	 * 其余以此类推
	 * @param container
	 * @param viewIds
	 * @param columnKeys
	 * @param dataWrapper
	 */
	public static final void fill(View container,int[] viewIds,String[] columnKeys,DataWrapper dataWrapper){
		if (container==null || columnKeys==null || dataWrapper==null || viewIds==null) {
			return;
		}
		
		int columCount=Math.min(viewIds.length, columnKeys.length);
		for (int i = 0; i < columCount; i++) {
			TextView item=(TextView) container.findViewById(viewIds[i]);
			if (item==null) {
				continue;
			}
			String data=dataWrapper.getString(columnKeys[i]);
			item.setText(data);
		}
	}
	
	/**
	 * 把values中的数据填充到container的子组件中
	 * 第一个放到第一列，第二个第二列，以此类推
	 * @param container
	 * @param values
	 */
	public static final void fill(ViewGroup container,String[] values){
		if (container==null || values==null ) {
			return;
		}
		
		int columCount=Math.min(container.getChildCount(), values.length);
		for (int i = 0; i < columCount; i++) {
			TextView item=(TextView) container.getChildAt(i);
			if (item==null) {
				continue;
			}
			item.setText(values[i]);
		}
	}
	
	
	
	/**
	 * 把values中的数据填充到container的子组件中
	 * @param container
	 * @param viewIds
	 * @param values
	 */
	public static final void fill(View container,int[] viewIds,String[] values){
		if (container==null || values==null) {
			return;
		}
		
		int columCount=Math.min(viewIds.length, values.length);
		for (int i = 0; i < columCount; i++) {
			TextView item=(TextView) container.findViewById(viewIds[i]);
			if (item==null) {
				continue;
			}
			item.setText(values[i]);
		}
	}
	
	public static final void fill(View container,int viewId,String value){
		if (container==null) {
			return;
		}
		TextView item=(TextView) container.findViewById(viewId);
		item.setText(value);
	}
}
