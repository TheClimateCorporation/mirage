package com.climate.mirage.cache.disk;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DiskCacheStrategy {

	public static final int NONE = 1;
	public static final int SOURCE = 2;
	public static final int RESULT = 4;
	public static final int ALL = 8;

	@IntDef({DiskCacheStrategy.SOURCE,
			DiskCacheStrategy.RESULT,
			DiskCacheStrategy.ALL,
			DiskCacheStrategy.NONE})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Enforce {}

}