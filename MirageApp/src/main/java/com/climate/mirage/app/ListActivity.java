package com.climate.mirage.app;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
		items.add("http://i.imgur.com/p4JZynw.jpg");
		items.add("http://i.imgur.com/z3bACFB.png");
		items.add("http://i.imgur.com/f6eUvmU.jpg");
		items.add("http://i.imgur.com/75RUeYi.jpg");
		items.add("http://i.imgur.com/yhFcaVP.jpg");
		items.add("http://i.imgur.com/Eu3vcth.jpg");
		items.add("http://i.imgur.com/0tpglPb.jpg");
		items.add("http://i.imgur.com/zIP4Q7b.jpg");
		items.add("http://i.imgur.com/O1g0n8S.jpg");
		items.add("http://i.imgur.com/zQCWo1u.jpg");
		items.add("http://i.imgur.com/A0Xhtyc.jpg");
		items.add("http://i.imgur.com/UP3HW5C.jpg");
		items.add("http://i.imgur.com/0Z6ixpw.jpg");
		items.add("http://i.imgur.com/oHgAUBG.jpg");
		items.add("http://i.imgur.com/nOWrxx4.jpg");
		items.add("http://i.imgur.com/f5nxRSY.jpg");
		items.add("http://i.imgur.com/G4um2VX.jpg");
		items.add("http://i.imgur.com/KwXxSkf.jpg");
		items.add("http://i.imgur.com/7O4GWLH.jpg");
		items.add("http://i.imgur.com/0Z6ixpw.jpg");
		items.add("http://i.imgur.com/VR9eoOc.jpg");
		items.add("http://i.imgur.com/DJe3mbc.jpg");

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
				iv.setMaxWidth(300);
				iv.setMaxHeight(200);
//				iv.setMinimumWidth(300);
//				iv.setMinimumHeight(300);
				iv.setAdjustViewBounds(true);
			} else {
				iv = (ImageView)convertView;
			}

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 8;
			Mirage.get(ListActivity.this)
					.load(getItem(position))
					.options(opts)
					.resize(300, 200)
					.into(iv)
					.placeHolder(R.drawable.mirage_ic_launcher)
					.error(R.drawable.ic_error)
					.fade()
					.go();

			return iv;
		}
	}
}