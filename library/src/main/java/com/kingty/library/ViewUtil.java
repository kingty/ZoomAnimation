package com.kingty.library;

import android.app.Activity;
import android.app.Application;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kingty on 16/1/29.
 */
public class ViewUtil {

	public static DisplayMetrics displayMetrics(View view) {
		return view.getResources().getDisplayMetrics();
	}
	public static int dp(View view ,float p) {
		return (int) (displayMetrics(view).density * p);
	}

	public static List<View> childsRecursive(ViewGroup v) {
		ArrayList<View> views = new ArrayList<>();
		for (int i = 0; i < v.getChildCount(); i++) {
			if (v.getChildAt(i) instanceof ViewGroup) {
				views.add(v.getChildAt(i));
				childsRecursive((ViewGroup) v.getChildAt(i));
			} else {
				views.add(v.getChildAt(i));
			}
		}
		return views;
	}
	public static class Pos {
		public int x;
		public int y;
		public int w;
		public int h;
	}

	public static Pos pos(View v) {
		View root = ((Activity) v.getContext()).getWindow().getDecorView();
		return pos(v, root);
	}

	public static Pos pos(View v, View root) {
		Pos pos = new Pos();
		View c = v;
		pos.w = v.getWidth();
		pos.h = v.getHeight();
		while (c != root) {
			pos.x += c.getLeft() - c.getScrollX() + c.getTranslationX();
			pos.y += c.getTop() - c.getScrollY() + c.getTranslationY();
			c = (View) c.getParent();
		}
		return pos;
	}
}
