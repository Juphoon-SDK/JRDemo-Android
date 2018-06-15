package ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TwoStateScrollViewPager extends ViewPager {

    public TwoStateScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScrollable = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScrollable && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mScrollable && super.onInterceptTouchEvent(event);
    }

    public void setScrollable(boolean b) {
        mScrollable = b;
    }

    private boolean mScrollable;
}
