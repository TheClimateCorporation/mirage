package com.climate.mirage.shadows;

import android.os.AsyncTask;

import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.shadows.ShadowAsyncTaskBridge;
import org.robolectric.util.ReflectionHelpers;

@DoNotInstrument
public class ShadowAsyncTaskBrideExt<Params, Progress, Result>
        extends ShadowAsyncTaskBridge<Params, Progress, Result> {

    private AsyncTask<Params, Progress, Result> asyncTask;

    public ShadowAsyncTaskBrideExt(AsyncTask<Params, Progress, Result> asyncTask) {
        super(asyncTask);
        this.asyncTask = asyncTask;
    }

    public void onCancelled(Result result) {
        ReflectionHelpers.callInstanceMethod(this.asyncTask, "onCancelled",
                new ReflectionHelpers.ClassParameter[]{
                        ReflectionHelpers.ClassParameter.from(Object.class, result)});
    }

}