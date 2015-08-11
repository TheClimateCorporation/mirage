package com.climate.mirage.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.CompositeDiskCache;
import com.climate.mirage.cache.disk.DiskCache;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.cache.disk.DiskLruCacheWrapper;
import com.climate.mirage.exceptions.MirageIOException;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.targets.ImageViewTarget;

import java.io.File;
import java.util.ArrayList;

public class SampleOfflineSyncActivity extends AppCompatActivity {

	private static final String TAG = SampleOfflineSyncActivity.class.getSimpleName();
	private ArrayList<String> items;
	private TextView textView;
	private Mirage syncMirage;
	private Mirage appMirage;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewpager);
		textView = (TextView)findViewById(R.id.textView);
		Button button1 = (Button)findViewById(R.id.button1);
		button1.setText("Reset");
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reset();
			}
		});

		if (Mirage.get(this).getDefaultMemoryCache() != null) {
			Mirage.get(this).getDefaultMemoryCache().clear();
		}

		syncMirage = new Mirage(this);
		syncMirage.setDefaultMemoryCache(null);
		syncMirage.setDefaultDiskCache(createOfflineCache());
		syncMirage.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		// let's not fool with the default mirage settings for this example
		appMirage = new Mirage(this);
		appMirage.setDefaultMemoryCache(null);
		appMirage.setDefaultDiskCache(createWrappedCaches());
		appMirage.setDefaultExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

		items = new ArrayList<>();
		items.add("http://i.imgur.com/p4JZynw.jpg");
		items.add("http://i.imgur.com/z3bACFB.png");
		items.add("http://i.imgur.com/f6eUvmU.jpg");
		items.add("http://i.imgur.com/75RUeYi.jpg");
		items.add("http://i.imgur.com/yhFcaVP.jpg");
		items.add("http://i.imgur.com/Eu3vcth.jpg");
		items.add("http://i.imgur.com/0tpglPb.jpg");
		items.add("http://i.imgur.com/zIP4Q7b.jpg");
		items.add("http://i.imgur.com/O1g0n8S.jpg");
//		items.add("http://i.imgur.com/zQCWo1u.jpg");
//		items.add("http://i.imgur.com/A0Xhtyc.jpg");
//		items.add("http://i.imgur.com/UP3HW5C.jpg");
//		items.add("http://i.imgur.com/0Z6ixpw.jpg");
//		items.add("http://i.imgur.com/oHgAUBG.jpg");
//		items.add("http://i.imgur.com/nOWrxx4.jpg");
//		items.add("http://i.imgur.com/f5nxRSY.jpg");
//		items.add("http://i.imgur.com/G4um2VX.jpg");
//		items.add("http://i.imgur.com/KwXxSkf.jpg");
//		items.add("http://i.imgur.com/7O4GWLH.jpg");
//		items.add("http://i.imgur.com/0Z6ixpw.jpg");
//		items.add("http://i.imgur.com/VR9eoOc.jpg");
//		items.add("http://i.imgur.com/DJe3mbc.jpg");

		reset();
	}

	private void reset() {
		syncMirage.clearCache();
		appMirage.clearCache();

		new AsyncTask<Void, File, Void>() {
			private int count = 0;
			@Override
			protected Void doInBackground(Void... params) {
				for (int i=0; i<items.size(); i++) {
					try {
						File file = syncMirage
							.load(items.get(i))
							.diskCacheStrategy(DiskCacheStrategy.SOURCE)
							.downloadOnlySync();
						publishProgress(file);
					} catch (MirageIOException e) {
						Log.w(TAG, "IO Exception", e);
					}
				}
				return null;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				textView.setText("Starting download");
			}

			@Override
			protected void onProgressUpdate(File... values) {
				super.onProgressUpdate(values);
				textView.setText("file " + (count++) + " at \n\n" + values[0].getAbsolutePath());
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				textView.setText("Downloading Complete");
				initPager();
			}
		}.execute();
	}

	private void initPager() {
		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(new MyAdapter());
	}

	private DiskCache createWrappedCaches() {
		// the "blank" cache will never get written to since we've already have it on our
		// "sync" cache as a source
		DiskCache cacheDisk = new DiskLruCacheWrapper(
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(new File(getCacheDir(), "blank"),
						100 * 1024 * 1024));
		DiskLruCacheWrapper syncDisk = new DiskLruCacheWrapper(
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(new File(getCacheDir(), "sync"),
						100 * 1024 * 1024));
		syncDisk.setReadOnly(true);
		DiskCache compositeDiskCache = new CompositeDiskCache(cacheDisk, syncDisk);
		return compositeDiskCache;
	}

	private DiskCache createOfflineCache() {
		DiskLruCacheWrapper syncDisk = new DiskLruCacheWrapper(
				new DiskLruCacheWrapper.SharedDiskLruCacheFactory(new File(getCacheDir(), "sync"),
						100 * 1024 * 1024));
		return syncDisk;
	}

	private class MyAdapter extends PagerAdapter {

		private Context context;
		private int width, height;

		private MyAdapter() {
			this.context = 	SampleOfflineSyncActivity.this;
			width = context.getResources().getDisplayMetrics().widthPixels;
			height = (int)(width * .75f);
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			appMirage.cancelRequest((ImageView)object);
			container.removeView((View) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView iv = new ImageView(context);
			ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			container.addView(iv, lp);
			MirageRequest request = appMirage.load(items.get(position))
					.resize(width, height)
					.skipWritingMemoryCache(true)
					.skipReadingMemoryCache(true)
					.diskCacheStrategy(DiskCacheStrategy.SOURCE);
			MyImageTarget target = new MyImageTarget(request, iv, position);
			request.into(target);
			target.fade().placeHolder(R.drawable.mirage_ic_launcher).error(R.drawable.ic_error).go();
			return iv;
		}
	}

	private class MyImageTarget extends ImageViewTarget {

		private int position;

		private MyImageTarget(MirageRequest request, ImageView imageView, int position) {
			super(request, imageView);
			this.position = position;
		}

		@Override
		protected void onResult(ImageView view, Drawable drawable,
								Mirage.Source source, MirageRequest request) {
			super.onResult(view, drawable, source, request);
			textView.setText("Image " + position + " from " + source.toString());
		}
	}
}