package com.pj.core.dialog;

public interface DialogListener {
	/**按下确认按钮*/
	int BTN_OK=0xF1F2F;
	/**按下取消按钮*/
	int BTN_CANCEL=0xF1F2C;
	/**未知按钮，一般是返回键取消*/
	int BTN_UNKNOWN=-1;
	
	/**
	 * 对话框取消时触发
	 * @param requestCode 
	 * @param dialog
	 * @param triggerbtn
	 * @param cacheData
	 */
	public void onDialogClose(int requestCode,CacheableDialog dialog,int triggerbtn,Object cacheData);
}
