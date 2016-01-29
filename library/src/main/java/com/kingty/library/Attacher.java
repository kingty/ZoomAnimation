package com.kingty.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by kingty on 16/1/29.
 */
public class Attacher implements IAttacher{
	View view;
	View preView;
	String key = null;
	public static final String TAG = "TZoomAnimation";
	private final int DURATION = 800;

	public Attacher(View view) {
		this.view = view;
	}

	private float rectX;
	private float rectWidth;
	private float rectY;
	private float rectHeight;

	private float preViewWidth;
	private float preViewHeight;
	private float preViewX;
	private float preViewY;

	private float originWidth;
	private float originHeight;

	float translationX;
	float translationY;
	private CallBack<Float> callBack;
	private boolean isClip;


	@Override
	public void setOriginHeight(float originHeight) {
		this.originHeight = originHeight;
	}
	@Override
	public void setOriginWidth(float originWidth) {
		this.originWidth = originWidth;
	}


	public void callOnDrawBeforeSuper(@NonNull Canvas canvas) {
		if (isClip)
			canvas.clipRect(rectX, rectY, rectX + rectWidth, rectY + rectHeight);
	}

	private View isVisibleInPreActivity(IAttacher view, ViewGroup viewGroup) {

		List<View> children = ViewUtil.childsRecursive(viewGroup);
		for (View child : children) {
			if (child instanceof IAttacher) {

				if (((IAttacher) child).key() != null
						&& view.key() != null
						&& ((IAttacher) child).key().equals(view.key())) {
					ViewUtil.Pos fromPos = ViewUtil.pos(child);
					preViewWidth = fromPos.w;
					preViewHeight = fromPos.h;
					preViewX = fromPos.x;
					preViewY = fromPos.y - (ViewUtil.dp((View) view,25));
					return child;
				}
			}
		}
		return null;
	}
	@Override
	public ValueAnimator zoomInAnimation(ViewGroup viewGroup, CallBack<Float> callBack) {

		return zoomAnimation(true, viewGroup, callBack);
	}
	@Override
	public ValueAnimator zoomOutAnimation(ViewGroup viewGroup, CallBack<Float> callBack) {
		return zoomAnimation(false, viewGroup, callBack);

	}
	@Override
	public String key() {
		return key;
	}
	@Override
	public void setKey(String key) {

		this.key = key;
	}

	private ValueAnimator zoomAnimation(boolean in, ViewGroup viewGroup, CallBack<Float> callBack) {
		preView = isVisibleInPreActivity((IAttacher) view, viewGroup);

		if ((view instanceof IAttacher) && preView != null) {
			preView.setVisibility(View.INVISIBLE);
			return zoomAnimation(in, callBack);
		} else {
			return null;
		}

	}

	//use when not implements the interface in some case
	public ValueAnimator zoomAnimation(boolean in, float preViewWidth, float preViewHeight, float preViewX, float preViewY, CallBack<Float> callBack) {
		this.preViewHeight = preViewHeight;
		this.preViewWidth = preViewWidth;
		this.preViewX = preViewX;
		this.preViewY = preViewY;
		return zoomAnimation(in, callBack);
	}

	private ValueAnimator zoomAnimation(final boolean in, CallBack<Float> callBack) {
		isClip = true;
		final float preZoom;
		this.callBack = callBack;
		translationX = (preViewX + preViewWidth / 2) - view.getWidth() / 2;
		translationY = (preViewY + preViewHeight / 2) - view.getHeight() / 2;

		if (originHeight / originWidth > (float) view.getHeight() / (float) view.getWidth()) {

			preZoom = preViewWidth / ((float) view.getHeight() * (originWidth / originHeight));
		} else {
			if (originHeight > originWidth) {
				preZoom = preViewWidth / ((float) view.getWidth());

			} else {
				preZoom = preViewHeight / ((float) view.getWidth() * (originHeight / originWidth));
			}
		}
		ValueAnimator anim;
		anim = ValueAnimator.ofFloat(0, 1).setDuration(DURATION);
		anim.addUpdateListener(
				new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						if (in) {
							initInAttr(animation.getAnimatedFraction(), preZoom);
						} else {
							initOutAttr(animation.getAnimatedFraction(), preZoom);
						}
						view.invalidate();
					}
				});

		anim.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (in) isClip = false;
				if (preView != null) {
					preView.setVisibility(View.VISIBLE);
					preView = null;
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		});
		return anim;
	}


	private void setScale(float scale) {
		view.setScaleX(scale);
		view.setScaleY(scale);
	}

	private void initInAttr(float t, float mZoomStart) {
		float ratioPreView = preViewWidth / preViewHeight;
		float ratioOrigin = originWidth / originHeight;
		if (ratioPreView > ratioOrigin) {//clip up and down
			if (originHeight / originWidth < (float) view.getHeight() / (float) view.getWidth()) {//width full screen
				float realHeight = view.getWidth() * (originHeight / originWidth);
				float scaleHeight = (realHeight / ratioPreView) * ratioOrigin;
				rectHeight = scaleHeight + (view.getHeight() - scaleHeight) * t;
				rectWidth = view.getWidth();
				rectX = 0;
				rectY = (view.getHeight() - rectHeight) / 2;
			} else {
				float realWidth = view.getHeight() * (originWidth / originHeight);
				float scaleheight = (realWidth / preViewWidth) * preViewHeight;
				rectHeight = scaleheight + (view.getHeight() - scaleheight) * t;
				rectWidth = view.getWidth();
				rectX = 0;
				rectY = (view.getHeight() - rectHeight) / 2;
			}

		} else {//clip left and right

			float realHeight = view.getWidth() * (originHeight / originWidth);
			float scaleWidth = (realHeight / preViewHeight) * preViewWidth;
			rectWidth = scaleWidth + (view.getWidth() - scaleWidth) * t;
			rectHeight = view.getHeight();
			rectY = 0;
			rectX = (view.getWidth() - rectWidth) / 2;
		}

		float scale = mZoomStart + t * (1 - mZoomStart);
		setScale(scale);

		view.setPivotX(view.getWidth() / 2);
		view.setPivotY(view.getHeight() / 2);
		view.setTranslationX(translationX * (1 - t));
		view.setTranslationY(translationY * (1 - t));
		callBack.call(t);
	}


	private void initOutAttr(float t, float mZoomEnd) {
		float ratio1 = preViewWidth / preViewHeight;
		float ratio2 = originWidth / originHeight;
		if (ratio1 > ratio2) {//clip up and down
			if (originHeight / originWidth < (float) view.getHeight() / (float) view.getWidth()) {
				float realHeight = view.getWidth() * (originHeight / originWidth);
				float scaleHeight = (realHeight / ratio1) * ratio2;

				rectHeight = view.getHeight() - (view.getHeight() -scaleHeight) * t;
				rectWidth = view.getWidth();
				rectX = 0;
				rectY = (view.getHeight() - rectHeight) / 2;
			} else {
				float realWidth = view.getHeight() * (originWidth / originHeight);
				float scaleheight = (realWidth / preViewWidth) * preViewHeight;
				rectHeight = view.getHeight() - (view.getHeight() - scaleheight) * t;
				rectWidth = view.getWidth();
				rectX = 0;
				rectY = (view.getHeight() - rectHeight) / 2;
			}

		} else {//clip left and right

			float realHeight = view.getWidth() * (originHeight / originWidth);
			float scaleWidth = (realHeight / preViewHeight) * preViewWidth;
			rectWidth = view.getWidth() - (view.getWidth() - scaleWidth) * t;
			rectHeight = view.getHeight();
			rectY = 0;
			rectX = (view.getWidth() - rectWidth) / 2;
		}
		float scale = 1 - (1 - mZoomEnd) * t;
		setScale(scale);

		view.setPivotX(view.getWidth() / 2);
		view.setPivotY(view.getHeight() / 2);
		view.setTranslationX(translationX * t);
		view.setTranslationY(translationY * t);
		callBack.call(t);
	}


}
