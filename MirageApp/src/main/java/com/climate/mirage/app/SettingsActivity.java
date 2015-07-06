package com.climate.mirage.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.CompositeDiskCache;
import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.cache.disk.DiskLruCacheWrapper;
import com.climate.mirage.cache.memory.BitmapLruCache;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);

		Button btn = new Button(this);
		ll.addView(btn);
		btn.setText("Clear Cache");
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Mirage.get(SettingsActivity.this).clearCache();
			}
		});

		btn = new Button(this);
		ll.addView(btn);
		btn.setText("Standard Cache Settings");
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Mirage.get(SettingsActivity.this)
						.setDefaultMemoryCache(new BitmapLruCache(0.25f))
						.setDefaultDiskCache(new DiskLruCacheWrapper(
								new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
										new File(SettingsActivity.this.getCacheDir(), "mirage"),
										50 * 1024 * 1024)))
						.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});

		btn = new Button(this);
		ll.addView(btn);
		btn.setText("No Memory Cache");
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Mirage.get(SettingsActivity.this)
						.setDefaultMemoryCache(null)
						.setDefaultDiskCache(new DiskLruCacheWrapper(
								new DiskLruCacheWrapper.SharedDiskLruCacheFactory(
										new File(SettingsActivity.this.getCacheDir(), "mirage"),
										50 * 1024 * 1024)))
						.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});

		btn = new Button(this);
		ll.addView(btn);
		btn.setText("Decorated Disk Cache");
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Mirage.get(SettingsActivity.this)
						.setDefaultMemoryCache(null)
						.setDefaultDiskCache(createWrappedCaches())
						.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});
	}

	private DiskCache createWrappedCaches() {
		DiskCache cacheDisk = new DiskLruCacheWrapper(
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(new File(getCacheDir(), "mirage"),
						5 * 1024 * 1024));
		DiskLruCacheWrapper syncDisk = new DiskLruCacheWrapper(
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(new File(getCacheDir(), "sync"),
						5 * 1024 * 1024));
		syncDisk.setReadOnly(true);
		DiskCache compositeDiskCache = new CompositeDiskCache(cacheDisk, syncDisk);
		return compositeDiskCache;
	}
}