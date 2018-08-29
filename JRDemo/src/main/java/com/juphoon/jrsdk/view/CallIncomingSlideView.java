package com.juphoon.jrsdk.view;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.juphoon.jrsdk.R;

public class CallIncomingSlideView extends RelativeLayout implements View.OnTouchListener {

    public static final int MODE_VIDEO = 1;
    public static final int MODE_VOICE = 2;

    public interface Callback {
        void callHandleViewAnswerVoice(CallIncomingSlideView v);

        void callHandleViewAnswerVideo(CallIncomingSlideView v);

        void callHandleViewAnswerCameraOff(CallIncomingSlideView v);

        void callHandleViewDecline(CallIncomingSlideView v);
    }

    public CallIncomingSlideView(Context context) {
        super(context);
    }

    public CallIncomingSlideView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public CallIncomingSlideView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (!mInited) {
            init();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAnswer = (CircleButton) findViewById(R.id.call_answer);
        mDecline = (CircleButton) findViewById(R.id.call_decline);
        mAnswerCameraOff = (CircleButton) findViewById(R.id.call_answer_camera_off);

        mAnswer.setOnTouchListener(this);
        mDecline.setOnTouchListener(this);
        mAnswerCameraOff.setOnTouchListener(this);

        mAnimationView = findViewById(R.id.call_incoming_anim);
        mShortTrack = findViewById(R.id.short_track);

        colorAnswerButton(mAnswer, R.drawable.call_answer_video);
        colorDeclineButton(mDecline, R.drawable.call_decline);
        colorAnswerCameraOffButton(mAnswerCameraOff, R.drawable.call_answer_camera_off);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(v, event);
                break;
            case MotionEvent.ACTION_MOVE:
                onActionMove(v, event);
                break;
            case MotionEvent.ACTION_UP:
                onActionUp(v, event);
                break;
        }
        return true;
    }

    public void destroy() {
        mInited = false;
        if (mAnimation != null) {
            mAnimation.destroy();
            mAnimation = null;
        }
        sHandler.removeCallbacks(mRollBackRunnable);
        mRollBackRunnable = null;
        mStartAnimation = null;
        mCallback = null;
    }

    public void reset() {
        if (mOriginalWidth != -1) {
            setWidth(mOriginalWidth);
        }
        mAnswer.setVisibility(View.VISIBLE);
        mDecline.setVisibility(View.VISIBLE);
    }

    public void setVideo(boolean isVideo) {
        if (isVideo) {
            mMode = MODE_VIDEO;
            mAnswer.setImageResource(R.drawable.call_answer_video);
            mAnswerCameraOff.setVisibility(View.VISIBLE);
        } else {
            mMode = MODE_VOICE;
            mAnswer.setImageResource(R.drawable.call_answer_audio);
            mAnswerCameraOff.setVisibility(View.GONE);
        }

    }

    private static final int BACK_DURATION = 10;
    private static final float VE_HORIZONTAL = 2.5f;
    private static final int ANIMATION_DURATION = 1000;

    private static Handler sHandler = new Handler();
    private static int BACKGROUND_RES = R.drawable.call_incoming_slide_bg;

    private CircleButton mDecline;
    private CircleButton mAnswer;
    private CircleButton mAnswerCameraOff;
    private CallIncomingAnimation mAnimation;

    private View mAnimationView;
    private View mShortTrack;

    private int mMinWidth;
    private int mShortMinWidth;
    private int mOriginalWidth = -1;
    private int mStartX;
    private int mCurrentWidth;
    private boolean mInited = false;
    private boolean mIsTouching = false;
    private int mMode;
    private Callback mCallback;

    private Runnable mStartAnimation;
    private Runnable mRollBackRunnable;

    private void init() {
        if (mOriginalWidth == -1) {
            mOriginalWidth = getMeasuredWidth();
            mMinWidth = getMeasuredHeight();
        }
        mCurrentWidth = mOriginalWidth;

        if (mMode == MODE_VIDEO) {
            mShortMinWidth = mMinWidth
                    * 2
                    - (int) (getResources().getDisplayMetrics().density * 3);
        } else {
            mShortMinWidth = mMinWidth;
        }

        mAnimation = new CallIncomingAnimation(mMinWidth,
                mOriginalWidth, ANIMATION_DURATION);
        mRollBackRunnable = new Runnable() {
            @Override
            public void run() {
                mCurrentWidth = Math.min(mCurrentWidth + (int) (VE_HORIZONTAL * BACK_DURATION), mOriginalWidth);
                setWidth(mCurrentWidth);
                if (mCurrentWidth != mOriginalWidth) {
                    sHandler.postDelayed(mRollBackRunnable, BACK_DURATION);
                } else {
                    resumeAnimation();
                }
            }
        };

        mStartAnimation = new Runnable() {
            @Override
            public void run() {
                startAnimation();
            }
        };
        sHandler.postDelayed(mStartAnimation, 1000);
        mInited = true;
    }

    private void resumeAnimation() {
        if (mIsTouching) {
            return;
        }
        mAnswer.setVisibility(View.VISIBLE);
        mDecline.setVisibility(View.VISIBLE);
        if (mMode == MODE_VIDEO) {
            mAnswerCameraOff.setVisibility(View.VISIBLE);
        }
        setBackgroundResource(BACKGROUND_RES);
        mShortTrack.setBackgroundResource(0);
        sHandler.postDelayed(mStartAnimation, 1000);
    }

    private void onActionDown(View v, MotionEvent event) {
        mStartX = (int) event.getRawX();
        mIsTouching = true;
        v.setPressed(true);
        stopAnimation();
        if (v == mAnswer) {
            setAlignRight();
            mDecline.setVisibility(View.GONE);
            mAnswerCameraOff.setVisibility(View.GONE);
        } else if (v == mDecline) {
            setAlignLeft();
            mAnswer.setVisibility(View.GONE);
            mAnswerCameraOff.setVisibility(View.GONE);
        } else if (v == mAnswerCameraOff) {
            setAlignRight();
            mAnswer.setVisibility(mMode == MODE_VIDEO ? View.INVISIBLE : View.GONE);
            mDecline.setVisibility(View.GONE);
            setBackgroundResource(0);
            mShortTrack.setBackgroundResource(BACKGROUND_RES);
        }
    }

    private void onActionMove(View v, MotionEvent event) {
        int minWidth = v == mAnswerCameraOff ? mShortMinWidth : mMinWidth;
        int deltaX = (int) event.getRawX() - mStartX;
        if (v == mDecline) {
            deltaX = -deltaX;
        }
        if (deltaX >= 0) {
            if (mOriginalWidth - deltaX < minWidth) {
                deltaX = mOriginalWidth - minWidth;
            }
            mCurrentWidth = mOriginalWidth - deltaX;
            setWidth(mCurrentWidth);
        }
    }

    private void onActionUp(View v, MotionEvent event) {
        int minWidth = v == mAnswerCameraOff ? mShortMinWidth : mMinWidth;
        mIsTouching = false;
        v.setPressed(false);
        if (mCurrentWidth <= minWidth + 20) {
            if (mCallback != null) {
                if (v == mAnswer) {
                    if (mMode == MODE_VOICE) {
                        mCallback.callHandleViewAnswerVoice(this);
                    } else {
                        mCallback.callHandleViewAnswerVideo(this);
                    }
                } else if (v == mDecline) {
                    mCallback.callHandleViewDecline(this);
                } else if (v == mAnswerCameraOff) {
                    mCallback.callHandleViewAnswerCameraOff(this);
                }
            }
        }
        rollBack();
    }

    private void setAlignLeft() {
        LayoutParams params = (LayoutParams) getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        setLayoutParams(params);
    }

    private void setAlignRight() {
        LayoutParams params = (LayoutParams) getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        setLayoutParams(params);
    }

    private void rollBack() {
        sHandler.postDelayed(mRollBackRunnable, BACK_DURATION);
    }

    private void setWidth(int width) {
        LayoutParams params = (LayoutParams) getLayoutParams();
        params.width = width;
        setLayoutParams(params);
    }

    private void startAnimation() {
        if (mAnimation != null)
            mAnimation.startAnimation(mAnimationView,
                    CallIncomingAnimation.ENDLESS);
    }

    private void stopAnimation() {
        if (mAnimation != null)
            mAnimation.stop();
        sHandler.removeCallbacks(mStartAnimation);
    }

    private void colorAnswerButton(CircleButton button, int resId) {
        Resources res = getResources();
        button.setBackgroundNormalColor(res
                .getColor(R.color.call_incoming_answer_bg_normal_color));
        button.setBackgroundPressedColor(res
                .getColor(R.color.call_incoming_answer_bg_pressed_color));
        button.setImageResource(resId);
    }

    private void colorDeclineButton(CircleButton button, int resId) {
        Resources res = getResources();
        button.setBackgroundNormalColor(res
                .getColor(R.color.call_incoming_decline_bg_normal_color));
        button.setBackgroundPressedColor(res
                .getColor(R.color.call_incoming_decline_bg_pressed_color));
        button.setImageResource(resId);
    }

    private void colorAnswerCameraOffButton(CircleButton button, int resId) {
        Resources res = getResources();
        button.setBackgroundNormalColor(res
                .getColor(R.color.call_incoming_answer_bg_selected_color));
        button.setBackgroundPressedColor(res
                .getColor(R.color.call_incoming_answer_bg_pressed_color));
        button.setImageResource(resId);
    }

}
