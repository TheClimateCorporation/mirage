package com.climate.mirage.load;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface BitmapProvider {

    Bitmap load() throws IOException;

    String id();

}
