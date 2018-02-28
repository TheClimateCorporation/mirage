package com.climate.mirage.processors;

import android.graphics.Bitmap;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RobolectricTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

public class ResizeProcessorTest extends RobolectricTest {

    @Test
    public void testId() {
        ResizeProcessor processor = new ResizeProcessor();
        processor.setDimensions(200, 300, ResizeProcessor.STRATEGY_SCALE_FREE);
        Assert.assertEquals("resize200x300_"+ResizeProcessor.STRATEGY_SCALE_FREE, processor.getId());
    }

    @Test
    public void testGetDimens() {
        ResizeProcessor processor = new ResizeProcessor();
        processor.setDimensions(200, 300, ResizeProcessor.STRATEGY_SCALE_FREE);
        Assert.assertEquals(200, processor.getWidth());
        Assert.assertEquals(300, processor.getHeight());
    }

    @Test
    public void testResizeFreeDown() {
        Bitmap bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        ResizeProcessor processor = new ResizeProcessor();
        processor.setDimensions(200, 300, ResizeProcessor.STRATEGY_SCALE_FREE);
        Bitmap result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(200, result.getWidth());
        Assert.assertEquals(300, result.getHeight());
    }

    @Test
    public void testResizeFreeUp() {
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        ResizeProcessor processor = new ResizeProcessor();
        processor.setDimensions(200, 300, ResizeProcessor.STRATEGY_SCALE_FREE);
        Bitmap result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(200, result.getWidth());
        Assert.assertEquals(300, result.getHeight());
    }

    @Test
    public void testBitmapRecycles() {
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        ResizeProcessor processor = new ResizeProcessor();
        processor.setDimensions(200, 300, ResizeProcessor.STRATEGY_SCALE_FREE);
        Bitmap result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertTrue(bitmap.isRecycled());
        Assert.assertFalse(result.isRecycled());
    }

    @Test
    public void testResizeDownOnly() {
        Bitmap bitmap = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        ResizeProcessor processor = new ResizeProcessor();
        processor.setDimensions(200, 300, ResizeProcessor.STRATEGY_SCALE_DOWN_ONLY);
        Bitmap result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(200, result.getWidth());
        Assert.assertEquals(300, result.getHeight());


        bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        result = processor.process(bitmap);
        Assert.assertSame(bitmap, result);
        Assert.assertEquals(100, result.getWidth());
        Assert.assertEquals(100, result.getHeight());


        bitmap = Bitmap.createBitmap(40, 120, Bitmap.Config.ARGB_8888);
        processor.setDimensions(50, 50, ResizeProcessor.STRATEGY_SCALE_DOWN_ONLY);
        result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(50, result.getWidth());
        Assert.assertEquals(50, result.getHeight());
    }

    @Test
    public void testResizeRatioMaintained() {
        Bitmap bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888);
        ResizeProcessor processor = new ResizeProcessor();
        processor.setDimensions(200, 200, ResizeProcessor.STRATEGY_RATIO_MAINTAINED);
        Bitmap result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(200, result.getWidth());
        Assert.assertEquals(150, result.getHeight());


        bitmap = Bitmap.createBitmap(300, 400, Bitmap.Config.ARGB_8888);
        processor.setDimensions(200, 200, ResizeProcessor.STRATEGY_RATIO_MAINTAINED);
        result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(150, result.getWidth());
        Assert.assertEquals(200, result.getHeight());


        bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888);
        processor.setDimensions(200, 200, ResizeProcessor.STRATEGY_RATIO_MAINTAINED);
        result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(200, result.getWidth());
        Assert.assertEquals(100, result.getHeight());

        bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888);
        processor.setDimensions(600, 800, ResizeProcessor.STRATEGY_RATIO_MAINTAINED);
        result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(600, result.getWidth());
        Assert.assertEquals(300, result.getHeight());
    }

    @Test
    public void testResizeScaleFree() {
        Bitmap bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888);
        ResizeProcessor processor = new ResizeProcessor();
        processor.setDimensions(200, 200, ResizeProcessor.STRATEGY_RATIO_FREE);
        Bitmap result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(200, result.getWidth());
        Assert.assertEquals(200, result.getHeight());


        bitmap = Bitmap.createBitmap(300, 400, Bitmap.Config.ARGB_8888);
        processor.setDimensions(200, 200, ResizeProcessor.STRATEGY_RATIO_FREE);
        result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(200, result.getWidth());
        Assert.assertEquals(200, result.getHeight());


        bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888);
        processor.setDimensions(200, 200, ResizeProcessor.STRATEGY_RATIO_FREE);
        result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(200, result.getWidth());
        Assert.assertEquals(200, result.getHeight());

        bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888);
        processor.setDimensions(600, 800, ResizeProcessor.STRATEGY_RATIO_FREE);
        result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(600, result.getWidth());
        Assert.assertEquals(800, result.getHeight());
    }

    @Test
    public void testResizeRatioMaintainedDownOnly() {
        Bitmap bitmap = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888);
        ResizeProcessor processor = new ResizeProcessor();
        processor.setDimensions(200, 200,
                ResizeProcessor.STRATEGY_RATIO_MAINTAINED | ResizeProcessor.STRATEGY_SCALE_DOWN_ONLY);
        Bitmap result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(200, result.getWidth());
        Assert.assertEquals(150, result.getHeight());


        bitmap = Bitmap.createBitmap(300, 400, Bitmap.Config.ARGB_8888);
        processor.setDimensions(200, 200,
                ResizeProcessor.STRATEGY_RATIO_MAINTAINED | ResizeProcessor.STRATEGY_SCALE_DOWN_ONLY);
        result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(150, result.getWidth());
        Assert.assertEquals(200, result.getHeight());


        bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888);
        processor.setDimensions(200, 200,
                ResizeProcessor.STRATEGY_RATIO_MAINTAINED | ResizeProcessor.STRATEGY_SCALE_DOWN_ONLY);
        result = processor.process(bitmap);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(200, result.getWidth());
        Assert.assertEquals(100, result.getHeight());

        bitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888);
        processor.setDimensions(600, 800,
                ResizeProcessor.STRATEGY_RATIO_MAINTAINED | ResizeProcessor.STRATEGY_SCALE_DOWN_ONLY);
        result = processor.process(bitmap);
        Assert.assertSame(bitmap, result);
        Assert.assertEquals(400, result.getWidth());
        Assert.assertEquals(200, result.getHeight());
    }


}