package com.climate.mirage.targets;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.Mirage;
import com.climate.mirage.R;
import com.climate.mirage.RoboManifestRunner;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.targets.animation.MirageAnimation;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class TextViewTargetTest {

    @Test
    public void testImageViewTarget() {
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
    public void testLifecycle() {
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(imageView);
        target.placeHolder(new ColorDrawable(Color.RED));
        target.error(new ColorDrawable(Color.BLUE));

        target.onPreparingLoad();
        Assert.assertNotNull(imageView.getDrawable());
        Assert.assertSame(target.getPlaceHolderDrawable(), imageView.getDrawable());

        target.onError(new IOException("test io"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertSame(target.getErrorDrawable(), imageView.getDrawable());

        imageView.setImageDrawable(null);
        Assert.assertNull(imageView.getDrawable());
        target.onResult(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888),
                Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNotNull(imageView.getDrawable());
    }

    @Test
    public void testFadeCreatesAnimation() {
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(imageView);
        target.placeHolder(new ColorDrawable(Color.RED));
        target.error(new ColorDrawable(Color.BLUE));
        Assert.assertNull(target.animation());
        target.fade();
        Assert.assertNotNull(target.animation());
    }

    @Test
    public void testDrawableChoices() {
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(imageView);
        target.placeHolder(new ColorDrawable(Color.BLUE));
        target.getPlaceHolderDrawable();

        target = new ImageViewTarget(imageView);
        target.placeHolder(R.drawable.abc_btn_radio_material);
        target.getPlaceHolderDrawable();

        target = new ImageViewTarget(imageView);
        target.error(new ColorDrawable(Color.RED));
        target.getErrorDrawable();

        target = new ImageViewTarget(imageView);
        target.placeHolder(R.drawable.abc_btn_radio_material);
        target.getPlaceHolderDrawable();

        // define both kinds of drawable should be an error
        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.placeHolder(new ColorDrawable(Color.BLUE));
        try {
            target.placeHolder(R.drawable.abc_btn_radio_material);
            Assert.fail("Can only allow 1 type of drawable");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

        // define both kinds of drawable should be an error
        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.placeHolder(R.drawable.abc_btn_radio_material);
        try {
            target.placeHolder(new ColorDrawable(Color.BLUE));
            Assert.fail("Can only allow 1 type of drawable");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.error(new ColorDrawable(Color.BLUE));
        try {
            target.error(R.drawable.abc_btn_radio_material);
            Assert.fail("Can only allow 1 type of drawable");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

        // define both kinds of drawable should be an error
        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.error(R.drawable.abc_btn_radio_material);
        try {
            target.error(new ColorDrawable(Color.BLUE));
            Assert.fail("Can only allow 1 type of drawable");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testOnPrepareChoices() {
        // a drawable
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(imageView);
        target.placeHolder(new ColorDrawable(Color.BLUE));
        target.onPreparingLoad();
        Assert.assertNotNull(imageView.getDrawable());

        // a resource id
        target = new ImageViewTarget(imageView);
        target.placeHolder(R.drawable.abc_btn_radio_material);
        target.onPreparingLoad();
        Assert.assertNotNull(imageView.getDrawable());

        // no drawable given
        target = new ImageViewTarget(imageView);
        Assert.assertNull(target.getPlaceHolderDrawable());
        target.onPreparingLoad();
        Assert.assertNull(imageView.getDrawable());
    }

    @Test
    public void testOnResult() {
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(imageView);
        target.placeHolder(new ColorDrawable(Color.BLUE));
        target.onPreparingLoad();
        target.onResult(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888),
                Mirage.Source.EXTERNAL, new MirageRequest());


        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.placeHolder(new ColorDrawable(Color.BLUE));
        target.fade();
        target.onPreparingLoad();
        target.onResult(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888),
                Mirage.Source.EXTERNAL, new MirageRequest());
    }

    @Test
    public void testOnResultWithCustomAnimation() {
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(imageView);
        target.placeHolder(new ColorDrawable(Color.BLUE));
        target.onPreparingLoad();
        target.onResult(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888),
                Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNotNull(imageView.getDrawable());

        //test animation doesnt set the drawable
        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.placeHolder(new ColorDrawable(Color.BLUE));
        target.animation(new MirageAnimation<ImageView>() {
            @Override
            public boolean animate(ImageView view, Drawable drawable, Mirage.Source source) {
                return false;
            }
        });
        target.onPreparingLoad();
        target.onResult(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888),
                Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNotNull(imageView.getDrawable());

        // test animation sets the drawable
        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.placeHolder(new ColorDrawable(Color.BLUE));
        target.animation(new MirageAnimation<ImageView>() {
            @Override
            public boolean animate(ImageView view, Drawable drawable, Mirage.Source source) {
                view.setImageDrawable(drawable);
                return true;
            }
        });
        target.onPreparingLoad();
        target.onResult(Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888),
                Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNotNull(imageView.getDrawable());
    }

    @Test
    public void testOnError() {
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(imageView);
        target.onError(new IOException("test exc"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNull(imageView.getDrawable());

        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.error(R.drawable.abc_btn_check_material);
        target.onError(new IOException("test exc"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNotNull(imageView.getDrawable());

        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.error(new ColorDrawable(Color.RED));
        target.onError(new IOException("test exc"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNotNull(imageView.getDrawable());
    }

    @Test
    public void testOnErrorWithAnimations() {
        ImageView imageView = new ImageView(RuntimeEnvironment.application);
        ImageViewTarget target = new ImageViewTarget(imageView);
        target.fade();
        target.onError(new IOException("test exc"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNull(imageView.getDrawable());

        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.error(R.drawable.abc_btn_check_material);
        target.fade();
        target.onError(new IOException("test exc"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNotNull(imageView.getDrawable());

        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.error(new ColorDrawable(Color.RED));
        target.fade();
        target.onError(new IOException("test exc"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNotNull(imageView.getDrawable());

        imageView = new ImageView(RuntimeEnvironment.application);
        target = new ImageViewTarget(imageView);
        target.error(new ColorDrawable(Color.RED));
        target.animation(new MirageAnimation<ImageView>() {
            @Override
            public boolean animate(ImageView view, Drawable drawable, Mirage.Source source) {
                view.setImageDrawable(drawable);
                return false;
            }
        });
        target.onError(new IOException("test exc"), Mirage.Source.EXTERNAL, new MirageRequest());
        Assert.assertNotNull(imageView.getDrawable());
    }


}