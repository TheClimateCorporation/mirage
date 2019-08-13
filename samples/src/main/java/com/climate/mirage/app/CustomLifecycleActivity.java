package com.climate.mirage.app;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.climate.mirage.Mirage;
import com.climate.mirage.cache.disk.DiskCacheStrategy;
import com.climate.mirage.targets.drawables.CircularDrawable;
import com.climate.mirage.targets.drawables.DrawableFactory;

import java.util.ArrayList;

public class CustomLifecycleActivity extends AppCompatActivity {

	private ListView listView;
	private MyLifecycleOwner owner;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		Button btn = new Button(this);
		btn.setText("Kill");
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (owner != null) {
					owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
				}
			}
		});
		Button btn2 = new Button(this);
		btn2.setText("Reload");
		btn2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				killAndLoad();
			}
		});
		listView = new ListView(this);
		ll.addView(btn);
		ll.addView(btn2);
		ll.addView(listView);
		setContentView(ll);

		killAndLoad();
	}

	private void killAndLoad() {
		if (owner != null) {
			owner.registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
		}
		owner = new MyLifecycleOwner();
		listView.setAdapter(new CustomLifecycleActivity.MyAdapter(this));
	}

	private static class MyLifecycleOwner implements LifecycleOwner {
		private LifecycleRegistry registry;
		private MyLifecycleOwner() {
			registry = new LifecycleRegistry(this);
			registry.markState(Lifecycle.State.RESUMED);
		}

		@Override
		public Lifecycle getLifecycle() {
			return registry;
		}
	}

	private class MyAdapter extends BaseAdapter {

		private LayoutInflater inflater;
		private String imgUrl = "https://upload.wikimedia.org/wikipedia/commons/6/66/An_up-close_picture_of_a_curious_male_domestic_shorthair_tabby_cat.jpg";

		public MyAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return 40;
		}

		@Override
		public String getItem(int position) {
			return imgUrl;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView iv;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_type1, parent, false);
				iv = (ImageView)convertView.findViewById(R.id.imageView);
				convertView.setTag(iv);
			} else {
				iv = (ImageView)convertView.getTag();
			}

			Mirage.get(CustomLifecycleActivity.this)
					.load(getItem(position))
					.lifecycle(owner.getLifecycle())
					.skipWritingMemoryCache(true)
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(iv)
					.fit()
					.placeHolder(R.drawable.mirage_ic_launcher)
					.error(R.drawable.ic_error)
					.fade()
					.go();

			return convertView;
		}
	}
}