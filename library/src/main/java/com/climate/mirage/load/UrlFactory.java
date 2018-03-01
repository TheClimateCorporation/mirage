package com.climate.mirage.load;

import android.net.Uri;

import java.io.IOException;
import java.net.URLConnection;

public interface UrlFactory {

	URLConnection getConnection(Uri uri) throws IOException;

}
