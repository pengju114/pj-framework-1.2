package com.pj.core.viewholders;

import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pj.core.BaseActivity;
import com.pj.core.datamodel.DataWrapper;
import com.pj.core.dialog.BaseDialog;
import com.pj.core.dialog.CacheableDialog;
import com.pj.core.dialog.DialogListener;
import com.pj.core.utilities.StringUtility;

/**
 * 对话框holder，可以当对话框用
 * 子类在关闭对话框时用 dismiss
 * 并且在 onApplyView的时候设置好视图
 * 注册按钮回调用 {@link DialogViewHolder#hookDialogButtonListeners}方法
 * pj-framework
 * @author lzw
 * 2014年5月24日 上午10:38:17
 * email: pengju114@163.com
 */
public abstract class DialogViewHolder extends ViewHolder implements CacheableDialog,DialogInterface.OnCancelListener,View.OnClickListener{

	private DataWrapper 		dataWrapper;
	private DialogListener    	listener;
	private int 				requestCode;
	
	
	protected TextView  titleTextView;
	protected TextView  messageTextView;
	protected Button    positiveButton;
	protected Button	negativeButton;
	
	
	public DialogViewHolder(BaseActivity activity) {
		super(activity);
		// TODO Auto-generated constructor stub
	}

	public DialogViewHolder(BaseActivity activity, View view) {
		super(activity, view);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void initialize(BaseActivity activity, View view) {
		// TODO Auto-generated method stub
		super.initialize(activity, view);
		dataWrapper = new DataWrapper();
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
	public void dismiss() {
		// TODO Auto-generated method stub
		dismissDialog();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == positiveButton.getId()) {
			dispatchButtonEvent(DialogListener.BTN_OK);
		}else if (v.getId() == negativeButton.getId()) {
			dispatchButtonEvent(DialogListener.BTN_CANCEL);
		}
		// 不需回调
		dismiss();
	}
	
	protected void hookDialogButtonListeners(){
		if (positiveButton!=null) {
			positiveButton.setOnClickListener(this);
		}
		if (negativeButton!=null) {
			negativeButton.setOnClickListener(this);
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		dispatchButtonEvent(DialogListener.BTN_UNKNOWN);
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		BaseDialog dialog = showInDialog();
		if (dialog!=null) {
			dialog.setOutsideCancelListener(this);
		}
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		cancelDialog();
	}

	@Override
	public void setMessage(CharSequence message) {
		// TODO Auto-generated method stub
		if (messageTextView!=null) {
			messageTextView.setText(message);
		}
	}

	@Override
	public void setTitle(CharSequence message) {
		// TODO Auto-generated method stub
		if (titleTextView!=null) {
			titleTextView.setText(message);
		}
	}

	@Override
	public void setMessage(int resId) {
		// TODO Auto-generated method stub
		if (messageTextView!=null) {
			messageTextView.setText(resId);
		}
	}

	@Override
	public void setTitle(int resId) {
		// TODO Auto-generated method stub
		if (titleTextView!=null) {
			titleTextView.setText(resId);
		}
	}

	@Override
	public boolean isShowing() {
		// TODO Auto-generated method stub
		return (attachedDialog!=null && attachedDialog.isShowing());
	}

	@Override
	public void setDialogListener(DialogListener listener) {
		// TODO Auto-generated method stub
		this.listener = listener;
	}

	@Override
	public void setDialogData(DataWrapper wrapper) {
		// TODO Auto-generated method stub
		setTitle(wrapper.getString(KEY_TITLE));
		setMessage(wrapper.getString(KEY_MESSAGE));
		setObject(KEY_DATA, wrapper.getObject(KEY_DATA));
		setDialogListener((DialogListener) wrapper.getObject(KEY_LISTENER));
		setRequestCode(wrapper.getInt(KEY_REQUEST_ID).intValue());
		
		if (positiveButton!=null && !StringUtility.isEmpty(wrapper.getString(KEY_POSITIVE_BUTTON_TEXT))) {
			positiveButton.setText(wrapper.getString(KEY_POSITIVE_BUTTON_TEXT));
		}
		if (negativeButton!=null && !StringUtility.isEmpty(wrapper.getString(KEY_NEGATIVE_BUTTON_TEXT))) {
			negativeButton.setText(wrapper.getString(KEY_NEGATIVE_BUTTON_TEXT));
		}
	}

	@Override
	public void setRequestCode(int requestCode) {
		// TODO Auto-generated method stub
		this.requestCode = requestCode;
	}

	@Override
	public int getRequestCode() {
		// TODO Auto-generated method stub
		return requestCode;
	}

	
	protected void dispatchButtonEvent(int button) {
		if (listener!=null) {
			listener.onDialogClose(requestCode, this, button, getObject(KEY_DATA));
		}
	}
}
