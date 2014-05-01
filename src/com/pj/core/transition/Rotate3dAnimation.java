package com.pj.core.transition;

import android.graphics.Camera;  
import android.graphics.Matrix;  
import android.view.animation.Animation;  
import android.view.animation.Transformation;  
  
public class Rotate3dAnimation extends Animation {  
	private float Z_VALUE=1000.0f;
	
    // 开始角度  
    private final float mFromDegrees;  
    // 结束角度  
    private final float mToDegrees;  
    // 中心点  
    private float mCenterX;  
    private float mCenterY;  
    private float mDepthZ;  
    // 是否需要扭曲  
    private final boolean mReverse;  
    // 摄像头  
    private Camera mCamera;  
  
    /**
     * 
     * @param fromDegrees 开始角度
     * @param toDegrees   目标角度
     * @param centerX     中心点X值的百分比(以视图宽度为参考)
     * @param centerY     中心点Y值的百分比(以视图高度为参考)
     * @param depthZ      中心点Z值的百分比
     * @param reverse     Z坐标在XY平面前面还是后面，true为前面，false为后面
     */
    public Rotate3dAnimation(float fromDegrees, float toDegrees, float centerX,  
            float centerY, float depthZ, boolean reverse) {  
        mFromDegrees = fromDegrees;  
        mToDegrees = toDegrees;  
        mCenterX = centerX;  
        mCenterY = centerY;  
        mDepthZ = depthZ;  
        mReverse = reverse;  
        
    }  
  
    @Override  
    public void initialize(int width, int height, int parentWidth,  
            int parentHeight) {  
        super.initialize(width, height, parentWidth, parentHeight);  
        mCamera = new Camera(); 
        
        if (width>0) {
        	Z_VALUE = width;
            mCenterX *= width;
		}else {
			Z_VALUE = parentWidth;
	        mCenterX *= parentWidth;
		}
        
        if (height>0) {
        	mCenterY *= height;
		}else {
			mCenterY *= parentHeight;
		}
        
        mDepthZ  *= Z_VALUE;
    }  
  
    // 生成Transformation  
    @Override  
    protected void applyTransformation(float interpolatedTime, Transformation t) {  
        final float fromDegrees = mFromDegrees;  
        // 生成中间角度  
        float degrees = fromDegrees  
                + ((mToDegrees - fromDegrees) * interpolatedTime);  
  
        final float centerX = mCenterX;  
        final float centerY = mCenterY;  
        final Camera camera = mCamera;  
  
        final Matrix matrix = t.getMatrix();  
  
        camera.save();  
        if (mReverse) {  
            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);  
        } else {  
            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));  
        }  
        camera.rotateY(degrees);  
        // 取得变换后的矩阵  
        camera.getMatrix(matrix);  
        camera.restore();  
  
        matrix.preTranslate(-centerX, -centerY);  
        matrix.postTranslate(centerX, centerY);  
    }  
}