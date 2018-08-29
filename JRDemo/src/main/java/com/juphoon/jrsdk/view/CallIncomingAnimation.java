package com.juphoon.jrsdk.view;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;

public class CallIncomingAnimation {

    public static int ENDLESS = -1;

    public CallIncomingAnimation(int min, int max, int duration) {
        mMin = min;
        mMax = max;
        mDuration = duration;
        mScaleRunnable = new Runnable() {

            @Override
            public void run() {
                long time = AnimationUtils.currentAnimationTimeMillis()
                        - mStartTime;
                if (time <= mDuration) {
                    setWidth(getWidthAtTime(time));
                    sHandler.postDelayed(mScaleRunnable, FRAME_TIME);
                } else {
                    mFrequency--;
                    if (mFrequency == 0) {
                        mView.setVisibility(View.INVISIBLE);
                        mIsAnimating = false;
                    } else {
                        performAnimation();
                    }
                }
            }

        };
    }

    public void startAnimation(View view, int frequency) {
        mView = view;
        mFrequency = frequency;
        setWidth(getWidthAtTime(0));
        mView.setVisibility(View.VISIBLE);
        mDrawable = view.getBackground();
        performAnimation();
    }

    public void startAnimation(View view) {
        startAnimation(view, 1);
    }

    private int getWidthAtTime(long time) {
        return (int) ((float) (mMax - mMin) / mDuration * time + mMin);
    }

    private void performAnimation() {
        mIsAnimating = true;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        sHandler.post(mScaleRunnable);
    }

    public void setWidth(int width) {
        LayoutParams params = mView.getLayoutParams();
        params.width = width;
        mView.setLayoutParams(params);
    }

    public void setAlpha(int alpha) {
        mDrawable.setAlpha(alpha);
    }

    public boolean isAnimating() {
        return mIsAnimating;
    }

    public void stop() {
        sHandler.removeCallbacks(mScaleRunnable);
        if (mView != null)
            mView.setVisibility(View.INVISIBLE);
        mIsAnimating = false;
    }

    public void destroy() {
        stop();
        mScaleRunnable = null;
        mDrawable = null;
    }

    private static final int FRAME_TIME = 10;
    private static Handler sHandler = new Handler();

    private Runnable mScaleRunnable;

    private View mView;
    private int mMin;
    private int mMax;
    private int mDuration;
    private Drawable mDrawable;
    private long mStartTime;
    private int mFrequency;
    private boolean mIsAnimating = false;

}
