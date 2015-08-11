package com.climate.mirage.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.targets.Target;

import java.io.File;

public class DownloadOnlyActivity extends AppCompatActivity {

	private TextView textView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		textView = new TextView(this);
		int pad = (int)(getResources().getDisplayMetrics().density * 30);
		textView.setPadding(pad, pad, pad, pad);
		setContentView(textView);

		MirageRequest request = Mirage.get(this)
				.load(Images.PUPPY)
				.skipReadingDiskCache(true)
				.diskCacheStrategy(DiskCacheStrategy.RESULT);

		request.into(new Target<File>() {
			@Override
			public void onPreparingLoad() {
				textView.setText("onPreparingLoad");
			}

			@Override
			public void onResult(File bitmap, Mirage.Source source, MirageRequest request) {
				String location = "null";
				if (bitmap != null)  location = bitmap.getAbsolutePath();
				Log.d("DownloadOnlyActivity", "onResult file:" + location);
				textView.setText("File downloaded to: \n\n" + location);
			}

			@Override
			public void onError(Exception e, Mirage.Source source, MirageRequest request) {
				Log.d("DownloadOnlyActivity", "onError", e);
				textView.setText("Error! " + e.toString());
			}
		});
		request.downloadOnly();

	}
}