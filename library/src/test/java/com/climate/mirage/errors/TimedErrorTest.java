package com.climate.mirage.errors;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RoboManifestRunner;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.lang.reflect.Field;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class TimedErrorTest {

    @Test
    public void testStandard() {
        TimedLoadError error = new TimedLoadError(1000, new IOException("test exception"));

        Assert.assertNotNull(error.getException());
        Assert.assertTrue(error.isValid());
    }

    @Test
    public void testTimeExpires() throws Exception {
        TimedLoadError error = new TimedLoadError(1000, new IOException("test exception"));
        Field field = error.getClass().getDeclaredField("when");
        field.setAccessible(true);
        field.set(error, System.currentTimeMillis()-90*1000);

        Assert.assertNotNull(error.getException());
        Assert.assertFalse(error.isValid());
    }

}