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

	public void addLoadError(String id, LoadError error) {
		synchronized (errors) {
			errors.put(id, error);
		}
	}

	public void addLoadError(String id, Exception exception, Mirage.Source source) {
		addLoadError(id, loadErrorFactory.createErrorLog(id, exception, source));
	}

	public LoadError getLoadError(String id) {
		synchronized (errors) {
			LoadError error = errors.get(id);
			return error;
		}
	}

	public void removeLoadError(String id) {
		synchronized (errors) {
			errors.remove(id);
		}
	}

}