package com.pj.core.utilities;

public class StringUtility {
	public static final String EMPTY_STRING="";
	public static boolean isEmpty(String str) {
		return (str==null || str.trim().length()<1);
	}
	
	public static String ensure(String str){
		return str==null?EMPTY_STRING:str;
	}
	
	public static String trim(String string) {
		return ensure(string).trim();
	}
	
	public static String toString(Object data) {
		return data==null?EMPTY_STRING:String.valueOf(data);
	}
	
	public static boolean equals(String str1,String str2){
		if (isEmpty(str1) && isEmpty(str2)) {
			return true;
		}
		return str1.equals(str2);
	}
}
