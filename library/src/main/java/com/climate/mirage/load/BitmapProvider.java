package com.climate.mirage.load;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;

public interface BitmapProvider {

    Bitmap load() throws IOException;

}
