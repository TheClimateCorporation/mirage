package com.climate.mirage.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.climate.mirage.exceptions.MirageIOException;
import com.climate.mirage.requests.MirageRequest;

import java.io.IOException;

abstract public class MirageTask<Params, Progress, Result>  extends AsyncTask<Params, Progress, Result>   {

	public static interface Callback<Result> {
		public void onCancel(MirageTask task, MirageRequest request);
		public void onPostExecute(MirageTask task, MirageRequest request, Result bitmap);
	}

	private static final String TAG = MirageTask.class.getSimpleName();
	private Exception exception; // out of memory exceptions and NPE
	private Callback<Result> callback;
	private MirageRequest request;


	public MirageTask(MirageRequest request, Callback<Result> callback) {
		this.request = request;
		this.callback = callback;
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

	@Override
	protected void onCancelled(Result result) {
		super.onCancelled(result);
		callback.onCancel(this, request);
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		if (isCancelled()) return;
		if (exception != null) {
			onPostError(exception);
		} else {
			onPostSuccess(result);
		}
		callback.onPostExecute(this, request, result);
	}

	abstract public Result doTask(Params... params) throws MirageIOException;

	abstract protected void onPostSuccess(Result bitmap);
	abstract protected void onPostError(Exception exception);
}