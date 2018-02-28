package com.climate.mirage.targets;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.Mirage;
import com.climate.mirage.RobolectricTest;
import com.climate.mirage.requests.MirageRequest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;

public class TextViewTargetTest extends RobolectricTest {

    @Test
    public void testTextViewTarget() {
        TextView textView = new TextView(RuntimeEnvironment.application);
        TextViewTarget target = new TextViewTarget(textView);
        Assert.assertNotNull(target.getView());
        target.placeHolder(new ColorDrawable(Color.RED));
        target.error(new ColorDrawable(Color.BLUE));
        Assert.assertNotNull(target.getPlaceHolderDrawable());
        Assert.assertTrue(target.getPlaceHolderResourceId() < 1);
        Assert.assertTrue(target.getErrorResourceId() < 1);
    }

    @Test
    public void testLifecycle_onPrepare() {
        // LEFT
        TextView textView = new TextView(RuntimeEnvironment.application);
        TextViewTarget target = new TextViewTarget(textView);
        target.location(TextViewTarget.LEFT);
        target.placeHolder(new ColorDrawable(Color.RED));

        target.onPreparingLoad();
        Assert.assertNotNull(textView.getCompoundDrawables()[0]);
        Assert.assertNull(textView.getCompoundDrawables()[1]);
        Assert.assertNull(textView.getCompoundDrawables()[2]);
        Assert.assertNull(textView.getCompoundDrawables()[3]);
        Assert.assertSame(target.getPlaceHolderDrawable(), textView.getCompoundDrawables()[0]);

        // TOP
        textView = new TextView(RuntimeEnvironment.application);
        target = new TextViewTarget(textView);
        target.location(TextViewTarget.TOP);
        target.placeHolder(new ColorDrawable(Color.RED));

        target.onPreparingLoad();
        Assert.assertNull(textView.getCompoundDrawables()[0]);
        Assert.assertNotNull(textView.getCompoundDrawables()[1]);
        Assert.assertNull(textView.getCompoundDrawables()[2]);
        Assert.assertNull(textView.getCompoundDrawables()[3]);
        Assert.assertSame(target.getPlaceHolderDrawable(), textView.getCompoundDrawables()[1]);

        // RIGHT
        textView = new TextView(RuntimeEnvironment.application);
        target = new TextViewTarget(textView);
        target.location(TextViewTarget.RIGHT);
        target.placeHolder(new ColorDrawable(Color.RED));

        target.onPreparingLoad();
        Assert.assertNull(textView.getCompoundDrawables()[0]);
        Assert.assertNull(textView.getCompoundDrawables()[1]);
        Assert.assertNotNull(textView.getCompoundDrawables()[2]);
        Assert.assertNull(textView.getCompoundDrawables()[3]);
        Assert.assertSame(target.getPlaceHolderDrawable(), textView.getCompoundDrawables()[2]);

        // BOTTOM
        textView = new TextView(RuntimeEnvironment.application);
        target = new TextViewTarget(textView);
        target.location(TextViewTarget.BOTTOM);
        target.placeHolder(new ColorDrawable(Color.RED));

        target.onPreparingLoad();
        Assert.assertNull(textView.getCompoundDrawables()[0]);
        Assert.assertNull(textView.getCompoundDrawables()[1]);
        Assert.assertNull(textView.getCompoundDrawables()[2]);
        Assert.assertNotNull(textView.getCompoundDrawables()[3]);
        Assert.assertSame(target.getPlaceHolderDrawable(), textView.getCompoundDrawables()[3]);

        // TOP - null holders
        textView = new TextView(RuntimeEnvironment.application);
        target = new TextViewTarget(textView);
        target.location(TextViewTarget.TOP);

        target.onPreparingLoad();
        Assert.assertNull(textView.getCompoundDrawables()[0]);
        Assert.assertNull(textView.getCompoundDrawables()[1]);
        Assert.assertNull(textView.getCompoundDrawables()[2]);
        Assert.assertNull(textView.getCompoundDrawables()[3]);
        Assert.assertNull(target.getPlaceHolderDrawable());
    }

    @Test
    public void testLifecycle_onError() {
        // LEFT
        TextView textView = new TextView(RuntimeEnvironment.application);
        TextViewTarget target = new TextViewTarget(textView);
        target.location(TextViewTarget.LEFT);
        target.error(new ColorDrawable(Color.BLUE));

        target.onError(new IOException("test io"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNotNull(textView.getCompoundDrawables()[0]);
        Assert.assertNull(textView.getCompoundDrawables()[1]);
        Assert.assertNull(textView.getCompoundDrawables()[2]);
        Assert.assertNull(textView.getCompoundDrawables()[3]);
        Assert.assertSame(target.getErrorDrawable(), textView.getCompoundDrawables()[0]);

        // TOP
        textView = new TextView(RuntimeEnvironment.application);
        target = new TextViewTarget(textView);
        target.location(TextViewTarget.TOP);
        target.error(new ColorDrawable(Color.BLUE));

        target.onError(new IOException("test io"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNull(textView.getCompoundDrawables()[0]);
        Assert.assertNotNull(textView.getCompoundDrawables()[1]);
        Assert.assertNull(textView.getCompoundDrawables()[2]);
        Assert.assertNull(textView.getCompoundDrawables()[3]);
        Assert.assertSame(target.getErrorDrawable(), textView.getCompoundDrawables()[1]);

        // RIGHT
        textView = new TextView(RuntimeEnvironment.application);
        target = new TextViewTarget(textView);
        target.location(TextViewTarget.RIGHT);
        target.error(new ColorDrawable(Color.BLUE));

        target.onError(new IOException("test io"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNull(textView.getCompoundDrawables()[0]);
        Assert.assertNull(textView.getCompoundDrawables()[1]);
        Assert.assertNotNull(textView.getCompoundDrawables()[2]);
        Assert.assertNull(textView.getCompoundDrawables()[3]);
        Assert.assertSame(target.getErrorDrawable(), textView.getCompoundDrawables()[2]);

        // BOTTOM
        textView = new TextView(RuntimeEnvironment.application);
        target = new TextViewTarget(textView);
        target.location(TextViewTarget.BOTTOM);
        target.error(new ColorDrawable(Color.BLUE));

        target.onError(new IOException("test io"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNull(textView.getCompoundDrawables()[0]);
        Assert.assertNull(textView.getCompoundDrawables()[1]);
        Assert.assertNull(textView.getCompoundDrawables()[2]);
        Assert.assertNotNull(textView.getCompoundDrawables()[3]);
        Assert.assertSame(target.getErrorDrawable(), textView.getCompoundDrawables()[3]);

        // TOP - null holders
        textView = new TextView(RuntimeEnvironment.application);
        target = new TextViewTarget(textView);
        target.location(TextViewTarget.TOP);

        target.onError(new IOException("test io"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNull(textView.getCompoundDrawables()[0]);
        Assert.assertNull(textView.getCompoundDrawables()[1]);
        Assert.assertNull(textView.getCompoundDrawables()[2]);
        Assert.assertNull(textView.getCompoundDrawables()[3]);
        Assert.assertNull(target.getPlaceHolderDrawable());
    }

    @Test
    public void testBounds() {
        TextView textView = new TextView(RuntimeEnvironment.application);
        TextViewTarget target = new TextViewTarget(textView);
        target.placeHolder(new ColorDrawable(Color.RED));
        target.error(new ColorDrawable(Color.BLUE));
        target.location(TextViewTarget.LEFT);
        target.bounds(0, 0, 80, 80);
        target.onPreparingLoad();

        Assert.assertEquals(new Rect(0, 0, 80, 80), textView.getCompoundDrawables()[0].getBounds());
    }

    @Test
    public void testBounds_unset() {
        TextView textView = new TextView(RuntimeEnvironment.application);
        TextViewTarget target = new TextViewTarget(textView);
        target.placeHolder(new ColorDrawable(Color.RED));
        target.location(TextViewTarget.LEFT);
        target.onPreparingLoad();

        Assert.assertTrue(textView.getCompoundDrawables()[0].getBounds().isEmpty());
        Assert.assertEquals(-1, textView.getCompoundDrawables()[0].getIntrinsicWidth());
        Assert.assertEquals(-1, textView.getCompoundDrawables()[0].getIntrinsicHeight());

        textView = new TextView(RuntimeEnvironment.application);
        target = new TextViewTarget(textView);
        target.placeHolder(new BitmapDrawable(
                RuntimeEnvironment.application.getResources(),
                Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)));
        target.location(TextViewTarget.LEFT);
        target.onPreparingLoad();

        Assert.assertEquals(400, textView.getCompoundDrawables()[0].getIntrinsicWidth());
        Assert.assertEquals(300, textView.getCompoundDrawables()[0].getIntrinsicHeight());
    }

    @Test
    public void testBounds_setOnIntrinisic() {
        TextView textView = new TextView(RuntimeEnvironment.application);
        TextViewTarget target = new TextViewTarget(textView);
        target.placeHolder(new BitmapDrawable(
                RuntimeEnvironment.application.getResources(),
                Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)));
        target.location(TextViewTarget.LEFT);
        target.bounds(0, 0, 40, 30);
        target.onPreparingLoad();

        Assert.assertEquals(new Rect(0, 0, 40, 30), textView.getCompoundDrawables()[0].getBounds());
        Assert.assertEquals(400, textView.getCompoundDrawables()[0].getIntrinsicWidth());
        Assert.assertEquals(300, textView.getCompoundDrawables()[0].getIntrinsicHeight());
    }

    @Test
    public void testFadeCreatesAnimation() {
        TextView textView = new TextView(RuntimeEnvironment.application);
        TextViewTarget target = new TextViewTarget(textView);
        target.placeHolder(new ColorDrawable(Color.RED));
        target.error(new ColorDrawable(Color.BLUE));
        Assert.assertNull(target.animation());
        target.fade();
        Assert.assertNotNull(target.animation());
    }

    @Test
    public void testOnResult() {
        TextView textView = new TextView(RuntimeEnvironment.application);
        TextViewTarget target = new TextViewTarget(textView);
        target.location(TextViewTarget.LEFT);
        target.bounds(0, 0, 300, 200);
        target.placeHolder(new ColorDrawable(Color.BLUE));
        target.onPreparingLoad();
        target.onResult(Bitmap.createBitmap(300, 200, Bitmap.Config.ARGB_8888),
                Mirage.Source.EXTERNAL, new MirageRequest());

        Assert.assertEquals(new Rect(0, 0, 300, 200), textView.getCompoundDrawables()[0].getBounds());
        Assert.assertEquals(300, textView.getCompoundDrawables()[0].getIntrinsicWidth());
        Assert.assertEquals(200, textView.getCompoundDrawables()[0].getIntrinsicHeight());
        Assert.assertTrue(textView.getCompoundDrawables()[0] instanceof BitmapDrawable);
        Assert.assertNull(textView.getCompoundDrawables()[1]);
        Assert.assertNull(textView.getCompoundDrawables()[2]);
        Assert.assertNull(textView.getCompoundDrawables()[3]);
    }

}