package com.pj.core.dialog;

import com.pj.core.R;
import com.pj.core.datamodel.DataWrapper;
import com.pj.core.utilities.StringUtility;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InputDialog extends BaseDialog implements android.view.View.OnClickListener {
	/**
	 * 输入框文本
	 */
	public static final String KEY_TEXT="dialog.text";
	public static final String KEY_HINT="dialog.hint";
	public static final String KEY_DESCRIPTION="dialog.description";
	/**
	 * 输入框类型
	 * {@link android.view.inputmethod.EditorInfo}
	 */
	public static final String KEY_INPUT_TYPE ="dialog.inputtype";
	
	private TextView titleLabel;
	private TextView messageLabel;
	private EditText text;
	private TextView descriptionLabel;
	
	private Button   positiveButton;
	private Button   negativeButton;

	public InputDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public InputDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.c_input_dialog);
		
		titleLabel=(TextView) findViewById(R.id.c_label_dialog_title);
		messageLabel=(TextView) findViewById(R.id.c_label_dialog_msg);
		text=(EditText) findViewById(R.id.c_text_dialog_input);
		descriptionLabel=(TextView) findViewById(R.id.c_label_dialog_input_des);
		
		positiveButton=(Button) findViewById(R.id.c_btn_dialog_ok);
		positiveButton.setOnClickListener(this);
		negativeButton=(Button) findViewById(R.id.c_btn_dialog_cancel);
		negativeButton.setOnClickListener(this);
	}

	@Override
	public void setTitle(CharSequence title) {
		// TODO Auto-generated method stub
		titleLabel.setText(title);
	}
	
	@Override
	public void setTitle(int titleId) {
		// TODO Auto-generated method stub
		titleLabel.setText(titleId);
	}
	
	@Override
	public void setMessage(CharSequence message) {
		// TODO Auto-generated method stub
		messageLabel.setText(message);
	}

	@Override
	public void setMessage(int resId) {
		// TODO Auto-generated method stub
		messageLabel.setText(resId);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId()==R.id.c_btn_dialog_ok) {
			dismiss();
			onDialogClose(DialogListener.BTN_OK);
		}else if (v.getId()==R.id.c_btn_dialog_cancel) {
			dismiss();
			onDialogClose(DialogListener.BTN_CANCEL);
		}
	}

	
	@Override
	protected void onDialogClose(int trigger) {
		// TODO Auto-generated method stub
		//可以在对话框中获取输入的文本值
		setObject(KEY_TEXT, text.getText().toString().trim());
		super.onDialogClose(trigger);
	}
	
	@Override
	public void setDialogData(DataWrapper wrapper) {
		// TODO Auto-generated method stub
		super.setDialogData(wrapper);
		text.setText(wrapper.getString(KEY_TEXT));
		text.setHint(wrapper.getString(KEY_HINT));
		text.setInputType(wrapper.getInt(KEY_INPUT_TYPE).intValue());
		descriptionLabel.setText(wrapper.getString(KEY_DESCRIPTION));
		
		String positiveText=wrapper.getString(KEY_POSITIVE_BUTTON_TEXT);
		String negativeText=wrapper.getString(KEY_NEGATIVE_BUTTON_TEXT);
		
		if (!StringUtility.isEmpty(positiveText)) {
			positiveButton.setText(positiveText);
		}
		if (!StringUtility.isEmpty(negativeText)) {
			negativeButton.setText(negativeText);
		}
	}
	
	public String getText() {
		return StringUtility.toString(getObject(KEY_TEXT));
	}
}
