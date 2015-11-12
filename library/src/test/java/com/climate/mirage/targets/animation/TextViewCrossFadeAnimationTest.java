package com.climate.mirage.targets.animation;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

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
public class TextViewCrossFadeAnimationTest {

    @Test
    public void testImageViewTarget() {
        TextViewDrawableCrossFadeAnimation anim = new TextViewDrawableCrossFadeAnimation();
        TextView textView = Mockito.mock(TextView.class);
        Mockito.when(textView.getCompoundDrawables()).thenReturn(new Drawable[4]);

        boolean setsDirectly = anim.animate(textView,
                new ColorDrawable(Color.BLUE), Mirage.Source.DISK);

        Assert.assertTrue(setsDirectly);
        Mockito.verify(textView, Mockito.times(1))
                .setCompoundDrawablesWithIntrinsicBounds(
                        (Drawable)Mockito.anyObject(),
                        (Drawable)Mockito.isNull(),
                        (Drawable)Mockito.anyObject(),
                        (Drawable)Mockito.anyObject());
    }

    @Test
    public void testSourceAsMemory() {
        TextViewDrawableCrossFadeAnimation anim = new TextViewDrawableCrossFadeAnimation();
        TextView textView = Mockito.mock(TextView.class);
        Mockito.when(textView.getCompoundDrawables()).thenReturn(new Drawable[4]);

        boolean setsDirectly = anim.animate(textView,
                new ColorDrawable(Color.BLUE), Mirage.Source.MEMORY);

        Assert.assertFalse(setsDirectly);
        Mockito.verify(textView, Mockito.times(0)).getCompoundDrawables();
    }

}