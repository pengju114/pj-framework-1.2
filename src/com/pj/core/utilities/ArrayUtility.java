/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pj.core.utilities;

import java.util.Collection;

/**
 *
 * @author 陆振文[PENGJU]
 * date:2012-7-15 13:40:54
 */
public class ArrayUtility {
    public static String join(Object[] array,String separator){
        StringBuilder sb=new StringBuilder();
        if (array!=null) {
            for (Object object : array) {
                sb.append(object).append(separator);
            }
            
            if (array.length>0) {
                sb.delete(sb.length()-separator.length(), sb.length());
            }
        }
        return  sb.toString();
    }
    
    public static String join(Object val,int count ,String separator){
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i <count; i++) {
            sb.append(val).append(separator);
        }
        if (count>0) {
            sb.delete(sb.length()-separator.length(), sb.length());
        }
        return  sb.toString();
    }
    public static String join(Collection<Object> collection,String separator) {
    	StringBuilder sb=new StringBuilder();
    	if (collection!=null) {
			for (Object object : collection) {
				sb.append(object).append(separator);
			}
			if (collection.size()>0) {
				sb.delete(sb.length()-separator.length(), sb.length());
			}
		}
    	return sb.toString();
	}
    
    
    public static boolean contains(Object[] datas,Object target) {
		for (Object object : datas) {
			if (object.equals(target)) {
				return true;
			}
		}
		return false;
	}
    
    public static String join(String separator,Object... vals) {
		return join(vals, separator);
	}
    
}
