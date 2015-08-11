package com.climate.mirage.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowBitmapFactory;

@Implements(BitmapFactory.class)
public class ReturnsFileBitmapFactoryShadow extends ShadowBitmapFactory {

    @Implementation
    public static Bitmap decodeFile(String pathName, BitmapFactory.Options opts) {
        return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    }

}