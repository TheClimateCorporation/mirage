package com.climate.mirage.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.RobolectricTest;
import com.climate.mirage.load.UriProvider;
import com.climate.mirage.processors.BitmapProcessor;
import com.climate.mirage.requests.MirageRequest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

public class SimpleKeyMakerTest extends RobolectricTest {

	@Test
	public void testMakesKey() throws Exception {
		MirageRequest request = new MirageRequest();
		request.uri(Uri.parse("http://www.google.com"));
		request.provider(new UriProvider(request));

		SimpleKeyMaker keyMaker = new SimpleKeyMaker();
		Assert.assertEquals("e5123341", keyMaker.getSourceKey(request));
		Assert.assertEquals("e5123341", keyMaker.getResultKey(request));
	}

	@Test
	public void testSourceKeyWithModifiers() throws Exception {
		MirageRequest request = new MirageRequest();
		request.uri(Uri.parse("http://www.google.com"));
        request.provider(new UriProvider(request));
		request.addProcessor(new BitmapProcessor() {
			@Override
			public String getId() {
				return "eeek";
			}

			@Override
			public Bitmap process(Bitmap in) {
				return null;
			}
		});
		request.addProcessor(new BitmapProcessor() {
			@Override
			public String getId() {
				return "rrrrr";
			}

			@Override
			public Bitmap process(Bitmap in) {
				return null;
			}
		});

		SimpleKeyMaker keyMaker = new SimpleKeyMaker();
		Assert.assertEquals("e5123341", keyMaker.getSourceKey(request));
	}

	@Test
	public void testResultKeyWithModifiers() throws Exception {
		MirageRequest request = new MirageRequest();
		request.uri(Uri.parse("http://www.google.com"));
        request.provider(new UriProvider(request));
		request.addProcessor(new BitmapProcessor() {
			@Override
			public String getId() {
				return "eeek";
			}

			@Override
			public Bitmap process(Bitmap in) {
				return null;
			}
		});
		request.addProcessor(new BitmapProcessor() {
			@Override
			public String getId() {
				return "rrrrr";
			}

			@Override
			public Bitmap process(Bitmap in) {
				return null;
			}
		});

		SimpleKeyMaker keyMaker = new SimpleKeyMaker();
		Assert.assertEquals("d53c8b77", keyMaker.getResultKey(request));
	}

	@Test
	public void testResultKeyWithModifiersAndOptions() throws Exception {
		MirageRequest request = new MirageRequest();
		request.uri(Uri.parse("http://www.google.com"));
        request.provider(new UriProvider(request));
		request.addProcessor(new BitmapProcessor() {
			@Override
			public String getId() {
				return "eeek";
			}

			@Override
			public Bitmap process(Bitmap in) {
				return null;
			}
		});
		request.addProcessor(new BitmapProcessor() {
			@Override
			public String getId() {
				return "rrrrr";
			}

			@Override
			public Bitmap process(Bitmap in) {
				return null;
			}
		});
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;
		request.options(opts);

		SimpleKeyMaker keyMaker = new SimpleKeyMaker();
		Assert.assertEquals("e5123341", keyMaker.getSourceKey(request));
		Assert.assertEquals("e4df80e2", keyMaker.getResultKey(request));
	}

}