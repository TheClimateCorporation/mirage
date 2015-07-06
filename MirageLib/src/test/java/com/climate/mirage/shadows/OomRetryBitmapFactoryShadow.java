package com.climate.mirage.shadows;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowBitmapFactory;

import java.io.InputStream;

@Implements(BitmapFactory.class)
public class OomRetryBitmapFactoryShadow extends ShadowBitmapFactory {

    private static int streamTries = 0;
    private static int fileTries = 0;

    @Implementation
    public static Bitmap decodeStream(InputStream is, Rect outPadding, BitmapFactory.Options opts) {
        if (streamTries == 0) {
            streamTries = 1;
            throw new OutOfMemoryError("Shadow OutOfMemoryError thrown");
        } else {
            streamTries = 0;
            return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        }
    }

    @Implementation
    public static Bitmap decodeFile(String pathName, BitmapFactory.Options opts) {
        if (fileTries == 0) {
            fileTries = 1;
            throw new OutOfMemoryError("Shadow OutOfMemoryError thrown");
        } else {
            fileTries = 0;
            return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        }
    }

}