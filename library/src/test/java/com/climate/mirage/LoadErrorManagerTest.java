package com.climate.mirage;

import android.net.Uri;

import com.climate.mirage.errors.LoadError;
import com.climate.mirage.errors.LoadErrorFactory;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

import java.io.IOException;

public class LoadErrorManagerTest extends RobolectricTest {

    @Before
    public void setUp() {
        Mirage.set(null);
    }

    @After
    public void tearDown() {
        Mirage.set(null);
    }

    @Test
    public void testErrorsAreNull() throws Exception {
        LoadErrorManager manager = new LoadErrorManager();
        LoadError error = manager.getLoadError("http://www.no_error.com");
        Assert.assertNull(error);
    }

    @Test
    public void testAddError() throws Exception {
        LoadErrorManager manager = new LoadErrorManager();
        manager.addLoadError("http://www.some_url.com",
                new IOException("something"), Mirage.Source.EXTERNAL);
        LoadError error = manager.getLoadError("http://www.some_url.com");
        Assert.assertNotNull(error);
        Assert.assertTrue(error.isValid());
        Assert.assertNotNull(error.getException());
    }

    @Test
    public void testRemoveError() throws Exception {
        LoadErrorManager manager = new LoadErrorManager();
        manager.addLoadError("http://www.some_url.com",
                new IOException("something"), Mirage.Source.EXTERNAL);
        LoadError error = manager.getLoadError("http://www.some_url.com");
        Assert.assertNotNull(error);

        manager.removeLoadError("http://www.some_url.com");
        error = manager.getLoadError("http://www.some_url.com");
        Assert.assertNull(error);
    }

    @Test
    public void testOverloadedConstructor() throws Exception {
        LoadErrorFactory factory = Mockito.mock(LoadErrorFactory.class);
        LoadErrorManager manager = new LoadErrorManager(factory);
        LoadError error = manager.getLoadError("http://www.no_error.com");
        Assert.assertNull(error);
    }

}