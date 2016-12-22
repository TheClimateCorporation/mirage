package com.climate.mirage.targets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.view.ViewTreeObserver;

import com.climate.mirage.Mirage;
import com.climate.mirage.requests.MirageRequest;
import com.climate.mirage.targets.animation.CrossFadeAnimation;
import com.climate.mirage.targets.animation.MirageAnimation;
import com.climate.mirage.targets.drawables.DrawableFactory;
import com.climate.mirage.tasks.MirageTask;

import java.lang.ref.WeakReference;

/**
 * The basic implementation of {@link com.climate.mirage.targets.Target} for images
 * into a view widget.
 */
abstract public class ViewTarget<V extends View> implements Target<Bitmap>, DrawableFactory {

	private MirageRequest request;
	private WeakReference<V> viewRef;
	private int placeHolderResourceId = 0;
	private Drawable placeHolderDrawable;
	private int errorResourceId = 0;
	private Drawable errorDrawable;
	private MirageAnimation animation; // TODO: add the generic back into this
 	private DrawableFactory successDrawableFactory;
    private boolean isFit = false;
    private MirageTask<Void, Void, Bitmap> mirageTask;

	public ViewTarget(V view) {
		this(null, view);
	}

	public ViewTarget(MirageRequest request, V view) {
		this.request = request;
		viewRef = new WeakReference<>(view); // must it be weak?
	}

	public V getView() {
		return viewRef.get();
	}

	/**
	 * Sets a place holder to be used when loading of the resource begins
	 *
	 * @param resourceId a resource id to use
	 * @return reference to daisy chain
	 */
	public ViewTarget<V> placeHolder(@DrawableRes int resourceId) {
		if (placeHolderDrawable != null) throw new IllegalStateException("Can only define 1 placeHolder");
		this.placeHolderResourceId = resourceId;
		return this;
	}

	/**
	 * Sets a place holder to be used when loading of the resource begins
	 *
	 * @param drawable a drawable to use when loading begins
	 * @return reference to daisy chain
	 */
	public ViewTarget<V> placeHolder(Drawable drawable) {
		if (placeHolderResourceId != 0) throw new IllegalStateException("Can only define 1 placeHolder");
		this.placeHolderDrawable = drawable;
		return this;
	}

	/**
	 * Sets a place holder to be used when an error happens
	 *
	 * @param resourceId a resource id to use
	 * @return reference to daisy chain
	 */
	public ViewTarget<V> error(@DrawableRes int resourceId) {
		if (errorDrawable != null) throw new IllegalStateException("Can only define 1 error");
		this.errorResourceId = resourceId;
		return this;
	}

	/**
	 * Sets a place holder to be used when an error happens
	 *
	 * @param drawable a drawable to use for the error
	 * @return reference to daisy chain
	 */
	public ViewTarget<V> error(Drawable drawable) {
		if (errorResourceId != 0) throw new IllegalStateException("Can only define 1 error");
		this.errorDrawable = drawable;
		return this;
	}

	/**
	 * Sets a custom animation to use for when the resource has loaded
	 *
	 * @param animation animation to use
	 * @return reference to daisy chain
	 */
	public ViewTarget<V> animation(MirageAnimation<V> animation) {
		this.animation = animation;
		return this;
	}

	public MirageAnimation<V> animation() {
		return animation;
	}

	/**
	 * A convenience method to create a fading animation
	 *
	 * @return reference to daisy chain
	 */
	public ViewTarget<V> fade() {
		this.animation = new CrossFadeAnimation();
		return this;
	}

	public ViewTarget<V> fit() {
        isFit = true;
		return this;
	}

	private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

	/**
	 * If the default {@link android.graphics.drawable.BitmapDrawable} is not desired
	 * set a drawable factory which converts the loaded resource into a drawable object
	 *
	 * @param factory the factory for the drawable
	 * @return reference to daisy chain
	 */
	public ViewTarget<V> drawableFactory(DrawableFactory factory) {
		successDrawableFactory = factory;
		return this;
	}

	/**
	 * Fires the request off asynchronously.
	 */
	public MirageTask go() {
        if (request == null)
            throw new IllegalStateException("Can not call this method without settings request instance");
        if (isFit) {
            View v = getView();
            if (v != null) {
                if (v.getWidth() > 0 && v.getHeight() > 0) {
                    request.dynamicResample(Math.max(v.getWidth(), v.getHeight()));
                    request.resize(v.getWidth(), v.getHeight());
                } else {
                    fitImageLater();
                    mirageTask = request.createGoTask();
                    return mirageTask;
                }
            }
        }

		return request.go();
	}

	public Drawable getPlaceHolderDrawable() {
		return placeHolderDrawable;
	}

	public int getPlaceHolderResourceId() {
		return placeHolderResourceId;
	}

	public Drawable getErrorDrawable() {
		return errorDrawable;
	}

	public int getErrorResourceId() {
		return errorResourceId;
	}

	@Override
	public void onPreparingLoad() {
		V view = getView();
        if (view != null) {
            cleanUpLayoutListener();
            onPreparingLoad(view);
		}
	}

	@Override
	public void onResult(Bitmap bitmap, Mirage.Source source, MirageRequest request) {
		V view = getView();
		if (view != null) {
			onResult(view, createDrawable(view.getContext(), bitmap), source, request);
		}
	}

	@Override
	public void onError(Exception e, Mirage.Source source, MirageRequest request) {
		V view = getView();
		if (view != null) {
			onError(view, e, source, request);
		}
	}

    @Override
    public void onCancel() {
        mirageTask = null;
        cleanUpLayoutListener();
    }

	@Override
	public Drawable createDrawable(Context context, Bitmap bitmap) {
		if (successDrawableFactory != null) {
			return successDrawableFactory.createDrawable(context, bitmap);
		} else {
			return new BitmapDrawable(context.getResources(), bitmap);
		}
	}

	protected void onPreparingLoad(V view) {

	}

	protected void onResult(V view, Drawable drawable, Mirage.Source source, MirageRequest request) {

	}

	protected void onError(V view, Exception e, Mirage.Source source, MirageRequest request) {

	}

    private void fitImageLater() {
        View v = getView();

        if (onGlobalLayoutListener == null) {
            onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    View v = getView();
                    if (v != null && v.getWidth() > 0 && v.getHeight() > 0) {
                        cleanUpLayoutListener();
                        // note: request is a pooled object. If the task was canceled
                        // this object is back in the object pool and it would be modifying
                        // someone else's call. No no....
                        if (mirageTask != null && !mirageTask.isCancelled()) {
                            request.dynamicResample(Math.max(v.getWidth(), v.getHeight()));
                            request.resize(v.getWidth(), v.getHeight());
                            if (mirageTask != null) {
                                request.executeGoTask(mirageTask);
                                mirageTask = null;
                            }
                        }
                    }
                }
            };
        }
        v.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    private void cleanUpLayoutListener() {
        View v = getView();
        if (onGlobalLayoutListener != null && v != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            } else {
                v.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
            }
        }
        onGlobalLayoutListener = null;
    }

}
