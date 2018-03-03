package com.climate.mirage.load;

import android.net.Uri;

import com.climate.mirage.RobolectricTest;
import com.climate.mirage.requests.MirageRequest;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileProviderTest extends RobolectricTest {

    @Test
    public void testLoads() throws IOException {
        MirageRequest request = mock(MirageRequest.class);
        UrlFactory factory = mock(UrlFactory.class);
        URLConnection conn = mock(URLConnection.class);
        when(factory.getConnection(any(Uri.class))).thenReturn(conn);
        when(request.urlFactory()).thenReturn(factory);
        FileProvider provider = new FileProvider(request);
        when(request.provider()).thenReturn(provider);
        File file = File.createTempFile("prefix", "suffix");
        when(request.uri()).thenReturn(Uri.fromFile(file));

        InputStream stream = provider.stream();

        Assert.assertNotNull(stream);
        verify(request, times(1)).uri();
    }

}