package com.climate.mirage.load;

import com.climate.mirage.requests.MirageRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileProvider extends SavingStreamProvider {

    private MirageRequest request;

    public FileProvider(MirageRequest request) {
        super(request);
        this.request = request;
    }

    @Override
    protected InputStream stream() throws IOException {
        File f = new File(request.uri().getPath());
        InputStream in = new FileInputStream(f);
        return in;
    }
}
