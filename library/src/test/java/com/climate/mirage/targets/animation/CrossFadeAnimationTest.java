package com.climate.mirage.targets.animation;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

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
public class CrossFadeAnimationTest {

    @SuppressLint("NewApi")
    @Test
    public void testImageViewTarget() {
        CrossFadeAnimation anim = new CrossFadeAnimation();
        View view = Mockito.mock(View.class);

        boolean setsDirectly = anim.animate(view,
                new ColorDrawable(Color.BLUE), Mirage.Source.DISK);

        Assert.assertTrue(setsDirectly);
        Mockito.verify(view, Mockito.times(1)).getBackground();
        Mockito.verify(view, Mockito.times(1)).setBackground((Drawable) Mockito.any());
    }

    @Test
    public void testSourceAsMemory() {
        CrossFadeAnimation anim = new CrossFadeAnimation();
        View view = Mockito.mock(View.class);

        boolean setsDirectly = anim.animate(view,
                new ColorDrawable(Color.BLUE), Mirage.Source.MEMORY);

        Assert.assertFalse(setsDirectly);
        Mockito.verify(view, Mockito.times(0)).getBackground();
    }

}