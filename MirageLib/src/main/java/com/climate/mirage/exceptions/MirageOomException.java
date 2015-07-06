package com.climate.mirage.exceptions;

import com.climate.mirage.Mirage;

public class MirageOomException extends RuntimeException implements MirageException {

	private Mirage.Source source;

	public MirageOomException(Mirage.Source source) {
		super();
		this.source = source;
	}

	public MirageOomException(Mirage.Source source, Throwable throwable) {
		super(throwable);
		this.source = source;
	}

	@Override
	public Mirage.Source getSource() {
		return source;
	}

}
