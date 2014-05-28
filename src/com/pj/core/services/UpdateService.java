package com.pj.core.services;

import java.io.File;
import java.text.NumberFormat;

import com.pj.core.R;
import com.pj.core.http.HttpDownloader;
import com.pj.core.http.HttpDownloader.HttpStateListener;
import com.pj.core.http.HttpRequest;
import com.pj.core.http.HttpState;
import com.pj.core.http.HttpTransfer;
import com.pj.core.managers.LogManager;
import com.pj.core.res.Constants;
import com.pj.core.utilities.AppUtility;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.RemoteViews;

public class UpdateService extends BaseService implements Runnable,HttpStateListener{
	private static final int NOTIFICATION_ID=0x11200102;
	
	private NotificationManager notificationManager;
	private Notification 		notification;
	private RemoteViews 		remoteViews;
	private HttpTransfer 		httpTransfer;
	private HttpState 			httpState;
	private HttpDownloader		downloader;
	private long 				lastReadedBytes;
	
	private Handler 			loopHandler=new Handler(Looper.getMainLooper());
	private int 				updateDelay=400;
	private NumberFormat 		numberFormat;

	
	public void onCreate() {
		super.onCreate();
		notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		numberFormat=NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(2);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public void onStart(Intent intent,int startId) {
		super.onStart(intent, startId);
		if (intent!=null) {
			httpTransfer=(HttpTransfer) intent.getParcelableExtra(Constants.Keys.TRANS_DATA);
			if (httpTransfer!=null) {
				notification=getNotification(httpTransfer);
				startDownload();
			}
		}
	}
	
	
	private void startDownload() {
		// TODO Auto-generated method stub
		downloader=new HttpDownloader(httpTransfer);
		//不支持断点续传
		downloader.setBreakpointContinuinglySupport(false);
		downloader.setHttpStateListener(this);
		downloader.setMethod(HttpRequest.METHOD_GET);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					downloader.download();
				} catch (Exception e) {
					// TODO: handle exception
					LogManager.trace(e);
				}
			}
		}).start();
		loopUpdate();
	}

	protected RemoteViews getRemoteViews(HttpTransfer transfer) {
		RemoteViews rv=new RemoteViews(getApplication().getPackageName(),R.layout.c_update_progress);
		rv.setTextViewText(R.id.c_update_label, transfer.getDescription());
		rv.setImageViewBitmap(R.id.c_update_icon, httpTransfer.getThumbnail());
		return rv;
	}
	
	protected PendingIntent getPendingIntent(HttpTransfer transfer) {
		return PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	@SuppressWarnings("deprecation")
	protected Notification getNotification(HttpTransfer transfer) {
		Notification n=new Notification(getIconResourceId(),getResources().getString(R.string.c_msg_n_start_download_update),System.currentTimeMillis());
		remoteViews=getRemoteViews(transfer);
		n.flags=n.flags|Notification.FLAG_ONGOING_EVENT;
		n.contentView=remoteViews;
		n.contentIntent=getPendingIntent(transfer);
		
		return n;
	}
	
	protected int getIconResourceId() {
		return R.drawable.ic_launcher;
	}
	
	protected void loopUpdate(){
		stopUpdate();
		loopHandler.postDelayed(this, updateDelay);
	}
	protected void stopUpdate(){
		loopHandler.removeCallbacks(this);
	}

	//在这里定时更新通知栏
	@Override
	public void run() {
		// TODO Auto-generated method stub
		refresh();
		loopUpdate();
	}

	/**
	 * 更新通知栏
	 * PENGJU
	 * 2012-12-4 上午10:40:49
	 */
	protected void refresh() {
		// TODO Auto-generated method stub
		if (httpState!=null) {
			String lString=byte2mb(httpState.getTotalTransferBytes())+"M/"+byte2mb(httpState.getLength())+"M";
			long r=httpState.getTotalTransferBytes();
			float speed=((float)(r-lastReadedBytes)/1024F)/(float)(updateDelay/1000F);//KB/s
			String rString="MB/s";
			if (speed<1000) {
				rString="KB/s";
			}else {
				speed/=1024F;
			}
			rString=numberFormat.format(speed)+rString;
					
			lastReadedBytes=r;
			remoteViews.setTextViewText(R.id.c_progress_llabel, lString);
			remoteViews.setTextViewText(R.id.c_progress_rlabel, rString);
			remoteViews.setProgressBar(R.id.c_progress, 100, (int)(((float)httpState.getTotalTransferBytes()/(float)httpState.getLength())*100), false);
			notificationManager.notify(NOTIFICATION_ID, notification);
		}
	}

	/**
	 * UI线程
	 */
	@Override
	public void httpStateChange(int state, HttpState httpState, HttpDownloader downloader,String status) {
		// TODO Auto-generated method stub
		this.httpState=httpState;
		if (state==HttpStateListener.STATE_RUNNING) {
			onDownloading(httpState);
		}else if (state==HttpStateListener.STATE_ERROR) {
			onDownloadError(httpState);
		}else if (state==HttpStateListener.STATE_FINISH) {
			onDownloadEnd(httpState,downloader.getFile());
		}else if (state==HttpStateListener.STATE_START) {
			notifyDownload(downloader.getFile());
		}
	}

	private void onDownloadError(HttpState httpState2) {
		// TODO Auto-generated method stub
		stopUpdate();
		showTip(getResources().getString(R.string.c_msg_n_download_update_error, httpState2.getStatusText()));
	}

	private void onDownloading(HttpState httpState2) {
		// 不在这里更新,
		
	}

	private void onDownloadEnd(HttpState httpState2,File file) {
		// TODO Auto-generated method stub
		stopUpdate();
		if (httpState2.getTotalTransferBytes()<httpState2.getLength()) {
			//未成功下载
		}else {//提示安装
			try {
				AppUtility.installApk(file);
			} catch (Exception e) {
				// TODO: handle exception
				LogManager.trace(e);
			}
		}
		
		notification.flags=Notification.FLAG_AUTO_CANCEL;
		notification.tickerText=getResources().getString(R.string.c_msg_n_download_finish);
		refresh();
		stop();
	}

	protected void notifyDownload(File file) {
		// TODO Auto-generated method stub
		remoteViews.setTextViewText(R.id.c_progress_llabel, "0M/"+byte2mb(Long.valueOf(file.length()))+"M");
		remoteViews.setTextViewText(R.id.c_progress_rlabel, "0KB/s");
		notificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	protected String byte2mb(Number number){
		double size=number.doubleValue();
		size/=1024D;
		size/=1024D;
		return numberFormat.format(size);
	}
	
	public void stop() {
		stopUpdate();
		downloader.setHttpStateListener(null);
		downloader.setAbort(true);
		notificationManager.cancel(NOTIFICATION_ID);
		stopSelf();
	}
}
