package com.climate.mirage.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowBitmapFactory;

import java.io.InputStream;

@Implements(BitmapFactory.class)
public class OomBitmapFactoryShadow extends ShadowBitmapFactory {

    @Implementation
    public static Bitmap decodeStream(InputStream is, Rect outPadding, BitmapFactory.Options opts) {
        throw new OutOfMemoryError("Shadow OutOfMemoryException thrown");
    }

    @Implementation
    public static Bitmap decodeFile(String pathName, BitmapFactory.Options opts) {
        throw new OutOfMemoryError("Shadow OutOfMemoryException thrown");
    }

}