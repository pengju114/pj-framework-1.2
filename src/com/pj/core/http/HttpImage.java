package com.pj.core.http;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pj.core.BaseApplication;
import com.pj.core.managers.LogManager;
import com.pj.core.managers.SDCardFileManager;
import com.pj.core.utilities.ImageUtility;
import com.pj.core.utilities.SecurityUtility;
import com.pj.core.utilities.StringUtility;





import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class HttpImage {

	private final MemoryCache 				memoryCache;
	private final FileCache 				fileCache;
	private final Map<ImageView, String> 	imageViews;
	private final ExecutorService 			executorService;
	
	private static HttpImage SINGLE_INSTANCE;

	private HttpImage() {
		memoryCache 	= new MemoryCache();
		fileCache     	= new FileCache();
		imageViews 		= Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
		executorService = Executors.newFixedThreadPool(5);
	}
	
	public static HttpImage getInstance(){
		if (SINGLE_INSTANCE==null) {
			synchronized (HttpImage.class) {
				if (SINGLE_INSTANCE==null) {
					SINGLE_INSTANCE=new HttpImage();
				}
			}
		}
		return SINGLE_INSTANCE;
	}
	
	protected void finalize() throws Throwable{
		executorService.shutdown();
		long s=Runtime.getRuntime().freeMemory();
		clearCache(false);
		s=Runtime.getRuntime().freeMemory()-s;
		if (LogManager.isLogEnable()) {
			LogManager.i(getClass().getSimpleName(), "清空缓存 %d KB",s/1024);
		}
		SINGLE_INSTANCE=null;
		super.finalize();
	}


	public void setImage(String url, ImageView imageView, int defaultImgRes) {
		setImage(url, imageView, defaultImgRes,0F);
	}
	
	public void setImage(String url, ImageView imageView, int defaultImgRes,float radius) {
		setImage(url, imageView, defaultImgRes,radius, 50);
	}
	
	public void setImage(String url, ImageView imageView, int defaultImgRes,float radius,int scaleMinSize) {
		if (StringUtility.isEmpty(url)) {
			applyImage(imageView, null, defaultImgRes, radius);
			return;
		}
		
		imageViews.put(imageView, url);
		// 先从内存缓存中查找
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap == null) {
			// 若没有的话则开启新线程加载图片
			queuePhoto(url, imageView,defaultImgRes,radius,scaleMinSize);
		}
		applyImage(imageView, bitmap, defaultImgRes, radius);
	}


	private void applyImage(ImageView target,Bitmap bitmap,int resId,float radius){
		try {//注意内存溢出错误
			if (bitmap==null) {
				bitmap=BitmapFactory.decodeResource(target.getContext().getResources(), resId);
			}
			if (radius>0) {
				bitmap=ImageUtility.toRoundCorner(bitmap, radius);
			}
			target.setImageBitmap(bitmap);
		} catch (Exception e) {
			// TODO: handle exception
			LogManager.trace(e);
		}
	}
	
	private void queuePhoto(String url, ImageView imageView,int defaultImgRes,float radius,int scaleMinSize) {
		PhotoToLoad p = new PhotoToLoad(url, imageView,defaultImgRes,radius,scaleMinSize);
		executorService.submit(new PhotosLoader(p));
	}

	
	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		
		public int defaultImgRes;
		public float radius;
		
		public int scaleMinSize;

		public PhotoToLoad(String u, ImageView i,int defaultImgRes,float radius,int scaleMinSize) {
			url = u;
			imageView = i;
			this.defaultImgRes=defaultImgRes;
			this.radius=radius;
			this.scaleMinSize=scaleMinSize;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad)) {
				return;
			}
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad)) {
				return;
			}
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			// 更新的操作放在UI线程中
			BaseApplication.UI_THREAD_HANDLER.post(bd);
		}
		
		
		private Bitmap getBitmap(String url) {
			LogManager.i("获取图片", url);
			File f = fileCache.getFile(url);

			// 先从文件缓存中查找是否有
			Bitmap b = decodeFile(f);
			if (b != null){
				return b;
			}

			// 最后从指定的url中下载图片
			try {
				Bitmap bitmap = null;
				URL imageUrl = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) imageUrl
						.openConnection();
				conn.setConnectTimeout(50000);
				conn.setReadTimeout(50000);
				conn.setInstanceFollowRedirects(true);
				InputStream is = conn.getInputStream();
				OutputStream os = new FileOutputStream(f);
				CopyStream(is, os);
				is.close();
				os.close();
				bitmap = decodeFile(f);
				return bitmap;
			} catch (Exception ex) {
				LogManager.trace(ex);
				return null;
			}
		}
		
		// decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
		private Bitmap decodeFile(File f) {
			if (f==null || !f.exists()) {
				return null;
			}
			return ImageUtility.scaleCompress(f.getAbsolutePath(),photoToLoad.scaleMinSize);
		}
	}

	/**
	 * 防止图片错位
	 * 
	 * @param photoToLoad
	 * @return
	 */
	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url)) {
			return true;
		}
		return false;
	}

	// 用于在UI线程中更新界面
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad)) {
				return;
			}
			applyImage(photoToLoad.imageView, bitmap, photoToLoad.defaultImgRes, photoToLoad.radius);

		}
	}

	public void clearCache(boolean clearFileCache) {
		memoryCache.clear();
		if (clearFileCache) {
			FileCache.clearTmpFile(-10000);
		}
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}
	
	
	public void removeCache(String url){
		String name=FileCache.getFileName(url);
		File file=new File(SDCardFileManager.CACHE_DIR+File.separator+name);
		if (file.exists()) {
			file.delete();
		}
		memoryCache.removeCache(url);
	}
	
	public static class FileCache {

		private static File CAHCHE_DIR;

		public static final String SUFFIX = ".ctmp";

		public FileCache() {
			CAHCHE_DIR = new File(SDCardFileManager.CACHE_DIR);
			CAHCHE_DIR.mkdirs();
		}

		public File getFile(String url) {
			// 将url的hashCode作为缓存的文件名
			String filename = getFileName(url);
			// Another possible solution
			// String filename = URLEncoder.encode(url);
			File f = new File(CAHCHE_DIR, filename);
			return f;

		}
		
		public static String getFileName(String url){
			return (SecurityUtility.MD5Encrypt(url, null) + SUFFIX);
		}
		
		public static void clearTmpFile(final long cacheDuration) {
			if (CAHCHE_DIR!=null) {
				final long now=System.currentTimeMillis();
				File[] tmpFiles = CAHCHE_DIR.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						// TODO Auto-generated method stub
						return (pathname.isFile() && pathname.getName().endsWith(SUFFIX) && (now-pathname.lastModified())>=cacheDuration);
					}
				});
				if (tmpFiles != null) {
					for (File f : tmpFiles) {
						f.delete();
					}
					LogManager.i("FileCache","清空了%d个本地缓存图片",tmpFiles.length);
				}
			}
		}
		
		public static void clearOriginalFile(final long cacheDuration) {
			if (CAHCHE_DIR!=null) {
				final long now=System.currentTimeMillis();
				File[] originalFiles=CAHCHE_DIR.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						// TODO Auto-generated method stub
						return (pathname.isFile() && (now-pathname.lastModified())>=cacheDuration);
					}
				});
				if (originalFiles!=null) {
					for (File file : originalFiles) {
						file.delete();
					}
					LogManager.i("FileCache","清空了%d个本地原始缓存图片",originalFiles.length);
				}
			}
		}

		public static void clear() {
			clearTmpFile(-1000);
			clearOriginalFile(-1000);
		}

	}
	
	
	public class MemoryCache {
		// 放入缓存时是个同步操作
		// LinkedHashMap构造方法的最后一个参数true代表这个map里的元素将按照最近使用次数由少到多排列，即LRU
		// 这样的好处是如果要将缓存中的元素替换，则先遍历出最近最少使用的元素来替换以提高效率
		private Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true));
		// 缓存中图片所占用的字节，初始0，将通过此变量严格控制缓存所占用的堆内存
		private long size = 0;// current allocated size
		// 缓存只能占用的最大堆内存
		private long limit = 1000000;// max memory in bytes

		public MemoryCache() {
			// use 10% of available heap size
			setLimit(Runtime.getRuntime().maxMemory() / 10);
		}

		public void setLimit(long new_limit) {
			limit = new_limit;
			LogManager.i(getClass().getSimpleName(), "内存缓冲区大小：%d 字节",limit);
		}

		public Bitmap get(String id) {
			if (cache != null) {
				return cache.get(id);
			}
			return null;
		}

		public void put(String id, Bitmap bitmap) {
			if (cache != null) {
				Bitmap oldBitmap = cache.get(id);
				size -= getSizeInBytes(oldBitmap);
				cache.put(id, bitmap);
				size += getSizeInBytes(bitmap);
				checkSize();
			}
		}

		/**
		 * 严格控制堆内存，如果超过将首先替换最近最少使用的那个图片缓存
		 * 
		 */
		private void checkSize() {
			LogManager.i(getClass().getSimpleName(), "cache size=%d bytes, length=%d", size , cache.size());
			if (size > limit) {
				// 先遍历最近最少使用的元素
				Iterator<Entry<String, Bitmap>> iter = cache.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<String, Bitmap> entry = iter.next();
					size -= getSizeInBytes(entry.getValue());
					iter.remove();
					if (size <= limit) {
						break;
					}
				}
				LogManager.i(getClass().getSimpleName(), "Clean cache. New size=%d bytes, length=%d", size , cache.size());
			}
		}

		public void clear() {
			cache.clear();
		}
		
		public void removeCache(String key){
			cache.remove(key);
		}

		/**
		 * 图片占用的内存
		 * 
		 * @param bitmap
		 * @return
		 */
		long getSizeInBytes(Bitmap bitmap) {
			return bitmap == null ? 0 : (bitmap.getRowBytes() * bitmap.getHeight());
		}
	}
}
