package com.shawn.lib.action_refresh.listeners;

import android.view.View;

public interface HeaderViewListener {
    /**
     * The state when the header view is completely visible.
     */
    public static int STATE_VISIBLE = 0;

    /**
     * The state when the header view is minimized. By default this means
     * that the progress bar is still visible, but the rest of the view is
     * hidden, showing the Action Bar behind.
     * <p/>
     * This will not be called in header minimization is disabled.
     */
    public static int STATE_MINIMIZED = 1;

    /**
     * The state when the header view is completely hidden.
     */
    public static int STATE_HIDDEN = 2;

    /**
     * Called when the visibility state of the Header View has changed.
     *
     * @param headerView
     *            HeaderView who's state has changed.
     * @param state
     *            The new state. One of {@link #STATE_VISIBLE},
     *            {@link #STATE_MINIMIZED} and {@link #STATE_HIDDEN}
     */
    public void onStateChanged(View headerView, int state);
}
