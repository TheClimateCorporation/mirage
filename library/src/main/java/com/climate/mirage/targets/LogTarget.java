package com.climate.mirage.targets;

import android.util.Log;

import com.climate.mirage.Mirage;
import com.climate.mirage.requests.MirageRequest;

public class LogTarget<Result> implements Target<Result> {

	@Override
	public void onPreparingLoad() {
		Log.d("LogTarget", "/onPreparingLoad()");
	}

	@Override
	public void onResult(Result result, Mirage.Source source, MirageRequest request) {
		Log.d("LogTarget", "/onResult() result:" + ((result != null) ? result.toString() : "null"));
	}

	@Override
	public void onError(Exception e, Mirage.Source source, MirageRequest request) {
		Log.d("LogTarget", "/onError()");
	}

}
