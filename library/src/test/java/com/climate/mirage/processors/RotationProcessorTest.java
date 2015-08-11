package com.climate.mirage.processors;

import android.graphics.Bitmap;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RoboManifestRunner;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class RotationProcessorTest {

    @Test
    public void testId() {
        RotateProcessor processor = new RotateProcessor(90);
        Assert.assertEquals("rotate90", processor.getId());
    }

    @Test
    public void testReturnsNewBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        RotateProcessor processor = new RotateProcessor(90);
        Bitmap result = processor.process(bitmap);
        Assert.assertNotNull(result);
        Assert.assertNotSame(bitmap, result);
        Assert.assertEquals(90, processor.getDegrees());
    }

}