package com.climate.mirage.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.cache.disk.writers.BitmapWriter;
import com.climate.mirage.cache.disk.writers.InputStreamWriter;
import com.climate.mirage.errors.LoadError;
import com.climate.mirage.exceptions.MirageException;
import com.climate.mirage.exceptions.MirageIOException;
import com.climate.mirage.exceptions.MirageOomException;
import com.climate.mirage.processors.BitmapProcessor;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.utils.IOUtils;
import com.climate.mirage.utils.MathUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.List;

abstract public class BitmapTask extends MirageTask<Void, Void, Bitmap> {

	private Mirage mirage;
	private MirageRequest request;
	private LoadErrorManager loadErrorManager;
	private Mirage.Source source;

	/**
	 *
	 * @param mirage
	 * @param request
	 * @param loadErrorManager
	 * @param callback This callback should only be used to know when the task has
	 *                 completed or canceled. If you want to do something with the result
	 *                 use the callback in request
	 */
	public BitmapTask(Mirage mirage, MirageRequest request,
					  LoadErrorManager loadErrorManager,
					  Callback<Bitmap> callback) {
		super(request, callback);
		this.mirage = mirage;
		this.request = request;
		this.loadErrorManager = loadErrorManager;
	}

	@Override
	public Bitmap doTask(Void... params) throws MirageIOException {
		Bitmap bitmap = null;

		// check to see if we have it in our memory cache
		// memory cache will only store the processed version
		if (!isCancelled()) {
			bitmap = getFromMemCache();
			if (bitmap != null) {
				source = Mirage.Source.MEMORY;
				// add to the disk cache while here
				// at this point, we don't care if the task has been canceled
				if (request.diskCache() != null) {
					putResultInDiskCache(bitmap);
				}
				return bitmap;
			}
		}

		// check to see if we have the source or the processed version in our disk cache
		if (!isCancelled() && !request.isSkipReadingDiskCache()) {
            bitmap = getFromResultDiskCache();

            // if there is not a cached copy in the result disk cache, look
            // to see if there's a copy in the source disk cache
            if (bitmap == null) {
                boolean isInCache = isInFileCache(request.getSourceKey());
                if (request.isInSampleSizeDynamic() && isInCache) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    getFromDiskCache(request.getSourceKey(), opts);
                    int sampleSize = determineSampleSize(opts);
                    request.inSampleSize(sampleSize);
                }

                if (isInCache) {
                    bitmap = getFromSourceDiskCache();
                    if (bitmap != null) bitmap = applyProcessors(bitmap);
                }
			}

			if (bitmap != null) {
				source = Mirage.Source.DISK;
				putInMemCache(bitmap);
				return bitmap;
			}
		}

		// before we go to the external source, check to see if this uri is in our
		// error log
		if (!isCancelled()) {
			// make sure the url isn't in the cached errors
			LoadError loadError = loadErrorManager.getLoadError(request.uri());
			if (loadError != null) {
				if (loadError.isValid()) {
					throw new MirageIOException(Mirage.Source.MEMORY, loadError.getException());
				} else {
					loadErrorManager.removeLoadError(request.uri());
				}
			}
		}

		// we have to get this image from the network, let's go!
		if (!isCancelled()) {
			try {
                if (request.isInSampleSizeDynamic()) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    InputStream in = createInputStreamForExternal();
                    BitmapFactory.decodeStream(in, null, opts);
                    int sampleSize = determineSampleSize(opts);
                    request.inSampleSize(sampleSize);
                }
				if (isCancelled() || Thread.interrupted()) return null;
				bitmap = loadFromExternal();
				if (isCancelled() || Thread.interrupted()) return null;
				if (bitmap != null) bitmap = applyProcessors(bitmap);
				if (isCancelled()) return null;
			} catch (InterruptedIOException e) {
				return null; // no error we just stopped loading it
			} catch (IOException e) {
				loadErrorManager.addLoadError(request.uri(), e, Mirage.Source.EXTERNAL);
				throw new MirageIOException(Mirage.Source.EXTERNAL, e);
			} catch (Exception e) {
				loadErrorManager.addLoadError(request.uri(), e, Mirage.Source.EXTERNAL);
				throw new MirageIOException(Mirage.Source.EXTERNAL, e);
			}

			if (bitmap != null && !Thread.interrupted()) {
				source = Mirage.Source.EXTERNAL;
				putInMemCache(bitmap);
				if (request.diskCacheStrategy() == DiskCacheStrategy.RESULT) {
					putResultInDiskCache(bitmap);
				} else if (request.diskCacheStrategy() == DiskCacheStrategy.ALL) {
					if (!request.getResultKey().equals(request.getSourceKey())) {
						putResultInDiskCache(bitmap);
					}
				}
				// saving the source is taken care of during loading
                // since it must directly write to the file
			}
		}

		return !isCancelled() ? bitmap : null;
	}

	// FIXME: this probably doesn't handle if the desered size is larger than the
	// server size. It might go negative. Fix this
    private int determineSampleSize(BitmapFactory.Options outOpts) {
        int dimen = Math.max(outOpts.outWidth, outOpts.outHeight);
        float div = dimen / (float)request.getResizeTargetDimen();
        if (request.isResizeSampleUndershoot()) {
            int sampleSize = MathUtils.upperPowerof2((int)div);
            return sampleSize;
        } else {
            int sampleSize = MathUtils.lowerPowerOf2((int)div);
            return sampleSize;
        }
    }

    abstract protected InputStream createInputStreamForExternal() throws IOException;

    private Bitmap loadFromExternal() throws IOException {
        Bitmap bitmap = null;
        InputStream in = createInputStreamForExternal();

        // if we need to keep the source, stream it directly to a file
        // we can't load it to memory first and then write to file because
        // there could be bitmap options on the stream.
        if (isSaveSource()) {
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

    private boolean isSaveSource() {
        return request.isRequestShouldSaveSource();
    }

	@Override
	protected void onPostSuccess(Bitmap bitmap) {
		if (request.target() != null) request.target().onResult(bitmap, source, request);
	}

	@Override
	protected void onPostError(Exception exception) {
		if (exception instanceof MirageException) {
			source = ((MirageException)exception).getSource();
		}
		if (request.target() != null) request.target().onError(exception, source, request);
	}


    // FIXME: So many methods here about the cache, can i clean it up any? 
    private Bitmap getFromMemCache() {
		if (request.memoryCache() != null && !request.isSkipReadingMemoryCache()) {
			return request.memoryCache().get(request.getResultKey());
		} else {
			return null;
		}
	}

    private void putInMemCache(Bitmap bitmap) {
		if (isCancelled()) return;
		if (request.memoryCache() != null && !request.isSkipWritingMemoryCache()) {
			request.memoryCache().put(request.getResultKey(), bitmap);
		}
	}

    private Bitmap getFromSourceDiskCache() throws MirageOomException {
		return getFromDiskCache(true);
	}

    private Bitmap getFromResultDiskCache() throws MirageOomException {
		return getFromDiskCache(false);
	}

    private boolean isInFileCache(String key) {
        if (request.diskCache() == null || request.isSkipReadingDiskCache()) return false;
        File file = request.diskCache().get(key);
        return file != null;
    }

	private void putResultInDiskCache(Bitmap bitmap) {
		if (isCancelled()) return;
		if (request.diskCache() != null) {
			if (request.diskCacheStrategy() == DiskCacheStrategy.RESULT
					|| request.diskCacheStrategy() == DiskCacheStrategy.ALL) {
				request.diskCache().put(request.getResultKey(), new BitmapWriter(bitmap));
			}
		}
	}

    private Bitmap getFromDiskCache(String key, @Nullable BitmapFactory.Options opts) {
        if (request.diskCache() == null || request.isSkipReadingDiskCache()) return null;
        File file = request.diskCache().get(key);
        if (file != null) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                return bitmap;
            } catch (OutOfMemoryError e) {
                if (request.memoryCache() != null) {
                    request.memoryCache().clear();
                }
                System.gc();
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                    return bitmap;
                } catch (OutOfMemoryError e2) {
                    throw new MirageOomException(Mirage.Source.DISK);
                }
            }
        } else {
            return null;
        }
    }

	private Bitmap getFromDiskCache(boolean fromSource) throws MirageOomException {
		if (request.diskCache() == null || request.isSkipReadingDiskCache()) return null;
		String key = fromSource ? request.getSourceKey() : request.getResultKey();
		BitmapFactory.Options options = fromSource ? request.options() : null;
        return getFromDiskCache(key, options);
	}

	private Bitmap applyProcessors(Bitmap bitmap) {
		List<BitmapProcessor> processors = request.getProcessors();
		// processors is a synchronized list
		if (processors != null) {
			for (int i=0; i<processors.size(); i++) {
				bitmap = processors.get(i).process(bitmap);
			}

		}

		return bitmap;
	}
}