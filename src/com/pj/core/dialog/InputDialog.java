package com.pj.core.dialog;

import com.pj.core.datamodel.DataWrapper;
import com.pj.core.utilities.DimensionUtility;
import com.pj.core.utilities.StringUtility;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InputDialog extends BaseDialog implements android.view.View.OnClickListener {
	/**
	 * 输入框文本
	 */
	public static final String KEY_TEXT="dialog.text";
	public static final String KEY_HINT="dialog.hint";
	/**
	 * 输入框类型
	 * {@link android.view.inputmethod.EditorInfo}
	 */
	public static final String KEY_INPUT_TYPE ="dialog.inputtype";
	
	private TextView titleLabel;
	private TextView messageLabel;
	private EditText text;
	
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
		setContentView(generateView());
	}
	
	@SuppressWarnings("deprecation")
	private View generateView(){
		LinearLayout rootLayout = new LinearLayout(getContext());
		rootLayout.setOrientation(LinearLayout.VERTICAL);
		rootLayout.setMinimumWidth(DimensionUtility.dp2px(260));
		
		GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.argb(0xCC, 0xFF, 0xFF, 0xFF),Color.argb(0xCC, 0xFF, 0xFF, 0xFF)});
		gradientDrawable.setCornerRadius(DimensionUtility.dp2px(6));
		gradientDrawable.setStroke(1, Color.parseColor("#CFFFFFFF"));
		
		rootLayout.setBackgroundDrawable(gradientDrawable);
		int padding = DimensionUtility.dp2px(8);
		rootLayout.setPadding(padding, padding, padding, padding);
		
		
		titleLabel = new TextView(getContext());
		titleLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		titleLabel.setGravity(Gravity.CENTER);
		titleLabel.setTextColor(Color.parseColor("#333333"));
		
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(fillParent(), wrapContent());
		rootLayout.addView(titleLabel, titleParams);
		
		
		
		messageLabel = new TextView(getContext());
		messageLabel.setTextColor(Color.parseColor("#333333"));
		messageLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		messageLabel.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(fillParent(), wrapContent());
		messageParams.topMargin = DimensionUtility.dp2px(10);
		rootLayout.addView(messageLabel, messageParams);
		
		
		text  =  new EditText(getContext());
		text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		text.setInputType(InputType.TYPE_CLASS_TEXT);
		LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(fillParent(),wrapContent());
		textParams.topMargin = DimensionUtility.dp2px(8);
		rootLayout.addView(text, textParams);
		
		LinearLayout container = new LinearLayout(getContext());
		container.setOrientation(LinearLayout.HORIZONTAL);
		container.setWeightSum(2);
		
		positiveButton = new Button(getContext());
		LinearLayout.LayoutParams positiveParams = new LinearLayout.LayoutParams(fillParent(), wrapContent());
		positiveParams.weight = 1;
		container.addView(positiveButton, positiveParams);
		
		negativeButton = new Button(getContext());
		LinearLayout.LayoutParams negativeParams = new LinearLayout.LayoutParams(fillParent(), wrapContent());
		negativeParams.weight = 1;
		container.addView(negativeButton, negativeParams);
		
		positiveButton.setOnClickListener(this);
		negativeButton.setOnClickListener(this);
		
		LinearLayout.LayoutParams ctrParams = new LinearLayout.LayoutParams(fillParent(), wrapContent());
		ctrParams.topMargin = DimensionUtility.dp2px(8);
		rootLayout.addView(container, ctrParams);
		
		return rootLayout;
	}
	
	@SuppressWarnings({ "deprecation" })
	private int fillParent() {
		// TODO Auto-generated method stub
		return LinearLayout.LayoutParams.FILL_PARENT;
	}
	private int wrapContent() {
		return LinearLayout.LayoutParams.WRAP_CONTENT;
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
		if (v == positiveButton) {
			dismiss();
			onDialogClose(DialogListener.BTN_OK);
		}else if (v == negativeButton) {
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
