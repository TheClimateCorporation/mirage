package com.climate.mirage.tasks;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RoboManifestRunner;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.IOException;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class MirageExecutorTest {

    @Before
    public void setUp() throws IOException {

    }

    @After
    public void tearDown() throws IOException {

    }

    @Test
    public void testGetsFromResultCache() {
        MirageExecutor executor = new MirageExecutor();
        Thread thread1 = executor.getThreadFactory().newThread(new Runnable() {
            @Override
            public void run() {

            }
        });
        Thread thread2 = executor.getThreadFactory().newThread(new Runnable() {
            @Override
            public void run() {

            }
        });

        Assert.assertNotNull(thread1.getName());
        Assert.assertNotNull(thread2.getName());
    }

}