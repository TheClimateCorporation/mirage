package com.climate.mirage.app;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.climate.mirage.Mirage;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

	private ListView listView;
	private ArrayList<String> items;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		listView = new ListView(this);
		setContentView(listView);

		items = new ArrayList<>();
		items.add("https://i.imgur.com/lCL6kEF.jpg");
		items.add("https://i.imgur.com/HDrJjF0.jpg");
		items.add("https://i.imgur.com/hY9kBxr.jpg");
		items.add("https://i.imgur.com/3ndso90.jpg");
		items.add("https://i.imgur.com/AGDbbKl.jpg");
		items.add("https://i.imgur.com/7IAT3YE.jpg");
		items.add("https://i.imgur.com/FQgMesN.jpg");
		items.add("https://i.imgur.com/J8eM6C0.jpg");
		items.add("https://i.imgur.com/drnRnjv.jpg");
		items.add("https://i.imgur.com/oDam7Rw.jpg");
		items.add("https://i.imgur.com/yFMQ72Z.jpg");
		items.add("https://i.imgur.com/hx08qkY.jpg");
		items.add("https://i.imgur.com/zD37eJG.jpg");
		items.add("https://i.imgur.com/a3yPtWq.jpg");
		items.add("https://i.imgur.com/6kR3jEk.jpg");

		listView.setAdapter(new MyAdapter(this, items));
	}

	private class MyAdapter extends BaseAdapter {

		private ArrayList<String> items;

		public MyAdapter(Context context, ArrayList<String> items) {
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public String getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView iv;
			if (convertView == null) {
				iv = new ImageView(ListActivity.this);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        toPx(250), toPx(250)
                );
                iv.setLayoutParams(lp);
				iv.setAdjustViewBounds(true);
			} else {
				iv = (ImageView)convertView;
			}

			Mirage.get(ListActivity.this)
					.load(getItem(position))
					.into(iv)
					.fit()
					.placeHolder(R.drawable.mirage_ic_launcher)
					.error(R.drawable.ic_error)
					.fade()
					.go();

			return iv;
		}
	}

	private int toPx(int dp) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return (int)px;
    }
}