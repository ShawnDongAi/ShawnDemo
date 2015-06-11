package com.shawn.lib.action_refresh.listeners;

import android.view.View;

/**
 * Simple Listener to listen for any callbacks to Refresh.
 */
public interface OnRefreshListener {
    /**
     * Called when the user has initiated a refresh by pulling.
     *
     * @param view
     *            - View which the user has started the refresh from.
     */
    public void onRefreshStarted(View view);
}
