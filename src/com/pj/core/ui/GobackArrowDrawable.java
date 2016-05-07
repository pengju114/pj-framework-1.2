package com.pj.core.ui;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class GobackArrowDrawable extends BaseDrawable {
	
	
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
		mPaint.setStyle(Paint.Style.STROKE);
		if (contentHeight == 0) {
			contentHeight = rect.height();
		}
		canvas.drawColor(Color.TRANSPARENT);
		
		float height = contentHeight - 4;
		float halfHeight = height * 0.5f;
		
		float x = strokeWidth * 0.5f;
		float y = (rect.height() - height) * 0.5f;
		
		float paddingX = x;
		
		x +=  halfHeight/Math.tan(Math.toRadians(degree * 0.5f));
		
		mPaint.setStrokeWidth(strokeWidth);
		mPaint.setColor(color);
		mPaint.setStrokeCap(Paint.Cap.SQUARE);
		
		Path path = new Path();
		
		path.moveTo(x, y);
		path.lineTo(paddingX, y + halfHeight);
		path.lineTo(x, y + height);
		canvas.drawPath(path, mPaint);
	}
}
