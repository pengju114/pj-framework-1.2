package com.pj.core.utilities;

public class ConvertUtility {
	public static Integer parseInt(String input){
		return parseInt(input,0);
	}
	
	public static Integer parseInt(String input,int defaultVal){
		Integer r=Integer.valueOf(defaultVal);
		if (!StringUtility.isEmpty(input)) {
			try {
				r=Integer.parseInt(input.trim());
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return r;
	}
	
	public static Float parseFloat(String input){
		return parseFloat(input,0F);
	}
	
	public static Float parseFloat(String input,float defaultVal){
		Float r=Float.valueOf(defaultVal);
		if (!StringUtility.isEmpty(input)) {
			try {
				r=Float.parseFloat(input.trim());
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return r;
	}
	
	public static Double parseDouble(String input){
		return parseDouble(input,0D);
	}
	
	public static Double parseDouble(String input,double defaultVal){
		Double r=Double.valueOf(defaultVal);
		if (!StringUtility.isEmpty(input)) {
			try {
				r=Double.parseDouble(input.trim());
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return r;
	}
	
	public static Long parseLong(String input){
		return parseLong(input,0L);
	}
	
	public static Long parseLong(String input,long defaultVal){
		Long r=Long.valueOf(defaultVal);
		if (!StringUtility.isEmpty(input)) {
			try {
				r=Long.parseLong(input.trim());
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return r;
	}
	
	public static boolean parseBoolean(String input){
		return Boolean.valueOf(input).booleanValue();
	}
}
