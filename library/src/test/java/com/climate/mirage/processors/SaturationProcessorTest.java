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
public class SaturationProcessorTest {

    @Test
    public void testId() {
        SaturationProcessor processor = new SaturationProcessor(50);
        Assert.assertEquals("saturation"+50, processor.getId());
        Assert.assertEquals(50, processor.getSaturation());
    }

    @Test
    public void testReturnsNewBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        SaturationProcessor processor = new SaturationProcessor(50);
        Bitmap result = processor.process(bitmap);
        Assert.assertNotNull(result);
        Assert.assertNotSame(bitmap, result);
    }

}