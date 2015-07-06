package com.climate.mirage.targets.animation;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

import com.climate.mirage.Mirage;

public class ImageViewCrossFadeAnimation implements MirageAnimation<ImageView> {

	private static final int DEFAULT_DURATION = 400;
	private int duration;

	public ImageViewCrossFadeAnimation() {
		this(DEFAULT_DURATION);
	}

	public ImageViewCrossFadeAnimation(int duration) {
		this.duration = duration;
	}

	@Override
	public boolean animate(ImageView imageView, Drawable targetDrawable, Mirage.Source source) {
		if (source == Mirage.Source.MEMORY) return false;
		Drawable currentDrawable = imageView.getDrawable();
		if (currentDrawable == null) {
			currentDrawable = new ColorDrawable(android.R.color.transparent);
		}
		Drawable[] fades = new Drawable[2];
		fades[0] = currentDrawable;
		fades[1] = targetDrawable;
		TransitionDrawable transitionDrawable = new TransitionDrawable(fades);
		transitionDrawable.setCrossFadeEnabled(true);
		imageView.setImageDrawable(transitionDrawable);
		transitionDrawable.startTransition(duration);
		return true;
	}
}