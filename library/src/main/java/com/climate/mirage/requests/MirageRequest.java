package com.climate.mirage.requests;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import com.climate.mirage.Mirage;
import com.climate.mirage.cache.KeyMaker;
import com.climate.mirage.cache.SimpleKeyMaker;
import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.cache.memory.MemoryCache;
import com.climate.mirage.exceptions.MirageIOException;
import com.climate.mirage.load.UrlFactory;
import com.climate.mirage.processors.BitmapProcessor;
import com.climate.mirage.processors.ResizeProcessor;
import com.climate.mirage.targets.ImageViewTarget;
import com.climate.mirage.targets.Target;
import com.climate.mirage.targets.TextViewTarget;
import com.climate.mirage.tasks.MirageTask;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * The MirageRequest is the basis for all loads which go through the Mirage loading system. The
 * configurations in this class are for loading options and not specific view or target options.
 * View options will come later after a call to {@link #into(android.widget.ImageView)} has been
 * made.
 *
 * While you can instantiate the MirageRequest directly it's considered an error to do so
 * as the Mirage instance object pools the request. To get a request instance call one of the
 * {@link com.climate.mirage.Mirage#load(android.net.Uri)} methods. Because the request pools
 * be careful when keeping an instance of it around. The object will recycle once the task
 * is complete.
 */
public class MirageRequest {

	private static final KeyMaker defaultKeymaker;
	static {
		defaultKeymaker = new SimpleKeyMaker();
	}

	private final Object LOCK = new Object();
	private Mirage mirage;
	private Uri uri;
	private boolean skipWritingMemoryCache = false;
	private boolean skipReadingMemoryCache = false;
	private boolean skipReadingDiskCache = false;
	private Executor executor;
	private MemoryCache<String, Bitmap> memoryCache;
	private DiskCache diskCache;
	private Target<?> target;
	private BitmapFactory.Options options;
	private Rect outPadding;
	private int diskCacheStrategy = DiskCacheStrategy.RESULT;
	private KeyMaker keyMaker;
	private UrlFactory urlFactory;
	private List<BitmapProcessor> processors;
	private ResizeProcessor resizeProcessor;
    private int resizeTargetDimen = -1;
    private boolean resizeSampleUndershoot = false;


	/**
	 * Default constructor. You probably shouldn't call this directly but
	 * let {@link com.climate.mirage.Mirage#load(String)} return an instance for you.
	 */
	public MirageRequest() {
		keyMaker = defaultKeymaker;
	}

	/**
	 * Sets the object back to an empty state and releases resources
	 */
	public void recycle() {
		mirage = null;
		uri = null;
		skipWritingMemoryCache = false;
		skipReadingMemoryCache = true;
		skipReadingDiskCache = false;
		executor = null;
		memoryCache = null;
		diskCache = null;
		target = null;
		options = null;
		outPadding = null;
		diskCacheStrategy = DiskCacheStrategy.RESULT;
		keyMaker = defaultKeymaker;
		urlFactory = null;
        resizeTargetDimen = -1;
        resizeSampleUndershoot = false;
		if (processors != null) {
			processors.clear();
		}
		if (resizeProcessor != null) {
			resizeProcessor.setDimensions(0, 0, ResizeProcessor.STRATEGY_SCALE_FREE);
		}
	}

	public Uri uri() {
		return uri;
	}

	/**
	 * Set the uri of the resource to load. Valid schemes are
	 * http, https, file, and content
	 *
	 * @param uri the uri of the resource to load
	 * @return class instance to daisy chain
	 */
	public MirageRequest uri(Uri uri) {
		this.uri = uri;
		return this;
	}

	public MirageRequest mirage(Mirage mirage) {
		this.mirage = mirage;
		return this;
	}

	public boolean isSkipWritingMemoryCache() {
		return skipWritingMemoryCache;
	}

	/**
	 * Sets that the loaded resource should not be added to the memory cache.
	 * If wish to not check the cache during loading call {@link #skipReadingMemoryCache(boolean)} (boolean)}
	 *
	 * @param skip true if you do not wish to place the loaded resource into the cache.
	 *             This is a good idea when the resource is very large and would eject lots
	 *             of other items from the cache
	 * @return class instance to daisy chain
	 */
	public MirageRequest skipWritingMemoryCache(boolean skip) {
		this.skipWritingMemoryCache = skip;
		return this;
	}

	public boolean isSkipReadingMemoryCache() {
		return skipReadingMemoryCache;
	}

	/**
	 * Sets if the memory cache should be checked during loading. If wish to not place
	 * the loaded bitmap into the memory cache call {@link #skipWritingMemoryCache(boolean)}
	 *
	 * @param skip true if you wish to ignore checking the cache during load
	 * @return class instance to daisy chain
	 */
	public MirageRequest skipReadingMemoryCache(boolean skip) {
		this.skipReadingMemoryCache = skip;
		return this;
	}

	public int diskCacheStrategy() {
		return diskCacheStrategy;
	}

	/**
	 * The strategy used to cache the resource that is loaded.
	 * Valid arguments are from {@link com.climate.mirage.cache.disk.DiskCacheStrategy}
	 *
	 * @param strategy The strategy to use for caching
	 * @return class instance to daisy chain
	 */
	public MirageRequest diskCacheStrategy(@DiskCacheStrategy.Enforce int strategy) {
		this.diskCacheStrategy = strategy;
		return this;
	}



	public boolean isSkipReadingDiskCache() {
		return skipReadingDiskCache;
	}

	/**
	 * Sets if the disk cache should be checked during loading. If wish to not place
	 * the loaded bitmap into the disk cache call {@link #diskCacheStrategy(int)} and set to
	 * {@value com.climate.mirage.cache.disk.DiskCacheStrategy#NONE}
	 *
	 * @param skip true if you wish to ignore checking the cache during load
	 * @return class instance to daisy chain
	 */
	public MirageRequest skipReadingDiskCache(boolean skip) {
		this.skipReadingDiskCache = skip;
		return this;
	}

	public KeyMaker keyMaker() {
		return keyMaker;
	}

	/**
	 * Sets the strategy for providing the keys for the disk cache.
	 *
	 * @param keyMaker strategy for providing the keys for the disk cache.
	 * @return class instance to daisy chain
	 */
	public MirageRequest keyMaker(KeyMaker keyMaker) {
		this.keyMaker = keyMaker;
		return this;
	}

	/**
	 * Resizes the loaded bitmap once in memory to the sizes specified here. If
	 * you are trying to resize to conserve memory use the {@link #options(android.graphics.BitmapFactory.Options)}
	 * method to resample the bitmap during streaming.
	 *
	 * @param width width the new bitmap should be
	 * @param height height the new bitmap should be
	 * @return class instance to daisy chain
	 */
	public MirageRequest resize(int width, int height) {
		return resize(width, height, ResizeProcessor.STRATEGY_RATIO_MAINTAINED);
	}

	/**
	 * Resizes the loaded bitmap once in memory to the sizes specified here. If
	 * you are trying to resize to conserve memory use the {@link #options(android.graphics.BitmapFactory.Options)}
	 * method to resample the bitmap during streaming.
	 *
	 * @param width width the new bitmap should be
	 * @param height height the new bitmap should be
	 * @param strategy defines how the resize should take place. Is one of the constants from
     *                 {@link ResizeProcessor}
	 *                 and can combined like <code>STRATEGY_SCALE_DOWN_ONLY | STRATEGY_RATIO_MAINTAINED</code>
	 * @return class instance to daisy chain
	 */
	public MirageRequest resize(int width, int height, int strategy) {
		if (width < 1 || height < 1) throw new IllegalArgumentException("dimens must be greater than 0");
		if (resizeProcessor == null) {
			resizeProcessor = new ResizeProcessor();
			resizeProcessor.setDimensions(width, height, strategy);
		} else {
			resizeProcessor.setDimensions(width, height, strategy);
		}
		addProcessor(resizeProcessor);
		return this;
	}

	/**
	 * Sets the {@link android.graphics.BitmapFactory.Options} to use during decompression
	 *
	 * @param options see {@link android.graphics.BitmapFactory#decodeStream(java.io.InputStream, android.graphics.Rect, android.graphics.BitmapFactory.Options)}
	 * @param outPadding see {@link android.graphics.BitmapFactory#decodeStream(java.io.InputStream, android.graphics.Rect, android.graphics.BitmapFactory.Options)}
	 * @return class instance to daisy chain
	 */
	public MirageRequest options(BitmapFactory.Options options, Rect outPadding) {
		this.options = options;
		this.outPadding = outPadding;
		return this;
	}

	/**
	 * Sets the {@link android.graphics.BitmapFactory.Options} to use during decompression
	 *
	 * @param options see {@link android.graphics.BitmapFactory#decodeStream(java.io.InputStream, android.graphics.Rect, android.graphics.BitmapFactory.Options)}
	 * @return class instance to daisy chain
	 */
	public MirageRequest options(BitmapFactory.Options options) {
		return options(options, null);
	}

	/**
	 * Set's the inSampleSize used by
	 * {@link android.graphics.BitmapFactory.Options} to use during decompression
	 *
	 * @param sampleSize see {@link android.graphics.BitmapFactory.Options#inSampleSize}
	 * @return class instance to daisy chain
	 */
	public MirageRequest inSampleSize(int sampleSize) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = sampleSize;
		return options(opts, null);
	}

    /**
     * Method to dynamically adjust the inSampleSize based upon the read dimension of the InputStream.
     * When this method is called and the images aren't in memory cache or result cache,
     * it causes the load system to do a double pass. The first pass
     * is to decode only the bounds of the image. The next pass dynamically sets the
     * {@link android.graphics.BitmapFactory.Options#inSampleSize} and loads the image again.
     *
     * The width and height params here are only estimates as inSampleSize only allows the image
     * to scale in powers of 2, it can not produce an exact size. If an exact size is needed, also
     * call {@link #resize(int, int)} which runs in memory
     *
     * @param resizeTargetDimen the desired (non exact) length of the greatest side of the image.
     *                          So if the image is 2000x1000, the targetMaxSideSize will match up
     *                          to 2000.
     * @param isUndershootSizes true if the inSampleSize should err on the size on making the images smaller.
     * @return class instance to daisy chain
     */
	public MirageRequest dynamicResample(int resizeTargetDimen, boolean isUndershootSizes) {
        this.resizeTargetDimen = resizeTargetDimen;
        this.resizeSampleUndershoot = isUndershootSizes;
        return this;
    }

	public MirageRequest dynamicResample(int resizeTargetDimen) {
        return dynamicResample(resizeTargetDimen, false);
	}

    public boolean isInSampleSizeDynamic() {
        return resizeTargetDimen > 0;
    }

	public int getResizeTargetDimen() {
		return resizeTargetDimen;
	}

	public boolean isResizeSampleUndershoot() {
		return resizeSampleUndershoot;
	}

	public BitmapFactory.Options options() {
		return options;
	}

	public Rect outPadding() {
		return outPadding;
	}

	/**
	 * Sets which executor this particular request will run on.
	 *
	 * @param executor see {@link java.util.concurrent.Executor}
	 * @return class instance to daisy chain
	 */
	public MirageRequest executor(Executor executor) {
		this.executor = executor;
		return this;
	}

	public Executor executor() {
		return this.executor;
	}

	/**
	 * A factory to retrieve a {@link java.net.URLConnection} instance. If you need to
	 * customize the URLConnection object to add authorization headers or anything else
	 * pass in a {@link com.climate.mirage.load.UrlFactory}
	 *
	 * @param urlFactory the factory to use for this request
	 * @return class instance to daisy chain
	 */
	public MirageRequest urlFactory(UrlFactory urlFactory) {
		this.urlFactory = urlFactory;
		return this;
	}

	public UrlFactory urlFactory() {
		return urlFactory;
	}

	/**
	 * Sets the memory cache to be used for this particular request. If you set a cache here
	 * the default cache from {@link com.climate.mirage.Mirage} will not be used.
	 *
	 * @param memoryCache a specific cache for use for this request
	 * @return class instance to daisy chain
	 */
	public MirageRequest memoryCache(MemoryCache<String, Bitmap> memoryCache) {
		this.memoryCache = memoryCache;
		return this;
	}

	public MemoryCache<String, Bitmap> memoryCache() {
		return memoryCache;
	}

	/**
	 * Sets the disk cache to be used for this particular request. If you set a cache here
	 * the default cache from {@link com.climate.mirage.Mirage} will not be used.
	 *
	 * @param diskCache a specific cache for use for this request
	 * @return class instance to daisy chain
	 */
	public MirageRequest diskCache(DiskCache diskCache) {
		this.diskCache = diskCache;
		return this;
	}

	public DiskCache diskCache() {
		return diskCache;
	}

	public Target target() {
		return target;
	}

	/**
	 * Defines a target/callback for when the load completes. Once load completes it's up to the
	 * target to define what to do with the loaded resource.
	 *
	 * @param target A generic callback which handles the loaded resource
	 * @return The instance and type of the target passed in
	 */
	public <T extends Target> T into(T target) {
		this.target = target;
		return target;
	}

	/**
	 * A convenience method to set the target that the load will go into.
	 *
	 * @param imageView plain old {@link android.widget.ImageView}
	 * @return the {@link com.climate.mirage.targets.ImageViewTarget} to configure before loading begins
	 */
	public ImageViewTarget into(ImageView imageView) {
		return into(new ImageViewTarget(this, imageView));
	}

    // TODO: make one for just plain View which goes into the setBackground()

	/**
	 * A convenience method to set the target that the load will go into.
	 *
	 * @param textView plain old {@link android.widget.TextView}
	 * @return the {@link com.climate.mirage.targets.ImageViewTarget} to configure before loading begins
	 */
	public TextViewTarget into(TextView textView) {
		return into(new TextViewTarget(this, textView));
	}

	/**
	 * Sends the request into action onto a background thread.

	 * @return The task for running the action or null if the resource was found in memory
	 */
	public MirageTask go() {
		if (mirage == null) throw new IllegalStateException("Must set a mirage instance before calling go");
		return mirage.go(this);
	}

	/**
	 * Sends the request into action using the current thread. This must not happen
	 * on the UI Thread or an exception is thrown
	 *
	 * @return The loaded image
	 */
	public Bitmap goSync() throws MirageIOException, InterruptedIOException {
		if (mirage == null) throw new IllegalStateException("Must set a mirage instance before calling goSync");
		return mirage.goSync(this);
	}

	public MirageTask<Void, Void, Bitmap> createGoTask() {
        if (mirage == null) throw new IllegalStateException("Must set a mirage instance before calling createGoTask");
        return mirage.createGoTask(this);
	}

    public void executeGoTask(MirageTask<Void, Void, Bitmap> task) {
        if (mirage == null) throw new IllegalStateException("Must set a mirage instance before calling executeGoTask");
        mirage.executeGoTask(task, this);
    }

	/**
	 * Downloads the image only to file. The image is not read into memory
	 *
	 * @return The task for running the action
	 */
	public MirageTask downloadOnly() {
		if (mirage == null) throw new IllegalStateException("Must set a mirage instance before calling goSync");
		return mirage.downloadOnly(this);
	}

	/**
	 * Downloads the image only to file using the current thread. This must not happen
	 * on the UI Thread or an exception is thrown.
	 *
	 * @return The File where the loaded image was stored
	 */
	public File downloadOnlySync() throws MirageIOException {
		if (mirage == null) throw new IllegalStateException("Must set a mirage instance before calling goSync");
		return mirage.downloadOnlySync(this);
	}

	/**
	 * Gets the defined processors
	 *
	 * @return List of processors
	 */
	public List<BitmapProcessor> getProcessors() {
		return processors;
	}

	/**
	 * Adds a {@link com.climate.mirage.processors.BitmapProcessor} to be used once the
	 * image is loaded from the source. The processor is then ran on the source to return
	 * back a resulting bitmap.
	 *
	 * @param processor A processor to manipulate the image
	 */
	public void addProcessor(BitmapProcessor processor) {
		synchronized (LOCK) {
			if (this.processors == null) {
				this.processors = Collections.synchronizedList(new ArrayList<BitmapProcessor>());
			}
		}
		processors.add(processor);
	}

	/**
	 * The key used for saving the resource as a source in the cache
	 *
	 * @return the key used for saving the the resource as a source in the cache
	 */
	public String getSourceKey() {
		return keyMaker.getSourceKey(this);
	}

	/**
	 * The key used for saving the resource as a result in the cache
	 *
	 * @return The key used for saving the resource as a result in the cache
	 */
	public String getResultKey() {
		return keyMaker.getResultKey(this);
	}

	/**
	 * Helper method to determine if the calling request should save the source file
	 *
	 * @return true if the source should be saved. false if not, which could mean
	 * 			to save the result
	 */
	public boolean isRequestShouldSaveSource() {
		if (diskCacheStrategy() == DiskCacheStrategy.NONE) return false;
		return (diskCacheStrategy() == DiskCacheStrategy.SOURCE
				|| diskCacheStrategy() == DiskCacheStrategy.ALL
				|| getSourceKey().equals(getResultKey())
				&& diskCache() != null);
	}

}
