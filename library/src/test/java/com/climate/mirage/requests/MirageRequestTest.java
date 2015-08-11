package com.climate.mirage.requests;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.Mirage;
import com.climate.mirage.RoboManifestRunner;
import com.climate.mirage.cache.SimpleKeyMaker;
import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.cache.memory.BitmapLruCache;
import com.climate.mirage.exceptions.MirageIOException;
import com.climate.mirage.load.SimpleUrlConnectionFactory;
import com.climate.mirage.processors.ResizeProcessor;
import com.climate.mirage.tasks.MirageTask;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class MirageRequestTest {

    @Test
    public void testRecycles() {
        MirageRequest request = new MirageRequest();
        request.resize(200, 200);
        request.recycle();

        request = new MirageRequest();
        request.recycle();
    }


    @Test
    public void testGoing() throws IOException {
        Mirage mirage = Mockito.mock(Mirage.class);
        MirageRequest request = new MirageRequest();
        Mockito.when(mirage.go(request)).thenReturn(Mockito.mock(MirageTask.class));
        Mockito.when(mirage.downloadOnly(request)).thenReturn(Mockito.mock(MirageTask.class));
        Mockito.when(mirage.goSync(request)).thenReturn(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
        Mockito.when(mirage.downloadOnlySync(request)).thenReturn(RuntimeEnvironment.application.getCacheDir());

        request.mirage(mirage)
                .uri(Uri.parse("http://www.fake_url.com/"))
                .go();

        request.mirage(mirage)
                .uri(Uri.parse("http://www.fake_url.com/"))
                .goSync();


        request.mirage(mirage)
                .uri(Uri.parse("http://www.fake_url.com/"))
                .downloadOnly();

        request.mirage(mirage)
                .uri(Uri.parse("http://www.fake_url.com/"))
                .downloadOnlySync();
    }


    @Test
    public void testGoOnNullMirage() {
        MirageRequest request = new MirageRequest();
        try {
            request.go();
            Assert.fail("this should have failed");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

        try {
            request.goSync();
            Assert.fail("this should have failed");
        } catch (MirageIOException e) {
            Assert.fail("this should have thrown an IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

        try {
            request.downloadOnly();
            Assert.fail("this should have failed");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

        try {
            request.downloadOnlySync();
            Assert.fail("this should have failed");
        } catch (MirageIOException e) {
            Assert.fail("this should have thrown an IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testProps() {
        MirageRequest request = new MirageRequest();
        request.uri(Uri.parse("http://www.google.com/"))
                .resize(200, 400)
                .diskCache(Mockito.mock(DiskCache.class))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .skipReadingDiskCache(true)
                .memoryCache(new BitmapLruCache(5 * 1024 * 1024))
                .skipReadingMemoryCache(true)
                .skipWritingMemoryCache(true)
                .urlFactory(new SimpleUrlConnectionFactory())
                .options(new BitmapFactory.Options())
                .into(new ImageView(RuntimeEnvironment.application));

        Assert.assertEquals(Uri.parse("http://www.google.com/"), request.uri());
        Assert.assertEquals(1, request.getProcessors().size());
        Assert.assertNotNull(request.diskCache());
        Assert.assertNotNull(request.memoryCache());
        Assert.assertTrue(request.isSkipReadingDiskCache());
        Assert.assertTrue(request.isSkipWritingMemoryCache());
        Assert.assertTrue(request.isSkipReadingMemoryCache());
        Assert.assertNotNull(request.target());
        Assert.assertNotNull(request.getResultKey());
        Assert.assertNotNull(request.getSourceKey());
        Assert.assertNotNull(request.urlFactory());
        Assert.assertNotNull(request.keyMaker());
        Assert.assertNull(request.outPadding());
        Assert.assertEquals(DiskCacheStrategy.RESULT, request.diskCacheStrategy());

        request = new MirageRequest();
        request.uri(Uri.parse("http://www.google.com/"))
                .into(new ImageView(RuntimeEnvironment.application));

        Assert.assertEquals(Uri.parse("http://www.google.com/"), request.uri());
        Assert.assertNull(request.getProcessors());
        Assert.assertNull(request.executor());
        Assert.assertNull(request.diskCache());
        Assert.assertNull(request.memoryCache());
        Assert.assertNotNull(request.keyMaker());


        request = new MirageRequest();
        request.uri(Uri.parse("http://www.google.com/"))
                .keyMaker(new SimpleKeyMaker())
                .executor(AsyncTask.THREAD_POOL_EXECUTOR)
                .into(new ImageView(RuntimeEnvironment.application));

        Assert.assertEquals(Uri.parse("http://www.google.com/"), request.uri());
        Assert.assertNotNull(request.executor());
        Assert.assertNotNull(request.getResultKey());
        Assert.assertNotNull(request.getSourceKey());
        Assert.assertNull(request.urlFactory());
        Assert.assertNull(request.options());
    }

    @Test
    public void testInSampleSize() {
        MirageRequest request = new MirageRequest();
        request.inSampleSize(4);
        Assert.assertEquals(4, request.options().inSampleSize);
    }

    @Test
    public void testResizeOnInvalidParams() {
        MirageRequest request = new MirageRequest();
        try {
            request.resize(0, 200);
            Assert.fail("This should throw an error");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

        try {
            request.resize(300, 0);
            Assert.fail("This should throw an error");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testResizeReset() {
        MirageRequest request = new MirageRequest();
        request.resize(200, 200, ResizeProcessor.STRATEGY_SCALE_FREE);
        request.recycle();
        request.resize(200, 200, ResizeProcessor.STRATEGY_SCALE_FREE);
    }


}