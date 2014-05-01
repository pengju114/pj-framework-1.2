package com.pj.core.transition;


import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;


public class AnimationFactory {
	
	//末尾8位(最后一个字节)表示平移动画
	public static final int ANIM_T_LEFT_IN  =0x00000081;//1000 0001
	public static final int ANIM_T_LEFT_OUT =0x000000FE;//1111 1110
	public static final int ANIM_T_RIGHT_IN =0x00000082;//1000 0010
	public static final int ANIM_T_RIGHT_OUT=0x000000FD;//1111 1101
	
	public static final int ANIM_T_TOP_IN	  =0x00000084;//1000 0100
	public static final int ANIM_T_TOP_OUT	  =0x000000FB;//1111 1011
	public static final int ANIM_T_BOTTOM_IN  =0x00000088;//1000 1000
	public static final int ANIM_T_BOTTOM_OUT =0x000000F7;//1111 0111
	
	//倒数第二个字节表示透明动画
	public static final int ANIM_F_FADE_OUT	=0x00008200;//1000 0010 0000 0000
	public static final int ANIM_F_FADE_IN	=0x0000FD00;//1111 1101 0000 0000
	
	public static final int RELATIVE_TO_SELF=0x0000FFFF;
	public static final int RELATIVE_TO_PARENT=Animation.RELATIVE_TO_PARENT;
	
	public static Animation getAnimation(int type){
		int bitMask=0xFF000000;
		int animCount=0;
		AnimationSet animationSet=new AnimationSet(false);
		Animation one=null;
		//每一个字节代表一种动画
		for (int i = 0; i < 4; i++) {
			Animation tmp=getSimpleAnimation(type & bitMask);
			if (tmp!=null) {
				animCount++;
				one=tmp;
				animationSet.addAnimation(tmp);
			}
			bitMask=bitMask>>>8;
		}
		return animCount>1?animationSet:one;
	}
	
	private static Animation getSimpleAnimation(int type){
		switch (type) {
		case ANIM_T_LEFT_IN:
			return getLeftInAnimation();
		case ANIM_T_LEFT_OUT:
			return getLeftOutAnimation();
		case ANIM_T_RIGHT_OUT:
			return getRightOutAnimation();
		case ANIM_T_RIGHT_IN:
			return getRightInAnimation();
		case ANIM_T_TOP_IN:
			return getTopInAnimation();
		case ANIM_T_TOP_OUT:
			return getTopOutAnimation();
		case ANIM_T_BOTTOM_IN:
			return getBottomInAnimation();
		case ANIM_T_BOTTOM_OUT:
			return getBottomOutAnimation();
		case ANIM_F_FADE_IN:
			return getFadeInAnimation();
		case ANIM_F_FADE_OUT:
			return getFadeOutAnimation();
		default:
			return null;
		}
	}
	

	private static Animation getLeftInAnimation() {
		int type=Animation.RELATIVE_TO_PARENT;
		TranslateAnimation animation=new TranslateAnimation(type, -1, type, 0, type, 0, type, 0);
		return animation;
	}
	private static Animation getLeftOutAnimation() {
		int type=Animation.RELATIVE_TO_PARENT;
		TranslateAnimation animation=new TranslateAnimation(type, 0, type, -1, type, 0, type, 0);
		return animation;
	}
	private static Animation getRightInAnimation() {
		int type=Animation.RELATIVE_TO_PARENT;
		TranslateAnimation animation=new TranslateAnimation(type, 1, type, 0, type, 0, type, 0);
		return animation;
	}
	private static Animation getRightOutAnimation() {
		int type=Animation.RELATIVE_TO_PARENT;
		TranslateAnimation animation=new TranslateAnimation(type, 0, type, 1, type, 0, type, 0);
		return animation;
	}
	
	private static Animation getTopInAnimation() {
		// TODO Auto-generated method stub
		int type=Animation.RELATIVE_TO_PARENT;
		TranslateAnimation animation=new TranslateAnimation(type, 0, type, 0, type, -1, type, 0);
		return animation;
	}

	private static Animation getTopOutAnimation() {
		// TODO Auto-generated method stub
		int type=Animation.RELATIVE_TO_PARENT;
		TranslateAnimation animation=new TranslateAnimation(type, 0, type, 0, type, 0, type, -1);
		return animation;
	}

	private static Animation getBottomInAnimation() {
		// TODO Auto-generated method stub
		int type=Animation.RELATIVE_TO_PARENT;
		TranslateAnimation animation=new TranslateAnimation(type, 0, type, 0, type, 1, type, 0);
		return animation;
	}

	private static Animation getBottomOutAnimation() {
		// TODO Auto-generated method stub
		int type=Animation.RELATIVE_TO_PARENT;
		TranslateAnimation animation=new TranslateAnimation(type, 0, type, 0, type, 0, type, 1);
		return animation;
	}

	private static Animation getFadeInAnimation() {
		// TODO Auto-generated method stub
		AlphaAnimation animation=new AlphaAnimation(0.0F, 1.0F);
		return animation;
	}

	private static Animation getFadeOutAnimation() {
		// TODO Auto-generated method stub
		AlphaAnimation animation=new AlphaAnimation(1.0F, 0.0F);
		return animation;
	}
	
	public static int getToggleAnimation(int animationType){
		int toggle=0x00000000;
		int bitMask=0xFF000000;
		int tmp;
		for (int i = 0; i < 4; i++) {
			int clear=(3-i)*8;
			//清空多余的字节
			tmp=animationType & bitMask;
			tmp=tmp>>>clear;

			if (tmp!=0) {
				tmp &= 0x7F;
				tmp=~tmp;
				tmp &= 0xFF;
			}
			
			toggle=toggle << 8;
			toggle |= tmp;
			bitMask>>>=8;
		}
		return toggle;
	}
	
	public static int getMatchAnimation(int animationType){
		int type=0;
		int bitMask=0xFF000000;
		for (int i = 0; i < 4; i++) {
			type |= getSimpleMatchAnimation(animationType & bitMask);
			bitMask=bitMask>>>8;
		}
		return type;
	}
	
	private static int getSimpleMatchAnimation(int animationType) {
		int type=0;
		switch (animationType) {
		case ANIM_T_LEFT_IN:
			type=ANIM_T_RIGHT_OUT;
			break;
		case ANIM_T_LEFT_OUT:
			type=ANIM_T_RIGHT_IN;
			break;
		case ANIM_T_RIGHT_IN:
			type=ANIM_T_LEFT_OUT;
			break;
		case ANIM_T_RIGHT_OUT:
			type=ANIM_T_LEFT_IN;
			break;
			
		case ANIM_T_TOP_IN:
			type=ANIM_T_BOTTOM_OUT;
			break;
			
		case ANIM_T_TOP_OUT:
			type=ANIM_T_BOTTOM_IN;
			break;
		case ANIM_T_BOTTOM_IN:
			type=ANIM_T_TOP_OUT;
			break;
		case ANIM_T_BOTTOM_OUT:
			type=ANIM_T_TOP_IN;
			break;
			
		case ANIM_F_FADE_IN:
			type=ANIM_F_FADE_OUT;
			break;
		case ANIM_F_FADE_OUT:
			type=ANIM_F_FADE_IN;
			break;

		default:
			break;
		}
		return type;
	}
}
