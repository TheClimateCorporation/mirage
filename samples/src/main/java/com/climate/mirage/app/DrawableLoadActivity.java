package com.climate.mirage.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.climate.mirage.Mirage;

public class DrawableLoadActivity extends AppCompatActivity {

	private ImageView iv;
	private Button button1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.row);
		iv = (ImageView)findViewById(R.id.imageView);
		button1 = (Button)findViewById(R.id.button1);
		findViewById(R.id.button2).setVisibility(View.GONE);
		button1.setText("Load Drawable");

		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Mirage.get(DrawableLoadActivity.this)
						.load(R.drawable.fiji_coral)
						.inSampleSize(8)
						.into(iv)
						.placeHolder(R.drawable.mirage_ic_launcher)
						.error(R.drawable.ic_error)
						.fade()
						.go();
			}
		});
	}
}