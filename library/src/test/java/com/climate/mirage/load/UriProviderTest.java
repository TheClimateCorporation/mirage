package com.climate.mirage.load;

import android.net.Uri;

import com.climate.mirage.Mirage;
import com.climate.mirage.RobolectricTest;
import com.climate.mirage.requests.MirageRequest;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UriProviderTest extends RobolectricTest {

    @Test
    public void testLoads() throws IOException {
        MirageRequest request = mock(MirageRequest.class);
        UrlFactory factory = mock(UrlFactory.class);
        URLConnection conn = mock(URLConnection.class);
        when(factory.getConnection(any(Uri.class))).thenReturn(conn);
        when(request.urlFactory()).thenReturn(factory);
        UriProvider provider = new UriProvider(request);
        when(request.provider()).thenReturn(provider);
        when(request.uri()).thenReturn(Uri.parse("http://www.sample.com/"));

        InputStream stream = provider.stream();
        Assert.assertNotNull(stream);

    }

}