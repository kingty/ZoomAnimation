package com.kingty.zoomanimation;

import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.kingty.library.CallBack;

public class MainActivity extends AppCompatActivity {

	ZoomImageView test;
	ZoomImageView test1;
	ZoomImageView zoomImageView;
	RelativeLayout preViewGroup;
	RelativeLayout showView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_main);

		test = (ZoomImageView) findViewById(R.id.test);
		//this key shold be the same as the image which bigger,usually it is the url or the url's commom part
		test.setZoomAnimationKey("test");
		test1 = (ZoomImageView) findViewById(R.id.test1);
		test1.setZoomAnimationKey("test1");

		zoomImageView = (ZoomImageView) findViewById(R.id.zoomview);
		/**
		 * this is a sample ,preview can be a pre activity's dectorView
		 */
		preViewGroup = (RelativeLayout) findViewById(R.id.preView);
		/**
		 * showview can be a new activity
		 */
		showView = (RelativeLayout) findViewById(R.id.showView);


		/**
		 * this can be a transition betown two activitys,so not only this way ,this is just a sample
		 */
		test.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										showView.setVisibility(View.VISIBLE);
										showView.getBackground().setAlpha(0);

										Drawable drawable = getResources().getDrawable(R.drawable.test);
										zoomImageView.setImageDrawable(drawable);

										//set this three attrs
										zoomImageView.setZoomAnimationKey(test.zoomAnimationKey());
										zoomImageView.setOriginalHeight(drawable.getIntrinsicHeight());
										zoomImageView.setOriginalWidth(drawable.getIntrinsicWidth());

										ValueAnimator valueAnimator = zoomImageView.zoomInAnimation(preViewGroup,
												new CallBack<Float>() {
													@Override
													public void call(Float t) {
														showView.getBackground().setAlpha(Math.round(255 * t));
													}
												}
										);
										valueAnimator.start();
									}
								}
		);


		test1.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showView.setVisibility(View.VISIBLE);
						showView.getBackground().setAlpha(0);

						Drawable drawable = getResources().getDrawable(R.drawable.test1);
						zoomImageView.setImageDrawable(drawable);

						zoomImageView.setZoomAnimationKey(test1.zoomAnimationKey());
						zoomImageView.setOriginalHeight(drawable.getIntrinsicHeight());
						zoomImageView.setOriginalWidth(drawable.getIntrinsicWidth());


						ValueAnimator valueAnimator = zoomImageView.zoomInAnimation(preViewGroup,
								new CallBack<Float>() {
									@Override
									public void call(Float t) {
										showView.getBackground().setAlpha(Math.round(255 * t));
									}
								}
						);

						valueAnimator.start();
					}
				});


		/**
		 * and this can be before a activity finished ,the animation
		 * so ,this just a simple sample
		 */
		zoomImageView.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						ValueAnimator valueAnimator = zoomImageView.zoomOutAnimation(preViewGroup,

								new CallBack<Float>() {
									@Override
									public void call(Float t) {
										showView.getBackground().setAlpha(Math.round(Math.round(255 - 255 * t)));
										if (t == 1) {
											showView.setVisibility(View.INVISIBLE);
										}
									}
								});

						valueAnimator.start();
					}
				});
	}


}
