package com.climate.mirage.processors;

import android.graphics.Bitmap;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ResizeProcessor implements BitmapProcessor {

	public static final int STRATEGY_SCALE_FREE = 1;
	public static final int STRATEGY_SCALE_DOWN_ONLY = 2;
	public static final int STRATEGY_RATIO_MAINTAINED = 4;
	public static final int STRATEGY_RATIO_FREE = 8;

	@IntDef(
			flag = true,
			value = {STRATEGY_SCALE_FREE,
			STRATEGY_SCALE_DOWN_ONLY,
			STRATEGY_RATIO_MAINTAINED,
			STRATEGY_RATIO_FREE})
	@Retention(RetentionPolicy.SOURCE)
	public @interface Strategy {}

	private int width, height;
	private int scaleStrategy = STRATEGY_SCALE_FREE;

	public ResizeProcessor() {

	}

	public void setDimensions(int width, int height, @Strategy int scaleStrategy) {
		this.width = width;
		this.height = height;
		this.scaleStrategy =  scaleStrategy;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public String getId() {
		return "resize" + width +"x"+ height+"_"+scaleStrategy;
	}

	@Override
	public Bitmap process(Bitmap in) {
		int w;
		int h;
		if ((scaleStrategy & STRATEGY_SCALE_DOWN_ONLY) > 0) {
			if (in.getWidth() > width || in.getHeight() > height) {
				if ((scaleStrategy & STRATEGY_RATIO_MAINTAINED) > 0) {
					if (in.getWidth() > in.getHeight()) {
						w = width;
						h = (int)Math.ceil(width * (in.getHeight() / (float)in.getWidth()));
					} else {
						h = height;
						w = (int)Math.ceil(height * (in.getWidth() / (float)in.getHeight()));
					}
				} else {
					w = width;
					h = height;
				}
			} else {
				return in;
			}
		} else {
			if ((scaleStrategy & STRATEGY_RATIO_MAINTAINED) > 0) {
				if (in.getWidth() > in.getHeight()) {
					w = width;
					h = (int)Math.ceil(width * (in.getHeight() / (float)in.getWidth()));
				} else {
					h = height;
					w = (int)Math.ceil(height * (in.getWidth() / (float)in.getHeight()));
				}
			} else {
				w = width;
				h = height;
			}
		}

		Bitmap out = Bitmap.createScaledBitmap(in, w, h, false);
		if (out != in) in.recycle();
		return out;
	}
}