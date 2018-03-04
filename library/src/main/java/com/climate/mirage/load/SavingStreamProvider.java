package com.climate.mirage.load;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.writers.InputStreamWriter;
import com.climate.mirage.exceptions.MirageOomException;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.utils.IOUtils;
import com.climate.mirage.utils.MathUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

abstract public class SavingStreamProvider implements BitmapProvider {

    private final MirageRequest request;

    public SavingStreamProvider(MirageRequest request) {
        this.request = request;
    }

    abstract protected InputStream stream() throws IOException;

    @Override
    public Bitmap load() throws IOException {
        Bitmap bitmap;
        try {
            bitmap = loadBitmap();
        } catch (OutOfMemoryError e) {
            if (request.memoryCache() != null) request.memoryCache().clear();
            System.gc();
            try {
                bitmap = loadBitmap();
            } catch (OutOfMemoryError e2) {
                throw e2;
            }
        }
        return bitmap;
    }

    private Bitmap loadBitmap() throws IOException {
        if (request.isInSampleSizeDynamic()) {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            InputStream in = stream();
            BitmapFactory.decodeStream(in, null, opts);
            int sampleSize = determineSampleSize(opts);
            request.inSampleSize(sampleSize);
        }

        // if we need to keep the source, stream it directly to a file
        // we can't load it to memory first and then write to file because
        // there could be bitmap options on the stream.
        InputStream in = stream();
        Bitmap bitmap = null;
        if (request.isRequestShouldSaveSource()) {
            request.diskCache().put(request.getSourceKey(), new InputStreamWriter(in));
            IOUtils.close(in);
            File file = request.diskCache().get(request.getSourceKey());
            if (file != null) {
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), request.options());
            }
        } else {
            bitmap = BitmapFactory.decodeStream(in, request.outPadding(), request.options());
            IOUtils.close(in);
        }

        return bitmap;
    }

    @Override
    public String id() {
        return request.uri().toString();
    }

    private int determineSampleSize(BitmapFactory.Options outOpts) {
        int sampleSize  = MathUtils.determineSampleSize(outOpts.outWidth, outOpts.outHeight,
                request.getResizeTargetDimen(), request.isResizeSampleUndershoot());
        return sampleSize;
    }
}