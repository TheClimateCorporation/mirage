package com.climate.mirage.exceptions;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.Mirage;
import com.climate.mirage.RobolectricTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.IOException;

public class MirageIOExceptionTest extends RobolectricTest {

    @Test
    public void testSource() throws Exception {
        MirageIOException e = new MirageIOException(Mirage.Source.EXTERNAL);
        Assert.assertEquals(Mirage.Source.EXTERNAL, e.getSource());

        e = new MirageIOException(Mirage.Source.DISK, new IOException("test exception"));
        Assert.assertEquals(Mirage.Source.DISK, e.getSource());
    }


}