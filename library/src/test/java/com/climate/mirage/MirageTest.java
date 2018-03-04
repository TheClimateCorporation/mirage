package com.climate.mirage;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.cache.memory.BitmapLruCache;
import com.climate.mirage.cache.memory.MemoryCache;
import com.climate.mirage.load.SimpleUrlConnectionFactory;
import com.climate.mirage.load.UriProvider;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.shadows.CancelAsyncTaskShadow;
import com.climate.mirage.shadows.NoExecuteAsyncTaskShadow;
import com.climate.mirage.shadows.OnlyPostExecuteAsyncTaskShadow;
import com.climate.mirage.targets.ImageViewTarget;
import com.climate.mirage.targets.Target;
import com.climate.mirage.tasks.MirageExecutor;
import com.climate.mirage.tasks.MirageTask;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MirageTest extends RobolectricTest {

    @Before
    public void setUp() {
        Mirage.set(null);
    }

    @After
    public void tearDown() {
        Mirage.set(null);
    }

    @Test
    public void testGetsDefault() throws Exception {
        Mirage mirage = Mirage.get(RuntimeEnvironment.application);
        Assert.assertNotNull(mirage.getDefaultDiskCache());
        Assert.assertNotNull(mirage.getDefaultMemoryCache());
        Assert.assertNotNull(mirage.getDefaultExecutor());
    }

    @Test
    public void testSetDefault() throws Exception {
        Mirage mirage = new Mirage(RuntimeEnvironment.application);
        Mirage.set(mirage);
        Assert.assertSame(mirage, Mirage.get(RuntimeEnvironment.application));
    }


    @Test
    public void testLoadString() throws Exception {
        Mirage mirage = new Mirage(RuntimeEnvironment.application);
        MirageRequest request = mirage.load("http://www.google.com/");
        Assert.assertEquals(Uri.parse("http://www.google.com/"), request.uri());

        request = mirage.load("file://sdcard/file1.jpg");
        Assert.assertEquals(Uri.parse("file://sdcard/file1.jpg"), request.uri());

        request = mirage.load("content://some/path/file1.jpg");
        Assert.assertEquals(Uri.parse("content://some/path/file1.jpg"), request.uri());
    }

    @Test
    public void testLoadUri() throws Exception {
        Mirage mirage = new Mirage(RuntimeEnvironment.application);
        MirageRequest request = mirage.load(Uri.parse("http://www.google.com/"));
        Assert.assertEquals(Uri.parse("http://www.google.com/"), request.uri());

        request = mirage.load(Uri.parse("file://sdcard/file1.jpg"));
        Assert.assertEquals(Uri.parse("file://sdcard/file1.jpg"), request.uri());

        request = mirage.load(Uri.parse("content://some/path/file1.jpg"));
        Assert.assertEquals(Uri.parse("content://some/path/file1.jpg"), request.uri());
    }

    @Test
    public void testLoadFile() throws Exception {
        Mirage mirage = new Mirage(RuntimeEnvironment.application);
        File file = new File(RuntimeEnvironment.application.getCacheDir(), "file.jpg");
        MirageRequest request = mirage.load(file);
        Assert.assertEquals(Uri.fromFile(file), request.uri());
    }

    @Test
    public void testSetupParams() throws Exception {
        Mirage mirage = new Mirage(RuntimeEnvironment.application);
        mirage.setDefaultDiskCache(null);
        mirage.setDefaultExecutor(null);
        mirage.setDefaultMemoryCache(null);
        mirage.setDefaultUrlConnectionFactory(null);

        mirage.setDefaultDiskCache(Mockito.mock(DiskCache.class));
        mirage.setDefaultExecutor(new MirageExecutor());
        mirage.setDefaultMemoryCache(new BitmapLruCache(5 * 1024 * 1024));
        mirage.setDefaultUrlConnectionFactory(new SimpleUrlConnectionFactory());
    }

    @Test
    public void testClearCache() throws Exception {
        Mirage mirage = new Mirage(RuntimeEnvironment.application);

        DiskCache diskCache = Mockito.mock(DiskCache.class);
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        mirage.setDefaultDiskCache(diskCache);
        mirage.setDefaultMemoryCache(memoryCache);

        mirage.clearCache();
        Mockito.verify(memoryCache, Mockito.times(1)).clear();
        Mockito.verify(diskCache, Mockito.times(1)).clear();

        mirage.clearMemoryCache();
        Mockito.verify(memoryCache, Mockito.times(2)).clear();

        mirage.clearDiskCache();
        Mockito.verify(diskCache, Mockito.times(2)).clear();
    }

    @Test
    public void testClearCache_onBackgroundThread() throws Exception {
        final AtomicBoolean wait = new AtomicBoolean(true);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    testClearCache();
                    testClearCache_byRequest();
                } catch (Exception e) {
                    Assert.fail("No exception should have been thrown");
                } finally {
                    wait.set(false);
                }
            }
        });
        while (wait.get()) {}
    }

    @Test
    public void testClearCache_byRequest() throws Exception {
        // removes where both keys are the same
        Mirage mirage = new Mirage(RuntimeEnvironment.application);
        MirageRequest request = mirage.load("http://www.some_site.com/file.jpg");

        DiskCache diskCache = Mockito.mock(DiskCache.class);
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
        mirage.setDefaultDiskCache(diskCache);
        mirage.setDefaultMemoryCache(memoryCache);

        mirage.removeFromCache(request);
        Mockito.verify(memoryCache, Mockito.times(1)).remove(Mockito.eq(request.getResultKey()));
        Mockito.verify(diskCache, Mockito.times(1)).delete(Mockito.eq(request.getResultKey()));

        // remove when both keys are different
        request = mirage.load("http://www.some_site.com/file.jpg");
        request.resize(600, 400);

        diskCache = Mockito.mock(DiskCache.class);
        memoryCache = Mockito.mock(MemoryCache.class);
        mirage.setDefaultDiskCache(diskCache);
        mirage.setDefaultMemoryCache(memoryCache);

        mirage.removeFromCache(request);
        Mockito.verify(memoryCache, Mockito.times(1)).remove(Mockito.eq(request.getResultKey()));
        Mockito.verify(diskCache, Mockito.times(1)).delete(Mockito.eq(request.getResultKey()));
        Mockito.verify(diskCache, Mockito.times(1)).delete(Mockito.eq(request.getSourceKey()));

        // but no disk cache
        request = mirage.load("http://www.some_site.com/file.jpg");

        diskCache = null;
        memoryCache = Mockito.mock(MemoryCache.class);
        mirage.setDefaultDiskCache(null);
        mirage.setDefaultMemoryCache(memoryCache);

        mirage.removeFromCache(request);
        Mockito.verify(memoryCache, Mockito.times(1)).remove(Mockito.eq(request.getResultKey()));

        // but no caches at all
        request = mirage.load("http://www.some_site.com/file.jpg");

        diskCache = null;
        memoryCache = null;
        mirage.setDefaultDiskCache(null);
        mirage.setDefaultMemoryCache(null);

        mirage.removeFromCache(request);
    }

    @Config(shadows = NoExecuteAsyncTaskShadow.class, constants = BuildConfig.class)
    @Test
    public void testCancelRunningTask_byView() throws Exception {
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        Mirage mirage = Mirage.get(RuntimeEnvironment.application);
        MirageTask task = mirage.load("http://www.some_url.com/bleh.jpg")
                .into(imageView)
                .go();
        Field field = Mirage.class.getDeclaredField("runningRequests");
        field.setAccessible(true);
        Map<Object, MirageTask> map = (Map<Object, MirageTask> )field.get(mirage);
        Assert.assertEquals(1, map.size());
        Assert.assertFalse(task.isCancelled());
        mirage.cancelRequest(imageView);
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(task.isCancelled());
    }

    @Config(shadows = NoExecuteAsyncTaskShadow.class, constants = BuildConfig.class)
    @Test
    public void testCancelRunningTask_byTarget() throws Exception {
        Mirage mirage = Mirage.get(RuntimeEnvironment.application);
        MirageRequest request = mirage.load("http://www.some_url.com/bleh.jpg");
        ImageViewTarget ivt = new ImageViewTarget(request,
                new ImageView(RuntimeEnvironment.application));
        MirageTask task = request.into(ivt).go();

        Field field = Mirage.class.getDeclaredField("runningRequests");
        field.setAccessible(true);
        Map<Object, MirageTask> map = (Map<Object, MirageTask> )field.get(mirage);
        Assert.assertEquals(1, map.size());
        Assert.assertFalse(task.isCancelled());
        mirage.cancelRequest(ivt);
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(task.isCancelled());
    }

    @Config(shadows = OnlyPostExecuteAsyncTaskShadow.class, constants = BuildConfig.class)
    @Test
    public void testRemovesFromQueueAfterExecute() throws Exception {
        Mirage mirage = Mirage.get(RuntimeEnvironment.application);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(null);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse("http://www.some_url.com/bleh.jpg"));
        Mockito.when(request.provider()).thenReturn(new UriProvider(request));
        Mockito.when(request.executor()).thenReturn(mirage.getDefaultExecutor());

        ImageViewTarget ivt = new ImageViewTarget(request,
                new ImageView(RuntimeEnvironment.application));
        Mockito.when(request.target()).thenReturn(ivt);
        MirageTask task = mirage.go(request);

        Field field = Mirage.class.getDeclaredField("runningRequests");
        field.setAccessible(true);
        Map<Object, MirageTask> map = (Map<Object, MirageTask> )field.get(mirage);
        Assert.assertEquals(AsyncTask.Status.FINISHED, task.getStatus());
        Assert.assertEquals(0, map.size());
        Mockito.verify(request, Mockito.times(1)).recycle();


        // do it again but not a view target
        Target<Bitmap> target2 = Mockito.mock(Target.class);
        request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(null);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse("http://www.some_url.com/bleh.jpg"));
        Mockito.when(request.provider()).thenReturn(new UriProvider(request));
        Mockito.when(request.executor()).thenReturn(mirage.getDefaultExecutor());
        Mockito.when(request.target()).thenReturn(target2);
        task = mirage.go(request);

        Assert.assertEquals(AsyncTask.Status.FINISHED, task.getStatus());
        Assert.assertEquals(0, map.size());
        Mockito.verify(request, Mockito.times(1)).recycle();
    }

    @Config(shadows = OnlyPostExecuteAsyncTaskShadow.class, constants = BuildConfig.class)
    @Test
    public void testRemovesFromQueueAfterExecute_downloadOnly() throws Exception {
        Mirage mirage = Mirage.get(RuntimeEnvironment.application);
        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.memoryCache()).thenReturn(null);
        Mockito.when(request.diskCache()).thenReturn(null);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse("http://www.some_url.com/bleh.jpg"));
        Mockito.when(request.executor()).thenReturn(mirage.getDefaultExecutor());

        ImageViewTarget ivt = new ImageViewTarget(request,
                new ImageView(RuntimeEnvironment.application));
        Mockito.when(request.target()).thenReturn(ivt);
        MirageTask task = mirage.downloadOnly(request);

        Field field = Mirage.class.getDeclaredField("runningRequests");
        field.setAccessible(true);
        Map<Object, MirageTask> map = (Map<Object, MirageTask> )field.get(mirage);
        Assert.assertEquals(AsyncTask.Status.FINISHED, task.getStatus());
        Assert.assertEquals(0, map.size());
        Mockito.verify(request, Mockito.times(1)).recycle();
    }

    @Config(shadows = CancelAsyncTaskShadow.class, constants = BuildConfig.class)
    @Test
    public void testRemovesFromQueueAfterExecute_cancel() throws Exception {
        testRemovesFromQueueAfterExecute();
        testRemovesFromQueueAfterExecute_downloadOnly();
    }

}