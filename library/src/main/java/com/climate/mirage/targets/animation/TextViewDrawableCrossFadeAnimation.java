package com.climate.mirage.targets.animation;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.IntDef;
import android.widget.TextView;

import com.climate.mirage.Mirage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TextViewDrawableCrossFadeAnimation implements MirageAnimation<TextView> {

	public static final int LEFT = 0;
	public static final int TOP = 1;
	public static final int RIGHT = 2;
	public static final int BOTTOM = 3;
    @IntDef({LEFT, TOP, RIGHT, BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Location {}

	private static final int DEFAULT_DURATION = 400;
	private int duration = DEFAULT_DURATION;
    private int location;
    private int boundsLeft = -1;
    private int boundsTop = -1;
    private int boundsRight = -1;
    private int boundsBottom = -1;

    public TextViewDrawableCrossFadeAnimation() {
        this(DEFAULT_DURATION);
    }

    public TextViewDrawableCrossFadeAnimation(int duration) {
        this.duration = duration;
    }

    public void setLocation(@Location int location) {
        this.location = location;
    }

    public void setBounds(int left, int top, int right, int bottom) {
        boundsLeft = left;
        boundsTop = top;
        boundsRight = right;
        boundsBottom = bottom;
    }

	@Override
	public boolean animate(TextView textView, Drawable targetDrawable, Mirage.Source source) {
		if (source == Mirage.Source.MEMORY) return false;
		Drawable currentDrawable = getCurrentDrawable(textView);
		if (currentDrawable == null) {
			currentDrawable = new ColorDrawable(Color.TRANSPARENT);
		}
        if (boundsLeft > -1) {
            targetDrawable.setBounds(boundsLeft, boundsTop, boundsRight, boundsBottom);
            currentDrawable.setBounds(boundsLeft, boundsTop, boundsRight, boundsBottom);
        }
		Drawable[] fades = new Drawable[2];
		fades[0] = currentDrawable;
		fades[1] = targetDrawable;
		TransitionDrawable transitionDrawable = new TransitionDrawable(fades);
		transitionDrawable.setCrossFadeEnabled(true);
        placeDrawable(textView, transitionDrawable);
		transitionDrawable.startTransition(duration);
		return true;
	}

    private Drawable getCurrentDrawable(TextView textView) {
        return textView.getCompoundDrawables()[location];
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