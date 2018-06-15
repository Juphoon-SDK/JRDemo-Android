package ui;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewClickListener extends RecyclerView.SimpleOnItemTouchListener {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemDoubleClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickListener mListener;
    private GestureDetectorCompat mGestureDetector;

    public RecyclerViewClickListener(Context context, final RecyclerView recyclerView,
                                     OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetectorCompat(context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        View touchedView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        int position = recyclerView.getChildLayoutPosition(touchedView);
                        if (touchedView != null && mListener != null) {
                            if (touchedView instanceof ViewGroup) {
                                final ViewGroup childViewGroup = (ViewGroup) touchedView;
                                final List<View> viewHierarchy = new ArrayList<>();
                                getViewHierarchyUnderChild(childViewGroup, e.getRawX(), e.getRawY(), viewHierarchy);
                                if (viewHierarchy.size() > 0) {
                                    touchedView = viewHierarchy.get(0);
                                }
                            }
                            mListener.onItemClick(touchedView, position);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        View touchedView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        int position = recyclerView.getChildLayoutPosition(touchedView);
                        if (touchedView != null && mListener != null) {
                            if (touchedView instanceof ViewGroup) {
                                final ViewGroup childViewGroup = (ViewGroup) touchedView;
                                final List<View> viewHierarchy = new ArrayList<>();
                                getViewHierarchyUnderChild(childViewGroup, e.getRawX(), e.getRawY(), viewHierarchy);
                                if (viewHierarchy.size() > 0) {
                                    touchedView = viewHierarchy.get(0);
                                }
                            }
                            mListener.onItemDoubleClick(touchedView, position);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        View touchedView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        int position = recyclerView.getChildLayoutPosition(touchedView);
                        if (touchedView != null && mListener != null) {
                            if (touchedView instanceof ViewGroup) {
                                final ViewGroup childViewGroup = (ViewGroup) touchedView;
                                final List<View> viewHierarchy = new ArrayList<>();
                                getViewHierarchyUnderChild(childViewGroup, e.getRawX(), e.getRawY(), viewHierarchy);
                                if (viewHierarchy.size() > 0) {
                                    touchedView = viewHierarchy.get(0);
                                }
                            }
                            mListener.onItemLongClick(touchedView, position);
                        }
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        return mGestureDetector.onTouchEvent(e);
    }

    private void getViewHierarchyUnderChild(ViewGroup root, float x, float y, List<View> viewHierarchy) {
        int[] location = new int[2];
        final int childCount = root.getChildCount();

        for (int i = 0; i < childCount; ++i) {
            final View child = root.getChildAt(i);
            child.getLocationOnScreen(location);
            final int childLeft = location[0], childRight = childLeft + child.getWidth();
            final int childTop = location[1], childBottom = childTop + child.getHeight();
            if (child.isShown() && x >= childLeft && x <= childRight && y >= childTop && y <= childBottom) {
                viewHierarchy.add(0, child);
            }
            if (child instanceof ViewGroup) {
                getViewHierarchyUnderChild((ViewGroup) child, x, y, viewHierarchy);
            }
        }
    }

    public static class SimpleOnItemClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {

        }

        @Override
        public void onItemDoubleClick(View view, int position) {

        }

        @Override
        public void onItemLongClick(View view, int position) {

        }
    }
}
