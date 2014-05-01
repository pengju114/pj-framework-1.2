package com.pj.core.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ExecutingDialog extends BaseDialog implements CacheableDialog, DialogInterface.OnDismissListener{
	private TextView statusTextView;

	public ExecutingDialog(Context context) {
		super(context);
	}


	public ExecutingDialog(Context context,int theme) {
		super(context, theme);
	}

	protected void init() {
		super.init();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(getContentView());
		
		Window window=getWindow();
		window.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM, WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		
		WindowManager.LayoutParams params=window.getAttributes();
		params.width=WindowManager.LayoutParams.WRAP_CONTENT;
		params.height=WindowManager.LayoutParams.WRAP_CONTENT;
		//模糊度，设为不模糊
		params.dimAmount=0.0F;
		window.setAttributes(params);
		
		ColorDrawable drawable=new ColorDrawable(Color.TRANSPARENT);
		window.setBackgroundDrawable(drawable);
		
		setOnDismissListener(this);
	}
	
	private View getContentView(){
		RelativeLayout layout=new RelativeLayout(getContext());
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(params);
		
		ProgressBar progressBar=new ProgressBar(getContext());
		progressBar.setId(0x1000457);
		
		RelativeLayout.LayoutParams progressBarLayoutParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		progressBarLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		progressBarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		layout.addView(progressBar, progressBarLayoutParams);
		
		TextView textView=new TextView(getContext());
		RelativeLayout.LayoutParams textViewLayoutParams=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		textViewLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		textViewLayoutParams.addRule(RelativeLayout.RIGHT_OF, progressBar.getId());
		
		statusTextView=textView;
		layout.addView(textView, textViewLayoutParams);
		
		return layout;
	}

	@Override
	public void setMessage(CharSequence message) {
		// TODO Auto-generated method stub
		statusTextView.setText(message);
	}


	@Override
	public void setMessage(int resId) {
		// TODO Auto-generated method stub
		statusTextView.setText(resId);
	}

	protected void reset() {
		// TODO Auto-generated method stub
		super.reset();
		setMessage("");
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		onCancel(dialog);
	}
}
