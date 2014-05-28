package com.pj.core.database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pj.core.datamodel.DataWrapper;
import com.pj.core.managers.LogManager;
import com.pj.core.utilities.ArrayUtility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;


/**
 * 操作数据库的基本优化类，只需实现这个类即可
 * 在大部分需要自己定制的时候可以实现DBService
 * @author 陆振文[pengju]
 * 2012-11-28 下午5:15:57
 * email: pengju114@163.com
 */
public abstract class CoreDBService extends DBService {
	
	private SQLiteOpenHelper openHelper;
	private int version=1;
	
	public CoreDBService(Context context){
		openHelper=new BaseOpenHelper(context, getDatabaseName(), null, getVersion());
	}

	private class BaseOpenHelper extends SQLiteOpenHelper{

		public BaseOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			CoreDBService.this.onCreate(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			CoreDBService.this.onUpgrade(db, oldVersion, newVersion);
		}
		
	}
	
	protected void onCreate(SQLiteDatabase db) {
		String[] sqls=getCreateSQLs();
		for (String sql : sqls) {
			LogManager.i("创建表",sql);
			db.execSQL(sql);
		}
	}
	
	protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		for (String tn : getAllTableNames()) {
			String sql="drop table if exists "+tn;
			LogManager.i("Upgrade DataBase",  sql);
			db.execSQL(sql);
		}
		
		onCreate(db);
	}
	
	/** 获取创建表的sql语句*/
	public abstract String[] getCreateSQLs() ;
	/** 获取所有表的表名*/
	public abstract String[] getAllTableNames() ;
	/** 获取表名*/
	public abstract String getTableName() ;
	/** 获取ID列列名*/
	public abstract String getIDColumn() ;
	public abstract String[] getColumns();
	
	/**
	 * 获取数据库版本,可重写
	 * PENGJU
	 * 2012-11-28 下午5:09:46
	 * @return
	 */
	public int getVersion(){
		return version;
	}
	
	protected SQLiteDatabase getReadableDatabase() {
		return openHelper.getReadableDatabase();
	}
	protected SQLiteDatabase getWritableDatabase() {
		try {
			return openHelper.getWritableDatabase();
		} catch (Exception e) {
			// TODO: handle exception
			LogManager.trace(e);
			return openHelper.getReadableDatabase();
		}
	}

	/**
	 * 把结果包装为结果Map
	 * 以keyColumn的值为key
	 * PENGJU
	 * 2013-1-15 下午3:53:01
	 * @param cursor
	 * @param keyColumn
	 * @return
	 */
	protected Map<String, DataWrapper> wrapToMap(Cursor cursor,String keyColumn) {
		LinkedHashMap<String, DataWrapper> map=new LinkedHashMap<String, DataWrapper>();
		
		while (cursor.moveToNext()) {
			DataWrapper wrapper=wrapToWrapper(cursor);
			map.put(wrapper.getString(keyColumn), wrapper);
		}
		
		return map;
	}
	
	/**
	 * 把结果包装为结果Map
	 * 以IDColumn的值为key
	 * PENGJU
	 * 2013-1-15 下午3:53:01
	 * @param cursor
	 * @param keyColumn
	 * @return
	 */
	protected Map<String, DataWrapper> wrapToMap(Cursor cursor) {
		return wrapToMap(cursor,getIDColumn());
	}
	
    /**
     * 执行更新操作
     * @param sql
     * @param paramVals 参数值
     * @return
     * @throws SQLException 
     */
    public long insert(ContentValues values) throws SQLException{
    	long c=-1;
    	SQLiteDatabase database=getWritableDatabase();
    	try {
    		LogManager.i(getClass().getSimpleName(), "insert values %s", values);
        	c=database.insertOrThrow(getTableName(), null, values);
		} catch (Exception e) {
			LogManager.trace(e);
		} finally{
			database.close();
		}
    	return c;
    }


    public long insert(DataWrapper wrapper) throws SQLException{
    	String[] cols=getColumns();
    	ArrayList<String> columnList=new ArrayList<String>();
    	ArrayList<String> vals=new ArrayList<String>();
    	for (int i = 0; i < cols.length; i++) {
			if (wrapper.containsKey(cols[i])) {
				columnList.add(cols[i]);
				vals.add(wrapper.getString(cols[i]));
			}
		}
    	String[] finalCols=new String[columnList.size()];
    	String[] finalVals=new String[vals.size()];
    	return insert(columnList.toArray(finalCols), vals.toArray(finalVals));
    }
    
    /**
     * 执行更新
     * @param sql
     * @return
     * @throws SQLException 
     */
    public long insert(String[] columns,String[] vals) throws SQLException{
    	ContentValues values=new ContentValues(columns.length);
    	for (int i = 0; i < columns.length; i++) {
			values.put(columns[i], vals[i]);
		}
    	return insert(values);
    }
    
    public int delete(String condition,String[] paramVals) throws SQLException{
    	int c=0;
    	SQLiteDatabase database=getWritableDatabase();
    	try {
    		if (LogManager.isLogEnable()) {
				LogManager.i(getClass().getSimpleName(), "delete with condition:%s[%s]", condition,ArrayUtility.join(paramVals, ","));
			}
			c=database.delete(getTableName(), condition, paramVals);
		} catch (SQLException e) {
			throw e;
		}finally{
			database.close();
		}
    	return c;
    }
    public int delete(DataWrapper wrapper) throws SQLException{
    	Set<String> allKeys=wrapper.keySet();
    	ArrayList<String> keys=new ArrayList<String>(allKeys.size());
    	ArrayList<String> paramVals=new ArrayList<String>(keys.size());
    	String[] columns=getColumns();
    	for (String col : allKeys) {
			if (ArrayUtility.contains(columns, col)) {
				keys.add(col);
				paramVals.add(wrapper.getString(col));
			}
		}
    	keys.add("1=1");
    	String[] contents=new String[paramVals.size()];
    	return delete(ArrayUtility.join(keys.toArray(), "=? and "), paramVals.toArray(contents));
    }
    
    public int update(ContentValues newValues,String condition,String[] paramVals) {
    	int c=0;
    	SQLiteDatabase database=getWritableDatabase();
    	String[] allColumns=getColumns();
    	for (String string : allColumns) {
			if (!newValues.containsKey(string)) {
				newValues.remove(string);
			}
		}
    	try {
    		if (LogManager.isLogEnable()) {
				LogManager.i(getClass().getSimpleName(), "update with condition:%s[%s] set %s", condition,ArrayUtility.join(paramVals, ","),newValues);
			}
			c=database.update(getTableName(), newValues, condition, paramVals);
		} catch (SQLException e) {
			throw e;
		}finally{
			database.close();
		}
    	return c;
	}
    
    /**
     * 按参数执行查询
     * @param sql
     * @param paramVals
     * @return
     * @throws SQLException 
     */
    public List<DataWrapper> query(String selection,String[] selectionArgs) throws SQLException{
    	return query(null,selection, selectionArgs);
    }
    public List<DataWrapper> query(String[] columns,String selection,String[] selectionArgs) throws SQLException{
    	return query(columns,selection, selectionArgs, null, null, null);
    }
    public List<DataWrapper> query(String selection,String[] selectionArgs,String groupBy, String having, String orderBy) throws SQLException{
    	return query(null, selection, selectionArgs, groupBy, having, orderBy);
    }
    public List<DataWrapper> query(String[] columns,String selection,String[] selectionArgs,String groupBy, String having, String orderBy) throws SQLException{
    	return query(false, columns,selection, selectionArgs, groupBy, having, orderBy);
    }
    /**
     * 执行查询
     * @param sql
     * @return
     * @throws SQLException 
     */
    public List<DataWrapper> query(boolean distinct,String selection,String[] selectionArgs,String groupBy,String having,String orderBy) throws SQLException{
    	return query(distinct, null, selection, selectionArgs, groupBy, having, orderBy);
    }
    public List<DataWrapper> query(boolean distinct,String[] columns,String selection,String[] selectionArgs,String groupBy,String having,String orderBy) throws SQLException{
    	SQLiteDatabase database=getReadableDatabase();
    	List<DataWrapper> list=null;
    	try {
    		if (LogManager.isLogEnable()) {
    			LogManager.i(getClass().getSimpleName(), "query:select %s from %s where %s[%s] group by(%s) having(%s) order by(%s)", columns==null?"*":ArrayUtility.join(columns, ","),getTableName(),selection,ArrayUtility.join(selectionArgs, ","),groupBy,having,orderBy);
			}
    		
    		Cursor cursor=database.query(distinct,getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy,null);
    		list=wrapToList(cursor);
    		cursor.close();
		} catch (Exception e) {
			LogManager.trace(e);
		}
    	
    	database.close();
    	return list;
    }

    /**
     * 执行查询，并返回一个结果(即使结果有多个)，无结果返回null
     */
    public DataWrapper querySingle(String condition,String[] paramVals) throws SQLException {
    	return querySingle(null,  condition,paramVals);
    }

	public DataWrapper querySingle(String[] columns, String condition, String[] paramVals) throws SQLException {
		// TODO Auto-generated method stub
		return querySingle(columns, condition, paramVals, null, null, null);
	}
	
	public DataWrapper querySingle(String selection,String[] selectionArgs,String groupBy, String having, String orderBy) throws SQLException{
		return querySingle(null, selection, selectionArgs, groupBy, having, orderBy);
    }
	public DataWrapper querySingle(String[] columns,String selection,String[] selectionArgs,String groupBy, String having, String orderBy) throws SQLException{
		DataWrapper wrapper=null;
    	SQLiteDatabase database=getReadableDatabase();
    	try {
    		if (LogManager.isLogEnable()) {
    			LogManager.i(getClass().getSimpleName(), "querySingle:select %s from %s where %s[%s] group by(%s) having(%s) order by(%s)", columns==null?"*":ArrayUtility.join(columns, ","),getTableName(),selection,ArrayUtility.join(selectionArgs, ","),groupBy,having,orderBy);
			}
    		Cursor cursor=database.query(getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy);
    		if (cursor.moveToNext()) {
				wrapper=wrapToWrapper(cursor);
			}
    		cursor.close();
		} catch (Exception e) {
			LogManager.trace(e);
		}
    	
    	database.close();
    	return wrapper;
    }
	
	/**
	 * 获取创建表的SQL语句
	 * @return
	 */
	public static String getCreateSql(){
		return null;
	}
}
