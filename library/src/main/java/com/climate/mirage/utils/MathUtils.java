package com.climate.mirage.utils;

import android.graphics.BitmapFactory;

public final class MathUtils {

    private MathUtils() {}

    public static int upperPowerof2(int v) {
        v--;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v++;
        return v;
    }

    public static int lowerPowerOf2(int v) {
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        return v - (v >> 1);
    }

    /**
     * Determines the BitmapOptions sample size
     * @param width the size of the image as determined from Bitmap.Options
     * @param height the size of the image as determined from Bitmap.Options
     * @param targetSize the max desired size
     * @param undershootSample
     * @return
     */
    public static int determineSampleSize(int width, int height, int targetSize, boolean undershootSample) {
        int dimen = Math.max(width, height);
        float div = dimen / (float)targetSize;
        if (div < 1) return 1;
        if (undershootSample) {
            int sampleSize = MathUtils.upperPowerof2((int)div);
            return sampleSize;
        } else {
            int sampleSize = MathUtils.lowerPowerOf2((int)div);
            return sampleSize;
        }
    }

}