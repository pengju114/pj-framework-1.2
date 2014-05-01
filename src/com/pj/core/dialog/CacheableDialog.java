package com.pj.core.dialog;

import android.content.DialogInterface;

import com.pj.core.datamodel.Cacheable;
import com.pj.core.datamodel.DataWrapper;
/**
 * 按 返回键 退出对话框时会调用 onCancel和onDismiss
 * framework
 * @author 陆振文[pengju]
 * 2013-4-2 下午5:01:10
 * email: pengju114@163.com
 */
public interface CacheableDialog extends Cacheable,DialogInterface{
	String KEY_MESSAGE		="dialog.message" ;//对话框信息键
	String KEY_DATA			="dialog.data";	  //对话框数据键
	String KEY_TITLE		="dialog.title";  //对话框标题键
	String KEY_LISTENER		="dialog.listener";  //对话框监听器
	String KEY_REQUEST_ID	="dialog.request";  //请求ID
	
	String KEY_POSITIVE_BUTTON_TEXT	="dialog.positive";
	String KEY_NEGATIVE_BUTTON_TEXT	="dialog.negative"; 
	
	public void show();
	public void cancel();
	public void setMessage(CharSequence message);
	public void setTitle(CharSequence message);
	public void setMessage(int resId);
	public void setTitle(int resId);
	public boolean isShowing();
	public void setDialogListener(DialogListener listener) ;
	public void setDialogData(DataWrapper wrapper) ;
	public void setRequestCode(int requestCode);
	public int  getRequestCode();
}
