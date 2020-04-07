package com.climate.mirage.load;

import android.graphics.Bitmap;
import android.net.Uri;

import com.climate.mirage.RobolectricTest;
import com.climate.mirage.cache.SimpleKeyMaker;
import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.cache.memory.MemoryCache;
import com.climate.mirage.requests.MirageRequest;

import junit.framework.Assert;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SavingStreamProviderTest extends RobolectricTest {

    @Test
    public void testLoads_doesNotSaveSource() throws IOException {
        MirageRequest request = mock(MirageRequest.class);
        UrlFactory factory = mock(UrlFactory.class);
        InputStream inputStream = mock(InputStream.class);
        when(factory.getStream(any(Uri.class))).thenReturn(inputStream);
        when(request.urlFactory()).thenReturn(factory);
        when(request.uri()).thenReturn(Uri.parse("http://www.sample.com/"));

        DiskCache diskCache = mock(DiskCache.class);
        MemoryCache<String, Bitmap> memCache = mock(MemoryCache.class);
        when(request.diskCache()).thenReturn(diskCache);
        when(request.memoryCache()).thenReturn(memCache);

        SavingStreamProvider provider = new SavingStreamProvider(request) {
            @Override
            protected InputStream stream() throws IOException {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Bitmap bitmap = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
                return bs;
            }
        };
        when(request.provider()).thenReturn(provider);

        Bitmap bitmap = provider.load();
        Assert.assertNotNull(bitmap);

        verify(diskCache, times(0)).put(anyString(), any(DiskCache.Writer.class));
    }

    @Test
    public void testLoads_savesSource() throws IOException {
        MirageRequest request = mock(MirageRequest.class);
        UrlFactory factory = mock(UrlFactory.class);
        InputStream inputStream = mock(InputStream.class);
        when(factory.getStream(any(Uri.class))).thenReturn(inputStream);
        when(request.urlFactory()).thenReturn(factory);
        when(request.uri()).thenReturn(Uri.parse("http://www.sample.com/"));
        when(request.isRequestShouldSaveSource()).thenReturn(true);
        when(request.keyMaker()).thenReturn(new SimpleKeyMaker());

        DiskCache diskCache = mock(DiskCache.class);
        MemoryCache<String, Bitmap> memCache = mock(MemoryCache.class);
        when(request.diskCache()).thenReturn(diskCache);
        when(request.memoryCache()).thenReturn(memCache);

        SavingStreamProvider provider = new SavingStreamProvider(request) {
            @Override
            protected InputStream stream() throws IOException {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Bitmap bitmap = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
                return bs;
            }
        };
        when(request.provider()).thenReturn(provider);

        Bitmap bitmap = provider.load();

        verify(diskCache, times(1)).put(anyString(), any(DiskCache.Writer.class));
        verify(diskCache, times(1)).get(anyString());
    }

    @Test
    public void testUsesBitmapOptions() throws IOException {
        MirageRequest request = mock(MirageRequest.class);
        UrlFactory factory = mock(UrlFactory.class);
        InputStream inputStream = mock(InputStream.class);
        when(factory.getStream(any(Uri.class))).thenReturn(inputStream);
        when(request.urlFactory()).thenReturn(factory);
        when(request.uri()).thenReturn(Uri.parse("http://www.sample.com/"));
        when(request.isRequestShouldSaveSource()).thenReturn(true);
        when(request.keyMaker()).thenReturn(new SimpleKeyMaker());
        when(request.isInSampleSizeDynamic()).thenReturn(true);
        when(request.getResizeTargetDimen()).thenReturn(50);
        when(request.isResizeSampleUndershoot()).thenReturn(true);

        DiskCache diskCache = mock(DiskCache.class);
        MemoryCache<String, Bitmap> memCache = mock(MemoryCache.class);
        when(request.diskCache()).thenReturn(diskCache);
        when(request.memoryCache()).thenReturn(memCache);

        SavingStreamProvider provider = new SavingStreamProvider(request) {
            @Override
            protected InputStream stream() throws IOException {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                Bitmap bitmap = Bitmap.createBitmap(400, 600, Bitmap.Config.ARGB_8888);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
                return bs;
            }
        };
        when(request.provider()).thenReturn(provider);
        provider.load();

        verify(request, times(1)).inSampleSize(eq(2));
    }

}