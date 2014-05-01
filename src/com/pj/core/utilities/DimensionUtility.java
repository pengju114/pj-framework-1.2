package com.pj.core.utilities;

import com.pj.core.BaseApplication;

import android.content.res.Resources;

public class DimensionUtility {
	public static final int dp2px(float dp){
		Resources resources=BaseApplication.getInstance().getResources();
		return Math.round(resources.getDisplayMetrics().density*dp);
	}
	
	public static final int sp2px(float sp){
		Resources resources=BaseApplication.getInstance().getResources();
		return Math.round(resources.getDisplayMetrics().scaledDensity*sp);
	}
	
	public static final int px2dp(float px){
		Resources resources=BaseApplication.getInstance().getResources();
		return Math.round(px/resources.getDisplayMetrics().density);
	}
	
	public static final int px2sp(float px){
		Resources resources=BaseApplication.getInstance().getResources();
		return Math.round(px/resources.getDisplayMetrics().scaledDensity);
	}
}
