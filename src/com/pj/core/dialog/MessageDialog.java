package com.pj.core.dialog;

import com.pj.core.datamodel.DataWrapper;
import com.pj.core.utilities.StringUtility;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
/**
 * 
 * @author 陆振文[pengju]
 *
 */
public class MessageDialog extends AlertDialog implements CacheableDialog, DialogInterface.OnCancelListener, DialogInterface.OnClickListener{
	private DataWrapper myWrapper;
	private DialogListener listener;
	
	private int requestCode;
	
	public MessageDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
	
	protected MessageDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		init();
	}

	protected void init() {
		// TODO Auto-generated method stub
		setCanceledOnTouchOutside(false);
		setCancelable(true);
		myWrapper=new DataWrapper();
		setOnCancelListener(this);
		
		setMessage("No Information Provided");
	}


	@Override
	public void setObject(String key, Object value) {
		// TODO Auto-generated method stub
		myWrapper.setObject(key, value);
	}

	@Override
	public Object getObject(String key) {
		// TODO Auto-generated method stub
		return myWrapper.getObject(key);
	}

	@Override
	public void setMessage(int resId) {
		// TODO Auto-generated method stub
		setMessage(getContext().getResources().getString(resId));
	}

	@Override
	public void setDialogListener(DialogListener listener) {
		// TODO Auto-generated method stub
		this.listener=listener;
	}
	
	public int getRequestCode() {
		return requestCode;
	}
	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}
	
	protected void onDialogClose(int triggerButton) {
		if (listener!=null) {
			listener.onDialogClose(requestCode, this, triggerButton, getObject(KEY_DATA));
		}
		//每处理完一次就清空信息
		reset();
	}

	private void reset() {
		// TODO Auto-generated method stub
		myWrapper.clear();
		listener=null;
	}

	/**
	 * 按返回键关闭时会调用onCancel和onDismiss
	 * 按下确定按钮和取消按钮会调用onDismiss，不会调用onCancel
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		onDialogClose(DialogListener.BTN_UNKNOWN);
	}

	@Override
	public void setDialogData(DataWrapper wrapper) {
		// TODO Auto-generated method stub
		setTitle(wrapper.getString(KEY_TITLE));
		setMessage(wrapper.getString(KEY_MESSAGE));
		setObject(KEY_DATA, wrapper.getObject(KEY_DATA));
		setDialogListener((DialogListener) wrapper.getObject(KEY_LISTENER));
		setRequestCode(wrapper.getInt(KEY_REQUEST_ID).intValue());
		
		String btnText=wrapper.getString(KEY_POSITIVE_BUTTON_TEXT);
		if (!StringUtility.isEmpty(btnText)) {
			setButton(DialogInterface.BUTTON_POSITIVE, btnText, this);
		}
		
		btnText=wrapper.getString(KEY_NEGATIVE_BUTTON_TEXT);
		if (!StringUtility.isEmpty(btnText)) {
			setButton(DialogInterface.BUTTON_NEGATIVE, btnText, this);
		}
	}

	/**
	 * 调用此方法对话框会自动dismiss
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which==DialogInterface.BUTTON_POSITIVE) {
			onDialogClose(DialogListener.BTN_OK);
		}else if (which==BUTTON_NEGATIVE) {
			onDialogClose(DialogListener.BTN_CANCEL);
		}
	}
}
