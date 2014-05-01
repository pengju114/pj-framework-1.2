package com.pj.core.utilities;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加解密
 * 
 * @author 陆振文[PENGJU]
 * 
 */
public class SecurityUtility {
	private static final String CHARSET_STRING="UTF-8";

	/**
	 * MD5加密，字符编码为UTF-8
	 * @param target
	 * @param optionKey
	 * @return
	 */
	public final static String MD5Encrypt(String target,String optionKey)  {
		if (target == null){
			target = "";
		}
		if (optionKey!=null) {
			// 将目标字符串附加上密钥后进行编码
			target=optionKey+target;
		}
		try {
			byte[] tar = target.getBytes(CHARSET_STRING);
			return MD5Encrypt(tar);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String MD5Encrypt(byte[] bytes){
		// 十六进制字符数组
		char[] hex = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8',
				'9', 'a', 'b', 'c', 'd', 'e', 'f' };
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");// MD5算法
			md.update(bytes);// 更新数据
			byte[] enc = md.digest();// 加密并获取结果

			char[] str = new char[enc.length * 2];// 用来生成字符串的字符数组
			int k = 0;
			// 根据MD5加密后的数据取字符
			for (int i = 0; i < enc.length; i++) {
				byte b = enc[i];
				str[k++] = hex[b >>> 4 & 0xf];
				str[k++] = hex[b & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static final byte[] iv = { 1, 2, 3, 4, 5, 6, 7, 8 };

	/**
	 * DES编码，字符编码为UTF-8
	 * @param data
	 * @param key
	 * @param optionIV
	 * @return BASE64编码后的字符串
	 */
	public static String DESBASE64Encrypt(String data,String key,byte[] optionIV){
		try {
			return DESBASE64Encrypt(data, key.getBytes(CHARSET_STRING), optionIV);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * DES解码，字符编码为UTF-8
	 * @param data
	 * @param key
	 * @param optionIV
	 * @return BASE64解码后的字符串
	 */
	public static String DESBASE64Decrypt(String data,String key,byte[] optionIV){
		try {
			return DESBASE64Decrypt(data, key.getBytes(CHARSET_STRING), optionIV);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String DESBASE64Encrypt(String data,byte[] key,byte[] optionIV){
		try {
			byte[] dataBytes=data.getBytes(CHARSET_STRING);
			return DESBASE64Encrypt(dataBytes, key, optionIV);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static String DESBASE64Decrypt(String data,byte[] key,byte[] optionIV){
		try {
			byte[] dataBytes=data.getBytes(CHARSET_STRING);
			return DESBASE64Decrypt(dataBytes, key, optionIV);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String DESBASE64Encrypt(byte[] data,byte[] key,byte[] optionIV){
		try {
			byte[] encBytes=doCrypt(Cipher.ENCRYPT_MODE, data, key, optionIV);
			return Base64.encodeToString(encBytes, Base64.NO_WRAP);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static String DESBASE64Decrypt(byte[] data,byte[] key,byte[] optionIV){
		try {
			byte[] decBytes=Base64.decode(data, Base64.NO_WRAP);
			decBytes = doCrypt(Cipher.DECRYPT_MODE, decBytes, key, optionIV);
			return new String(decBytes, CHARSET_STRING);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] DESEncrypt(byte[] data,byte[] key,byte[] optionIV){
		try {
			return doCrypt(Cipher.ENCRYPT_MODE, data, key, optionIV);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static byte[] DESDecrypt(byte[] data,byte[] key,byte[] optionIV){
		try {
			return doCrypt(Cipher.DECRYPT_MODE, data, key, optionIV);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static byte[] doCrypt(int mode,byte[] data,byte[] key,byte[] optionIV) throws Exception{
		if (optionIV==null) {
			optionIV=iv;
		}
		if (data==null) {
			return null;
		}
		
		IvParameterSpec zeroIv = new IvParameterSpec(optionIV);
		SecretKeySpec keySpec = new SecretKeySpec(key, "DES");
		Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
		cipher.init(mode, keySpec, zeroIv);
		byte cryptedData[] = cipher.doFinal(data);
		return cryptedData;
	}
}
