package com.pj.core.ui;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class GobackArrowDrawable extends Drawable {
	
	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private float contentHeight = 0;
	
	private int   color ;
	
	private int   degree = 90;
	/** 像素 */
	private float strokeWidth = 2;
	
	
	
	public GobackArrowDrawable(int color,float contentHeight){
		this.color = color;
		this.contentHeight = contentHeight;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	public void setDegree(int degree) {
		this.degree = degree;
	}
	public void setStrokeWidth(float strokeWidth) {
		this.strokeWidth = strokeWidth;
	}
	
	public void setContentHeight(float contentHeight) {
		this.contentHeight = contentHeight;
	}
	

	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		Rect rect = getBounds();
		
		if (contentHeight == 0) {
			contentHeight = rect.height();
		}
		canvas.drawColor(Color.TRANSPARENT);
		
//		Path path = new Path();
		float height = contentHeight - 4;
		float halfHeight = height * 0.5f;
		
		float x = strokeWidth*0.5f;
		float y = (rect.height() - height) * 0.5f;
		
		float paddingX = x;
		
		x +=  halfHeight/Math.tan(Math.toRadians(degree * 0.5f));
		
		mPaint.setStrokeWidth(strokeWidth);
		mPaint.setColor(color);
		
		canvas.drawLine(x, y, paddingX, y + halfHeight + 0.7f, mPaint);
		canvas.drawLine(paddingX, y + halfHeight - 0.7f, x, y + height, mPaint);
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return PixelFormat.OPAQUE;
	}

}
