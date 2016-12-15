package com.climate.mirage.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.climate.mirage.Mirage;

public class DrawableLoadActivity extends AppCompatActivity {

	private ImageView iv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.row);
		iv = (ImageView)findViewById(R.id.imageView);
        findViewById(R.id.buttons).setVisibility(View.GONE);
		loadImage();
	}

    private void loadImage() {
        int maxViewSize = Math.max(iv.getWidth(), iv.getHeight());
        Log.i("DrawableLoadActivity", "insampling to " + maxViewSize
                + ", then in memory to " + iv.getWidth() + "x" + iv.getHeight()
                + " but maintaining the image's aspect ratio");
        Mirage.get(DrawableLoadActivity.this)
                .load(R.drawable.fiji_coral)
                .skipWritingMemoryCache(true)
                .into(iv)
				.fit()
				.go();
    }
}