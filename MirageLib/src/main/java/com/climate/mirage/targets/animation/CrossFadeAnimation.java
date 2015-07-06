package com.climate.mirage.targets.animation;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.view.View;

import com.climate.mirage.Mirage;

public class CrossFadeAnimation implements MirageAnimation<View> {

	private static final int DEFAULT_DURATION = 400;
	private int duration;

	public  CrossFadeAnimation() {
		this(DEFAULT_DURATION);
	}

	public CrossFadeAnimation(int duration) {
		this.duration = duration;
	}

	@Override
	public boolean animate(View view, Drawable targetDrawable, Mirage.Source source) {
		if (source == Mirage.Source.MEMORY) return false;
		Drawable currentDrawable = view.getBackground();
		if (currentDrawable == null) {
			currentDrawable = new ColorDrawable(android.R.color.transparent);
		}
		Drawable[] fades = new Drawable[2];
		fades[0] = currentDrawable;
		fades[1] = targetDrawable;
		TransitionDrawable transitionDrawable = new TransitionDrawable(fades);
		transitionDrawable.setCrossFadeEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackground(transitionDrawable);
		} else {
			view.setBackgroundDrawable(transitionDrawable);
		}
		transitionDrawable.startTransition(duration);
		return true;
	}
}