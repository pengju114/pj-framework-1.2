package com.pj.core.utilities;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.pj.core.managers.LogManager;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;

public class ImageUtility {
	public static Bitmap toRoundCorner(Bitmap bitmap,float radius) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Config.ARGB_8888);
	        //得到画布
	        Canvas canvas = new Canvas(output);
	    
	       //将画布的四角圆化
	        final int color = Color.RED; 
	        final Paint paint = new Paint(); 
	        //得到与图像相同大小的区域  由构造的四个值决定区域的位置以及大小
	        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()); 
	        final RectF rectF = new RectF(rect); 
	        //值越大角度越明显
	      
	        paint.setAntiAlias(true); 
	        canvas.drawARGB(0, 0, 0, 0); 
	        paint.setColor(color); 
	        //drawRoundRect的第2,3个参数一样则画的是正圆的一角，如果数值不同则是椭圆的一角
	        canvas.drawRoundRect(rectF, radius,radius, paint); 
	      
	        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN)); 
	        canvas.drawBitmap(bitmap, rect, rect, paint); 
	      
	        return output;
	}
	
	/**
	 * 压缩图片，最小尺寸为480px(最常见屏幕宽度)
	 * @param path
	 * @return
	 */
	public static Bitmap scaleCompress(String path){
		return scaleCompress(path, 480);
	}
	public static Bitmap scaleCompress(String path,int minSize){
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeFile(path, o);

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int size=Math.min(width_tmp, height_tmp);
			int scale = Math.round((float)size/(float)minSize);
			
			if (scale%2!=0) {
				scale--;
			}
			
			if (scale<1) {
				scale=1;
			}
			
			
			// decode with inSampleSize
			o.inJustDecodeBounds=false;
			o.inSampleSize=scale;
			return BitmapFactory.decodeFile(path, o);
		} catch (Exception e) {
			// TODO: handle exception
			LogManager.trace(e);
		}
		return null;
	}
	
	public static Bitmap scaleCompress(Bitmap bitmap,int minSize){
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		ByteArrayOutputStream outputStream=new ByteArrayOutputStream(bitmap.getRowBytes());
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
		ByteArrayInputStream inputStream=new ByteArrayInputStream(outputStream.toByteArray());
		try {
			Rect sizeRect=new Rect();
			BitmapFactory.decodeStream(inputStream, sizeRect, o);

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int size=Math.min(width_tmp, height_tmp);
			int scale = Math.round((float)size/(float)minSize);
			if (scale%2!=0) {
				scale--;
			}
			
			if (scale<1) {
				scale=1;
			}
			o.inJustDecodeBounds=false;
			o.inSampleSize=scale;
			inputStream.reset();
			return BitmapFactory.decodeStream(inputStream, sizeRect, o);
		} catch (Exception e) {
			// TODO: handle exception
			LogManager.trace(e);
		}
		return null;
	}
	
	public static Bitmap compressBitmap(Bitmap bitmap,int maxKBSize) {  
		int rawBytes=bitmap.getRowBytes()*bitmap.getHeight();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(rawBytes);  
        int options = 0;
        long maxBytes=maxKBSize*1024;
        options=Math.round(((float)maxBytes/(float)rawBytes)*100);
        if (options<0) {
			options=1;
		}else if (options>100) {
			options=100;
		}
        
        do {
        	baos.reset();//重置baos即清空baos 
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中  
            LogManager.i(ImageUtility.class.getSimpleName(), "compress image quality=%d and size=%d[orgn=%d]", options,baos.size(),rawBytes);
            options -= 10;//每次都减少10 
            if (options<0) {
				break;
			}
		} while (baos.size()>maxBytes);
        
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中  
        Bitmap newBitmap = BitmapFactory.decodeStream(isBm);//把ByteArrayInputStream数据生成图片  
        return newBitmap;  
    }
	
	public static Bitmap compressBitmap(String path,int maxKBSize) {  
		  
        return compressBitmap(BitmapFactory.decodeFile(path), maxKBSize);
    }
	
	/**
	 * 创建带倒影的图片
	 * @param originalImage 原始图片
	 * @param heightScale   倒影高度和原始图片高度的百分比
	 * @param reflectionGap 原始图片和倒影图片间距
	 * @return
	 */
	public static Bitmap createBitmapWithReflection(Bitmap originalImage,float heightScale,int reflectionGap) {
		int orgnWidth =originalImage.getWidth();
		int orgnHeight=originalImage.getHeight();
        int reflectionHeight =Math.round(orgnHeight*heightScale);
        
          
        Matrix matrix = new Matrix();     
        matrix.preScale(1.0f, -1.0f);         // 图片矩阵变换（从低部向顶部的倒影）  
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, orgnHeight-reflectionHeight, orgnWidth, reflectionHeight, matrix, false);   // 截取部分原图     
        Bitmap bitmapWithReflection = Bitmap.createBitmap(orgnWidth, (orgnHeight + reflectionHeight+reflectionGap), Config.ARGB_8888);      // 创建倒影图片（高度为原图3/2）     
          
        Canvas canvas = new Canvas(bitmapWithReflection);   // 绘制倒影图（原图 + 间距 + 倒影）     
        canvas.drawBitmap(originalImage, 0, 0, null);       // 绘制原图     
             
        canvas.drawBitmap(reflectionImage, 0, orgnHeight + reflectionGap, null);    // 绘制倒影图     
        
        Paint paint = new Paint();    
        LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0, bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);     
        paint.setShader(shader);    // 线性渐变效果     
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));     // 倒影遮罩效果     
        canvas.drawRect(0, orgnHeight+reflectionGap, orgnWidth, bitmapWithReflection.getHeight(), paint);     // 绘制倒影的阴影效果
        return bitmapWithReflection;
	}
}
