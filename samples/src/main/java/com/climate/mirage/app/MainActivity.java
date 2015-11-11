package com.climate.mirage.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.climate.mirage.Mirage;

public class MainActivity extends AppCompatActivity {

	private Adapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PackageManager pm = getPackageManager();
		try {
			PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
			adapter = new Adapter(this, info.activities);
			ListView lv = new ListView(this);
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ActivityInfo info = adapter.getItem(position);
					try {
						Class clazz = Class.forName(info.name);
						startActivity(new Intent(MainActivity.this, clazz));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			});
			setContentView(lv);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Mirage.get(this).clearCache();
	}

	private class Adapter extends ArrayAdapter<ActivityInfo> {
		private Adapter(Context context, ActivityInfo[] items) {
			super(context, 0, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv;
			if (convertView == null) {
				tv = new TextView(getContext());
				tv.setTextSize(16);
				tv.setPadding(30, 30, 30, 30);
			} else {
				tv = (TextView)convertView;
			}

			ActivityInfo info = getItem(position);
			tv.setText( info.nonLocalizedLabel );

			return tv;
		}

		@Override
		public int getCount() {
			return super.getCount() - 1;
		}

		@Override
		public ActivityInfo getItem(int position) {
			return super.getItem(position+1);
		}
	}
}