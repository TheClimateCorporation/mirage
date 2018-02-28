package com.climate.mirage.processors;

import android.graphics.Bitmap;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RobolectricTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

public class InverseProcessorTest extends RobolectricTest {

    @Test
    public void testId() {
        InverseProcessor processor = new InverseProcessor();
        Assert.assertEquals("inverse", processor.getId());
    }

    @Test
    public void testReturnsNewBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        InverseProcessor processor = new InverseProcessor();
        Bitmap result = processor.process(bitmap);
        Assert.assertNotNull(result);
        Assert.assertNotSame(bitmap, result);
    }

}