package com.climate.mirage.utils;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.CoverageUtil;
import com.climate.mirage.RoboManifestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.InputStream;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class IOUtilsTest {

    @Test
    public void testHandlesNull() throws Exception {
        IOUtils.close(null);
    }

    @Test
    public void testClosesStream() throws Exception {
        InputStream is = Mockito.mock(InputStream.class);
        IOUtils.close(is);
        Mockito.verify(is, Mockito.times(1)).close();
    }

    @Test
    public void testClosesThrowsError() throws Exception {
        InputStream is = Mockito.mock(InputStream.class);
        Mockito.doThrow(new IOException("throwing for test")).when(is).close();
        IOUtils.close(is);
        Mockito.verify(is, Mockito.times(1)).close();
    }

    @Test
    public void test_elimNoist() throws Exception {
        CoverageUtil.initPrivateConstructor(IOUtils.class);
    }
}