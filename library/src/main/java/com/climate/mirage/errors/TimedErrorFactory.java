package com.climate.mirage.errors;

import com.climate.mirage.Mirage;

public class TimedErrorFactory implements LoadErrorFactory {

	private long timeToPersist;

	public TimedErrorFactory() {
		this(60 * 1000);
	}

	public TimedErrorFactory(long timeToPersist) {
		this.timeToPersist = timeToPersist;
	}

	@Override
	public LoadError createErrorLog(String url, Exception exception, Mirage.Source source) {
		return new TimedLoadError(timeToPersist, exception);
	}
}