package com.pj.core.http;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.pj.core.BaseApplication;
import com.pj.core.R;
import com.pj.core.managers.LogManager;
import com.pj.core.utilities.AppUtility;
import com.pj.core.utilities.StringUtility;
import com.pj.core.utilities.URLUtility;

/**
 * HTTP下载器,在启用断点续传前要确保服务器端也支持
 * @author 陆振文[PENGJU]
 * 2012-12-5 上午10:56:02
 * email: pengju114@163.com
 */
public class HttpDownloader extends Parameter{
	private static final long 	serialVersionUID = -3894876982244667042L;
	
	public  static final String CHARSET="UTF-8";
	public  static final int 	READ_TIME=15000;
	private static final int 	BUFFER_SIZE=1024*10;//10KB;
	
	private String 			fileName;
	private File 			file;
	private File 			saveDir;
	private String 			method;
	private HttpState 		httpState;
	private HttpTransfer 	httpTransfer;
	private int				requestCode;
	private Object			extraObject;
	
	
	//是否断点续传
	private boolean breakpointContinuinglySupport;
	//是否中断
	private boolean abort;
	
	private Parameter 			headers;
	private HttpStateListener 	stateListener;
	
	
	public HttpDownloader(HttpTransfer httpTransfer){
		method=HttpRequest.METHOD_GET;
		headers=new Parameter();
		abort=false;
		breakpointContinuinglySupport=false;//默认不开启断点续传
		httpState=new HttpState();
		this.httpTransfer=httpTransfer;
		
		saveDir=new File(httpTransfer.getTo());
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}
	}

	/**
	 * 添加一个HTTP头
	 * @param name
	 * @param value
	 */
	public void addHeader(String name,String value) {
		headers.addParameter(name, value);
	}
	/**
	 * 设置一个HTTP头
	 * @param name
	 * @param value
	 */
	public void setHeader(String name,String value) {
		headers.setParameter(name, value);
	}
	
	public String[] getHeaderNames() {
		return headers.getParameterNames();
	}
	public String getHeader(String name) {
		return StringUtility.toString(headers.getParameter(name));
	}
	
	public String[] getHeaderValues(String name) {
		return (String[]) headers.getParameterValues(name);
	}
	
	public void clearHeader() {
		headers.removeAll();
	}

	
	public void setHttpStateListener(HttpStateListener listener) {
		stateListener=listener;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public File getFile() {
		return file;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	

	public HttpState getFinalState(){
		return httpState;
	}
	
	private void triggerState(int state) {
		if (stateListener!=null) {
			stateListener.httpStateChange(state, httpState, this,httpState.getStatusText());
		}
	}

	private String getString(int id){
		return BaseApplication.getInstance().getResources().getString(id);
	}
	
	public void download() throws Exception{
		if (!AppUtility.isNetworkAvailable()) {
			httpState.setStatusText(getString(R.string.c_msg_network_not_available));
			triggerState(HttpStateListener.STATE_ERROR);
			throw new Exception(httpState.getStatusText());
		}
		if (isAbort()) {
			return;
		}
		
		try {
			checkServer();
			if (httpState.getSkipBytes()>=httpState.getLength()) {
				triggerState(HttpStateListener.STATE_FINISH);
			}else {
				doDownload();
			}
		} catch (Exception e) {
			// TODO: handle exception
			httpState.setStatusText(e.getMessage());
			triggerState(HttpStateListener.STATE_ERROR);
			triggerState(HttpStateListener.STATE_FINISH);
			throw e;
		}
		
	}
	
	/**
	 * 这里只是检测用,看看实际文件有多大
	 */
	private void checkServer() throws Exception{
		// TODO Auto-generated method stub
		httpState.setStatusText("正在准备");
		triggerState(HttpStateListener.STATE_PREPARING);
		
		URL urlObject=new URL(getRealUrl());
		HttpURLConnection connection=(HttpURLConnection) urlObject.openConnection();
		setPlainHeader(connection);
		setHeaderToURL(connection);
		connection.setDoInput(true);
		
		if (isBreakpointContinuinglySupport()) {
			//这里要跳过的字节为0，因为在这里要获取文件的原始长度
			connection.setRequestProperty("Range", "bytes=0-");
		}
		
		if (connection.getResponseCode()==200 || connection.getResponseCode()==206) {
			extraFileName(connection);
			
			//文件的原始长度
			httpState.setLength(connection.getContentLength());
			httpState.setTotalTransferBytes(0);
			httpState.setTransferBytes(0);
			//如果不是断点续传,则不用检测,直接返回
			if (!isBreakpointContinuinglySupport()) {
				return;
			}
			//文件不存在
			if (!getFile().exists()) {
				httpState.setSkipBytes(0);//不跳过
				return;
			}
			//文件已经存在了
			String acceptRange=null;
			String lastModified=null;
			//String date=null;
			for (Entry<String,List<String>> entry : connection.getHeaderFields().entrySet()) {
                if ("Accept-Ranges".equalsIgnoreCase(entry.getKey())) {
					acceptRange=connection.getHeaderField(entry.getKey());
				}else if ("Last-Modified".equalsIgnoreCase(entry.getKey())) {
					lastModified=connection.getHeaderField(entry.getKey());
				}
            }
			//不支持断点续传
			if (StringUtility.isEmpty(acceptRange) || !"bytes".equalsIgnoreCase(acceptRange)) {
				setBreakpointContinuinglySupport(false);
				return;
			}
			
			SimpleDateFormat fmt=new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",Locale.ENGLISH);
			long serverLastModified=0L;
			try {
				serverLastModified=fmt.parse(lastModified).getTime();
			} catch (Exception e) {}
			//服务器最后修改时间比本地文件的最后修改时间新,就是服务器的文件更新了
			if (getFile().lastModified()>0 && serverLastModified>0 && getFile().lastModified()<serverLastModified) {
				httpState.setSkipBytes(0);//重新下载
				getFile().setLastModified(serverLastModified);
				return;
			}
			
			//否则
			httpState.setSkipBytes(getFile().length());
			httpState.setTotalTransferBytes(httpState.getSkipBytes());
			httpState.setTransferBytes(0);
			LogManager.i(getClass().getName(),"下载文件[%s], 跳过%d字节,保存路径:%s",httpTransfer.getFrom(), httpState.getSkipBytes(),getFile().getAbsolutePath());
		}else {
			throw new Exception(connection.getResponseMessage());
		}
		
		triggerState(HttpStateListener.STATE_READY);
		
		connection.disconnect();
	}

	private void extraFileName(HttpURLConnection connection) {
		// 如果没指定文件名,则根据服务器返回的信息解析文件名
		if (StringUtility.isEmpty(getFileName())) {
			Map<String, List<String>> map=connection.getHeaderFields();
			String name=null;
			if (map!=null) {
				for (Entry<String, List<String>> entry : map.entrySet()) {
					if ("Content-Disposition".equalsIgnoreCase(entry.getKey())) {
						name=connection.getHeaderField(entry.getKey());
						break;
					}
				}
			}
			if (StringUtility.isEmpty(name)) {
				name=connection.getURL().getPath();
				int i=Math.max(0, name.lastIndexOf("/")+1);
				name=name.substring(i);
			}else {
				String attr="filename=";
				int i=name.indexOf(attr);
				if (i>0) {
					name=name.substring(i+attr.length());
					name=name.replaceAll("^\"|\"$", "");
				}
			}
			setFileName(name);
		}
		if (getSaveDir().getAbsolutePath().endsWith(getFileName())) {
			saveDir=getSaveDir().getParentFile();
		}
		file=new File(getSaveDir(), getFileName());
	}
	
	/**
	 * 获取保存文件的目录
	 */
	public File getSaveDir() {
		return saveDir;
	}
	

	private void doDownload() throws Exception{
		// TODO Auto-generated method stub
		OutputStream out=null;
		InputStream inputStream=null;
		RandomAccessFile randomAccessFile=null;
		readyFile();
		
		
		try {
			URL urlObject=new URL(getRealUrl());
			HttpURLConnection connection=(HttpURLConnection) urlObject.openConnection();
			setPlainHeader(connection);
			setHeaderToURL(connection);
			
			if (isBreakpointContinuinglySupport()) {
				connection.setRequestProperty("Range", "bytes="+httpState.getSkipBytes()+"-");
			}
			
			//connection.setReadTimeout(READ_TIME);
			connection.setDoInput(true);
			
			LogManager.i("Range:"+connection.getRequestProperty("Range"));
			
			if (connection.getResponseCode()==200 || connection.getResponseCode()==206) {
				
				inputStream=connection.getInputStream();
				
//				setLength(connection.getContentLength()+getSkip());
				httpState.setStartTime(System.currentTimeMillis());
				httpState.setTotalTransferBytes(httpState.getSkipBytes());
				httpState.setTransferBytes(0);
				
				//开始
				httpState.setStatusText("开始下载");
				triggerState(HttpStateListener.STATE_START);
				
				byte[] buff=new byte[BUFFER_SIZE];
				int readed=-1;
				long bytesReaded=0;

				randomAccessFile=new RandomAccessFile(getFile(), "rwd");
				
				if (isBreakpointContinuinglySupport()) {
					//定位到开始位置
					randomAccessFile.seek(httpState.getSkipBytes());
				}
				
				LogManager.i(getClass().getName(),"开始读取网络字节,跳过%d字节,总长度:%d", httpState.getSkipBytes(),httpState.getLength());
				httpState.setStatusText("正在下载");
				while (!isAbort() && (readed=inputStream.read(buff))!=-1 ) {
					randomAccessFile.write(buff, 0, readed);
					bytesReaded+=readed;
					
					//下载字节,包括之前下载数据的字节,即整个文件已下载长度
					httpState.setTotalTransferBytes(bytesReaded+httpState.getSkipBytes());
					//这次下载实际传输的字节
					httpState.setTransferBytes(bytesReaded);
					triggerState(HttpStateListener.STATE_RUNNING);
				}
				LogManager.i(getClass().getName(),"读取网络字节结束,总共读取%d字节,总长度%d", bytesReaded,httpState.getLength());
			}else {
				httpState.setStatusText(connection.getResponseMessage());
				triggerState(HttpStateListener.STATE_ERROR);
				LogManager.e(getClass().getName(),null,"HTTP 错误:%s",connection.getResponseMessage());
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			httpState.setStatusText(e.getMessage());
			triggerState(HttpStateListener.STATE_ERROR);
			LogManager.trace(e);
			throw e;
		}finally{
			try {
				if (randomAccessFile!=null) {
					randomAccessFile.close();
				}
				if (out!=null) {
					out.close();
				}
				if (inputStream!=null) {
					inputStream.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
				LogManager.trace(e2);
			}
			httpState.setStatusText("下载结束");
			triggerState(HttpStateListener.STATE_FINISH);
		}
	}
	
	private void readyFile() {
		if (!isBreakpointContinuinglySupport()) {
			httpState.setSkipBytes(0);
			if (file.exists()) {
				file.delete();
			}
		}
	}


	private void setPlainHeader(HttpURLConnection urlConnection) throws Exception{
		urlConnection.setReadTimeout(READ_TIME);
		urlConnection.setRequestMethod(getMethod());
		
		urlConnection.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		urlConnection.setRequestProperty("Accept-Language", "zh_CN, en-us");
		urlConnection.setRequestProperty("Referer", getUrl());
		urlConnection.setRequestProperty("Charset", CHARSET);
		urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		urlConnection.setRequestProperty("User-Agent", "Android pengju httpdownloader Version/1.0");
		if (HttpRequest.METHOD_POST.equals(getMethod())) {
			urlConnection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
		}
		urlConnection.setRequestProperty("Connection", "Keep-Alive");
	}

	private void setHeaderToURL(HttpURLConnection urlConnection){
		for (Map.Entry<String, LinkedList<? extends Object>> entry : headers.getParameterEntrys()) {
			for (Object v : entry.getValue()) {
				urlConnection.setRequestProperty(entry.getKey(), StringUtility.toString(v));
			}
		}
	}
	
	private String getRealUrl() {
		// TODO Auto-generated method stub
		if (HttpRequest.METHOD_GET.equals(getMethod())) {
			return URLUtility.appendParameter(getUrl(), this);
		}
		return getUrl();
	}
	
	private String getUrl() {
		// TODO Auto-generated method stub
		return httpTransfer.getFrom();
	}
	
	/**
	 * 设置中断,一旦设置中断为true则停止传输,即使在传输中
	 * PENGJU
	 * 2012-12-3 上午10:20:11
	 * @param b
	 */
	public void setAbort(boolean b) {
		abort=b;
	}
	/**
	 * 判断是否中断
	 * PENGJU
	 * 2012-12-3 上午10:20:11
	 * @param b
	 */
	public boolean isAbort() {
		return abort;
	}
	/**
	 * 是否支持断点续传
	 * 断点续传会继续原来的传输
	 * 将读取的数据附加在原来的数据后面
	 * PENGJU
	 * 2012-12-3 上午10:21:54
	 * @return
	 */
	public boolean isBreakpointContinuinglySupport() {
		return breakpointContinuinglySupport;
	}
	/**
	 * 设置是否启用断点续传
	 * PENGJU
	 * 2012-12-3 上午10:23:08
	 * @param breakpointContinuinglySupport
	 */
	public void setBreakpointContinuinglySupport(
			boolean breakpointContinuinglySupport) {
		this.breakpointContinuinglySupport = breakpointContinuinglySupport;
	}

	public HttpTransfer getHttpTransfer() {
		return httpTransfer;
	}
	
	public int getRequestCode() {
		return requestCode;
	}
	public Object getExtraObject() {
		return extraObject;
	}
	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}
	public void setExtraObject(Object extraObject) {
		this.extraObject = extraObject;
	}
	
	
	/**
	 * HTTP上传/下载 状态监听器
	 * @author 陆振文[PENGJU]
	 * 2012-11-30 下午4:10:25
	 * email: pengju114@163.com
	 */
	public interface HttpStateListener {
		/** 正在准备..,比如正在检验断点续传信息 */
		int STATE_PREPARING	=0+Integer.MIN_VALUE;
		/** 准备就绪 */
		int STATE_READY     =1+Integer.MIN_VALUE;
		/** 开始传输前调用 */
		int STATE_START		=2+Integer.MIN_VALUE;
		/** 正在传输时调用,直到传输完毕,每读写一次就会调用一次,调用频率很高 */
		int STATE_RUNNING	=3+Integer.MIN_VALUE;
		/** 传输完毕(无论正常结束还是发生异常)时调用 */
		int STATE_FINISH	=4+Integer.MIN_VALUE;
		/** 发生异常时调用 */
		int STATE_ERROR		=5+Integer.MIN_VALUE;
		
		/**
		 * 在调用线程里面调用
		 * PENGJU
		 * 2012-11-30 下午4:51:39
		 * @param state 当前传输状态
		 * @param httpState 状态数据
		 * @param downloader 触发此事件的下载器
		 * @param statusText 描述状态的文本
		 */
		public void httpStateChange(int state,HttpState httpState,HttpDownloader downloader,String statusText);
	}
}
