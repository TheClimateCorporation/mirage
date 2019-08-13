package com.climate.mirage;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.AnyRes;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.view.View;

import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.cache.disk.DiskLruCacheWrapper;
import com.climate.mirage.cache.memory.BitmapLruCache;
import com.climate.mirage.cache.memory.MemoryCache;
import com.climate.mirage.errors.LoadError;
import com.climate.mirage.exceptions.MirageIOException;
import com.climate.mirage.load.BitmapProvider;
import com.climate.mirage.load.ContentUriProvider;
import com.climate.mirage.load.FileProvider;
import com.climate.mirage.load.SimpleUrlConnectionFactory;
import com.climate.mirage.load.UriProvider;
import com.climate.mirage.load.UrlFactory;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.targets.Target;
import com.climate.mirage.targets.ViewTarget;
import com.climate.mirage.tasks.BitmapDownloadTask;
import com.climate.mirage.tasks.BitmapTask;
import com.climate.mirage.tasks.MirageExecutor;
import com.climate.mirage.tasks.MirageTask;
import com.climate.mirage.utils.ActivityLifecycleStub;
import com.climate.mirage.utils.ObjectFactory;
import com.climate.mirage.utils.ObjectPool;

import java.io.File;
import java.io.InterruptedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;

import static android.content.ContentResolver.SCHEME_ANDROID_RESOURCE;
import static android.content.ContentResolver.SCHEME_CONTENT;
import static android.content.ContentResolver.SCHEME_FILE;

/**
 * Mirage is an image loading and caching system. In addition to convenience to basic loading,
 * it's was developed to solve the need
 * for allowing images to be downloaded for offline sync but not to be ejected from
 * and LRU disk cache while online.
 *
 * A standard use of this loader system will probably look something like
 * <pre>{@code Mirage.get(this)
 * 		.load("http://www.google.com/images/foo.jpg")
 * 		.into(imageView)
 * 		.fade()
 * 		.placeHolder(R.drawable.ic_placeholder)
 * 		.error(R.drawable.ic_errorholder)
 * 		.go()}</pre>
 *
 * In the case of listview, if you recycle an {@link android.widget.ImageView} the previous
 * load will automatically cancel.
 */
public class Mirage {

	/**
	 * Location of where the resource came from
	 */
	public static enum Source {
		MEMORY,
		DISK,
		EXTERNAL
	}

	public static final Executor THREAD_POOL_EXECUTOR = new MirageExecutor();

	private static final String TAG = Mirage.class.getSimpleName();
	private static Mirage mirage;
	private Executor defaultExecutor;
	private MemoryCache<String, Bitmap> defaultMemoryCache;
	private DiskCache defaultDiskCache;
	private UrlFactory defaultUrlConnectionFactory;
	private Map<Object, MirageTask> runningRequests;
	private LoadErrorManager loadErrorManager;
	private ObjectPool<MirageRequest> requestObjectPool;
	private Context applicationContext;
    private ActivityLifecycleStub activityLifecycles;

	public Mirage(Context applicationContext) {
		this.applicationContext = applicationContext.getApplicationContext();
		requestObjectPool = new ObjectPool<>(new MirageRequestFactory(), 50);
		loadErrorManager = new LoadErrorManager();
		runningRequests = Collections.synchronizedMap(new HashMap<Object, MirageTask>());
		defaultUrlConnectionFactory = new SimpleUrlConnectionFactory();
		((Application)this.applicationContext).registerActivityLifecycleCallbacks(activityLifecycles = new ActivityLifecycleStub() {
			@Override
			public void onActivityDestroyed(Activity activity) {
				Iterator<Map.Entry<Object, MirageTask>> it = runningRequests.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<Object, MirageTask> item = it.next();
					if (item.getKey() instanceof View) {
						Context context = ((View)item.getKey()).getContext();
						if (activity == context) {
							it.remove();
							MirageTask task = item.getValue();
							if (task != null) task.mirageCancel();
						}
					}
				}
			}
		});
	}

	synchronized public static void set(Mirage mirage) {
		Mirage.mirage = mirage;
	}

	synchronized public static Mirage get(Context context) {
		if (mirage == null) {
			mirage = new Mirage(context.getApplicationContext());
			mirage.defaultMemoryCache = new BitmapLruCache(0.25f);
			mirage.defaultDiskCache = new DiskLruCacheWrapper(
					new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
							new File(context.getCacheDir(), "mirage"),
							50 * 1024 * 1024));
			mirage.defaultExecutor = THREAD_POOL_EXECUTOR;
		}
		return mirage;
	}

    /**
     * If you create a mirage instance directly, call this to cancel all pending tasks
     * and to release application resources.
     */
    public void dispose() {
        ((Application)applicationContext).unregisterActivityLifecycleCallbacks(activityLifecycles);
        Iterator<Map.Entry<Object, MirageTask>> it = runningRequests.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, MirageTask> item = it.next();
            it.remove();
            MirageTask task = item.getValue();
            if (task != null) task.mirageCancel();
        }
    }

	public MirageRequest load(String uri) {
		if (TextUtils.isEmpty(uri)) return load((Uri)null);
		return load(Uri.parse(uri));
	}

	public MirageRequest load(File file) {
		if (file == null) return load((Uri)null);
        return load(Uri.fromFile(file));
	}

	/**
	 * Method for loading from the drawables folders
	 * @param resId
	 * @return
	 */
	public MirageRequest load(@AnyRes int resId) {
		Resources res = applicationContext.getResources();
		Uri uri = Uri.parse(SCHEME_ANDROID_RESOURCE +
				"://" + res.getResourcePackageName(resId)
				+ '/' + res.getResourceTypeName(resId)
				+ '/' + res.getResourceEntryName(resId));
		return load(uri);
	}

    public MirageRequest load(BitmapProvider provider) {
        MirageRequest r = load((Uri)null);
        return r.provider(provider);
    }

	/**
	 * The URI of the asset to load. Types of Uri's supported include
	 * http, https, file, and content
	 *
	 * @see android.net.Uri#parse(String)
	 * @see android.net.Uri#fromFile(java.io.File)
	 *
	 * @param uri the uri to load
	 * @return an new (or recycled) {@link com.climate.mirage.requests.MirageRequest} instance to daisy chain
	 */
	public MirageRequest load(Uri uri) {
		MirageRequest r = requestObjectPool.getObject();
        r.mirage(this);
		if (uri == null || TextUtils.isEmpty(uri.toString())) {
		    return r;
        }

		String scheme = uri.getScheme();

		// if this is a file or content resource, no need to cache all.
        // default to just caching the result
		if (!TextUtils.isEmpty(scheme)) {
			if (scheme.startsWith(SCHEME_FILE) ||
					scheme.startsWith(SCHEME_ANDROID_RESOURCE) ||
                    scheme.startsWith(SCHEME_CONTENT)) {
				r.diskCacheStrategy(DiskCacheStrategy.RESULT);
			}
		}

        BitmapProvider provider;
        if (scheme.startsWith(SCHEME_FILE)) {
		    provider = new FileProvider(r);
        } else if (scheme.startsWith(SCHEME_CONTENT) ||
                scheme.startsWith(SCHEME_ANDROID_RESOURCE)) {
            provider = new ContentUriProvider(applicationContext, r);
        } else {
            provider = new UriProvider(r);
        }
        r.uri(uri);
		r.provider(provider);
		return r;
	}
	
	/**
	 * Fires off the loading asynchronous. Mostly likely you will not access this directly
	 * but instead go through {@link com.climate.mirage.targets.ImageViewTarget#go()}
	 * or {@link com.climate.mirage.requests.MirageRequest#go()}
	 *
	 * @param request the configured request for the resource to load
	 * @return The AsyncTask responsible for running the request. It could be null if the resource is in the memory cache
	 */
    @MainThread
	public MirageTask go(MirageRequest request) {
		MirageTask<Void, Void, Bitmap> task = createGoTask(request);
        return executeGoTask(task, request);
	}

    /**
     * Creates the go task that Mirage will run without actually running it yet.

     * @param request the configured request for the resource to load.
     * @return
     */
    public MirageTask<Void, Void, Bitmap> createGoTask(MirageRequest request) {
        cancelRequest(request.target());

        // if the url is blank, fault out immediately
        if ((request.uri() == null || TextUtils.isEmpty(request.uri().toString()))
                && request.provider() == null) {
            if (request.target() != null) request.target().onError(
                    new IllegalArgumentException("Uri is null"), Source.MEMORY,
                    request);
            return null;
        }

        MirageTask<Void, Void, Bitmap> task = new BitmapTask(this, request, loadErrorManager, bitmapGoTaskCallback);
        addRequestToList(request, task);
        return task;
    }

    /**
     * Runs the go task.
     *
     * @param task the mirage task
     * @param request the configured request for the resource to load
     * @return The AsyncTask responsible for running the request. It could be null if the resource is in the memory cache
     */
	public MirageTask<Void, Void, Bitmap> executeGoTask(MirageTask<Void, Void, Bitmap> task, MirageRequest request) {
        if (task == null || task.isCancelled()) {
            return null;
        }

        // exit early if the lifecycle is not active.
        Lifecycle lifecycle = request.lifecycle();
        if (lifecycle != null && lifecycle.getCurrentState() == Lifecycle.State.DESTROYED) {
        	return null;
		}

        // TODO: clean up this duplicate
        if (request.memoryCache() == null) request.memoryCache(defaultMemoryCache);
        if (request.diskCache() == null) request.diskCache(defaultDiskCache);
        if (request.executor() == null) request.executor(defaultExecutor);
        if (request.urlFactory() == null) request.urlFactory(defaultUrlConnectionFactory);

        // if the url is blank, fault out immediately
        // TODO: clean up this duplicate
        if ((request.uri() == null || TextUtils.isEmpty(request.uri().toString()))
                && request.provider() == null) {
            if (request.target() != null) request.target().onError(
                    new IllegalArgumentException("Uri is null"), Source.MEMORY,
                    request);
            return null;
        }

        Bitmap resource;

        // Check immediately if the resource is in the memory cache
        MemoryCache<String, Bitmap> memCache = request.memoryCache();
        if (memCache != null) {
            resource = memCache.get(request.getResultKey());
            if (resource != null) {
                if (request.target() != null) request.target().onResult(resource,
                        Source.MEMORY, request);
                return null;
            }
        }

        // Check immediately if the resource is in the error cache
        LoadError error = getLoadError(request.provider().id());
        if (error != null && error.isValid()) {
            if (request.target() != null) request.target().onError(error.getException(),
                    Source.MEMORY, request);
            return null;
        }

		Executor executor = request.executor();
		if (request.target() != null) request.target().onPreparingLoad();
        task.executeOnExecutor(executor);
        return task;
	}


	/**
	 * Fires off the loading synchronous.
	 *
	 * @param request the configured request for the resource to load
	 * @return the loaded resource
	 */
    @WorkerThread
	public Bitmap goSync(MirageRequest request) throws MirageIOException, InterruptedIOException {
		if (isMainThread()) throw new NetworkOnMainThreadException();
		if (request.target() != null) throw new IllegalArgumentException("goSync does not allow for callbacks");

		if (request.memoryCache() == null) request.memoryCache(defaultMemoryCache);
		if (request.diskCache() == null) request.diskCache(defaultDiskCache);
		if (request.executor() == null) request.executor(defaultExecutor);
		if (request.urlFactory() == null) request.urlFactory(defaultUrlConnectionFactory);

        cancelRequest(request.target());

        MirageTask<Void, Void, Bitmap> task = createGoTask(request);
        if (task == null) return null;
		Bitmap bitmap = task.doTask();
		requestObjectPool.recycle(request);
		return bitmap;
	}

	/**
	 * Fires off the loading asynchronous. The return returned to the target here will be a
	 * File reference and not a bitmap.
	 *
	 * @param request the configured request for the resource to load
	 * @return The AsyncTask responsible for running the request. It could be null if the resource is in the memory cache
	 */
	public MirageTask downloadOnly(MirageRequest request) {
		if (request.memoryCache() == null) request.memoryCache(defaultMemoryCache);
		if (request.diskCache() == null) request.diskCache(defaultDiskCache);
		if (request.executor() == null) request.executor(defaultExecutor);
		if (request.urlFactory() == null) request.urlFactory(defaultUrlConnectionFactory);
		cancelRequest(request.target());

		// TODO: dont cache if the thread has been interrupted
        // FIXME: Can i modify the others tasks so I dont have to make a new task here?
		BitmapDownloadTask task = new BitmapDownloadTask(this, request, loadErrorManager, downloadTaskCallback);
		addRequestToList(request, task);
		if (request.target() != null) request.target().onPreparingLoad();
		task.executeOnExecutor(request.executor());
		return task;
	}

	/**
	 * Fires off the loading synchronous. The return returned to the target here will be a
	 * File reference and not a bitmap. This is good to use when in a sync adapter to where
	 * the caching file never needs to go into memory just into a sync cache.
	 *
	 * @param request the configured request for the resource to load
	 * @return The file location the image was loaded to. If the cache strategy is
	 * 		{@link com.climate.mirage.cache.disk.DiskCacheStrategy#ALL} the return file
	 * 		is from the result and not source.
	 */
	public File downloadOnlySync(MirageRequest request) throws MirageIOException {
		if (isMainThread()) throw new NetworkOnMainThreadException();
		if (request.target() != null) throw new IllegalArgumentException("/downloadOnlySync does not allow for callbacks");

		if (request.memoryCache() == null) request.memoryCache(defaultMemoryCache);
		if (request.diskCache() == null) request.diskCache(defaultDiskCache);
		if (request.executor() == null) request.executor(defaultExecutor);
		if (request.urlFactory() == null) request.urlFactory(new SimpleUrlConnectionFactory());

		BitmapDownloadTask task = new BitmapDownloadTask(this, request, loadErrorManager, null);
		File file = task.doTask();
		requestObjectPool.recycle(request);
		return file;
	}

	/**
	 * Cancels a loading request or nothing if one doesn't exist.
	 *
	 * @param target The target as defined in the request
	 */
	public void cancelRequest(Target target) {
		if (target instanceof ViewTarget) {
			View view = ((ViewTarget) target).getView();
			if (view != null) {
				cancelRequest(view);
			}
		} else {
			MirageTask t = runningRequests.remove(target);
			if (t != null) {
				t.mirageCancel();
			}
		}
	}

	/**
	 * Cancels a loading request or nothing if one doesn't exist.
	 *
	 * @param view The view the which the resource was going into.
	 */
	public void cancelRequest(View view) {
		MirageTask t = runningRequests.remove(view);
		if (t != null) {
            t.mirageCancel();
		}
	}

	/**
	 * Clear all caches. This is safe to run from the UI thread as it will background automatically if needed
	 */
	public void clearCache() {
		clearMemoryCache();
		clearDiskCache();
	}

	/**
	 * Removes a saved result or source resource from the memory and disk cache
	 *
	 * @param request The request which can hit the cache
	 */
	public void removeFromCache(MirageRequest request) {
		final String sourceKey = request.getSourceKey();
		final String resultKey = request.getResultKey();

		if (defaultMemoryCache != null) {
			defaultMemoryCache.remove(resultKey);
		}

		if (defaultDiskCache != null) {
			if (isMainThread()) {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						if (defaultDiskCache != null) {
							defaultDiskCache.delete(resultKey);
							if (resultKey != null && !resultKey.equals(sourceKey)) {
								defaultDiskCache.delete(sourceKey);
							}
						}
						return null;
					}
				}.execute();
			}

			// if on a BG thread
			else {
				defaultDiskCache.delete(resultKey);
				if (resultKey != null && !resultKey.equals(sourceKey)) {
					defaultDiskCache.delete(sourceKey);
				}
			}
		}
	}

	/**
	 * Clears the default memory cache.
	 */
	public void clearMemoryCache() {
		if (defaultMemoryCache != null) defaultMemoryCache.clear();
	}

	/**
	 * Clears the default disk cache
	 */
	public void clearDiskCache() {
		if (defaultDiskCache != null) {
			if (isMainThread()) {
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						if (defaultDiskCache != null) defaultDiskCache.clear();
						return null;
					}
				}.execute();
			}

			// if on a BG thread
			else {
				defaultDiskCache.clear();
			}
		}
	}

	public void setDefaultUrlConnectionFactory(UrlFactory defaultUrlConnectionFactory) {
		this.defaultUrlConnectionFactory = defaultUrlConnectionFactory;
	}

	public Executor getDefaultExecutor() {
		return defaultExecutor;
	}

	public Mirage setDefaultExecutor(Executor defaultExecutor) {
		this.defaultExecutor = defaultExecutor;
		return this;
	}

	public Mirage setDefaultMemoryCache(MemoryCache<String, Bitmap> defaultMemoryCache) {
		this.defaultMemoryCache = defaultMemoryCache;
		return this;
	}

	public MemoryCache<String, Bitmap> getDefaultMemoryCache() {
		return defaultMemoryCache;
	}

	public Mirage setDefaultDiskCache(DiskCache defaultDiskCache) {
		this.defaultDiskCache = defaultDiskCache;
		return this;
	}

	public DiskCache getDefaultDiskCache() {
		return defaultDiskCache;
	}

	private boolean isMainThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}

	private LoadError getLoadError(String id) {
		return loadErrorManager.getLoadError(id);
	}

	private void addRequestToList(MirageRequest request, MirageTask task) {
		if (request.target() != null) {
			if (request.target() instanceof ViewTarget) {
				View view = ((ViewTarget)request.target()).getView();
				if (view != null) {
					runningRequests.put(view, task);
				}
			} else {
				runningRequests.put(request.target(), task);
			}
		}
	}

	private BitmapTask.Callback<Bitmap> bitmapGoTaskCallback = new BitmapTask.Callback<Bitmap>() {
		@Override
		public void onCancel(MirageTask task, MirageRequest request) {
			removeSavedTask(request, task);
			requestObjectPool.recycle(request);
		}

		@Override
		public void onPostExecute(MirageTask task, MirageRequest request, Bitmap bitmap) {
			removeSavedTask(request, task);
			requestObjectPool.recycle(request);
		}
	};

	private BitmapDownloadTask.Callback<File> downloadTaskCallback = new BitmapDownloadTask.Callback<File>() {
		@Override
		public void onCancel(MirageTask task, MirageRequest request) {
			removeSavedTask(request, task);
			requestObjectPool.recycle(request);
		}

		@Override
		public void onPostExecute(MirageTask task, MirageRequest request, File file) {
			removeSavedTask(request, task);
			requestObjectPool.recycle(request);
		}
	};

	private void removeSavedTask(MirageRequest request, AsyncTask task) {
		if (request.target() != null) {
			if (request.target() instanceof ViewTarget) {
				View view = ((ViewTarget) request.target()).getView();
				if (view != null) {
					if (runningRequests.get(view) == task) {
						AsyncTask t = runningRequests.remove(view);
					}
				}
			} else {
				if (runningRequests.get(request.target()) == task) {
					AsyncTask t = runningRequests.remove(request.target());
				}
			}
		}
	}

	private static class MirageRequestFactory implements ObjectFactory<MirageRequest> {
		@Override
		public MirageRequest create() {
			return new MirageRequest();
		}

		@Override
		public void recycle(MirageRequest object) {
			object.recycle();
		}
	}

}