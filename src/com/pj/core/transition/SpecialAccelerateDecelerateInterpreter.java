package com.pj.core.transition;

import android.view.animation.Interpolator;

public class SpecialAccelerateDecelerateInterpreter implements Interpolator {

	public SpecialAccelerateDecelerateInterpreter() {
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public float getInterpolation(float factor) {
		// TODO Auto-generated method stub
		return (float)(Math.cos((factor + 1) * Math.PI) / 2.0f) + 0.5f;
	}
}
