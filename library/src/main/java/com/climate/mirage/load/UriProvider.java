package com.climate.mirage.load;

import com.climate.mirage.requests.MirageRequest;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class UriProvider extends SavingStreamProvider {

    private final MirageRequest request;
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    public UriProvider(MirageRequest request) {
        super(request);
        this.request = request;
    }

    @Override
    protected InputStream stream() throws IOException {
        URLConnection connection = getConnection();
        InputStream in = new BufferedInputStream(connection.getInputStream(), IO_BUFFER_SIZE);
        return in;
    }

    private URLConnection getConnection() throws IOException {
        return request.urlFactory().getConnection(request.uri());
    }
}