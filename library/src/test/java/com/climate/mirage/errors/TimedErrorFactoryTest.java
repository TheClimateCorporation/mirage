package com.climate.mirage.errors;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.Mirage;
import com.climate.mirage.RobolectricTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.IOException;

public class TimedErrorFactoryTest extends RobolectricTest {

    @Test
    public void testCreatesLog() {
        TimedErrorFactory factory = new TimedErrorFactory();
        LoadError error = factory.createErrorLog("something", new IOException("test exception"), Mirage.Source.EXTERNAL);
        Assert.assertNotNull(error);
    }

    @Test
    public void testCreatesLogTimed() {
        TimedErrorFactory factory = new TimedErrorFactory(3000);
        LoadError error = factory.createErrorLog("something", new IOException("test exception"), Mirage.Source.EXTERNAL);
        Assert.assertNotNull(error);
    }


}