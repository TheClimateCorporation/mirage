package com.climate.mirage.targets;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.climate.mirage.Mirage;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.targets.animation.ImageViewCrossFadeAnimation;

/**
 * The standard {@link com.climate.mirage.targets.Target} for loading images
 * into an {@link android.widget.ImageView}
 */
public class ImageViewTarget extends ViewTarget<ImageView> {

	public ImageViewTarget(ImageView imageView) {
		super(imageView);
	}

	public ImageViewTarget(MirageRequest request, ImageView imageView) {
		super(request, imageView);
	}

	@Override
	protected void onPreparingLoad(ImageView view) {
		if (getPlaceHolderResourceId() != 0) {
			view.setImageResource(getPlaceHolderResourceId());
		} else if (getPlaceHolderDrawable() != null) {
			view.setImageDrawable(getPlaceHolderDrawable());
		} else {
			view.setImageDrawable(null);
		}
	}

	@Override
	protected void onResult(ImageView view, Drawable drawable, Mirage.Source source, MirageRequest request) {
		if (animation() != null) {
			if (!animation().animate(view, drawable, source)) {
				view.setImageDrawable(drawable);
			}
		} else {
			view.setImageDrawable(drawable);
		}
	}

	@Override
	protected void onError(ImageView view, Exception e, Mirage.Source source, MirageRequest request) {
		if (animation() != null) {
			Drawable drawable;
			if (getErrorResourceId() > 0) {
				drawable = view.getContext().getResources().getDrawable(getErrorResourceId());
			} else {
				drawable = getErrorDrawable();
			}
			if (drawable != null) {
				if (!animation().animate(view, drawable, source)) {
					view.setImageDrawable(drawable);
				}
			} else {
				view.setImageDrawable(null);
			}
		} else {
			if (getErrorResourceId() != 0) {
				view.setImageResource(getErrorResourceId());
			} else if (getErrorDrawable() != null) {
				view.setImageDrawable(getErrorDrawable());
			} else {
				view.setImageDrawable(null);
			}
		}
	}

	@Override
	public ViewTarget<ImageView> fade() {
		return animation(new ImageViewCrossFadeAnimation());
	}
}