package com.climate.mirage.targets.drawables;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;


/**
 * A drawable to turn a bitmap into a circular drawable. You can also pass in a
 * explict diameter and that diameter is bigger than the image, it will create
 * a nice clipping effect.
 *
 */
public class CircularDrawable extends Drawable {

	private Bitmap bitmap;
	private Paint paint;
	private BitmapShader shader;
	private Matrix matrix;
	private float centerX;
	private float centerY;
	private float diameter;
	private Paint borderPaint;
	private boolean useBorder = false;


	/**
	 * Default constructor. Pass the bitmap wished to be drawn. The largest diameter
	 * is automatically choosen
	 *
	 * @param bitmap
	 */
	public CircularDrawable(Bitmap bitmap) {
		this(bitmap, bitmap.getWidth() < bitmap.getHeight()
				? bitmap.getWidth() : bitmap.getHeight());
	}

	/**
	 *  Constructor.
	 *
	 * @param bitmap  Pass the bitmap wished to be drawn.
	 * @param explicitDiameter Pass an explict diameter of the circle. If the diameter is larger
	 *                         than any of the dimension of the bitmap, a nice clipping effect
	 *                         will occur. If the diameter is too big, there will be nice effect
	 *                         at all.
	 */
	public CircularDrawable(Bitmap bitmap, float explicitDiameter) {
		this.bitmap = bitmap;
		this.diameter = explicitDiameter;

		shader = new BitmapShader(bitmap,
				Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
		matrix = new Matrix();

		paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setShader(shader);

		borderPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setAntiAlias(true);
		borderPaint.setDither(true);

		centerX = bitmap.getWidth()/2f;
		centerY = bitmap.getHeight()/2f;
	}

	public float getDiameter() {
		return diameter;
	}

	public void setBorder(float borderWidth, int borderColor) {
		useBorder = true;
		borderPaint.setColor(borderColor);
		borderPaint.setStrokeWidth(borderWidth);
		invalidateSelf();
	}

	public void clearBorder() {
		useBorder = false;
		invalidateSelf();
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		centerX = bitmap.getWidth()/2f;
		centerY = bitmap.getHeight()/2f;

		float diameterMinX = Math.min(diameter, bitmap.getWidth());
		float diameterMinY = Math.min(diameter, bitmap.getHeight());
		matrix.reset();
		matrix.setTranslate(-centerX + diameterMinX / 2, -centerY + diameterMinY / 2);
		shader.setLocalMatrix(matrix);
	}

	@Override
	public void draw(Canvas canvas) {
		float offset = 0;
		if (useBorder) {
			offset = borderPaint.getStrokeWidth();
			float diam = getIntrinsicWidth()/2;
			canvas.drawCircle(diam, diam, diam - offset/2, borderPaint);
		}
		canvas.drawCircle(getIntrinsicWidth()/2 , getIntrinsicHeight()/2 , diameter/2, paint);

	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
		paint.setAlpha(alpha);
		invalidateSelf();
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		paint.setColorFilter(cf);
		invalidateSelf();
	}

	@Override
	public int getIntrinsicHeight() {
		int height = (int) Math.min(bitmap.getHeight(), diameter);
		if (useBorder) {
			height = height + (int)Math.ceil(borderPaint.getStrokeWidth());
		}

		int h = height;
		return h;
	}

	@Override
	public int getIntrinsicWidth() {
		int width = (int)Math.min(bitmap.getWidth(), diameter);
		if (useBorder) {
			width =  width + (int)Math.ceil(borderPaint.getStrokeWidth());
		}

		int w = width;
		return w;
	}

}