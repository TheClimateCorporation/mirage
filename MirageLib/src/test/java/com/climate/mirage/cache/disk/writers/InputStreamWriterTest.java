package com.climate.mirage.cache.disk.writers;

import android.content.Context;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RoboManifestRunner;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class InputStreamWriterTest {

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
	public void testWritesPng() throws Exception {
		InputStream is = new InputStream() {
			private int calls = 10;
			@Override
			public int read() throws IOException {
				if (calls < 20) return calls++;
				else return -1;
			}
		};
		InputStreamWriter writer = new InputStreamWriter(is);
		File file = new File(getContext().getCacheDir(), "test_file");
		boolean result = writer.write(file);
		Assert.assertTrue(result);

		file = new File(getContext().getCacheDir(), "test_file");
		Assert.assertTrue(file.exists());
	}

	@Test
	public void testFailsGracefullyOnFileNotFound() throws Exception {
		InputStream is = new InputStream() {
			private int calls = 10;
			@Override
			public int read() throws IOException {
				if (calls < 20) return calls++;
				else return -1;
			}
		};
		InputStreamWriter writer = new InputStreamWriter(is);
		File file = new File("http://invalid_file_name");
		boolean result = writer.write(file);
		Assert.assertFalse(result);
	}

	@Test
	public void testFailsGracefullyOnIOException() throws Exception {
		InputStream is = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("exception for test");
			}
		};
		InputStreamWriter writer = new InputStreamWriter(is);
		File file = new File(getContext().getCacheDir(), "test_file");
		boolean result = writer.write(file);
		Assert.assertFalse(result);
	}

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

}