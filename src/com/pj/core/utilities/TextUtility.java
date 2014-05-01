package com.pj.core.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

public class TextUtility {
	private static final Pattern SIMPLE_EL_PATTERN=Pattern.compile("\\$\\{((\\d)+)\\}");
	
	public static String calculateTime(String passTime,String addTime){
		long dis=ConvertUtility.parseLong(passTime);
		dis=Math.max(dis, 0);
		
		long minuteLong=60*1000;
		long hourLong=60*minuteLong;
		long dayLong=24*hourLong;
		long weekLong=dayLong*7;
		long monthLong=dayLong*30;//每个月按平均30天算
		long yearLong=monthLong*12;//一年按12个月算
		
		long[] periods=new long[]{
				Integer.MIN_VALUE,
				minuteLong,
				hourLong,
				dayLong,
				weekLong,
				monthLong,
				yearLong,
				Long.MAX_VALUE
		};
		String[] periodDesStrings=new String[]{
				"刚刚",
				"分钟前",
				"小时前",
				"天前",
				"周前",
				"个月前",
				"年前",
				""
		};
		
		StringBuilder builder=new StringBuilder();
		for (int i = 1/*从分钟开始算*/; i < periods.length; i++) {
			if (dis<periods[i]) {
				int prevIndex=i-1;
				long temp=periods[prevIndex];
				long v=dis/temp;
				if (v>0) {
					builder.append(v);
				}
				builder.append(periodDesStrings[prevIndex]);
				return builder.toString();
			}
		}

		return addTime;
	}
	
	public static SpannableStringBuilder foreGroundSpan(String[] texts,int[] colors){
		SpannableStringBuilder spannableStringBuilder=new SpannableStringBuilder(ArrayUtility.join(texts, ""));
		int len=0;
		for (int i = 0; i < colors.length; i++) {
			ForegroundColorSpan colorSpan=new ForegroundColorSpan(colors[i]);
			spannableStringBuilder.setSpan(colorSpan, len, len+texts[i].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			len+=texts[i].length();
		}
		return spannableStringBuilder;
	}
	
	public static final String evalEL(String el,Object... vals){
        StringBuilder builder=new StringBuilder(el);
        
        Matcher matcher=SIMPLE_EL_PATTERN.matcher(el);
        while (matcher.find()) {            
            String index=matcher.group(1).trim();
            int pos=ConvertUtility.parseInt(index);
            Object v=null;
            if (pos>-1 && pos<vals.length) {
				v=vals[pos];
			}
            builder.replace(matcher.start(), matcher.end(), StringUtility.toString(v));
            matcher.reset(builder.toString());
        }
        
        return builder.toString();
    }
}
