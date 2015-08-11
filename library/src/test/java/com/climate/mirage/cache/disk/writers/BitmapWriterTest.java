package com.climate.mirage.cache.disk.writers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RoboManifestRunner;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowBitmapFactory;

import java.io.File;
import java.io.OutputStream;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class BitmapWriterTest {

	@Before
	public void setUp() throws Exception {
		File file = new File(getContext().getCacheDir(), "test_file");
		file.delete();
	}

	@After
	public void tearDown() throws Exception {
		File file = new File(getContext().getCacheDir(), "test_file");
		file.delete();
	}

	@Test
	public void testWritesPng() {
		File file = new File(getContext().getCacheDir(), "test_file");
		Bitmap bitmap = Mockito.mock(Bitmap.class);
		Mockito.when(bitmap.hasAlpha()).thenReturn(true);
		Mockito.when(
				bitmap.compress((Bitmap.CompressFormat) Mockito.any(), Mockito.anyInt(),
						(OutputStream) Mockito.any())).
				thenReturn(true);
		Mockito.when(bitmap.getWidth()).thenReturn(400);
		Mockito.when(bitmap.getHeight()).thenReturn(400);

		// According to the docs, hasAlpha should return true for ARGB_8888
		// which i think it does on a real device. Use Mockito to get around this
		// on Robolectric
//		Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
		BitmapWriter writer = new BitmapWriter(bitmap);
		boolean result = writer.write(file);
		Assert.assertTrue(result);

		ShadowBitmapFactory.provideWidthAndHeightHints(file.getAbsolutePath(), 400, 400);
		Bitmap writtenBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		Assert.assertNotNull(writtenBitmap);
		Assert.assertEquals(bitmap.getWidth(), writtenBitmap.getWidth());
		Assert.assertEquals(bitmap.getHeight(), writtenBitmap.getHeight());
		// ShadowBitmapFactory doesn't save the state of the bitmap.
		// Can't test this with robolectric it seems
//		Assert.assertTrue(writtenBitmap.hasAlpha());
	}

	@Test
	public void testWritesJpeg() {
		File file = new File(getContext().getCacheDir(), "test_file");
		Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
		BitmapWriter writer = new BitmapWriter(bitmap);
		boolean result = writer.write(file);
		Assert.assertTrue(result);

		ShadowBitmapFactory.provideWidthAndHeightHints(file.getAbsolutePath(), 200, 200);
		Bitmap writtenBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		Assert.assertNotNull(writtenBitmap);
		Assert.assertEquals(bitmap.getWidth(), writtenBitmap.getWidth());
		Assert.assertEquals(bitmap.getHeight(), writtenBitmap.getHeight());
		// ShadowBitmapFactory doesn't save the state of the bitmap.
		// Can't test this with robolectric it seems
//		Assert.assertTrue(writtenBitmap.hasAlpha());
	}

	@Test
	public void testWriteFailsGracefully() {
		File file = new File("http://invalid_file_name");
		Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
		BitmapWriter writer = new BitmapWriter(bitmap);
		boolean result = writer.write(file);
		Assert.assertFalse(result);
	}

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

}