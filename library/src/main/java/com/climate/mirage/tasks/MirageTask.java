package com.climate.mirage.tasks;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.AsyncTask;
import android.util.Log;

import com.climate.mirage.exceptions.MirageIOException;
import com.climate.mirage.requests.MirageRequest;

import java.io.IOException;
import java.io.InterruptedIOException;

abstract public class MirageTask<Params, Progress, Result>  extends AsyncTask<Params, Progress, Result>   {

	public static interface Callback<Result> {
		public void onCancel(MirageTask task, MirageRequest request);
		public void onPostExecute(MirageTask task, MirageRequest request, Result bitmap);
	}

	private static final String TAG = MirageTask.class.getSimpleName();
	private Exception exception; // out of memory exceptions and NPE
	private Callback<Result> callback;
	private MirageRequest request;
    private boolean didInternalCancel = false;
    private Lifecycle lifecycle;
	private LifecycleObserver lifecycleObserver;

	public MirageTask(MirageRequest request, Callback<Result> callback) {
		this.request = request;
		this.callback = callback;
		this.lifecycle = request.lifecycle();
		if (lifecycle != null) {
			lifecycleObserver = new LifecycleObserver() {
				@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
				public void onDestroy() {
					mirageCancel();
				}
			};
			lifecycle.addObserver(lifecycleObserver);
		}
	}

	@Override
	protected Result doInBackground(Params... params) {
		try {
			return doTask(params);
		} catch (IOException ex) {
			Log.e("MirageTask", "Logging IOException", ex);
			exception = ex;
			return null;
		} catch (Exception ex) {
			Log.e("MirageTask", "Logging Exception", ex);
			exception = ex;
			return null;
		}
	}

    /**
     * A call to this will ensure that the cancel calls happen
     * even if the task hasn't been executed yet. The code always
     * needs the cancel callbacks because our targets must be notified
     */
    public void mirageCancel() {
        // if the task is already running, let the onCancel call it
        // so we dont object pool recycle the request object in the
        // middle of doing working still
        if (getStatus() != Status.RUNNING) {
            internalCancel();
        }
        cancel(true);
    }

	@Override
	protected void onCancelled(Result result) {
		super.onCancelled(result);
        internalCancel();
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		removeLifecycleObserver();
		if (isCancelled()) return;
		if (exception != null) {
			onPostError(exception);
		} else {
			onPostSuccess(result);
		}
		callback.onPostExecute(this, request, result);
	}

	abstract public Result doTask(Params... params) throws MirageIOException, InterruptedIOException;

	abstract protected void onPostSuccess(Result bitmap);
	abstract protected void onPostError(Exception exception);

    private void internalCancel() {
        if (didInternalCancel) return;
        didInternalCancel = true;
        if (request.target() != null) request.target().onCancel();
        callback.onCancel(this, request);
		removeLifecycleObserver();
    }

    private void removeLifecycleObserver() {
    	if (lifecycle != null) {
    		lifecycle.removeObserver(lifecycleObserver);
		}
    	lifecycle = null;
    	lifecycleObserver = null;
	}
}