package com.pj.core.ui;

import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class SearchIconDrawable extends BaseDrawable {

	public static enum Direction {
		LeftTop, TopRight, RightBottom, BottomLeft
	}

	public static enum Gravity {
		Top, Right, Bottom, Left, Center
	}

	private int padding = 0;
	private Direction direction = Direction.RightBottom;
	private boolean lighting = false;
	private int color = Color.WHITE;
	private int backgroundColor = Color.TRANSPARENT;
	private float strokeWidth = 2;
	private Gravity gravity = Gravity.Center;

	@Override
	public void draw(Canvas canvas) {
		
		// TODO Auto-generated method stub
		canvas.drawColor(backgroundColor);

		Rect rect = copyBounds();
		rect.left += padding;
		rect.top += padding;
		rect.right -= padding;
		rect.bottom -= padding;

		mPaint.setColor(color);
		mPaint.setStrokeWidth(strokeWidth);
		mPaint.setStyle(Paint.Style.STROKE);

		// 如果启用了硬件加速，发光效果将不起作用
		if (lighting) {
			float r = 10;
			int density = canvas.getDensity();
			if (density != Bitmap.DENSITY_NONE) {
				r *= density;
			}
			BlurMaskFilter blurMaskFilter = new BlurMaskFilter(r, BlurMaskFilter.Blur.SOLID);
			mPaint.setMaskFilter(blurMaskFilter);
		} else {
			mPaint.setMaskFilter(null);
		}
		float w = rect.width();
		float h = rect.height();
		float radius = Math.min(w, h) * 0.5f - strokeWidth;
		float delta = radius / (float) Math.sqrt(2);
		float radiusExt = delta/2 + radius;
		float x1 = 0, y1 = 0, x2 = 0, y2 = 0;

		// 默认是Center
		float centerX = rect.left + w * 0.5f;
		float centerY = rect.top + h * 0.5f;
		
		if (h != w) {
			
			float movement = Math.abs(w - h) * 0.5f;
			if (h < w) {// 无需考虑 Top和Bottom
				if (gravity == Gravity.Left) {
					centerX -= movement;
				}else if (gravity == Gravity.Right) {
					centerX += movement;
				}
			}else {
				if (gravity == Gravity.Top) {
					centerY -= movement;
				}else if (gravity == Gravity.Bottom) {
					centerY += movement;
				}
			}
		}
		canvas.drawCircle(centerX, centerY, radius, mPaint);

		switch (direction) {
		case LeftTop:
			x1 = centerX - radiusExt;
			y1 = centerY - radiusExt;
			x2 = centerX - delta;
			y2 = centerY - delta;
			break;
		case TopRight:
			x1 = centerX + radiusExt;
			y1 = centerY - radiusExt;
			x2 = centerX + delta;
			y2 = centerY - delta;
			break;
		case RightBottom:
			x1 = centerX + radiusExt;
			y1 = centerY + radiusExt;
			x2 = centerX + delta;
			y2 = centerY + delta;
			break;
		case BottomLeft:
			x1 = centerX - radiusExt;
			y1 = centerY + radiusExt;
			x2 = centerX - delta;
			y2 = centerY + delta;
			break;

		default:

			break;
		}

		canvas.drawLine(x1, y1, x2, y2, mPaint);
	}

	public int getPadding() {
		return padding;
	}

	public void setPadding(int padding) {
		this.padding = padding;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public boolean isLighting() {
		return lighting;
	}

	public void setLighting(boolean lighting) {
		this.lighting = lighting;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public float getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(float strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	public Gravity getGravity() {
		return gravity;
	}

	public void setGravity(Gravity gravity) {
		this.gravity = gravity;
	}
}
