package com.climate.mirage.cache;

import com.climate.mirage.requests.MirageRequest;

public interface KeyMaker {

	public String getSourceKey(MirageRequest request);
	public String getResultKey(MirageRequest request);

}