package com.shawn.sample;


import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.shawn.lib.action_refresh.ActionBarPullToRefresh;
import com.shawn.lib.action_refresh.Options;
import com.shawn.lib.action_refresh.PullToRefreshLayout;
import com.shawn.lib.action_refresh.listeners.OnRefreshListener;
import com.shawn.lib.swipe_back.SwipeBackActivity;

public class SwipeBackDemoActivity extends SwipeBackActivity implements OnRefreshListener {
	private PullToRefreshLayout mPullToRefreshLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_swipe_back);
		mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);

		Options.Builder builder = Options.create();
		builder.refreshOnUp(true);
		builder.noMinimize();
		Options option = builder.build();
        // We can now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(this)
                // We need to insert the PullToRefreshLayout into the Fragment's ViewGroup
//                .insertLayoutInto((ViewGroup) getWindow().getDecorView())
                // Here we mark just the ListView and it's Empty View as pullable
        .options(option)
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);
	}

	@Override
	public void onRefreshStarted(View view) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mPullToRefreshLayout.setRefreshComplete();
			}
		}, 2000);
	}

}
