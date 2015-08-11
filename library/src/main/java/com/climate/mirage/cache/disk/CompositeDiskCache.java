package com.climate.mirage.cache.disk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompositeDiskCache implements DiskCache {

	private List<DiskCache> leafs;

	public CompositeDiskCache(DiskCache... caches) {
		synchronized (this) {
			leafs = new ArrayList<>();
			for (int i=0; i<caches.length; i++) {
				leafs.add(caches[i]);
			}
		}
	}

	@Override
	public File get(String key) {
		synchronized (this) {
			for (int i=0; i<leafs.size(); i++) {
				DiskCache cache = leafs.get(i);
				File file = cache.get(key);
				if (file != null) {
					return file;
				}
			}
		}
		return null;
	}

	@Override
	public void put(String key, Writer writer) {
		synchronized (this) {
			for (int i=0; i<leafs.size(); i++) {
				DiskCache cache = leafs.get(i);
				cache.put(key, writer);
			}
		}
	}

	@Override
	public void delete(String key) {
		synchronized (this) {
			for (int i=0; i<leafs.size(); i++) {
				DiskCache cache = leafs.get(i);
				cache.delete(key);
			}
		}
	}

	@Override
	public void clear() {
		synchronized (this) {
			for (int i=0; i<leafs.size(); i++) {
				DiskCache cache = leafs.get(i);
				cache.clear();
			}
		}
	}
}
