package com.climate.mirage.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.load.BitmapProvider;
import com.climate.mirage.targets.drawables.CircularDrawable;
import com.climate.mirage.targets.drawables.DrawableFactory;

import java.io.IOException;

public class CustomBitmapActivity extends AppCompatActivity {

	private ImageView iv;
	private Button button1, button2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.row);
		iv = (ImageView)findViewById(R.id.imageView);
		button1 = (Button)findViewById(R.id.button1);
		button2 = (Button)findViewById(R.id.button2);
		button2.setVisibility(View.GONE);
		button1.setText("Load Bitmap 1");

		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Mirage.get(CustomBitmapActivity.this)
						.load(new CustomBitmap())
                        .skipWritingMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
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

	private static class CustomBitmap implements BitmapProvider {

        @Override
        public Bitmap load() throws IOException {
            Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            for (int i = 0; i < 6; i++) {
                int angle = (360/6) * i;
                float x = (float)(150 * Math.cos(angle * Math.PI / 180F)) + 200;
                float y = (float)(150 * Math.sin(angle * Math.PI / 180F)) + 200;
                canvas.drawCircle(x, y, 20, paint);
            }
            return bitmap;
        }

        @Override
        public String id() {
            return "circles_in_cirles";
        }
    }
}