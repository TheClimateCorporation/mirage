package com.climate.mirage.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.climate.mirage.LoadErrorManager;
import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.cache.disk.writers.BitmapWriter;
import com.climate.mirage.cache.disk.writers.InputStreamWriter;
import com.climate.mirage.exceptions.MirageIOException;
import com.climate.mirage.processors.BitmapProcessor;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.utils.IOUtils;
import com.climate.mirage.utils.MathUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class BitmapDownloadTask extends MirageTask<Void, Void, File> {

	private Mirage mirage;
	private MirageRequest request;
	private static final int IO_BUFFER_SIZE = 8 * 1024;
	private LoadErrorManager loadErrorManager;
	private Mirage.Source source;

	public BitmapDownloadTask(Mirage mirage, MirageRequest request,
							  LoadErrorManager loadErrorManager,
							  Callback<File> callback) {
		super(request, callback);
		this.mirage = mirage;
		this.request = request;
		this.loadErrorManager = loadErrorManager;
	}

	@Override
	public File doTask(Void... params) throws MirageIOException {
		// check to see if we have the source or the processed version in our disk cache
		if (!isCancelled() && !request.isSkipReadingDiskCache()) {
			if (request.diskCacheStrategy() == DiskCacheStrategy.ALL) {
				File sourceFile = request.diskCache().get(request.getSourceKey());
				File resultFile = request.diskCache().get(request.getResultKey());
				if (sourceFile != null && resultFile == null) {
                    if (request.isInSampleSizeDynamic()) {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(sourceFile.getAbsolutePath(), opts);
                        int sampleSize = determineSampleSize(opts);
                        request.inSampleSize(sampleSize);
                    }

					Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath(), request.options());
					bitmap = applyProcessors(bitmap);
					putResultInDiskCache(bitmap);
					bitmap.recycle();
					resultFile = getFromCache(request.getResultKey());
				}
				source = Mirage.Source.DISK;
				if (resultFile != null) return resultFile;
			} else if (request.diskCacheStrategy() == DiskCacheStrategy.SOURCE) {
				File sourceFile = getFromCache(request.getSourceKey());
				source = Mirage.Source.DISK;
				if (sourceFile != null) return sourceFile;
			} else if (request.diskCacheStrategy() == DiskCacheStrategy.RESULT) {
				File resultFile = getFromCache(request.getResultKey());
				source = Mirage.Source.DISK;
				if (resultFile != null) return resultFile;
			}
		}

		// we have to get this image from the network, let's go!
		if (!isCancelled()) {
			source = Mirage.Source.EXTERNAL;
			if (request.diskCacheStrategy() == DiskCacheStrategy.SOURCE
					|| (request.getSourceKey().equals(request.getResultKey())
					&& request.diskCacheStrategy() != DiskCacheStrategy.NONE)) {
				// stream straight to file
				try {
					streamIntoFile(request.getSourceKey());
					return getFromCache(request.getSourceKey());
				} catch (IOException e) {
					throw new MirageIOException(Mirage.Source.EXTERNAL, e);
				}
			} else if (request.diskCacheStrategy() == DiskCacheStrategy.RESULT) {
				// stream to memory
				try {
					Bitmap bitmap = streamFromNetwork();
					if (bitmap != null) {
						bitmap = applyProcessors(bitmap);
						putResultInDiskCache(bitmap);
						bitmap.recycle();
						return getFromCache(request.getResultKey());
					} else {
						return null;
					}
				} catch (IOException e) {
					throw new MirageIOException(Mirage.Source.EXTERNAL, e);
				}
			} else if (request.diskCacheStrategy() == DiskCacheStrategy.ALL) {
				try {
					streamIntoFile(request.getSourceKey());
				} catch (IOException e) {
					throw new MirageIOException(Mirage.Source.EXTERNAL, e);
				}
				File file = getFromCache(request.getSourceKey());
				if (file != null) {
					Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), request.options());
					bitmap = applyProcessors(bitmap);
					putResultInDiskCache(bitmap);
					bitmap.recycle();
					return getFromCache(request.getResultKey());
				} else {
					return null;
				}
			}
		}

		return null;
	}

	@Override
	protected void onPostSuccess(File file) {
		request.target().onResult(file, source, request);
	}

	@Override
	protected void onPostError(Exception exception) {
		request.target().onError(exception, source, request);
	}

    private int determineSampleSize(BitmapFactory.Options outOpts) {
        int dimen = Math.max(outOpts.outWidth, outOpts.outHeight);
        float div = dimen / (float)request.getResizeTargetDimen();
        if (div < 1) return 1;
        if (request.isResizeSampleUndershoot()) {
            int sampleSize = MathUtils.upperPowerof2((int)div);
            return sampleSize;
        } else {
            int sampleSize = MathUtils.lowerPowerOf2((int)div);
            return sampleSize;
        }
    }

	private void streamIntoFile(String key) throws IOException {
		InputStream in = null;
		try {
			InputStream inputStream = getStream();
			in = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
			request.diskCache().put(key, new InputStreamWriter(in));
		} finally {
			IOUtils.close(in);
		}
	}

	private Bitmap streamFromNetwork() throws IOException {

        InputStream in = null;
        try {
            if (request.isInSampleSizeDynamic()) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
				InputStream inputStream = getStream();
                in = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
                BitmapFactory.decodeStream(in, null, opts);
                int sampleSize = determineSampleSize(opts);
                request.inSampleSize(sampleSize);
            }
        } finally {
            IOUtils.close(in);
        }

		try {
			InputStream inputStream = getStream();
			in = new BufferedInputStream(inputStream, IO_BUFFER_SIZE);
			Bitmap bitmap = BitmapFactory.decodeStream(in, request.outPadding(), request.options());
			return bitmap;
		} finally {
			IOUtils.close(in);
		}
	}

	private Bitmap applyProcessors(Bitmap bitmap) {
		List<BitmapProcessor> processors = request.getProcessors();
		// processors is a synchronzied list
		if (processors != null) {
			for (int i=0; i<processors.size(); i++) {
				bitmap = processors.get(i).process(bitmap);
			}
		}

		return bitmap;
	}

	private InputStream getStream() throws IOException {
		return request.urlFactory().getStream(request.uri());
	}

	private void putResultInDiskCache(Bitmap bitmap) {
		if (request.diskCache() != null) {
			if (request.diskCacheStrategy() == DiskCacheStrategy.RESULT
					|| request.diskCacheStrategy() == DiskCacheStrategy.ALL) {
				request.diskCache().put(request.getResultKey(), new BitmapWriter(bitmap));
			}
		}
	}

	private File getFromCache(String key) {
		if (request.diskCache() != null) {
			return request.diskCache().get(key);
		} else {
			return null;
		}
	}

}