package com.climate.mirage;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.cache.memory.MemoryCache;
import com.climate.mirage.errors.LoadError;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.targets.Target;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class MirageGoTest extends RobolectricTest {

    private MockWebServer mockWebServer;
    private URL baseUrl;
    private AtomicBoolean wait;

    @Before
    public void setUp() throws IOException {
        Mirage.set(null);
        mockWebServer = new MockWebServer();
        mockWebServer.play();
        baseUrl = mockWebServer.getUrl("");
        if (wait == null) wait = new AtomicBoolean();
        wait.set(true);
    }

    @After
    public void tearDown() throws IOException {
        Mirage.set(null);
        try {
            mockWebServer.shutdown();
        } catch (Exception e) {
            Log.e("BitmapUrlTaskTest", "Mock WebServer couldn't shut down", e);
        }
        baseUrl = null;
        mockWebServer = null;
    }

    @Test
    public void testHasLoadError() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("No Image"));
        LoadErrorManager loadErrorManager = Mockito.mock(LoadErrorManager.class);
        LoadError loadError = Mockito.mock(LoadError.class);
        Mockito.when(loadError.isValid()).thenReturn(true);
        Mockito.when(loadError.getException()).thenReturn(new IOException("mocked"));
        Mockito.when(loadErrorManager.getLoadError(Mockito.anyString())).thenReturn(loadError);
        Target<Bitmap> target = Mockito.mock(Target.class);

        Mirage mirage = Mirage.get(RuntimeEnvironment.application);
        Field field = Mirage.class.getDeclaredField("loadErrorManager");
        field.setAccessible(true);
        field.set(mirage, loadErrorManager);

        MirageRequest request = mirage.load(baseUrl.toString());
        request.into(target);
        request.go();

        Mockito.verify(loadErrorManager, Mockito.times(1))
                .getLoadError(Mockito.eq(request.provider().id()));

        Assert.assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    public void testGo() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("No Image"));
        Target<Bitmap> target = Mockito.mock(Target.class);

        // url request
        MirageRequest request = Mirage.get(RuntimeEnvironment.application)
                .load(baseUrl.toString());
        request.into(target);
        request.go();

        // file request
        request = Mirage.get(RuntimeEnvironment.application)
                .load(new File(RuntimeEnvironment.application.getCacheDir(), "no_file.jpg"));
        request.into(target);
        request.go();

        // content uri request
        request = Mirage.get(RuntimeEnvironment.application)
                .load("content://some/provider");
        request.into(target);
        request.go();
    }

    @Test
    public void testBlankUrlExists() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("No Image"));
        Target<Bitmap> target = Mockito.mock(Target.class);

        // url request
        Mirage mirage = Mirage.get(RuntimeEnvironment.application);
        MirageRequest request = new MirageRequest();
        request.into(target);
        mirage.go(request);


        Mockito.verify(target, Mockito.times(1)).onError(
                (Exception) Mockito.any(),
                Mockito.eq(Mirage.Source.MEMORY), (MirageRequest) Mockito.any());
    }

    @Test
    public void testResourceInMemoryCache() throws Exception {
        Mirage mirage = new Mirage(RuntimeEnvironment.application);

        DiskCache diskCache = Mockito.mock(DiskCache.class);
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);

        mirage.setDefaultDiskCache(diskCache);
        mirage.setDefaultMemoryCache(memoryCache);

        Target<Bitmap> target = Mockito.mock(Target.class);

        MirageRequest request = mirage.load("http://www.some_domain.com/file.jpg");
        request.into(target);
        Mockito.when(memoryCache.get(Mockito.eq(request.getResultKey())))
                .thenReturn(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
        mirage.go(request);


        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(),
                Mockito.eq(Mirage.Source.MEMORY), (MirageRequest) Mockito.any());
    }

    // robolectric thinks the test is on the main thread.
    // use the static AsyncTask.execute to put this on a different thread
    @Test
    public void testGoSync() throws Exception {
        final Holder holder = new Holder();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runAndThrow();
                } catch (Exception e) {
                    holder.e = e;
                } finally {
                    wait.set(false);
                }
            }

            private void runAndThrow() throws Exception {
                Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
                mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bitmap)));
                Target<Bitmap> target = Mockito.mock(Target.class);

                // url request
                MirageRequest request = Mirage.get(RuntimeEnvironment.application)
                        .load(baseUrl.toString());
                request.goSync();

                // file request
                request = Mirage.get(RuntimeEnvironment.application)
                        .load(new File(RuntimeEnvironment.application.getCacheDir(), "no_file.jpg"));
                try {
                    request.goSync();
                } catch (Exception e) {
                    // dont care about any error here (which will happen)
                    // as not testing for this.
                }

                // content uri request
                request = Mirage.get(RuntimeEnvironment.application)
                        .load("content://some/provider");
                try {
                    request.goSync();
                } catch (Exception e) {
                    // dont care about any error here (which will happen)
                    // as not testing for this.
                }

                // has target
                request = Mirage.get(RuntimeEnvironment.application)
                        .load("content://some/provider");
                request.into(target);
                try {
                    request.goSync();
                    Assert.fail("IllegalState should have been thrown. Targets aren't allowed");
                } catch (IllegalArgumentException e) {
                    Assert.assertNotNull(e);
                }
            }
        });

        while(wait.get()) {}
        Assert.assertNull(holder.e);
    }

    // robolectric thinks the test is on the main thread.
    // use the static AsyncTask.execute to put this on a different thread
    @Test
    public void testDownloadSync() throws Exception {
        final Holder holder = new Holder();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    runAndThrow();
                } catch (Exception e) {
                    holder.e = e;
                } finally {
                    wait.set(false);
                }
            }

            private void runAndThrow() throws Exception {
                Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
                mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bitmap)));
                Target<Bitmap> target = Mockito.mock(Target.class);

                // url request
                MirageRequest request = Mirage.get(RuntimeEnvironment.application)
                        .load(baseUrl.toString());
                request.downloadOnlySync();

                // file request
                request = Mirage.get(RuntimeEnvironment.application)
                        .load(new File(RuntimeEnvironment.application.getCacheDir(), "no_file.jpg"));
                try {
                    request.downloadOnlySync();
                } catch (Exception e) {
                    // dont care about any error here (which will happen)
                    // as not testing for this.
                }

                // content uri request
                request = Mirage.get(RuntimeEnvironment.application)
                        .load("content://some/provider");
                try {
                    request.downloadOnlySync();
                } catch (Exception e) {
                    // dont care about any error here (which will happen)
                    // as not testing for this.
                }

                // has target
                request = Mirage.get(RuntimeEnvironment.application)
                        .load("content://some/provider");
                request.into(target);
                try {
                    request.downloadOnlySync();
                    Assert.fail("IllegalState should have been thrown. Targets aren't allowed");
                } catch (IllegalArgumentException e) {
                    Assert.assertNotNull(e);
                }
            }
        });

        while(wait.get()) {}
        Assert.assertNull(holder.e);
    }

    private static class Holder {
        private Exception e;
    }

    private byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap.CompressFormat f = bitmap.hasAlpha() ? Bitmap.CompressFormat.PNG :
                Bitmap.CompressFormat.JPEG;
        bitmap.compress(f, 90, stream);
        byte[] bytes = stream.toByteArray();
        return bytes;
    }

}