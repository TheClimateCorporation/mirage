package com.climate.mirage.tasks;

import android.graphics.Bitmap;
import android.net.Uri;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.RoboManifestRunner;
import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.cache.memory.MemoryCache;
import com.climate.mirage.load.SimpleUrlConnectionFactory;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.targets.Target;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class BitmapFileTaskTest {

    private AtomicBoolean wait;
    private File testingFile;

    @Before
    public void setUp() throws IOException {
        if (wait == null) wait = new AtomicBoolean();
        wait.set(true);
        Bitmap bitmap = Bitmap.createBitmap(600, 200, Bitmap.Config.ARGB_8888);
        testingFile = new File(RuntimeEnvironment.application.getCacheDir(), "someImage.jpg");
        FileOutputStream out = new FileOutputStream(testingFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.close();
    }

    @After
    public void tearDown() throws IOException {
        if (testingFile != null) {
            testingFile.delete();
        }
    }

    @Test
    public void testGetFromFile() {
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
        Mockito.when(request.uri()).thenReturn(Uri.fromFile(testingFile));
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

        BitmapFileTask task = new BitmapFileTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(target, Mockito.times(0)).onError(
                (Exception) Mockito.any(), (Mirage.Source) Mockito.any(),
                (MirageRequest) Mockito.any());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), (Mirage.Source) Mockito.any(),
                (MirageRequest) Mockito.any());
    }

    @Test
    public void testGetFromFile_saveSource() {
        MemoryCache<String, Bitmap> memoryCache = Mockito.mock(MemoryCache.class);
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
        Mockito.when(request.isSkipReadingDiskCache()).thenReturn(false);
        Mockito.when(request.isSkipReadingMemoryCache()).thenReturn(false);
        Mockito.when(request.isSkipWritingMemoryCache()).thenReturn(false);
        Mockito.when(request.memoryCache()).thenReturn(memoryCache);
        Mockito.when(request.diskCache()).thenReturn(diskCache);
        Mockito.when(request.diskCacheStrategy()).thenReturn(DiskCacheStrategy.SOURCE);
        Mockito.when(request.getResultKey()).thenReturn("123");
        Mockito.when(request.getSourceKey()).thenReturn("1234");
        Mockito.when(request.uri()).thenReturn(Uri.fromFile(testingFile));
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

        BitmapFileTask task = new BitmapFileTask(null, request, manager, callback);
        task.execute(); // can not executeOnExecutor or the text will never finish
        waitForIt();
        Mockito.verify(target, Mockito.times(0)).onError(
                (Exception) Mockito.any(), (Mirage.Source) Mockito.any(),
                (MirageRequest) Mockito.any());
        Mockito.verify(target, Mockito.times(1)).onResult(
                (Bitmap) Mockito.any(), (Mirage.Source) Mockito.any(),
                (MirageRequest) Mockito.any());
        Mockito.verify(diskCache, Mockito.times(1)).put(
                Mockito.eq("1234"),
                (DiskCache.Writer) Mockito.anyObject());
        Mockito.verify(memoryCache, Mockito.times(1)).put(
                Mockito.eq("123"),
                (Bitmap)Mockito.anyObject());
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