package com.shawn.lib.action_refresh.viewdelegates;

import android.view.View;
import android.webkit.WebView;

/**
 * FIXME
 */
public class WebViewDelegate implements ViewDelegate {

    public static final Class[] SUPPORTED_VIEW_CLASSES =  { WebView.class };

    @Override
    public boolean isReadyForPull(View view, float x, float y) {
        return view.getScrollY() <= 0;
    }
}
