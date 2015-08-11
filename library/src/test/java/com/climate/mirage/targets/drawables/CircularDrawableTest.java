package com.climate.mirage.targets.drawables;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RoboManifestRunner;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class CircularDrawableTest {

    @Test
    public void testDrawable() {
        Bitmap bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888);
        CircularDrawable drawable = new CircularDrawable(bitmap);
        Canvas canvas = Mockito.mock(Canvas.class);
        drawable.draw(canvas);
        Mockito.verify(canvas, Mockito.times(1)).drawCircle(Mockito.anyFloat(),
                Mockito.anyFloat(), Mockito.anyFloat(), (Paint)Mockito.any());
    }

    @Test
    public void testCorrectOpacity() {
        Bitmap bitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
        CircularDrawable drawable = new CircularDrawable(bitmap);
        Assert.assertEquals(PixelFormat.TRANSLUCENT, drawable.getOpacity());
    }

    @Test
    public void testDiameter() {
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        CircularDrawable drawable = new CircularDrawable(bitmap);
        Assert.assertEquals(200.0f,  drawable.getDiameter());

        drawable = new CircularDrawable(bitmap, 50.0f);
        Assert.assertEquals(50.0f, drawable.getDiameter());
    }

    @Test
    public void testSetBorder() {
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        CircularDrawable drawable = new CircularDrawable(bitmap);
        drawable.setBorder(2.0f, Color.BLACK);

        Canvas canvas = Mockito.mock(Canvas.class);
        drawable.draw(canvas);
        // draws twice. once for border paint and once for the bitmap
        Mockito.verify(canvas, Mockito.times(2)).drawCircle(Mockito.anyFloat(),
                Mockito.anyFloat(), Mockito.anyFloat(), (Paint) Mockito.any());

        Assert.assertEquals(202, drawable.getIntrinsicWidth());
        Assert.assertEquals(202, drawable.getIntrinsicHeight());

        drawable.clearBorder();

        Assert.assertEquals(200, drawable.getIntrinsicWidth());
        Assert.assertEquals(200, drawable.getIntrinsicHeight());
    }

    @Test
    public void testAlphaDraw() {
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        CircularDrawable drawable = new CircularDrawable(bitmap);
        drawable.setBorder(2.0f, Color.BLACK);
        drawable.setAlpha(50);

        Canvas canvas = Mockito.mock(Canvas.class);
        drawable.draw(canvas);
        // draws twice. once for border paint and once for the bitmap
        Mockito.verify(canvas, Mockito.times(2)).drawCircle(Mockito.anyFloat(),
                Mockito.anyFloat(), Mockito.anyFloat(), (Paint) Mockito.any());


        drawable.clearBorder();
    }

    @Test
    public void testColorFilterDraw() {
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        CircularDrawable drawable = new CircularDrawable(bitmap);
        drawable.setColorFilter(new ColorFilter());

        Canvas canvas = Mockito.mock(Canvas.class);
        drawable.draw(canvas);
        // draws twice. once for border paint and once for the bitmap
        Mockito.verify(canvas, Mockito.times(1)).drawCircle(Mockito.anyFloat(),
                Mockito.anyFloat(), Mockito.anyFloat(), (Paint) Mockito.any());


        drawable.clearBorder();
    }

    @Test
    public void testBoundsChanged() {
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        CircularDrawable drawable = new CircularDrawable(bitmap);
        drawable.onBoundsChange(new Rect(0, 0, 500, 500));
    }

}