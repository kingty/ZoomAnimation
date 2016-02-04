package com.kingty.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by kingty on 16/1/29.
 */
public class Attacher implements IAttacher {
	public static final String TAG = "TZoomAnimation";
	private View view;
	private View preView;
	private String key = null;
	public static final int DEFAULT_DURATION = 800;
	private static final int WIDTH_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN = 0;
	private static final int HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN = 1;
	private static final int WIDTH_FULL_VIEW_PREVIEW_CLIP_LEFT_AND_RIGHT = 2;

	private float rectX, rectWidth, rectY, rectHeight;

	private float preViewWidth, preViewHeight, preViewX, preViewY;

	private float originWidth, originHeight;

	private float translationX, translationY;

	private boolean isClip;

	private float clipL, clipT, clipR, clipB;


	public Attacher(View view) {
		this.view = view;
	}

	public void setOriginalHeight(float originHeight) {
		this.originHeight = originHeight;
	}

	public void setOriginalWidth(float originWidth) {
		this.originWidth = originWidth;
	}


	public void callOnDrawBeforeSuper(@NonNull Canvas canvas) {
		if (isClip)
			canvas.clipRect(clipL, clipT, clipR, clipB);
	}


	private View isVisibleInPreActivity(IAttacher view, ViewGroup viewGroup) {

		List<View> children = ViewUtil.childsRecursive(viewGroup);
		for (View child : children) {
			if (child instanceof IAttacher) {

				if (((IAttacher) child).zoomAnimationKey() != null
						&& view.zoomAnimationKey() != null
						&& ((IAttacher) child).zoomAnimationKey().equals(view.zoomAnimationKey())) {
					ViewUtil.Pos fromPos = ViewUtil.pos(child);
					preViewWidth = fromPos.w;
					preViewHeight = fromPos.h;
					preViewX = fromPos.x;
					preViewY = fromPos.y - (ViewUtil.dp((View) view, 25));
					return child;
				}
			}
		}
		return null;
	}

	public ValueAnimator zoomInAnimation(ViewGroup viewGroup, CallBack callBack) {

		return zoomAnimation(true, viewGroup, callBack);
	}

	public ValueAnimator zoomOutAnimation(ViewGroup viewGroup, CallBack callBack) {
		return zoomAnimation(false, viewGroup, callBack);

	}

	public String zoomAnimationKey() {
		return key;
	}

	public void setZoomAnimationKey(String key) {

		this.key = key;
	}

	private ValueAnimator zoomAnimation(boolean in, ViewGroup viewGroup, CallBack callBack) {
		preView = isVisibleInPreActivity((IAttacher) view, viewGroup);

		if ((view instanceof IAttacher) && preView != null) {
			ViewUtil.Pos fromPos = ViewUtil.pos(preView);
			preViewWidth = fromPos.w;
			preViewHeight = fromPos.h;
			preViewX = fromPos.x;
			preViewY = fromPos.y - ViewUtil.statusBarHeight(viewGroup.getContext());

			preView.postDelayed(
					new Runnable() {
						@Override
						public void run() {
							if (preView != null) {
								preView.setVisibility(View.INVISIBLE);
							}
						}
					}, 10);
			return zoomAnimation(in, callBack);
		} else {
			return null;
		}

	}


	private void setScale(float scale) {
		view.setScaleX(scale);
		view.setScaleY(scale);
	}

	private float ratioZoomPreViewToView() {
		if (ratioView() > ratioOrigin()) {

			return preViewWidth / (getViewHeight() * ratioOrigin());
		} else {
			if (originHeight > originWidth) {
				return preViewWidth / getViewWidth();

			} else {
				return preViewHeight / (getViewWidth() / ratioOrigin());
			}
		}
	}

	private float ratioPreView() {
		return preViewWidth / preViewHeight;
	}

	private float ratioOrigin() {
		return originWidth / originHeight;
	}

	private float ratioView() {
		return getViewWidth() / getViewHeight();
	}

	private float getViewWidth() {
		return (float) view.getWidth();
	}

	private float getViewHeight() {
		return (float) view.getHeight();
	}

	private float getRealWidthInView() {
		if (viewType() == HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
			return getViewHeight() * ratioOrigin();
		} else {
			return getViewWidth();
		}

	}

	private float getRealHeightInView() {
		if (viewType() == HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
			return getViewHeight();
		} else {
			return getViewWidth() / ratioOrigin();
		}
	}

	private int viewType() {
		if (ratioPreView() > ratioOrigin()) { //clip up and down

			if (originWidth > originHeight) {//in case the almost same ratio
				return WIDTH_FULL_VIEW_PREVIEW_CLIP_LEFT_AND_RIGHT;//in case of the warn
			}

			if (originHeight / originWidth < getViewHeight() / getViewWidth()) {//width full screen

				return WIDTH_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN;
			} else {
				return HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN;
			}
		} else {//clip left and right
			if (originHeight > originWidth) {//in case the almost same ratio
				if (originHeight / originWidth < getViewHeight() / getViewWidth()) {//width full screen

					return WIDTH_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN;
				} else {
					return HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN;
				}
			}
			return WIDTH_FULL_VIEW_PREVIEW_CLIP_LEFT_AND_RIGHT;
		}
	}


	//use when not implements the interface in some case
	public ValueAnimator zoomAnimation(boolean in, float preViewWidth, float preViewHeight, float preViewX, float preViewY, CallBack callBack) {
		this.preViewHeight = preViewHeight;
		this.preViewWidth = preViewWidth;
		this.preViewX = preViewX;
		this.preViewY = preViewY;
		return zoomAnimation(in, callBack);
	}

	private ValueAnimator zoomAnimation(final boolean in, final CallBack callBack) {
		isClip = true;
		final float preZoom = ratioZoomPreViewToView();
		translationX = (preViewX + preViewWidth / 2) - getViewWidth() / 2;
		translationY = (preViewY + preViewHeight / 2) - getViewHeight() / 2;
		ValueAnimator anim;
		anim = ValueAnimator.ofFloat(0, 1).setDuration(DEFAULT_DURATION);
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
						if (callBack != null) {
							callBack.call(animation.getAnimatedFraction());
						}
					}
				});
		anim.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (in) isClip = false;
				if (in) {
					if (preView != null) {
						preView.post(
								new Runnable() {
									@Override
									public void run() {
										if (preView != null) {
											preView.setVisibility(View.VISIBLE);
											preView = null;
										}
									}
								});
					}
				} else {
					if (preView != null) {
						preView.setVisibility(View.VISIBLE);
						preView = null;
					}
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

	private void initInClipOnlyVisiblePart(float t) {

		float left, top, right, bottom;
		Rect visibleRect = new Rect();
		preView.getLocalVisibleRect(visibleRect);
		left = visibleRect.left;
		right = visibleRect.right;
		top = visibleRect.top;
		bottom = visibleRect.bottom;

		if (top != 0) {//top part is invisible
			float ratio = top / preViewHeight;
			float realHeight = getRealHeightInView();
			if (t != 1) {
				if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_LEFT_AND_RIGHT) {
					clipT = (getViewHeight() - realHeight) / 2 + realHeight * ratio * (1 - t);
				} else {
					float scaleheight = 0;
					if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
						scaleheight = (getRealHeightInView() / ratioPreView()) * ratioOrigin();
					} else if (viewType() == HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
						scaleheight = getRealWidthInView() / ratioPreView();
					}

					float moreClipT = scaleheight * ratio * (1 - t);
					clipT = rectY + moreClipT;
				}
			} else {
				clipT = rectY;
			}
		} else if (bottom != preViewHeight) {//bottom part is invisible
			float ratio = (preViewHeight - bottom) / preViewHeight;
			float realHeight = getRealHeightInView();
			if (t != 1) {
				if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_LEFT_AND_RIGHT) {
					clipB = rectY + rectHeight - ((getViewHeight() - realHeight) / 2 + realHeight * ratio * (1 - t));
				} else {
					float scaleheight = 0;
					if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
						scaleheight = (getRealHeightInView() / ratioPreView()) * ratioOrigin();
					} else if (viewType() == HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
						scaleheight = getRealWidthInView() / ratioPreView();
					}

					float moreClipT = scaleheight * ratio * (1 - t);
					clipB = rectY + rectHeight - moreClipT;
				}
			} else {
				clipT = rectY + rectHeight;
			}
		}


	}

	private void initOutClipOnlyVisiblePart(float t) {
		float left, top, right, bottom;
		Rect visibleRect = new Rect();
		preView.getLocalVisibleRect(visibleRect);
		left = visibleRect.left;
		right = visibleRect.right;
		top = visibleRect.top;
		bottom = visibleRect.bottom;

		if (top != 0) {
			float ratio = top / preViewHeight;
			float realHeight = getRealHeightInView();

			if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_LEFT_AND_RIGHT) {
				clipT = (getViewHeight() - realHeight) / 2 + realHeight * ratio * t;
			} else {
				float scaleheight = 0;
				if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
					scaleheight = (getRealHeightInView() / ratioPreView()) * ratioOrigin();
				} else if (viewType() == HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
					scaleheight = getRealWidthInView() / ratioPreView();
				}
				float moreClipT = scaleheight * ratio * t;
				clipT = rectY + moreClipT;
			}
		} else if (bottom != preViewHeight) {//bottom part is invisible
			float ratio = (preViewHeight - bottom) / preViewHeight;
			float realHeight = getRealHeightInView();

			if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_LEFT_AND_RIGHT) {
				clipB = rectY + rectHeight - ((getViewHeight() - realHeight) / 2 + realHeight * ratio * t);
			} else {
				float scaleheight = 0;
				if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
					scaleheight = (getRealHeightInView() / ratioPreView()) * ratioOrigin();
				} else if (viewType() == HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
					scaleheight = getRealWidthInView() / ratioPreView();
				}
				float moreClipT = scaleheight * ratio * t;
				clipB = rectY + rectHeight - moreClipT;
			}
		}

	}

	private void initInAttr(float t, float mZoomStart) {
		if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
			float scaleHeight = (getRealHeightInView() / ratioPreView()) * ratioOrigin();
			rectHeight = scaleHeight + (getViewHeight() - scaleHeight) * t;
			rectWidth = getViewWidth();
			rectX = 0;
			rectY = (getViewHeight() - rectHeight) / 2;
		} else if (viewType() == HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
			float scaleheight = getRealWidthInView() / ratioPreView();
			rectHeight = scaleheight + (getViewHeight() - scaleheight) * t;
			rectWidth = getViewWidth();
			rectX = 0;
			rectY = (getViewHeight() - rectHeight) / 2;
		} else if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_LEFT_AND_RIGHT) {
			float scaleWidth = getRealHeightInView() * ratioPreView();
			rectWidth = scaleWidth + (getViewWidth() - scaleWidth) * t;
			rectHeight = getViewHeight();
			rectY = 0;
			rectX = (getViewWidth() - rectWidth) / 2;
		}

		float scale = mZoomStart + t * (1 - mZoomStart);
		setScale(scale);

		view.setPivotX(getViewWidth() / 2);
		view.setPivotY(getViewHeight() / 2);
		view.setTranslationX(translationX * (1 - t));
		view.setTranslationY(translationY * (1 - t));

		clipL = rectX;
		clipT = rectY;
		clipR = rectX + rectWidth;
		clipB = rectY + rectHeight;

		initInClipOnlyVisiblePart(t);
	}


	private void initOutAttr(float t, float mZoomEnd) {
		if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
			float scaleHeight = (getRealHeightInView() / ratioPreView()) * ratioOrigin();
			rectHeight = getViewHeight() - (getViewHeight() - scaleHeight) * t;
			rectWidth = getViewWidth();
			rectX = 0;
			rectY = (getViewHeight() - rectHeight) / 2;
		} else if (viewType() == HEIGHT_FULL_VIEW_PREVIEW_CLIP_UP_AND_DOWN) {
			float scaleheight = getRealWidthInView() / ratioPreView();
			rectHeight = getViewHeight() - (getViewHeight() - scaleheight) * t;
			rectWidth = getViewWidth();
			rectX = 0;
			rectY = (getViewHeight() - rectHeight) / 2;
		} else if (viewType() == WIDTH_FULL_VIEW_PREVIEW_CLIP_LEFT_AND_RIGHT) {

			float scaleWidth = getRealHeightInView() * ratioPreView();

			rectWidth = getViewWidth() - (getViewWidth() - scaleWidth) * t;
			rectHeight = getViewHeight();
			rectY = 0;
			rectX = (getViewWidth() - rectWidth) / 2;
		}

		float scale = 1 - (1 - mZoomEnd) * t;
		setScale(scale);

		view.setPivotX(getViewWidth() / 2);
		view.setPivotY(getViewHeight() / 2);
		view.setTranslationX(translationX * t);
		view.setTranslationY(translationY * t);

		clipL = rectX;
		clipT = rectY;
		clipR = rectX + rectWidth;
		clipB = rectY + rectHeight;

		initOutClipOnlyVisiblePart(t);
	}


}
