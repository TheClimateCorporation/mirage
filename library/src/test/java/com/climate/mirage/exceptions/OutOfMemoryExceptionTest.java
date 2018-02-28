package com.climate.mirage.exceptions;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.Mirage;
import com.climate.mirage.RobolectricTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.IOException;

public class OutOfMemoryExceptionTest extends RobolectricTest {

    @Test
    public void testConstructors() throws Exception {
        MirageOomException e3 = new MirageOomException(Mirage.Source.DISK, new IOException("test exc"));
        MirageOomException e4 = new MirageOomException(Mirage.Source.DISK);
    }


}