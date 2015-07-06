package com.climate.mirage.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.climate.mirage.Mirage;

public class ContentResolverLoadActivity extends AppCompatActivity {

	private ImageView iv;
	private Button button1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.row);
		iv = (ImageView)findViewById(R.id.imageView);
		button1 = (Button)findViewById(R.id.button1);
		findViewById(R.id.button2).setVisibility(View.GONE);
		button1.setText("Launch Picker");

		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), 66);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 66 && resultCode == Activity.RESULT_OK && data != null) {
			Uri selectedImage = data.getData();
			Mirage.get(ContentResolverLoadActivity.this)
					.load(selectedImage)
					.into(iv)
					.placeHolder(R.drawable.mirage_ic_launcher)
					.error(R.drawable.ic_error)
					.fade()
					.go();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}