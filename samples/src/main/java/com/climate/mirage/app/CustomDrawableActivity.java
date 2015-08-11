package com.climate.mirage.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.climate.mirage.Mirage;
import com.climate.mirage.targets.drawables.CircularDrawable;
import com.climate.mirage.targets.drawables.DrawableFactory;

public class CustomDrawableActivity extends AppCompatActivity {

	private ImageView iv;
	private Button button1, button2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.row);
		iv = (ImageView)findViewById(R.id.imageView);
		button1 = (Button)findViewById(R.id.button1);
		button2 = (Button)findViewById(R.id.button2);
		button1.setText("Load Puppy");
		button2.setText("Load Cat");

		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Mirage.get(CustomDrawableActivity.this)
						.load(Images.PUPPY)
						.into(iv)
						.placeHolder(R.drawable.mirage_ic_launcher)
						.error(R.drawable.ic_error)
						.drawableFactory(new DrawableFactory() {
							@Override
							public Drawable createDrawable(Context context, Bitmap bitmap) {
								return new CircularDrawable(bitmap);
							}
						})
						.fade()
						.go();
			}
		});

		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Mirage.get(CustomDrawableActivity.this)
						.load(Images.CAT)
						.into(iv)
						.placeHolder(R.drawable.mirage_ic_launcher)
						.error(R.drawable.ic_error)
						.drawableFactory(new DrawableFactory() {
							@Override
							public Drawable createDrawable(Context context, Bitmap bitmap) {
								return new CircularDrawable(bitmap);
							}
						})
						.fade()
						.go();
			}
		});
	}
}