package com.climate.mirage.exceptions;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.Mirage;
import com.climate.mirage.RoboManifestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.IOException;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class OutOfMemoryExceptionTest {

    @Test
    public void testConstructors() throws Exception {
        MirageOomException e3 = new MirageOomException(Mirage.Source.DISK, new IOException("test exc"));
        MirageOomException e4 = new MirageOomException(Mirage.Source.DISK);
    }


}