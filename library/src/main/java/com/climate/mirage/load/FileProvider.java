package com.climate.mirage.load;

import com.climate.mirage.requests.MirageRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileProvider implements StreamProvider {

    private MirageRequest request;
    private File file;

    public FileProvider(MirageRequest request) {
        this.request = request;
    }

    public FileProvider(File file) {
        this.file = file;
    }

    @Override
    public InputStream load() throws IOException {
        File f = file != null ? file : new File(request.uri().getPath());
        InputStream in = new FileInputStream(f);
        return in;
    }
}
