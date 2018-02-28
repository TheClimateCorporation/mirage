package com.climate.mirage.processors;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RobolectricTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

public class MatrixProcessorTest extends RobolectricTest {

    @Test
    public void testId() {
        Matrix matrix = new Matrix();
        MatrixProcessor processor = new MatrixProcessor(matrix);
        Assert.assertEquals("matrix"+matrix.toString(), processor.getId());
    }

    @Test
    public void testReturnsNewBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        Matrix matrix = new Matrix();
        matrix.postScale(.5f, 1f);
        MatrixProcessor processor = new MatrixProcessor(matrix);
        Bitmap result = processor.process(bitmap);
        Assert.assertNotNull(result);
        Assert.assertNotSame(bitmap, result);
    }

}