package com.climate.mirage;

import android.net.Uri;
import android.support.v4.util.LruCache;

import com.climate.mirage.errors.LoadError;
import com.climate.mirage.errors.LoadErrorFactory;
import com.climate.mirage.errors.TimedErrorFactory;

public class LoadErrorManager {

	private LoadErrorFactory loadErrorFactory;
	private LruCache<String, LoadError> errors;

	public LoadErrorManager(LoadErrorFactory loadErrorFactory) {
		this.loadErrorFactory = loadErrorFactory;
		if (loadErrorFactory == null) this.loadErrorFactory = new TimedErrorFactory();
		errors = new LruCache<>(200);
	}

	public LoadErrorManager() {
		this(null);
	}

	public void addLoadError(Uri uri, LoadError error) {
		synchronized (errors) {
			errors.put(uri.toString(), error);
		}
	}

	public void addLoadError(Uri uri, Exception exception, Mirage.Source source) {
		addLoadError(uri, loadErrorFactory.createErrorLog(uri.toString(), exception, source));
	}

	public LoadError getLoadError(Uri uri) {
		synchronized (errors) {
			LoadError error = errors.get(uri.toString());
			return error;
		}
	}

	public void removeLoadError(Uri uri) {
		synchronized (errors) {
			errors.remove(uri.toString());
		}
	}

}