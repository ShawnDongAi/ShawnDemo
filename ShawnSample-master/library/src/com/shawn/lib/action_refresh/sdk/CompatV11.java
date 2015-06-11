package com.shawn.lib.action_refresh.sdk;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.View;

class CompatV11 {

    @SuppressLint("NewApi")
	static void setAlpha(View view, float alpha) {
        view.setAlpha(alpha);
    }

    @SuppressLint("NewApi")
	static void postOnAnimation(View view, Runnable runnable) {
        view.postDelayed(runnable, ValueAnimator.getFrameDelay());
    }

}
