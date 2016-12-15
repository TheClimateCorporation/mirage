package com.climate.mirage.targets;

import com.climate.mirage.Mirage;
import com.climate.mirage.requests.MirageRequest;

/**
 * The basic callback for the {@link com.climate.mirage.requests.MirageRequest#into(Target)}.
 * The Target will be notified of the loading status and it can respond accordingly.
 *
 * Targets are not supposed for the synchronous operations
 * {@link com.climate.mirage.Mirage#goSync(com.climate.mirage.requests.MirageRequest)}
 * and {@link com.climate.mirage.Mirage#downloadOnlySync(com.climate.mirage.requests.MirageRequest)}
 *
 * @param <Result> The resouce type that will be loaded. Currently supported are Bitmap and File
 */
public interface Target<Result> {

	/**
	 * This is called right before the AsyncTask fires and from the same thread as the
	 * {@link com.climate.mirage.Mirage#go(com.climate.mirage.requests.MirageRequest)}
	 * was called on.
	 */
	public void onPreparingLoad();

	/**
	 * Called on the main UI thread when loading was not canceled and download has completed.
	 *
	 * @param resouce The resource that was loaded
	 * @param source The location the resource came from. e.g Memory cache, disk cache, or web
	 * @param request the request that was used to load the resource. Do not hold onto a reference as
	 *                this will be recycled immediately after this method is called
	 */
	public void onResult(Result resouce, Mirage.Source source, MirageRequest request);


	/**
	 * Called on the main UI thread when loading was not canceled and download has an error.
	 *
	 * @param e The exception that occured
	 * @param source The location the error came from. e.g Memory cache, disk cache, or web
	 * @param request the request that was used to load the resource. Do not hold onto a reference as
	 *                this will be recycled immediately after this method is called
	 */
	public void onError(Exception e, Mirage.Source source, MirageRequest request);


    /**
     * Called if the loading task has been canceled. Targets can do any clean up here.
     */
    public void onCancel();

}
