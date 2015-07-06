package com.climate.mirage.targets.animation;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.climate.mirage.Mirage;

public interface MirageAnimation<V extends View> {


	// TODO: an animation can either take a drawable or it can take a
	// source like a bitmap. View can't display a bitmap so they display drawables.
	// is there any benefit at all to allowing the animation to create the drawable?
	// The benefit of passing in the drawable is to allow different types of drawables
	// to be uses (such as CircularDrawable) and still animate.
	// This could be accomplished as well if we pass in the drawable factory
	// to the animation.

	/**
	 *
	 * @param view the view the drawable will go into
	 * @param drawable the drawable that we got from the load
	 * @param source the location the resource from the load came from useful so we dont
	 *               show animation if it came from the memory
	 * @return true is the animation set the drawable on the view. false if it didn't
	 */
	public boolean animate(V view, Drawable drawable, Mirage.Source source);

}