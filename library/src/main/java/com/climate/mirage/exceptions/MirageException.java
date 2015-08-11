package com.climate.mirage.exceptions;

import com.climate.mirage.Mirage;

public interface MirageException {

	Mirage.Source getSource();
}