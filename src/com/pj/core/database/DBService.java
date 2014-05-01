package com.pj.core.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.pj.core.datamodel.DataWrapper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 操作数据库所有类的基本类
 * @author 陆振文[pengju]
 * 2012-11-28 下午5:14:43
 * email: pengju114@163.com
 */
public abstract class DBService {
	public static final String PATTERN="yyyy-MM-dd hh:mm:ss";
	private static final String DATABASE_NAME="core.db";

	protected abstract SQLiteDatabase getReadableDatabase() ;
	protected abstract SQLiteDatabase getWritableDatabase() ;
	
	protected String getDatabaseName() {
		return DATABASE_NAME;
	}
	
	public static DataWrapper copyFrom(ContentValues values){
		DataWrapper wrapper=null;
		if (values!=null) {
			wrapper=new DataWrapper();
			for (Entry<String, Object> key : values.valueSet()) {
				wrapper.put(key.getKey(), key.getValue());
			}
		}
		return wrapper;
	}
	
	public static ContentValues copyFrom(DataWrapper values){
		ContentValues contentValues=null;
		if (values!=null) {
			contentValues=new ContentValues();
			Iterator<String> keys=values.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				contentValues.put(key, values.getString(key));
			}
		}
		return contentValues;
	}
	
	public static DataWrapper copyFrom(ContentValues values,String... keys){
		DataWrapper wrapper=null;
		if (values!=null) {
			wrapper=new DataWrapper();
			for (String key : keys) {
				wrapper.put(key, values.get(key));
			}
		}
		return wrapper;
	}
	
	public static ContentValues copyFrom(DataWrapper values,String... keys){
		ContentValues contentValues=null;
		if (values!=null) {
			contentValues=new ContentValues();
			for (String key : keys) {
				contentValues.put(key, values.getString(key));
			}
		}
		return contentValues;
	}
	
	/**
	 * 把结果集包装成DataWrapper列表
	 * 每个DataWrapper的值为字符串
	 * 可以通过DataWrapper 的get[Type]函数转换
	 * PENGJU
	 * 2013-1-15 下午12:04:50
	 * @param cursor
	 * @return
	 */
	public List<DataWrapper> wrapToList(Cursor cursor) {
		ArrayList<DataWrapper> rs=new ArrayList<DataWrapper>();
		while (cursor.moveToNext()) {
			rs.add(wrapToWrapper(cursor));
		}
		return rs;
	}
	
	/**
	 * 把一行数据包装成 DataWrapper
	 * 每个值都是字符串
	 * 可以通过DataWrapper 的get[Type]函数转换
	 * PENGJU
	 * 2013-1-15 下午12:03:10
	 * @param cursor
	 * @return
	 */
	public DataWrapper wrapToWrapper(Cursor cursor) {
		DataWrapper wrapper=new DataWrapper();
		for (int i = 0; i < cursor.getColumnCount(); i++) {
			wrapper.put(cursor.getColumnName(i), cursor.getString(i));
		}
		return wrapper;
	}
}
