package com.juphoon.jrsdk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;


public class CircleButton extends ImageButton implements Rotatable {

    public CircleButton(Context context) {
        this(context, null);
    }

    public CircleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setStroke(int width, int color) {
        mNormal.setStroke(width, color);
        mPressed.setStroke(width, color);
        mSelected.setStroke(width, color);
    }

    public void setBackgroundNormalColor(int color) {
        mNormal.setColor(color);
    }

    public void setBackgroundPressedColor(int color) {
        mPressed.setColor(color);
    }

    public void setBackgroundSelectedColor(int color) {
        mSelected.setColor(color);
    }

    public void setBackgroundDisabledColor(int color) {
        mDisabled.setColor(color);
    }

    public void setDisabledStroke(int width, int color) {
        mDisabled.setStroke(width, color);
    }

    public void setPressedStroke(int width, int color) {
        mPressed.setStroke(width, color);
    }

    public void setSelectedStroke(int width, int color) {
        mSelected.setStroke(width, color);
    }

    protected int getDegree() {
        return mTargetDegree;
    }

    // Rotate the view counter-clockwise
    @Override
    public void setOrientation(int degree, boolean animation) {
        mEnableAnimation = animation;
        // make sure in the range of [0, 359]
        degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
        if (degree == mTargetDegree) return;

        mTargetDegree = degree;
        if (mEnableAnimation) {
            mStartDegree = mCurrentDegree;
            mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();

            int diff = mTargetDegree - mCurrentDegree;
            boolean clockwise = diff == 180;
            diff = diff >= 0 ? diff : 360 + diff; // make it in range [0, 359]

            // Make it in range [-179, 180]. That's the shorted distance between the
            // two angles
            diff = diff > 180 ? diff - 360 : diff;

            mClockwise = diff >= 0 && diff < 180 || clockwise;
            mAnimationEndTime = mAnimationStartTime
                    + Math.abs(diff) * 1000 / ANIMATION_SPEED;
        } else {
            mCurrentDegree = mTargetDegree;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) return;

        Rect bounds = drawable.getBounds();
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;

        if (w == 0 || h == 0) return; // nothing to draw

        if (mCurrentDegree != mTargetDegree) {
            long time = AnimationUtils.currentAnimationTimeMillis();
            if (time < mAnimationEndTime) {
                int deltaTime = (int) (time - mAnimationStartTime);
                int degree = mStartDegree + ANIMATION_SPEED
                        * (mClockwise ? deltaTime : -deltaTime) / 1000;
                degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
                mCurrentDegree = degree;
                invalidate();
            } else {
                mCurrentDegree = mTargetDegree;
            }
        }

        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int width = getWidth() - left - right;
        int height = getHeight() - top - bottom;

        int saveCount = canvas.getSaveCount();

        // Scale down the image first if required.
        if ((getScaleType() == ScaleType.FIT_CENTER) &&
                ((width < w) || (height < h))) {
            float ratio = Math.min((float) width / w, (float) height / h);
            canvas.scale(ratio, ratio, width / 2.0f, height / 2.0f);
        }
        canvas.translate(left + width / 2, top + height / 2);
        canvas.rotate(-mCurrentDegree);
        canvas.translate(-w / 2, -h / 2);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    @SuppressWarnings("deprecation")
    private void init() {
        mNormal = new GradientDrawable();
        mNormal.setShape(GradientDrawable.OVAL);

        mPressed = new GradientDrawable();
        mPressed.setShape(GradientDrawable.OVAL);

        mSelected = new GradientDrawable();
        mSelected.setShape(GradientDrawable.OVAL);

        mDisabled = new GradientDrawable();
        mDisabled.setShape(GradientDrawable.OVAL);

        StateListDrawable background = new StateListDrawable();
        background.addState(STATE_SET_DISABLED, mDisabled);
        background.addState(STATE_SET_PRESSED, mPressed);
        background.addState(STATE_SET_SELECTED, mSelected);
        background.addState(STATE_SET_NONE, mNormal);
        setBackgroundDrawable(background);
    }

    private static int[] STATE_SET_DISABLED = new int[]{-android.R.attr.state_enabled};
    private static int[] STATE_SET_PRESSED = new int[]{android.R.attr.state_pressed};
    private static int[] STATE_SET_SELECTED = new int[]{android.R.attr.state_selected};
    private static int[] STATE_SET_NONE = new int[]{};

    private static final int ANIMATION_SPEED = 270; // 270 deg/sec

    private GradientDrawable mNormal;
    private GradientDrawable mPressed;
    private GradientDrawable mSelected;
    private GradientDrawable mDisabled;

    private int mCurrentDegree = 0; // [0, 359]
    private int mStartDegree = 0;
    private int mTargetDegree = 0;

    private boolean mClockwise = false;
    private boolean mEnableAnimation = true;

    private long mAnimationStartTime = 0;
    private long mAnimationEndTime = 0;
}
