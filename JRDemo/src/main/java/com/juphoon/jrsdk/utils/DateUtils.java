package com.juphoon.jrsdk.utils;

import android.content.Context;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Upon on 2018/2/28.
 */

public class DateUtils {
    public static String getTimeStringX(Context context, long milliseconds, boolean full) {
        int flags = android.text.format.DateUtils.FORMAT_SHOW_DATE;
        long now = System.currentTimeMillis();
        long tmp = milliseconds - now;
        if (tmp <= 0) {
            Time curTime = new Time();
            curTime.set(now);
            tmp += ((curTime.hour * android.text.format.DateUtils.HOUR_IN_MILLIS)
                    + (curTime.minute * android.text.format.DateUtils.MINUTE_IN_MILLIS)
                    + (curTime.second * android.text.format.DateUtils.SECOND_IN_MILLIS)
            );
            if (tmp >= 0) {
                flags = android.text.format.DateUtils.FORMAT_SHOW_TIME;
            } else if (tmp >= -android.text.format.DateUtils.DAY_IN_MILLIS) {
                if (full) {
                    return (String) android.text.format.DateUtils.getRelativeDateTimeString(context, milliseconds, android.text.format.DateUtils.DAY_IN_MILLIS, System.currentTimeMillis(), android.text.format.DateUtils.FORMAT_SHOW_TIME);
                }
                return (String) android.text.format.DateUtils.getRelativeTimeSpanString(milliseconds, System.currentTimeMillis(), android.text.format.DateUtils.DAY_IN_MILLIS, android.text.format.DateUtils.FORMAT_SHOW_DATE);
            } else if (tmp >= -android.text.format.DateUtils.DAY_IN_MILLIS * 6) {
                flags = android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY | (full ? android.text.format.DateUtils.FORMAT_SHOW_TIME : 0);
            } else {
                flags = android.text.format.DateUtils.FORMAT_SHOW_DATE | (full ? android.text.format.DateUtils.FORMAT_SHOW_TIME : 0);
            }
        }
        return android.text.format.DateUtils.formatDateTime(context, milliseconds, flags);
    }

    /**
     * 获取精确到秒的时间戳
     *
     * @return
     */
    public static int getSecondTimestamp(Date date) {
        if (null == date) {
            return 0;
        }
        String timestamp = String.valueOf(date.getTime());
        int length = timestamp.length();
        if (length > 3) {
            return Integer.valueOf(timestamp.substring(0, length - 3));
        } else {
            return 0;
        }
    }

    /**
     * 获取精确到秒的时间戳
     *
     * @return
     */
    public static int getSecondTimestamp(long time) {
        String timestamp = String.valueOf(time);
        int length = timestamp.length();
        if (length > 3) {
            return Integer.valueOf(timestamp.substring(0, length - 3));
        } else {
            return 0;
        }
    }

    /**
     * 年月日format
     * @param second 秒
     * @return
     */
    public static String formatTimeToYMD(long second) {
        Time time = new Time();
        time.set(second*1000);
        Time tm = new Time();
        tm.setToNow();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(second*1000));
    }
}
