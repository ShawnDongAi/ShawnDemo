package com.keyu.kymep_hn.view.menudrawer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class OverlayDrawer extends DraggableDrawer {

    private static final String TAG = "OverlayDrawer";

    private int mPeekSize;

    private Runnable mRevealRunnable = new Runnable() {
        @Override
        public void run() {
            cancelContentTouch();
            animateOffsetTo(mPeekSize, 250);
        }
    };

    OverlayDrawer(Activity activity, int dragMode) {
        super(activity, dragMode);
    }

    public OverlayDrawer(Context context) {
        super(context);
    }

    public OverlayDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OverlayDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initDrawer(Context context, AttributeSet attrs, int defStyle) {
        super.initDrawer(context, attrs, defStyle);
        super.addView(mContentContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        if (USE_TRANSLATIONS) {
            mContentContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
        mContentContainer.setHardwareLayersEnabled(false);
        super.addView(mMenuContainer, -1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mPeekSize = dpToPx(20);
    }

    @Override
    protected void drawOverlay(Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();
        final int offsetPixels = (int) mOffsetPixels;
        final float openRatio = Math.abs(mOffsetPixels) / mMenuSize;
        mMenuOverlay.setBounds(offsetPixels, 0, width, height);

        mMenuOverlay.setAlpha((int) (MAX_MENU_OVERLAY_ALPHA * openRatio));
        mMenuOverlay.draw(canvas);
    }

    @Override
    public void openMenu(boolean animate) {
        animateOffsetTo(mMenuSize, 0, animate);
    }

    @Override
    public void closeMenu(boolean animate) {
        animateOffsetTo(0, 0, animate);
    }

    @Override
    protected void onOffsetPixelsChanged(int offsetPixels) {
        if (USE_TRANSLATIONS) {
        	mMenuContainer.setTranslationX(offsetPixels - mMenuSize);
        } else {
        	mMenuContainer.offsetLeftAndRight(offsetPixels - mMenuContainer.getRight());
        }

        invalidate();
    }

    @Override
    protected void initPeekScroller() {
    	 mPeekScroller.startScroll(0, 0, mPeekSize, 0, PEEK_DURATION);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onOffsetPixelsChanged((int) mOffsetPixels);
    }

    @Override
    protected void updateDropShadowRect() {
        final float openRatio = Math.abs(mOffsetPixels) / mMenuSize;
        final int dropShadowSize = (int) (mDropShadowSize * openRatio);

        mDropShadowRect.top = 0;
        mDropShadowRect.bottom = getHeight();
        mDropShadowRect.left = ViewHelper.getRight(mMenuContainer);
        mDropShadowRect.right = mDropShadowRect.left + dropShadowSize;
    }

    @Override
    protected void startLayerTranslation() {
        if (USE_TRANSLATIONS && mHardwareLayersEnabled && !mLayerTypeHardware) {
            mLayerTypeHardware = true;
            mMenuContainer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @Override
    protected void stopLayerTranslation() {
        if (mLayerTypeHardware) {
            mLayerTypeHardware = false;
            mMenuContainer.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;

        mContentContainer.layout(0, 0, width, height);

        if (USE_TRANSLATIONS) {
        	 mMenuContainer.layout(0, 0, mMenuSize, height);
        } else {
            final int offsetPixels = (int) mOffsetPixels;
            final int menuSize = mMenuSize;

            mMenuContainer.layout(-menuSize + offsetPixels, 0, offsetPixels, height);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            throw new IllegalStateException("Must measure with an exact size");
        }

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);

        if (mOffsetPixels == -1) openMenu(false);

        int menuWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, mMenuSize);
        int menuHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        mMenuContainer.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);

        final int contentWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeightMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, height);
        mContentContainer.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);

        setMeasuredDimension(width, height);

        updateTouchAreaSize();
    }

    private boolean isContentTouch(int x, int y) {
        return ViewHelper.getRight(mMenuContainer) < x;
    }

    protected boolean onDownAllowDrag(int x, int y) {
    	return (!mMenuVisible && mInitialMotionX <= mTouchSize)
                || (mMenuVisible && mInitialMotionX <= mOffsetPixels);
    }

    protected boolean onMoveAllowDrag(int x, int y, float dx, float dy) {
        if (mMenuVisible && mTouchMode == TOUCH_MODE_FULLSCREEN) {
            return true;
        }

        return (!mMenuVisible && mInitialMotionX <= mTouchSize && (dx > 0)) // Drawer closed
                || (mMenuVisible && x <= mOffsetPixels) // Drawer open
                || (Math.abs(mOffsetPixels) <= mPeekSize && mMenuVisible); // Drawer revealed
    }

    protected void onMoveEvent(float dx, float dy) {
    	setOffsetPixels(Math.min(Math.max(mOffsetPixels + dx, 0), mMenuSize));
    }

    protected void onUpEvent(int x, int y) {
        final int offsetPixels = (int) mOffsetPixels;

        if (mIsDragging) {
            mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
            final int initialVelocity = (int) getXVelocity(mVelocityTracker);
            mLastMotionX = x;
            animateOffsetTo(initialVelocity > 0 ? mMenuSize : 0, initialVelocity, true);

            // Close the menu when content is clicked while the menu is visible.
        } else if (mMenuVisible) {
            closeMenu();
        }
    }

    protected boolean checkTouchSlop(float dx, float dy) {
    	return Math.abs(dx) > mTouchSlop && Math.abs(dx) > Math.abs(dy);
    }

    @Override
    protected void stopAnimation() {
        super.stopAnimation();
        removeCallbacks(mRevealRunnable);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
        removeCallbacks(mRevealRunnable);
        if (mIsPeeking) {
            endPeek();
            animateOffsetTo(0, PEEK_DURATION);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            removeCallbacks(mRevealRunnable);
            mActivePointerId = INVALID_POINTER;
            mIsDragging = false;
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }

            if (Math.abs(mOffsetPixels) > mMenuSize / 2) {
                openMenu();
            } else {
                closeMenu();
            }

            return false;
        }

        if (action == MotionEvent.ACTION_DOWN && mMenuVisible && isCloseEnough()) {
            setOffsetPixels(0);
            stopAnimation();
            endPeek();
            setDrawerState(STATE_CLOSED);
            mIsDragging = false;
        }

        // Always intercept events over the content while menu is visible.
        if (mMenuVisible) {
            int index = 0;
            if (mActivePointerId != INVALID_POINTER) {
                index = ev.findPointerIndex(mActivePointerId);
                index = index == -1 ? 0 : index;
            }

            final int x = (int) ev.getX(index);
            final int y = (int) ev.getY(index);
            if (isContentTouch(x, y)) {
                return true;
            }
        }

        if (!mMenuVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && mIsDragging) {
            return true;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                final boolean allowDrag = onDownAllowDrag((int) mLastMotionX, (int) mLastMotionY);
                mActivePointerId = ev.getPointerId(0);

                if (allowDrag) {
                    setDrawerState(mMenuVisible ? STATE_OPEN : STATE_CLOSED);
                    stopAnimation();
                    endPeek();

                    if (!mMenuVisible && mInitialMotionX <= mPeekSize) {
                        postDelayed(mRevealRunnable, 160);
                    }

                    mIsDragging = false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                    closeMenu(true);
                    return false;
                }

                final float x = ev.getX(pointerIndex);
                final float dx = x - mLastMotionX;
                final float y = ev.getY(pointerIndex);
                final float dy = y - mLastMotionY;

                if (Math.abs(dx) >= mTouchSlop || Math.abs(dy) >= mTouchSlop) {
                    removeCallbacks(mRevealRunnable);
                    endPeek();
                }

                if (checkTouchSlop(dx, dy)) {
                    if (mOnInterceptMoveEventListener != null && (mTouchMode == TOUCH_MODE_FULLSCREEN || mMenuVisible)
                            && canChildrenScroll((int) dx, (int) dy, (int) x, (int) y)) {
                        endDrag(); // Release the velocity tracker
                        requestDisallowInterceptTouchEvent(true);
                        return false;
                    }

                    final boolean allowDrag = onMoveAllowDrag((int) x, (int) y, dx, dy);

                    if (allowDrag) {
                        endPeek();
                        stopAnimation();
                        setDrawerState(STATE_DRAGGING);
                        mIsDragging = true;
                        mLastMotionX = x;
                        mLastMotionY = y;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mMenuVisible && !mIsDragging && mTouchMode == TOUCH_MODE_NONE) {
            return false;
        }
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = mInitialMotionX = ev.getX();
                mLastMotionY = mInitialMotionY = ev.getY();
                final boolean allowDrag = onDownAllowDrag((int) mLastMotionX, (int) mLastMotionY);

                mActivePointerId = ev.getPointerId(0);

                if (allowDrag) {
                    stopAnimation();
                    endPeek();

                    if (!mMenuVisible && mLastMotionX <= mPeekSize) {
                        postDelayed(mRevealRunnable, 160);
                    }

                    startLayerTranslation();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                if (pointerIndex == -1) {
                    mIsDragging = false;
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                    closeMenu(true);
                    return false;
                }

                if (!mIsDragging) {
                    final float x = ev.getX(pointerIndex);
                    final float dx = x - mLastMotionX;
                    final float y = ev.getY(pointerIndex);
                    final float dy = y - mLastMotionY;

                    if (checkTouchSlop(dx, dy)) {
                        final boolean allowDrag = onMoveAllowDrag((int) x, (int) y, dx, dy);

                        if (allowDrag) {
                            endPeek();
                            stopAnimation();
                            setDrawerState(STATE_DRAGGING);
                            mIsDragging = true;
                            mLastMotionX = x;
                            mLastMotionY = y;
                        } else {
                            mInitialMotionX = x;
                            mInitialMotionY = y;
                        }
                    }
                }

                if (mIsDragging) {
                    startLayerTranslation();

                    final float x = ev.getX(pointerIndex);
                    final float dx = x - mLastMotionX;
                    final float y = ev.getY(pointerIndex);
                    final float dy = y - mLastMotionY;

                    mLastMotionX = x;
                    mLastMotionY = y;
                    onMoveEvent(dx, dy);
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                removeCallbacks(mRevealRunnable);
                int index = ev.findPointerIndex(mActivePointerId);
                index = index == -1 ? 0 : index;
                final int x = (int) ev.getX(index);
                final int y = (int) ev.getY(index);
                onUpEvent(x, y);
                mActivePointerId = INVALID_POINTER;
                mIsDragging = false;
                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN:
                final int index = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                mLastMotionX = ev.getX(index);
                mLastMotionY = ev.getY(index);
                mActivePointerId = ev.getPointerId(index);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(ev);
                mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
                mLastMotionY = ev.getY(ev.findPointerIndex(mActivePointerId));
                break;
        }

        return true;
    }

    private void onPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionX = ev.getX(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }
}
