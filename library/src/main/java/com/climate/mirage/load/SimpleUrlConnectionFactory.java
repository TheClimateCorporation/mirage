package com.climate.mirage.load;

import android.net.Uri;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

public class SimpleUrlConnectionFactory implements UrlFactory {

	private static final int MAX_REDIRECTS = 5;

	private Map<String, String> requestProps;

	public SimpleUrlConnectionFactory() {

	}

	public SimpleUrlConnectionFactory(Map<String, String> requestProps) {
		this.requestProps = requestProps;
	}

	public InputStream getStream(Uri uri) throws IOException {
		return createConnection(uri, null, 0);
	}

	private InputStream createConnection(Uri uri, Uri lastUri, int numRedirects) throws IOException {
		if (numRedirects >= MAX_REDIRECTS) {
			throw new IOException("Max number of directs");
		} else if (lastUri != null && uri.toString().equals(lastUri.toString())) {
			throw new IOException("Infinite redirect loop");
		}

		URL u = new URL(uri.toString());
		HttpURLConnection conn = (HttpURLConnection)u.openConnection();
		conn.setConnectTimeout(2500);
		conn.setReadTimeout(2500);
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setInstanceFollowRedirects(false);

		if (requestProps != null) {
			Iterator<Map.Entry<String, String>> it = requestProps.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = it.next();
				if (!entry.getKey().equals("Authorization")) {
					conn.addRequestProperty(entry.getKey(), entry.getValue());
				} else if ( lastUri == null || lastUri.getHost().equals(uri.getHost()) ) {
					conn.addRequestProperty(entry.getKey(), entry.getValue());
				}
			}
		}

		conn.connect();
		final int statusCode = conn.getResponseCode();
		if (statusCode / 100 == 2) {
			return conn.getInputStream();
		} else if (statusCode / 100 == 3) {
			String redirectUrlString = conn.getHeaderField("Location");
			if (TextUtils.isEmpty(redirectUrlString)) {
				throw new IOException("Received empty or null redirect url");
			}
			return createConnection(Uri.parse(redirectUrlString), uri, ++numRedirects);
		} else {
			if (statusCode == -1) {
				throw new IOException("Unable to retrieve response code from HttpUrlConnection.");
			}
			throw new IOException("Request failed " + statusCode + ": "
					+ conn.getResponseMessage());
		}
	}
}
