package com.climate.mirage.shadows;

import android.os.AsyncTask;
import android.os.Handler;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowAsyncTask;

import java.util.concurrent.Executor;

@Implements(AsyncTask.class)
public class NoExecuteAsyncTaskShadow<Params, Progress, Result>
        extends ShadowAsyncTask<Params, Progress, Result> {

    @RealObject
    private AsyncTask<Params, Progress, Result> realAsyncTask;
    private AsyncTask.Status status = AsyncTask.Status.PENDING;
    private Handler handler = new Handler();

    public NoExecuteAsyncTaskShadow() {
        super();
    }

    @Implementation
    public AsyncTask<Params, Progress, Result> execute(Params... params) {
//        status = AsyncTask.Status.FINISHED;
//        handler.postDelayed(delayed, 10);
        return this.realAsyncTask;
    }

    @Implementation
    public AsyncTask<Params, Progress, Result> executeOnExecutor(Executor executor, Params... params) {
//        status = AsyncTask.Status.FINISHED;
        return this.realAsyncTask;
    }

    @Override
    public AsyncTask.Status getStatus() {
        return status;
    }

//    private Runnable delayed = new Runnable() {
//        @Override
//        public void run() {
//            getBridge().onPostExecute(null);
//        }
//    };
//
//    private ShadowAsyncTaskBridge<Params, Progress, Result> getBridge() {
//        return new ShadowAsyncTaskBridge(this.realAsyncTask);
//    }
}