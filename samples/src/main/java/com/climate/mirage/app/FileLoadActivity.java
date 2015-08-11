package com.climate.mirage.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.climate.mirage.Mirage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileLoadActivity extends AppCompatActivity {

	private ImageView iv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.row);
		iv = (ImageView)findViewById(R.id.imageView);
		findViewById(R.id.button1).setVisibility(View.GONE);
		findViewById(R.id.button2).setVisibility(View.GONE);

		new AsyncTask<Void, Void, File>() {
			@Override
			protected File doInBackground(Void... params) {
				File toFile = new File("/sdcard/", "red_fish.jpg");
				if (!toFile.exists()) {
					try {
						InputStream in = getResources().getAssets().open("red_fish.jpg");
						FileOutputStream stream = new FileOutputStream(toFile);
						int bufferSize = 1024;
						byte[] buffer = new byte[bufferSize];
						int len = 0;
						while ((len = in.read(buffer)) != -1) {
							stream.write(buffer, 0, len);
						}
					} catch (IOException e) {

					}
				}
				return toFile;
			}

			@Override
			protected void onPostExecute(File file) {
				super.onPostExecute(file);
				Mirage.get(FileLoadActivity.this)
						.load(file)
						.into(iv)
						.placeHolder(R.drawable.mirage_ic_launcher)
						.error(R.drawable.ic_error)
						.fade()
						.go();
			}
		}.execute();

	}

}