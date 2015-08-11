package com.climate.mirage.shadows;

import android.os.AsyncTask;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowAsyncTask;
import org.robolectric.shadows.ShadowAsyncTaskBridge;

import java.util.concurrent.Executor;

@Implements(AsyncTask.class)
public class OnlyPostExecuteAsyncTaskShadow<Params, Progress, Result>
        extends ShadowAsyncTask<Params, Progress, Result> {

    @RealObject
    private AsyncTask<Params, Progress, Result> realAsyncTask;
    private AsyncTask.Status status = AsyncTask.Status.PENDING;

    public OnlyPostExecuteAsyncTaskShadow() {
        super();
    }

    @Implementation
    public AsyncTask<Params, Progress, Result> execute(Params... params) {
        status = AsyncTask.Status.FINISHED;
        getBridge().onPostExecute(null);
        return this.realAsyncTask;
    }

    @Implementation
    public AsyncTask<Params, Progress, Result> executeOnExecutor(Executor executor, Params... params) {
        status = AsyncTask.Status.FINISHED;
        getBridge().onPostExecute(null);
        return this.realAsyncTask;
    }

    @Override
    public AsyncTask.Status getStatus() {
        return status;
    }

    private ShadowAsyncTaskBridge<Params, Progress, Result> getBridge() {
        return new ShadowAsyncTaskBridge(this.realAsyncTask);
    }

}