package com.climate.mirage.tasks;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.RobolectricTest;
import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.cache.memory.MemoryCache;
import com.climate.mirage.errors.LoadError;
import com.climate.mirage.exceptions.MirageOomException;
import com.climate.mirage.load.SimpleUrlConnectionFactory;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.shadows.OomBitmapFactoryShadow;
import com.climate.mirage.shadows.OomRetryBitmapFactoryShadow;
import com.climate.mirage.targets.Target;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class BitmapUrlTaskTest extends RobolectricTest {

    private MockWebServer mockWebServer;
    private URL baseUrl;
    private AtomicBoolean wait;

    @Before
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.play();
        baseUrl = mockWebServer.getUrl("");
        if (wait == null) wait = new AtomicBoolean();
        wait.set(true);
    }

    @After
    public void tearDown() throws IOException {
        try {
            mockWebServer.shutdown();
        } catch (Exception e) {
            Log.e("BitmapUrlTaskTest", "Mock WebServer couldn't shut down", e);
        }
        baseUrl = null;
        mockWebServer = null;
    }

    @Test
    public void testDownloadFromWeb() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(true);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(true);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(true);
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(null);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);
        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = new MirageTask.Callback<Bitmap>() {
            @Override
            public void onCancel(MirageTask task, MirageRequest request) {
                Assert.fail("Test shouldn't have canceled");
                wait.set(false);
            }

            @Override
            public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
                Assert.assertNotNull(bitmap);
                wait.set(false);
            }
        };

        BitmapUrlTask task = new BitmapUrlTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(target, Mockito.times(0)).onPreparingLoad(); // this is not called by this class
        Mockito.verify(target, Mockito.times(0)).onError(
                (Exception) Mockito.any(), (Mirage.Source) Mockito.any(), (MirageRequest) Mockito.any());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), (Mirage.Source) Mockito.any(), (MirageRequest) Mockito.any());
        Mockito.verify(manager, Mockito.times(0)).addLoadError(
                (Uri) Mockito.anyObject(), (LoadError) Mockito.anyObject());
        Mockito.verify(manager, Mockito.times(0)).addLoadError(
                (Uri) Mockito.anyObject(), (Exception) Mockito.anyObject(),
                (Mirage.Source) Mockito.anyObject());
    }

    @Test
    public void testDownloadFromMemory() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        Mockito.when(memoryCache.get(Mockito.anyString())).thenReturn(bm);
        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(true);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(false);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(false);
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(null);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
        Mockito.when(request.target()).thenReturn(target);
        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = new MirageTask.Callback<Bitmap>() {
            @Override
            public void onCancel(MirageTask task, MirageRequest request) {
                Assert.fail("Test shouldn't have canceled");
                wait.set(false);
            }

            @Override
            public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
                Assert.assertNotNull(bitmap);
                wait.set(false);
            }
        };

        BitmapUrlTask task = new BitmapUrlTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.MEMORY), Mockito.eq(request));
        Assert.assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    public void testDownloadFromDisk() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        File file = Mockito.mock(File.class);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        // shadow BitmapFactory class will return a bitmap from this file
        Mockito.when(diskCache.get(Mockito.anyString())).thenReturn(file);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(false);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(false);
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);

        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = new MirageTask.Callback<Bitmap>() {
            @Override
            public void onCancel(MirageTask task, MirageRequest request) {
                Assert.fail("Test shouldn't have canceled");
                wait.set(false);
            }

            @Override
            public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
                Assert.assertNotNull(bitmap);
                wait.set(false);
            }
        };

        BitmapUrlTask task = new BitmapUrlTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(memoryCache, Mockito.times(1)).get(Mockito.eq("123"));
        Mockito.verify(memoryCache, Mockito.times(0)).get(Mockito.eq("1234")); // only store the processed version
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.DISK), Mockito.eq(request));
        Assert.assertEquals(0, mockWebServer.getRequestCount());
    }



    @Test
    public void testFetchFromWebAddsToCache_ResultOnly() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        Mockito.when(memoryCache.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(memoryCache.has(Mockito.anyString())).thenReturn(false);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        Mockito.when(diskCache.get(Mockito.anyString())).thenReturn(null);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(false);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(false);
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);

        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = new MirageTask.Callback<Bitmap>() {
            @Override
            public void onCancel(MirageTask task, MirageRequest request) {
                Assert.fail("Test shouldn't have canceled");
                wait.set(false);
            }

            @Override
            public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
                Assert.assertNotNull(bitmap);
                wait.set(false);
            }
        };

        BitmapUrlTask task = new BitmapUrlTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(memoryCache, Mockito.times(1)).put(Mockito.eq("123"), (Bitmap) Mockito.any());
        Mockito.verify(diskCache, Mockito.times(1)).put(Mockito.eq("123"), (DiskCache.Writer) Mockito.any());
        Mockito.verify(diskCache, Mockito.times(0)).put(Mockito.eq("1234"), (DiskCache.Writer) Mockito.any());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL), Mockito.eq(request));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    public void testFetchFromWebAddsToCache_SourceAndResult() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        Mockito.when(memoryCache.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(memoryCache.has(Mockito.anyString())).thenReturn(false);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        File file = Mockito.mock(File.class);
        Mockito.when(diskCache.get(Mockito.anyString())).thenReturn(file);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        // skip reading the disk cache so it forces a download and our next call to get can return a file
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(true);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(false);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(false);
        Mockito.when(request.isRequestShouldSaveSource()).thenReturn(true);
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.ALL);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);

        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = new MirageTask.Callback<Bitmap>() {
            @Override
            public void onCancel(MirageTask task, MirageRequest request) {
                Assert.fail("Test shouldn't have canceled");
                wait.set(false);
            }

            @Override
            public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
                Assert.assertNotNull(bitmap);
                wait.set(false);
            }
        };

        BitmapUrlTask task = new BitmapUrlTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(memoryCache, Mockito.times(1)).put(Mockito.eq("123"), (Bitmap) Mockito.any());
        Mockito.verify(diskCache, Mockito.times(1)).put(Mockito.eq("123"), (DiskCache.Writer) Mockito.any());
        Mockito.verify(diskCache, Mockito.times(1)).put(Mockito.eq("1234"), (DiskCache.Writer)Mockito.any());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL), Mockito.eq(request));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    public void testErrorsAddsToLoadManager() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("Not Allowed"));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        Mockito.when(memoryCache.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(memoryCache.has(Mockito.anyString())).thenReturn(false);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        Mockito.when(diskCache.get(Mockito.anyString())).thenReturn(null);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(false);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(false);
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);

        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = new MirageTask.Callback<Bitmap>() {
            @Override
            public void onCancel(MirageTask task, MirageRequest request) {
                Assert.fail("Test shouldn't have canceled");
                wait.set(false);
            }

            @Override
            public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
                Assert.assertNull(bitmap);
                wait.set(false);
            }
        };

        BitmapUrlTask task = new BitmapUrlTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(memoryCache, Mockito.times(0)).put(Mockito.eq("123"), (Bitmap) Mockito.any());
        Mockito.verify(diskCache, Mockito.times(0)).put(Mockito.eq("123"), (DiskCache.Writer) Mockito.any());
        Mockito.verify(diskCache, Mockito.times(0)).put(Mockito.eq("1234"), (DiskCache.Writer)Mockito.any());
        Mockito.verify(target, Mockito.times(1)).onError(
                (Exception) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL),
                (MirageRequest) Mockito.any());
        Mockito.verify(manager, Mockito.times(1)).addLoadError(
                Mockito.eq(request.uri()), (Exception) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Config(shadows = OomBitmapFactoryShadow.class, constants = BuildConfig.class)
    @Test
    public void testOutOfMemoryFromWebHandled() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(true);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(true);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(true);
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(null);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);
        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = new MirageTask.Callback<Bitmap>() {
            @Override
            public void onCancel(MirageTask task, MirageRequest request) {
                Assert.fail("Test shouldn't have canceled");
                wait.set(false);
            }

            @Override
            public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
                Assert.assertNull(bitmap);
                wait.set(false);
            }
        };

        BitmapUrlTask task = new BitmapUrlTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(target, Mockito.times(0)).onPreparingLoad(); // this is not called by this class
        Mockito.verify(target, Mockito.times(1)).onError(
                (Exception) Mockito.any(), (Mirage.Source) Mockito.any(),
                (MirageRequest) Mockito.any());
        Mockito.verify(target, Mockito.times(0)).onResult(
                (Bitmap) Mockito.any(), (Mirage.Source) Mockito.any(), (MirageRequest) Mockito.any());
        Mockito.verify(manager, Mockito.times(0)).addLoadError(
                (Uri) Mockito.anyObject(), (LoadError) Mockito.anyObject());
        Mockito.verify(manager, Mockito.times(1)).addLoadError(
                (Uri) Mockito.anyObject(), (MirageOomException) Mockito.anyObject(),
                (Mirage.Source) Mockito.anyObject());
    }

    @Config(shadows = OomRetryBitmapFactoryShadow.class, constants = BuildConfig.class)
    @Test
    public void testOutOfMemoryFromWebRetryHandled() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(true);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(true);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(true);
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(null);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);
        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = new MirageTask.Callback<Bitmap>() {
            @Override
            public void onCancel(MirageTask task, MirageRequest request) {
                Assert.fail("Test shouldn't have canceled");
                wait.set(false);
            }

            @Override
            public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
                Assert.assertNotNull(bitmap);
                wait.set(false);
            }
        };

        BitmapUrlTask task = new BitmapUrlTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(target, Mockito.times(0)).onPreparingLoad(); // this is not called by this class
        Mockito.verify(target, Mockito.times(0)).onError(
                (MirageOomException) Mockito.any(), (Mirage.Source) Mockito.any(),
                (MirageRequest) Mockito.any());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), (Mirage.Source) Mockito.any(), (MirageRequest) Mockito.any());
        Mockito.verify(manager, Mockito.times(0)).addLoadError(
                (Uri) Mockito.anyObject(), (LoadError) Mockito.anyObject());
        Mockito.verify(manager, Mockito.times(0)).addLoadError(
                (Uri) Mockito.anyObject(), (MirageOomException) Mockito.anyObject(),
                (Mirage.Source) Mockito.anyObject());
        Mockito.verify(memoryCache, Mockito.times(1)).clear();
    }

    @Config(shadows = OomRetryBitmapFactoryShadow.class, constants = BuildConfig.class)
    @Test
    public void testOutOfMemoryFromDiskRetryHandled() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        File file = Mockito.mock(File.class);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        Mockito.when(diskCache.get(Mockito.anyString())).thenReturn(file);
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);

        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);
        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = new MirageTask.Callback<Bitmap>() {
            @Override
            public void onCancel(MirageTask task, MirageRequest request) {
                Assert.fail("Test shouldn't have canceled");
                wait.set(false);
            }

            @Override
            public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
                Assert.assertNotNull(bitmap);
                wait.set(false);
            }
        };

        BitmapUrlTask task = new BitmapUrlTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.DISK), (MirageRequest) Mockito.any());
        Mockito.verify(memoryCache, Mockito.times(1)).clear();
    }

    @Config(shadows = OomBitmapFactoryShadow.class, constants = BuildConfig.class)
    @Test
    public void testOutOfMemoryFromDiskHandled() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        File file = Mockito.mock(File.class);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        Mockito.when(diskCache.get(Mockito.anyString())).thenReturn(file);
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);

        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);
        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = new MirageTask.Callback<Bitmap>() {
            @Override
            public void onCancel(MirageTask task, MirageRequest request) {
                Assert.fail("Test shouldn't have canceled");
                wait.set(false);
            }

            @Override
            public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
                Assert.assertNull(bitmap);
                wait.set(false);
            }
        };

        BitmapUrlTask task = new BitmapUrlTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(target, Mockito.times(1)).onError(
                (MirageOomException) Mockito.any(),
                Mockito.eq(Mirage.Source.DISK), (MirageRequest) Mockito.any());
        Mockito.verify(memoryCache, Mockito.times(1)).clear();
    }


    private byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap.CompressFormat f = bitmap.hasAlpha() ? Bitmap.CompressFormat.PNG :
                Bitmap.CompressFormat.JPEG;
        bitmap.compress(f, 90, stream);
        byte[] bytes = stream.toByteArray();
        return bytes;
    }

    private void waitForIt() {
        long t = System.currentTimeMillis();
//        while (wait.get() && (System.currentTimeMillis() - t < 1000)) {}
        while (wait.get()) {}
    }

}