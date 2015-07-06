package com.climate.mirage.errors;

import com.climate.mirage.Mirage;

public interface LoadErrorFactory {

	public LoadError createErrorLog(String url, Exception exception, Mirage.Source source);

}