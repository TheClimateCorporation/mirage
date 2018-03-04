package com.climate.mirage.tasks;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.RobolectricTest;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.cache.memory.MemoryCache;
import com.climate.mirage.load.SimpleUrlConnectionFactory;
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
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class MirageTaskTest extends RobolectricTest {

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
    public void testCancels() {
        Bitmap bm = Bitmap.createBitmap(600, 400, Bitmap.Config.ARGB_8888);
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(toByteArray(bm)));
        Target<Bitmap> target = Mockito.mock(Target.class);
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);

        MirageRequest request = Mockito.mock(MirageRequest.class);
        Mockito.when(request.urlFactory()).thenReturn(new SimpleUrlConnectionFactory());
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(false);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(false);
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(null);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.RESULT);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.parse(baseUrl.toString()));
        Mockito.when(request.target()).thenReturn(target);
        LoadErrorManager manager = Mockito.mock(LoadErrorManager.class);

        MirageTask.Callback<Bitmap> callback = Mockito.mock(MirageTask.Callback.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Bitmap answer(InvocationOnMock invocation) throws Throwable {
                wait.set(false);
                Assert.fail("Task should have been canceled");
                return null;
            }
        }).when(callback).onPostExecute(
                (MirageTask)Mockito.any(),
                (MirageRequest)Mockito.any(),
                (Bitmap)Mockito.any() );

        Mockito.doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                wait.set(false);
                return null;
            }
        }).when(callback).onCancel(
                (MirageTask) Mockito.any(),
                (MirageRequest) Mockito.any());

        final BitmapTask task = new BitmapTask(null, request, manager, callback);
        Mockito.when(memoryCache.get(Mockito.eq("123"))).then(new Answer<Bitmap>() {
            @Override
            public Bitmap answer(InvocationOnMock invocation) throws Throwable {
                while (!task.isCancelled()) { }
                return Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            }
        });

        task.cancel(true);
        task.onCancelled(null);
        waitForIt();
        Mockito.verify(callback, Mockito.times(1)).onCancel((MirageTask) Mockito.any(),
                (MirageRequest) Mockito.any());
        Mockito.verify(target, Mockito.times(0)).onResult(
                (Bitmap) Mockito.any(), (Mirage.Source) Mockito.any(), Mockito.eq(request));
        Mockito.verify(target, Mockito.times(0)).onError(
                (Exception) Mockito.any(), (Mirage.Source) Mockito.any(), Mockito.eq(request)
        );
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