package com.climate.mirage.errors;

public class TimedLoadError implements LoadError {

	private long when;
	private Exception exception;
	private long timeToLive;

	public TimedLoadError(long timeToLiveMilli, Exception error) {
		exception = error;
		timeToLive = timeToLiveMilli;
		when = System.currentTimeMillis();
	}

	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public boolean isValid() {
		long diff = System.currentTimeMillis() - when;
		// TODO: maybe check for if the error is a NetworkErrorException
		// and if we have internet now the error is no longer valid
		// else use the time
		return (diff < timeToLive);
	}
}
