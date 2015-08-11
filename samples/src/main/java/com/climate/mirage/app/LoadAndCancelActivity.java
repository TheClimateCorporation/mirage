package com.climate.mirage.app;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.climate.mirage.Mirage;

public class LoadAndCancelActivity extends AppCompatActivity {

	private ImageView iv;
	private Button button1, button2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.row);
		iv = (ImageView)findViewById(R.id.imageView);
		button1 = (Button)findViewById(R.id.button1);
		button2 = (Button)findViewById(R.id.button2);
		button1.setText("Load Image");
		button2.setText("Cancel and Clear Cache");

		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadMirageImage();
			}
		});

		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Context cxt = LoadAndCancelActivity.this;
				Mirage.get(cxt).cancelRequest(iv);
				Mirage.get(cxt).clearCache();
				iv.setImageDrawable(null);
			}
		});
	}

	private void loadMirageImage() {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 8;
		Mirage.get(LoadAndCancelActivity.this)
				.load("http://i.imgur.com/UP3HW5C.jpg")
				.options(opts)
				.resize(300, 200)
				.into(iv)
				.placeHolder(R.drawable.mirage_ic_launcher)
				.error(R.drawable.ic_error)
				.fade()
				.go();
	}
}