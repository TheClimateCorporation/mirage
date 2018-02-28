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
import com.climate.mirage.load.SimpleUrlConnectionFactory;
import com.climate.mirage.processors.BitmapProcessor;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BitmapTaskTest extends RobolectricTest {

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
    public void testGetsFromResultCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        File file = Mockito.mock(File.class);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        Mockito.when(diskCache.get(Mockito.eq("123"))).thenReturn(file);
        Mockito.when(diskCache.get(Mockito.eq("1234"))).thenReturn(null);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
        Mockito.when(request.memoryCache()).thenReturn(null);
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
        Mockito.verify(diskCache, Mockito.times(1)).get(Mockito.eq("123"));
        Mockito.verify(diskCache, Mockito.times(0)).get(Mockito.eq("1234")); // only store the processed version
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.DISK), Mockito.eq(request));
        Assert.assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    public void testGetsFromSourceCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        File file = Mockito.mock(File.class);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        Mockito.when(diskCache.get(Mockito.eq("123"))).thenReturn(null);
        Mockito.when(diskCache.get(Mockito.eq("1234"))).thenReturn(file);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
        Mockito.when(request.memoryCache()).thenReturn(null);
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
        task.execute(); // can not executeOnExecutor or the test will never finish
        waitForIt();
        Mockito.verify(diskCache, Mockito.times(1)).get(Mockito.eq("123"));
        Mockito.verify(diskCache, Mockito.times(2)).get(Mockito.eq("1234")); // only store the processed version
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.DISK), Mockito.eq(request));
        Assert.assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    public void testGetsFromMemoryCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        Mockito.when(memoryCache.get(Mockito.eq("123"))).thenReturn(bm);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
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
        Mockito.verify(memoryCache, Mockito.times(1)).get(Mockito.eq("123"));
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.MEMORY), Mockito.eq(request));
        Assert.assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    public void testSkipsWritingToMemoryCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        Mockito.when(memoryCache.get(Mockito.eq("123"))).thenReturn(null);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(true);
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
        Mockito.verify(memoryCache, Mockito.times(0)).put(Mockito.eq("123"), (Bitmap) Mockito.anyObject());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL), Mockito.eq(request));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    public void testSkipsReadingMemoryCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        Mockito.when(memoryCache.get(Mockito.eq("123"))).thenReturn(bm);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(true);
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
        Mockito.verify(memoryCache, Mockito.times(0)).get(Mockito.eq("123"));
        Mockito.verify(memoryCache, Mockito.times(1)).put(Mockito.eq("123"), (Bitmap) Mockito.anyObject());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL), Mockito.eq(request));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    public void testSkipsReadingDiskCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        File file = Mockito.mock(File.class);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        Mockito.when(diskCache.get(Mockito.eq("123"))).thenReturn(file);
        Mockito.when(diskCache.get(Mockito.eq("1234"))).thenReturn(file);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(true);
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
        Mockito.verify(diskCache, Mockito.times(0)).get(Mockito.eq("123"));
        Mockito.verify(diskCache, Mockito.times(0)).get(Mockito.eq("1234"));
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL), Mockito.eq(request));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    public void testWritesToSourceCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        final AtomicInteger counter = new AtomicInteger();
        Mockito.doAnswer(new Answer() {
            @Override
            public File answer(InvocationOnMock invocation) throws Throwable {
                if (counter.get() < 1) {
                    counter.incrementAndGet();
                    return null;
                } else {
                    counter.incrementAndGet();
                    return Mockito.mock(File.class);
                }

            }
        }).when(diskCache).get(Mockito.eq("1234"));


        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.SOURCE);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);
        Mockito.when(request.isRequestShouldSaveSource()).thenReturn(true);

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
        // its called twice. first time is to check if it's in the cache.
        // second time is to get the file it created to write to it.
        Mockito.verify(diskCache, Mockito.times(2)).get(Mockito.eq("1234"));
        Mockito.verify(diskCache, Mockito.times(1)).put(Mockito.eq("1234"), (DiskCache.Writer) Mockito.anyObject());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL), Mockito.eq(request));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    public void testWritesToSourceCache_WhenResultAndSourceAreTheSame() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        final AtomicInteger counter = new AtomicInteger();
        Mockito.doAnswer(new Answer() {
            @Override
            public File answer(InvocationOnMock invocation) throws Throwable {
                if (counter.get() < 2) {
                    counter.incrementAndGet();
                    return null;
                } else {
                    counter.incrementAndGet();
                    return Mockito.mock(File.class);
                }

            }
        }).when(diskCache).get(Mockito.eq("1234"));


        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.ALL);
        Mockito.when(request.getResultKey()).thenReturn("1234");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);
        Mockito.when(request.isRequestShouldSaveSource()).thenReturn(true);

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
        Mockito.verify(diskCache, Mockito.times(1)).put(Mockito.eq("1234"), (DiskCache.Writer)Mockito.anyObject());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL), Mockito.eq(request));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    public void testWritesToResultCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        DiskCache diskCache = Mockito.mock(DiskCache.class);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(null);
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
        // its called twice. first time is to check if it's in the cache.
        // second time is to get the file it created to write to it.
        Mockito.verify(diskCache, Mockito.times(1)).put(Mockito.eq("123"), (DiskCache.Writer)Mockito.anyObject());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL), Mockito.eq(request));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    public void testFetchFromDiskAddsToMemoryCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("nothing"));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        Mockito.when(memoryCache.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(memoryCache.has(Mockito.anyString())).thenReturn(false);
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
        Mockito.verify(diskCache, Mockito.times(1)).get(Mockito.eq("123"));
        Mockito.verify(memoryCache, Mockito.times(1)).put(Mockito.eq("123"), (Bitmap) Mockito.any());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.DISK), Mockito.eq(request));
        Assert.assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    public void testFastFailsOnLoadManagerError() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(400).setBody("nothing"));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        DiskCache diskCache = Mockito.mock(DiskCache.class);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);

        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);
        LoadError loadError = Mockito.mock(LoadError.class);
        Mockito.when(loadError.isValid()).thenReturn(true);
        Mockito.when(manager.getLoadError((Uri)Mockito.anyObject())).thenReturn(loadError);

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
                (Exception) Mockito.any(), Mockito.eq(Mirage.Source.MEMORY), Mockito.eq(request));
        Mockito.verify(loadError, Mockito.times(1)).isValid();
        Assert.assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    public void testRemovesLoadManagerErrorIfNoLongerValid() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(null);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);

        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);
        LoadError loadError = Mockito.mock(LoadError.class);
        Mockito.when(loadError.isValid()).thenReturn(false);
        Mockito.when(manager.getLoadError((Uri)Mockito.anyObject())).thenReturn(loadError);

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
        Mockito.verify(loadError, Mockito.times(1)).isValid();
        Mockito.verify(manager, Mockito.times(1)).removeLoadError(Mockito.eq(request.uri()));
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL), Mockito.eq(request));
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    public void testAppliesProcessorsFromSourceDiskCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        File file = Mockito.mock(File.class);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        // shadow BitmapFactory class will return a bitmap from this file
        Mockito.when(diskCache.get(Mockito.eq("123"))).thenReturn(null);
        Mockito.when(diskCache.get(Mockito.eq("1234"))).thenReturn(file);
        BitmapProcessor processor1 = Mockito.mock(BitmapProcessor.class);
        Mockito.when(processor1.process((Bitmap)Mockito.anyObject()))
                .thenReturn(Bitmap.createBitmap(300, 100, Bitmap.Config.ARGB_8888));
        List<BitmapProcessor> processorList = new ArrayList<>();
        processorList.add(processor1);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(false);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(false);
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);
        Mockito.when(request.getProcessors()).thenReturn(processorList);

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
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.DISK), Mockito.eq(request));
        Mockito.verify(processor1, Mockito.times(1)).process((Bitmap) Mockito.anyObject());
        Assert.assertEquals(0, mockWebServer.getRequestCount());
    }

    @Test
    public void testAppliesProcessorsFromWeb() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        BitmapProcessor processor1 = Mockito.mock(BitmapProcessor.class);
        Mockito.when(processor1.process((Bitmap)Mockito.anyObject()))
                .thenReturn(Bitmap.createBitmap(300, 100, Bitmap.Config.ARGB_8888));
        List<BitmapProcessor> processorList = new ArrayList<>();
        processorList.add(processor1);

        Target<Bitmap> target = Mockito.mock(Target.class);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(false);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(false);
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(null);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);
        Mockito.when(request.getProcessors()).thenReturn(processorList);

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
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.EXTERNAL), Mockito.eq(request));
        Mockito.verify(processor1, Mockito.times(1)).process((Bitmap)Mockito.anyObject());
        Assert.assertEquals(1, mockWebServer.getRequestCount());
    }

    @Test
    public void testFetchFromMemoryPutsBackInCache() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        Mockito.when(memoryCache.get(Mockito.eq("123"))).thenReturn(bm);
        File file = Mockito.mock(File.class);
        DiskCache diskCache = Mockito.mock(DiskCache.class);
        Mockito.when(diskCache.get(Mockito.eq("123"))).thenReturn(file);
        Mockito.when(diskCache.get(Mockito.eq("1234"))).thenReturn(file);

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
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), Mockito.eq(Mirage.Source.MEMORY), Mockito.eq(request));
        Mockito.verify(memoryCache, Mockito.times(1)).get(Mockito.eq("123"));
        Mockito.verify(diskCache, Mockito.times(1)).put(Mockito.eq("123"),
                (DiskCache.Writer)Mockito.anyObject());
        Assert.assertEquals(0, mockWebServer.getRequestCount());
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