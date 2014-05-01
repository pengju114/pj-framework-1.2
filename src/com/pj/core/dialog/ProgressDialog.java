package com.pj.core.dialog;

import com.pj.core.datamodel.DataWrapper;

import android.content.Context;
import android.content.DialogInterface;

public class ProgressDialog extends android.app.ProgressDialog implements CacheableDialog,DialogInterface.OnCancelListener, DialogInterface.OnDismissListener{
	private DataWrapper dataWrapper;
	private DialogListener listener;
	private int requestCode;

	public ProgressDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
	public ProgressDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		init();
	}
	private void init() {
		// TODO Auto-generated method stub
		dataWrapper=new DataWrapper();
		setCanceledOnTouchOutside(false);
		setCancelable(true);
		setOnCancelListener(this);
		setOnDismissListener(this);
	}


	@Override
	public void setObject(String key, Object value) {
		// TODO Auto-generated method stub
		dataWrapper.setObject(key, value);
	}
	@Override
	public Object getObject(String key) {
		// TODO Auto-generated method stub
		return dataWrapper.getObject(key);
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
	
	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}
	public int getRequestCode() {
		return requestCode;
	}
	
	@Override
	public void setDialogData(DataWrapper wrapper) {
		// TODO Auto-generated method stub
		setTitle(wrapper.getString(KEY_TITLE));
		setMessage(wrapper.getString(KEY_MESSAGE));
		setObject(KEY_DATA, wrapper.getObject(KEY_DATA));
		setDialogListener((DialogListener) wrapper.getObject(KEY_LISTENER));
		setRequestCode(wrapper.getInt(KEY_REQUEST_ID).intValue());
	}
	
	protected void onDialogClose(int triggerButton) {
		if (listener!=null) {
			listener.onDialogClose(getRequestCode(),this, triggerButton, getObject(KEY_DATA));
		}
		//没处理完一次就清空信息,否则有可能会被连续调用两次
		reset();
	}

	private void reset() {
		// TODO Auto-generated method stub
		dataWrapper.clear();
		listener=null;
		setMessage("");
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		onDialogClose(DialogListener.BTN_UNKNOWN);
	}
	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		onCancel(dialog);
	}
}
