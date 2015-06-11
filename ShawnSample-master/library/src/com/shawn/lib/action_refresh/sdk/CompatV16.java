package com.shawn.lib.action_refresh.sdk;

import android.annotation.SuppressLint;
import android.view.View;

class CompatV16 {

    @SuppressLint("NewApi")
	static void postOnAnimation(View view, Runnable runnable) {
        view.postOnAnimation(runnable);
    }

}
