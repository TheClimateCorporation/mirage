package com.climate.mirage.targets;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.Mirage;
import com.climate.mirage.RobolectricTest;
import com.climate.mirage.requests.MirageRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;

public class LogTargetTest extends RobolectricTest {

    @Test
    public void testThingsOk() {
        LogTarget<File> target = new LogTarget<File>();
        target.onPreparingLoad();
        target.onResult(RuntimeEnvironment.application.getCacheDir(), Mirage.Source.DISK, new MirageRequest());
        target.onResult(null, Mirage.Source.DISK, new MirageRequest());
        target.onError(new IOException("test exc"), Mirage.Source.DISK, new MirageRequest());
    }
}