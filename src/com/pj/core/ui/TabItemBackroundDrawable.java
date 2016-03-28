package com.pj.core.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class TabItemBackroundDrawable extends Drawable {
	
	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private int backgroundColor = Color.TRANSPARENT;
	private int tintStartColor = Color.TRANSPARENT;
	private int tintEndColor = Color.argb(0x66, 0xFF, 0xFF, 0xFF);
	private float paddingTopPercent = 0.3f;
	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		Rect rect = getBounds();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setShader(null);
		canvas.drawColor(backgroundColor);
		
		// 画椭圆的一部分
		Path path = new Path();
		path.moveTo(rect.right, rect.bottom);
		path.lineTo(rect.left, rect.bottom);
		
		System.out.println(rect);
		
		//计算曲线
		float h = rect.height();
		float b = h * (1.0f - paddingTopPercent);
		float w = rect.width();
		float a = w * 0.5f;
		// 椭圆公式 x²/a²+y²/b² = 1,其中a是长轴半径，b是短轴半径
		for (float x = -a; x <= a; x+=1) {
			float y = b * (float)Math.sqrt(1.0f - Math.pow(x*(1.0f/a), 2));
			path.lineTo(x + a, h - y);
		}
		
		// 渐变
		RadialGradient radialGradient = new RadialGradient(a, rect.bottom, b, tintEndColor, tintStartColor, Shader.TileMode.CLAMP);
//		LinearGradient linearGradient = new LinearGradient(a, h - b, a, rect.bottom, tintStartColor, tintEndColor, Shader.TileMode.CLAMP);
		
//		ComposeShader shader = new ComposeShader(radialGradient, linearGradient,Mode.OVERLAY );
		mPaint.setShader(radialGradient);	
		canvas.drawPath(path, mPaint);
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getTintStartColor() {
		return tintStartColor;
	}

	public void setTintStartColor(int tintStartColor) {
		this.tintStartColor = tintStartColor;
	}

	public int getTintEndColor() {
		return tintEndColor;
	}

	public void setTintEndColor(int tintEndColor) {
		this.tintEndColor = tintEndColor;
	}

	public float getPaddingTopPercent() {
		return paddingTopPercent;
	}

	public void setPaddingTopPercent(float paddingTopPercent) {
		this.paddingTopPercent = paddingTopPercent;
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
