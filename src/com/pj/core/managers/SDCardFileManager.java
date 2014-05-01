package com.pj.core.managers;

import java.io.File;

import com.pj.core.BaseApplication;
import com.pj.core.res.AppConfig;

import android.os.Environment;
import android.os.StatFs;

public class SDCardFileManager {
	/** 应用程序根目录  */
	public static final String APP_DIR;
	/** 缓存目录  */
	public static final String CACHE_DIR;
	/** 下载目录  */
	public static final String DOWNLOAD_DIR;
	
	static{
		String dir=AppConfig.getConfig(AppConfig.CONF_APP_DIR, AppConfig.VALUE_APP_DIR);

		if (isSDCardReady()) {
			APP_DIR=Environment.getExternalStorageDirectory()+File.separator+dir;
		}else {
			APP_DIR=BaseApplication.getInstance().getCacheDir()+File.separator+dir;
		}
		
		String tmpDir=AppConfig.getConfig(AppConfig.CONF_APP_CACHE_DIR, AppConfig.VALUE_CACHE_DIR);
		CACHE_DIR=APP_DIR+File.separator+tmpDir;
		
		String downloadDir=AppConfig.getConfig(AppConfig.CONF_APP_DOWNLOADS_DIR, AppConfig.VALUE_APP_DOWNLOADS_DIR);
		DOWNLOAD_DIR=APP_DIR+File.separator+downloadDir;
	}
	/**
	 * 判断sdcard是否嵌入并且可以读写
	 * PENGJU
	 * 2012-7-29 上午9:39:03
	 * @return
	 */
	public static boolean isSDCardReady() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * 获取sdcard总容量（单位：MB）,获取失败返回-1
	 * 2013-6-9 下午9:45:04
	 * @return mb
	 */
	public static float getTotalMBSize(){
		float all=-1;
		if (isSDCardReady()) {
			File root=new File(APP_DIR).getParentFile();
			
			if (root!=null) {
				try {
					StatFs fs=new StatFs(root.getPath());
					float size=fs.getBlockSize()/1024.0f/1024.0f;//MB
					all=fs.getBlockCount()*size;
				} catch (Exception e) {
					LogManager.trace(e);
				}
			}
		}
		return all;
	}
	
	
	/**
	 * 获取sdcard可用空间（单位：MB），如果存储卡损坏了可能返回负数或者0
	 * 2013-6-9 下午9:45:04
	 * @return mb
	 */
	public static float getFreeMBSize(){
		float all=-1;
		if (isSDCardReady()) {
			File root=new File(APP_DIR).getParentFile();
			if (root!=null) {
				try {
					StatFs fs=new StatFs(root.getPath());
					float size=fs.getBlockSize()/1024.0f/1024.0f;//MB
					all=fs.getAvailableBlocks()*size;
				} catch (Exception e) {
					LogManager.trace(e);
				}
			}
		}
		
		return all;
	}
}
