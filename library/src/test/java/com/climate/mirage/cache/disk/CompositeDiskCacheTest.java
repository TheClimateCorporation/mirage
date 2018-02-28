package com.climate.mirage.cache.disk;

import android.content.Context;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RobolectricTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;

public class CompositeDiskCacheTest extends RobolectricTest {

	@Test
	public void testCompositeInsertsToAllCaches() {
		DiskCache cache1 = Mockito.mock(DiskCache.class);
		DiskCache cache2 = Mockito.mock(DiskCache.class);
		DiskCache cache3 = Mockito.mock(DiskCache.class);

		CompositeDiskCache cache = new CompositeDiskCache(cache1, cache2, cache3);
		cache.put("", null);
		Mockito.verify(cache1, Mockito.times(1)).put(Mockito.anyString(), (DiskCache.Writer)Mockito.any());
		Mockito.verify(cache2, Mockito.times(1)).put(Mockito.anyString(), (DiskCache.Writer)Mockito.any());
		Mockito.verify(cache3, Mockito.times(1)).put(Mockito.anyString(), (DiskCache.Writer)Mockito.any());
	}

	@Test
	public void testCompositeGetsToAllCaches() {
		DiskCache cache1 = Mockito.mock(DiskCache.class);
		DiskCache cache2 = Mockito.mock(DiskCache.class);
		DiskCache cache3 = Mockito.mock(DiskCache.class);

		CompositeDiskCache cache = new CompositeDiskCache(cache1, cache2, cache3);
		cache.get("");
		Mockito.verify(cache1, Mockito.times(1)).get(Mockito.anyString());
		Mockito.verify(cache2, Mockito.times(1)).get(Mockito.anyString());
		Mockito.verify(cache3, Mockito.times(1)).get(Mockito.anyString());
	}

	@Test
	public void testCompositeDeletesToAllCaches() {
		DiskCache cache1 = Mockito.mock(DiskCache.class);
		DiskCache cache2 = Mockito.mock(DiskCache.class);
		DiskCache cache3 = Mockito.mock(DiskCache.class);
		DiskCache cache4 = Mockito.mock(DiskCache.class);

		CompositeDiskCache cache = new CompositeDiskCache(cache1, cache2, cache3, cache4);
		cache.delete("");
		Mockito.verify(cache1, Mockito.times(1)).delete(Mockito.anyString());
		Mockito.verify(cache2, Mockito.times(1)).delete(Mockito.anyString());
		Mockito.verify(cache3, Mockito.times(1)).delete(Mockito.anyString());
		Mockito.verify(cache4, Mockito.times(1)).delete(Mockito.anyString());
	}

	@Test
	public void testCompositeClearsToAllCaches() {
		DiskCache cache1 = Mockito.mock(DiskCache.class);
		DiskCache cache2 = Mockito.mock(DiskCache.class);
		DiskCache cache3 = Mockito.mock(DiskCache.class);
		DiskCache cache4 = Mockito.mock(DiskCache.class);

		CompositeDiskCache cache = new CompositeDiskCache(cache1, cache2, cache3, cache4);
		cache.clear();
		Mockito.verify(cache1, Mockito.times(1)).clear();
		Mockito.verify(cache2, Mockito.times(1)).clear();
		Mockito.verify(cache3, Mockito.times(1)).clear();
		Mockito.verify(cache4, Mockito.times(1)).clear();
	}

	@Test
	public void testDoesGetStopsAfterFind() {
		DiskCache cache1 = Mockito.mock(DiskCache.class);
		DiskCache cache2 = Mockito.mock(DiskCache.class);
		DiskCache cache3 = Mockito.mock(DiskCache.class);
		DiskCache cache4 = Mockito.mock(DiskCache.class);
		Mockito.when(cache1.get(Mockito.anyString())).thenReturn(getContext().getFilesDir());

		CompositeDiskCache cache = new CompositeDiskCache(cache1, cache2, cache3, cache4);
		File file = cache.get("anything");
		Assert.assertNotNull(file);
		Mockito.verify(cache1, Mockito.times(1)).get(Mockito.anyString());
		Mockito.verify(cache2, Mockito.never()).get(Mockito.anyString());
		Mockito.verify(cache3, Mockito.never()).get(Mockito.anyString());
		Mockito.verify(cache4, Mockito.never()).get(Mockito.anyString());
	}

	private Context getContext() {
		return RuntimeEnvironment.application;
	}
}