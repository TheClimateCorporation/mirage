package com.climate.mirage.cache.disk.writers;

import android.util.Log;

import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.utils.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileLock;

public class InputStreamWriter implements DiskCache.Writer {
	private InputStream in;

	public InputStreamWriter(InputStream in) {
		this.in = in;
	}

	@Override
	public boolean write(File file) {
		FileOutputStream stream = null;
		FileLock lock = null;
		try {
			stream = new FileOutputStream(file);
			lock = stream.getChannel().lock();
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			int len = 0;
			while ((len = in.read(buffer)) != -1) {
				stream.write(buffer, 0, len);
			}
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			if (lock != null) {
				try {
					lock.release();
				} catch (IOException e) {
					Log.w("InputStreamWriter", "Couldn't release lock");
				}
			}
			IOUtils.close(stream);
		}
	}
}