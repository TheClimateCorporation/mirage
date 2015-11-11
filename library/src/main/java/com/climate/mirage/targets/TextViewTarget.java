package com.climate.mirage.targets;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.widget.TextView;

import com.climate.mirage.Mirage;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.targets.animation.TextViewDrawableCrossFadeAnimation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The standard {@link Target} for loading images
 * into an {@link TextView} drawable
 */
public class TextViewTarget extends ViewTarget<TextView> {

    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;

    private int location = LEFT;
    private int boundsLeft = -1;
    private int boundsTop = -1;
    private int boundsRight = -1;
    private int boundsBottom = -1;

    @IntDef({LEFT, TOP, RIGHT, BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Location {}

	public TextViewTarget(TextView textView) {
		super(textView);
	}

	public TextViewTarget(MirageRequest request, TextView textView) {
		super(request, textView);
	}

    /**
     * Controls where the drawable will be placed. The choices are left, top, right, and bottom
     *
     * @param location Defines the location of the drawable
     * @return reference to daisy chain
     */
    public TextViewTarget location(@Location int location) {
        this.location = location;
        return this;
    }

    /**
     * Controls the size of the drawables. This is one size fits all, meaning the loading
     * drawable, error drawable, and loaded drawable and receive the same size. This method
     * is used the same as {@link Drawable#setBounds(int, int, int, int)}
     *
     * @return reference to daisy chain
     */
    public TextViewTarget bounds(int left, int top, int right, int bottom) {
        boundsLeft = left;
        boundsTop = top;
        boundsRight = right;
        boundsBottom = bottom;
        return this;
    }

	@Override
	protected void onPreparingLoad(TextView view) {
		if (getPlaceHolderResourceId() != 0) {
            placeDrawable(view, getPlaceHolderResourceId());
		} else if (getPlaceHolderDrawable() != null) {
            placeDrawable(view, getPlaceHolderDrawable());
		} else {
            view.setCompoundDrawables(null, null, null, null);
		}
	}

	@Override
	protected void onResult(TextView view, Drawable drawable, Mirage.Source source, MirageRequest request) {
		if (animation() != null) {
			if (!animation().animate(view, drawable, source)) {
                placeDrawable(view, drawable);
			}
		} else {
            placeDrawable(view, drawable);
		}
	}

	@Override
	protected void onError(TextView view, Exception e, Mirage.Source source, MirageRequest request) {
		if (animation() != null) {
			Drawable drawable;
			if (getErrorResourceId() > 0) {
				drawable = view.getContext().getResources().getDrawable(getErrorResourceId());
			} else {
				drawable = getErrorDrawable();
			}
			if (drawable != null) {
				if (!animation().animate(view, drawable, source)) {
                    placeDrawable(view, drawable);
				}
			} else {
                placeDrawable(view, null);
			}
		} else {
			if (getErrorResourceId() != 0) {
                placeDrawable(view, getErrorResourceId());
			} else if (getErrorDrawable() != null) {
                placeDrawable(view, getErrorDrawable());
			} else {
                placeDrawable(view, null);
			}
		}
	}

	@Override
	public ViewTarget<TextView> fade() {
        int loc = TextViewDrawableCrossFadeAnimation.LEFT;
        switch (location) {
            case LEFT: loc = TextViewDrawableCrossFadeAnimation.LEFT; break;
            case TOP: loc = TextViewDrawableCrossFadeAnimation.TOP; break;
            case RIGHT: loc = TextViewDrawableCrossFadeAnimation.RIGHT; break;
            case BOTTOM: loc = TextViewDrawableCrossFadeAnimation.BOTTOM; break;
        }
        TextViewDrawableCrossFadeAnimation ani = new TextViewDrawableCrossFadeAnimation();
        ani.setLocation(loc);
        if (boundsLeft > -1) {
            ani.setBounds(boundsLeft, boundsTop, boundsRight, boundsBottom);
        }
		return animation(ani);
	}

    private void placeDrawable(TextView view, @DrawableRes int resId) {
        Drawable drawable = view.getResources().getDrawable(resId);
        placeDrawable(view, drawable);
    }

    private void placeDrawable(TextView view, Drawable drawable) {
        Drawable[] drawables = view.getCompoundDrawables();
        drawables[location] = drawable;
        if (boundsLeft > -1) {
            drawable.setBounds(boundsLeft, boundsTop, boundsRight, boundsBottom);
            view.setCompoundDrawables(
                    drawables[0], drawables[1], drawables[2], drawables[3]);
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(
                    drawables[0], drawables[1], drawables[2], drawables[3]);
        }
    }
}