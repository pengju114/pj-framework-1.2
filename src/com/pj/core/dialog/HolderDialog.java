package com.pj.core.dialog;

import android.content.DialogInterface;
import android.view.Window;

import com.pj.core.managers.LogManager;
import com.pj.core.viewholders.ViewHolder;

public class HolderDialog extends BaseDialog{
	
	private ViewHolder root;
	private ViewHolder targetHolder;
	
	public HolderDialog(ViewHolder holder){
		super(holder.getActivity());
		
		root=holder.getActivity().new DecorViewHolder(holder.getActivity());
		
		if (root.getView().getLayoutParams()!=null) {
			setContentView(root.getView(), root.getView().getLayoutParams());
		}else {
			setContentView(root.getView());
		}
		setCanceledOnTouchOutside(false);
		root.onViewAttached();
		holder.attachedDialog=this;
		targetHolder=holder;
		//这样才会触发holder的生命周期函数
		root.addChild(holder);
	}
	
	@Override
	protected void onDialogClose(int trigger) {
		// TODO Auto-generated method stub
		super.onDialogClose(trigger);
		LogManager.i("dialog close");
	}
	
	protected void clear() {
		targetHolder.dispathWillDisappear(targetHolder, false);
		targetHolder.dispathDidDisappear(targetHolder, false);
		
		root.dispathDettached(root);
		targetHolder.attachedDialog=null;
	}
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.init();
	}

	@Override
	public void setMessage(CharSequence message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMessage(int resId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		clear();
		super.onDismiss(dialog);
	}

}
