package com.climate.mirage.targets.animation;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;

import com.climate.mirage.BuildConfig;
import com.climate.mirage.Mirage;
import com.climate.mirage.RoboManifestRunner;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;

@RunWith(RoboManifestRunner.class)
@Config(constants = BuildConfig.class)
public class ImageViewCrossFadeAnimationTest {

    @Test
    public void testImageViewTarget() {
        ImageViewCrossFadeAnimation anim = new ImageViewCrossFadeAnimation();
        ImageView imageView = Mockito.mock(ImageView.class);

        boolean setsDirectly = anim.animate(imageView,
                new ColorDrawable(Color.BLUE), Mirage.Source.DISK);

        Assert.assertTrue(setsDirectly);
        Mockito.verify(imageView, Mockito.times(1)).getDrawable();
    }

    @Test
    public void testSourceAsMemory() {
        ImageViewCrossFadeAnimation anim = new ImageViewCrossFadeAnimation();
        ImageView imageView = Mockito.mock(ImageView.class);

        boolean setsDirectly = anim.animate(imageView,
                new ColorDrawable(Color.BLUE), Mirage.Source.MEMORY);

        Assert.assertFalse(setsDirectly);
        Mockito.verify(imageView, Mockito.times(0)).getDrawable();
    }

}