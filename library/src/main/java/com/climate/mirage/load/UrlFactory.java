package com.climate.mirage.load;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

public interface UrlFactory {

	InputStream getStream(Uri uri) throws IOException;

}
