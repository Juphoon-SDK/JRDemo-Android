package com.juphoon.jrsdk.utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Upon on 2018/7/26.
 */

public class ViewUtils {

    public static void setViewEnabled(View view, boolean enabled) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setViewEnabled(viewGroup.getChildAt(i), enabled);
            }
        } else if (view != null) {
            view.setEnabled(enabled);
        }
    }

    public static void setMultipleViewsEnabled(boolean enabled, View... views) {
        View[] var2 = views;
        int var3 = views.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            View v = var2[var4];
            setViewEnabled(v, enabled);
        }

    }
}
