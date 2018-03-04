package com.climate.mirage;

import android.app.Application;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
abstract public class RobolectricTest {

    @Before
    public void __setup() {
        Mirage.set(null);
    }


    public Application getApp() {
        return RuntimeEnvironment.application;
    }
}