package com.pj.core.dialog;

import com.pj.core.R;
import com.pj.core.viewholders.ViewHolder;

import android.annotation.SuppressLint;
import android.view.WindowManager;
import android.widget.PopupWindow;

@SuppressLint("ViewConstructor")
@SuppressWarnings("deprecation")
public class HolderPopupWindow extends PopupWindow implements PopupWindow.OnDismissListener{
	
	
	public static final int FILL_PARENT=WindowManager.LayoutParams.FILL_PARENT;
	public static final int WRAP_CONTENT=WindowManager.LayoutParams.WRAP_CONTENT;
	
	private ViewHolder contentHolder;
	private ViewHolder rootHolder;

	public HolderPopupWindow(ViewHolder contentView) {
		this(contentView,FILL_PARENT,WRAP_CONTENT);
	}

	public HolderPopupWindow(ViewHolder contentView, int width, int height) {
		super(contentView.getActivity());
		// TODO Auto-generated constructor stub
		contentHolder=contentView;
		
		rootHolder   =contentHolder.getActivity().new DecorViewHolder(contentHolder.getActivity());
				
		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);
		setSoftInputMode(PopupWindow.INPUT_METHOD_FROM_FOCUSABLE);
		
		
		setBackgroundDrawable(contentView.getActivity().defaultResources().getDrawable(R.drawable.bg_transparent));
		
		setContentView(rootHolder.getView());
		setWidth(width);
		setHeight(height);
		
		initialize(contentView, width, height);
		setOnDismissListener(this);
		contentHolder.attachedPopupWindow=this;
		rootHolder.addChild(contentHolder);
	}
	
	protected void initialize(ViewHolder holder,int width,int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDismiss() {
		// TODO Auto-generated method stub
		rootHolder.removeChild(contentHolder);
		contentHolder.attachedPopupWindow=null;
	}
}
