package com.shawn.lib.pinned_section;

import android.widget.ListAdapter;

/** List adapter to be implemented for being used with PinnedSectionListView adapter. */
public interface PinnedSectionListAdapter extends ListAdapter {
	/** This method shall return 'true' if views of given type has to be pinned. */
	boolean isItemViewTypePinned(int viewType);
}
