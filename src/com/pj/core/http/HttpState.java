package com.pj.core.http;

import java.io.Serializable;

/**
 * Http传输状态
 * @author 陆振文[PENGJU]
 * 2012-11-30 下午4:18:04
 * email: pengju114@163.com
 */
public class HttpState implements Serializable{
	private static final long serialVersionUID = -7119094550895901497L;
	
	private long startTime;			//开始时间
	private long totalTransferBytes;//已经传输的字节(包括此次传输之前已经传输的字节数,在不是断点续传时和transferBytes相同)
	private long transferBytes;		//此次传输从开始到现在传输的字节
	private long length;			//数据长度，字节
	private long skipBytes;			//跳过的字节数,非断点续传事忽略
	private String statusText;		//状态描述
	
	/**
	 * 获取已经传输的字节,包括此次传输之前已经传输的字节数
	 * 在不是断点续传时和transferBytes相同
	 * PENGJU
	 * 2012-11-30 下午4:31:47
	 * @return 已经传输的字节
	 */
	public long getTotalTransferBytes() {
		return totalTransferBytes;
	}
	/**
	 * 此次传输从开始到现在传输的字节数
	 * PENGJU
	 * 2012-11-30 下午4:32:47
	 * @return
	 */
	public long getTransferBytes() {
		return transferBytes;
	}
	
	/**
	 * 传输的数据总长度
	 * 整个文件的进度,包括之前传输的（断点续传）
	 * PENGJU
	 * 2012-11-30 下午4:33:15
	 * @return
	 */
	public long getLength() {
		return length;
	}
	/**
	 * 传输跳过的字节数,非断点续传为0
	 * PENGJU
	 * 2012-11-30 下午4:33:15
	 * @return
	 */
	public long getSkipBytes() {
		return skipBytes;
	}
	
	/**
	 * 已经传输的字节(包括此次传输之前已经传输的字节数,在不是断点续传时和transferBytes相同)
	 * PENGJU
	 * 2012-12-3 下午12:41:23
	 * @param totalTransferBytes
	 */
	public void setTotalTransferBytes(long totalTransferBytes) {
		this.totalTransferBytes = totalTransferBytes;
	}
	/**
	 * 此次传输从开始到现在传输的字节
	 * PENGJU
	 * 2012-12-3 下午12:41:44
	 * @param transferBytes
	 */
	public void setTransferBytes(long transferBytes) {
		this.transferBytes = transferBytes;
	}
	/**
	 * 数据长度，字节
	 * PENGJU
	 * 2012-12-3 下午12:42:18
	 * @param length
	 */
	public void setLength(long length) {
		this.length = length;
	}
	public void setSkipBytes(long skipBytes) {
		this.skipBytes = skipBytes;
	}
	/**
	 * 传输开始时间
	 * PENGJU
	 * 2012-12-3 下午12:42:34
	 * @return 传输开始时间
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * 传输开始时间
	 * PENGJU
	 * 2012-12-3 下午12:42:34
	 * @return
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * 状态描述,如错误描述
	 * PENGJU
	 * 2012-12-3 下午12:43:08
	 * @return 状态描述
	 */
	public String getStatusText() {
		return statusText;
	}

	/**
	 * 状态描述,如错误描述
	 * PENGJU
	 * 2012-12-3 下午12:43:08
	 * @return
	 */
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}
	
}
