package com.climate.mirage.load;

import android.content.Context;
import android.net.Uri;

import com.climate.mirage.RobolectricTest;
import com.climate.mirage.requests.MirageRequest;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContentUriProviderTest extends RobolectricTest {

    @Test
    public void testLoads() throws IOException {
        Context context = spy(getApp());

        MirageRequest request = mock(MirageRequest.class);
        UrlFactory factory = mock(UrlFactory.class);
        InputStream inputStream = mock(InputStream.class);
        when(factory.getStream(any(Uri.class))).thenReturn(inputStream);
        when(request.urlFactory()).thenReturn(factory);
        ContentUriProvider provider = new ContentUriProvider(context, request);
        when(request.provider()).thenReturn(provider);
        when(request.uri()).thenReturn(Uri.parse("http://www.sample.com/"));

        provider.load();

        verify(context, times(1)).getContentResolver();
    }
}