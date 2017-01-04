package com.climate.mirage.cache.disk;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The default DiskCache implementation.
 *
 */
public class DiskLruCacheWrapper implements DiskCache {

	public static interface DiskLruCacheFactory {
		public DiskLruCache getDiskCache() throws IOException;
		public void resetDiskCache();
	}

	public static class SharedDiskLruCacheFactory implements DiskLruCacheFactory {
		private static Map<String, DiskLruCache> wrappers;
		private static final int APP_VERSION = 1;
		private static final int VALUE_COUNT = 1;

		static {
			wrappers = Collections.synchronizedMap(new HashMap<String, DiskLruCache>());
		}

		private File directory;
		private int maxSize;

		public SharedDiskLruCacheFactory(File directory, int maxSize) {
			this.directory = directory;
			this.maxSize = maxSize;
		}

		@Override
		public DiskLruCache getDiskCache() throws IOException{
			if (wrappers.containsKey(directory.getAbsolutePath())) {
				return wrappers.get(directory.getAbsolutePath());
			} else {
				if (!directory.exists()) {
					directory.mkdirs();
					boolean made = directory.isDirectory();
				}
				DiskLruCache diskLruCache = DiskLruCache.open(directory, APP_VERSION, VALUE_COUNT, maxSize);
				wrappers.put(directory.getAbsolutePath(), diskLruCache);
				return diskLruCache;
			}
		}

		@Override
		public void resetDiskCache() {
			if (wrappers.containsKey(directory.getAbsolutePath())) {
				wrappers.remove(directory.getAbsolutePath());
			}
		}
	}

	private static final String TAG = DiskLruCacheWrapper.class.getSimpleName();
	private DiskLruCacheFactory diskLruCacheFactory;
	volatile private boolean isReadOnly = false;

    public DiskLruCacheWrapper(DiskLruCacheFactory diskLruCacheFactory) {
        this.diskLruCacheFactory = diskLruCacheFactory;
    }

	/**
	 * Allows this cache to toggle between read &amp; write or just read-only mode. This is
	 * useful for when data is being read from an offline sync cache, but we do not want to
	 * add more data to the cache if it's read it from online.
	 *
	 * @param isReadOnly true if cache is read-only mode
	 */
	synchronized public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	@Override
    public File get(String key) {
        String safeKey = key;
        File result = null;
        try {
            //It is possible that the there will be a put in between these two gets. If so that shouldn't be a problem
            //because we will always put the same value at the same key so our input streams will still represent
            //the same data
            final DiskLruCache.Value value = getDiskCache().get(safeKey);
            if (value != null) {
                result = value.getFile(0);
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to get from disk cache", e);
        }
        return result;
    }

    @Override
    public void put(String key, Writer writer) {
		if (isReadOnly) return;
        String safeKey = key;
        try {
            DiskLruCache.Editor editor = getDiskCache().edit(safeKey);
            // Editor will be null if there are two concurrent puts. In the worst case we will just silently fail.
            if (editor != null) {
                try {
                    File file = editor.getFile(0);
                    if (writer.write(file)) {
                        editor.commit();
                    }
                } finally {
                    editor.abortUnlessCommitted();
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Unable to put to disk cache", e);
        }
    }

    @Override
    public void delete(String key) {
        String safeKey = key;
        try {
            getDiskCache().remove(safeKey);
        } catch (IOException e) {
            Log.w(TAG, "Unable to delete from disk cache", e);
        }
    }

    @Override
    public synchronized void clear() {
        try {
            getDiskCache().delete();
            resetDiskCache();
        }  catch (IOException e) {
            Log.w(TAG, "Unable to clear disk cache", e);
        }
    }

	private synchronized DiskLruCache getDiskCache() throws IOException {
		return diskLruCacheFactory.getDiskCache();
	}

	private synchronized void resetDiskCache() {
		diskLruCacheFactory.resetDiskCache();
	}
}
