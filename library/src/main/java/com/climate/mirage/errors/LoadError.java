package com.climate.mirage.errors;

public interface LoadError {

	public boolean isValid();
	public Exception getException();

}