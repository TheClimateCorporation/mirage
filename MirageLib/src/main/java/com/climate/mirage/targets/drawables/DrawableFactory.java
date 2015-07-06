package com.climate.mirage.targets.drawables;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface DrawableFactory {

	public Drawable createDrawable(Context context, Bitmap bitmap);

}