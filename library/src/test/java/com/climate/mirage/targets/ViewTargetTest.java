package com.climate.mirage.targets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.Mirage;
import com.climate.mirage.RobolectricTest;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.targets.drawables.DrawableFactory;
import com.climate.mirage.tasks.MirageTask;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ViewTargetTest extends RobolectricTest {
    
    @Test
    public void testGo() {
        Mirage mirage = Mockito.mock(Mirage.class);
        MirageRequest request = new MirageRequest();
        request.mirage(mirage).uri(Uri.parse("http://www.anything.com"));

        Mockito.when(mirage.go((MirageRequest)Mockito.any())).thenReturn(Mockito.mock(MirageTask.class));

        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(request, imageView);
        target.go();
        Mockito.verify(mirage, Mockito.times(1)).go(request);
    }

    @Test
    public void testGoWithNoRequest() {
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(imageView);
        try {
            target.go();
            Assert.fail("This should thrown an error without a request on it");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testDrawableFactory() {
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(imageView);
        DrawableFactory factory = Mockito.mock(DrawableFactory.class);
        Mockito.when(
                    factory.createDrawable((Context) Mockito.any(), (Bitmap) Mockito.any()))
                .thenReturn(new ColorDrawable(Color.RED));
        target.drawableFactory(factory);
        Drawable d = target.createDrawable(RuntimeEnvironment.application,
                Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888));
        Assert.assertNotNull(d);
        Mockito.verify(factory, Mockito.times(1)).createDrawable((Context) Mockito.any(),
                (Bitmap) Mockito.any());
    }

    @Test
    public void testFadeCreatesAnimation() {
        ViewTarget<ImageView> target = new ViewTarget<ImageView>(new ImageView(RuntimeEnvironment.application)) {
            @Override
            protected void onResult(ImageView view, Drawable drawable, Mirage.Source source, MirageRequest request) {
                super.onResult(view, drawable, source, request);
            }
        };
        Assert.assertNull(target.animation());
        target.fade();
        Assert.assertNotNull(target.animation());
    }

    @Test
    public void testDelegatesToHooks() {
        final AtomicBoolean onResult = new AtomicBoolean(false);
        final AtomicBoolean onPrepare = new AtomicBoolean(false);
        final AtomicBoolean onFault = new AtomicBoolean(false);
        ViewTarget<View> target = new ViewTarget<View>(new View(RuntimeEnvironment.application)) {
            @Override
            protected void onResult(View view, Drawable drawable, Mirage.Source source, MirageRequest request) {
                onResult.set(true);
            }

            @Override
            protected void onPreparingLoad(View view) {
                onPrepare.set(true);
            }

            @Override
            protected void onError(View view, Exception e, Mirage.Source source, MirageRequest request) {
                onFault.set(true);
            }
        };
        target.onResult(Bitmap.createBitmap(200, 200,
                Bitmap.Config.ARGB_8888), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertTrue(onResult.get());

        target.onPreparingLoad();
        Assert.assertTrue(onPrepare.get());

        target.onError(new IOException("text exc"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertTrue(onFault.get());
    }

    @Test
    public void testNoDelegatesToHooks() {
        final AtomicBoolean onResult = new AtomicBoolean(false);
        final AtomicBoolean onPrepare = new AtomicBoolean(false);
        final AtomicBoolean onFault = new AtomicBoolean(false);
        ViewTarget<View> target = new ViewTarget<View>(null) {
            @Override
            protected void onResult(View view, Drawable drawable, Mirage.Source source, MirageRequest request) {
                onResult.set(true);
            }

            @Override
            protected void onPreparingLoad(View view) {
                onPrepare.set(true);
            }

            @Override
            protected void onError(View view, Exception e, Mirage.Source source, MirageRequest request) {
                onFault.set(true);
            }
        };
        target.onResult(Bitmap.createBitmap(200, 200,
                Bitmap.Config.ARGB_8888), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertFalse(onResult.get());

        target.onPreparingLoad();
        Assert.assertFalse(onPrepare.get());

        target.onError(new IOException("text exc"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertFalse(onFault.get());
    }

}