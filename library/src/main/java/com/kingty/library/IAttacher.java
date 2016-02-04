package com.kingty.library;

import android.animation.ValueAnimator;
import android.view.ViewGroup;

/**
 * Created by kingty on 16/1/29.
 */
public interface IAttacher {
	/**
	 * set the really height of view
	 * @param originHeight
	 */
	void setOriginalHeight(float originHeight);

	/**
	 * set the really width of view
	 * @param originWidth
	 */
	void setOriginalWidth(float originWidth);

	ValueAnimator zoomInAnimation(ViewGroup viewGroup, CallBack<Float> callBack);
	ValueAnimator zoomOutAnimation(ViewGroup viewGroup, CallBack<Float> callBack);

	/**
	 * get the key
	 * @return
	 */
	String zoomAnimationKey();

	/**
	 * set the key when two view in diffrent act ,to find the position
	 * @param key
	 */
	void setZoomAnimationKey(String key);
}
