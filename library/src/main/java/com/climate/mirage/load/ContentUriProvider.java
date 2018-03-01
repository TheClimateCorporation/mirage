package com.climate.mirage.load;

import android.content.Context;
import android.graphics.Bitmap;

import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.tasks.MirageTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

public class ContentUriProvider implements StreamProvider {

    private final MirageRequest request;
    private final Context context;

    public ContentUriProvider(Context context, MirageRequest request) {
        this.request = request;
        this.context = context;
    }

    @Override
    public InputStream load() throws IOException {
        InputStream in = context.getContentResolver().openInputStream(request.uri());
        return in;
    }
}