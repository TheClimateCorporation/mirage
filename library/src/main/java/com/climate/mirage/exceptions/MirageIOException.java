package com.climate.mirage.exceptions;

import com.climate.mirage.Mirage;

import java.io.IOException;

public class MirageIOException extends IOException implements MirageException {

	private Mirage.Source source;

	public MirageIOException(Mirage.Source source) {
		super();
		this.source = source;
	}

	public MirageIOException(Mirage.Source source, Throwable cause) {
		super(cause);
		this.source = source;
	}

	@Override
	public Mirage.Source getSource() {
		return source;
	}
}