package com.pj.core.http;

import java.io.Serializable;

import com.pj.core.utilities.StringUtility;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Http传输对象
 * @author 陆振文[PENGJU]
 * 2012-11-30 下午6:11:47
 * email: pengju114@163.com
 */
public class HttpTransfer implements Parcelable,Serializable{
	private static final long serialVersionUID = -1176011100180126027L;
	
	private String from;
	private String to;
	private String description;
	private Bitmap thumbnail;
	private String thumbnailUrl;
	
	/**
	 * 数据来源，如果是上传则是要上传的文件路径
	 * 如果是下载则是资源url
	 * PENGJU
	 * 2012-11-30 下午6:12:08
	 * @return
	 */
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	
	/**
	 * 传输目的地,如果是上传则是服务器url
	 * 如果是下载则是要保存的文件目录路径(只需制定目录而不包含文件名)
	 * PENGJU
	 * 2012-11-30 下午6:13:02
	 * @return
	 */
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	/**
	 * 描述信息
	 * PENGJU
	 * 2012-11-30 下午6:14:02
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * 资源缩略图
	 * PENGJU
	 * 2012-11-30 下午6:14:11
	 * @return
	 */
	public Bitmap getThumbnail() {
		return thumbnail;
	}
	/**
	 * 设置资源缩略图
	 * PENGJU
	 * 2012-11-30 下午6:14:11
	 * @return
	 */
	public void setThumbnail(Bitmap thumbnail) {
		this.thumbnail = thumbnail;
	}
	
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(StringUtility.ensure(getFrom()));
		dest.writeString(StringUtility.ensure(getTo()));
		dest.writeString(StringUtility.ensure(getDescription()));
		dest.writeString(StringUtility.ensure(getThumbnailUrl()));
		if (getThumbnail()!=null) {
			dest.writeParcelable(getThumbnail(), flags);
		}
	}
	
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public static final Parcelable.Creator<HttpTransfer> CREATOR=new Parcelable.Creator<HttpTransfer>() {

		@Override
		public HttpTransfer createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			HttpTransfer transfer=new HttpTransfer();
			transfer.setFrom(source.readString());
			transfer.setTo(source.readString());
			transfer.setDescription(source.readString());
			transfer.setThumbnailUrl(source.readString());
			transfer.setThumbnail((Bitmap) source.readParcelable(Bitmap.class.getClassLoader()));
			return transfer;
		}

		@Override
		public HttpTransfer[] newArray(int size) {
			// TODO Auto-generated method stub
			return new HttpTransfer[size];
		}
	};
}
