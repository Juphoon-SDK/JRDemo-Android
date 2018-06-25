package common.utils;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Upon on 2018/2/28.
 */

public class JRDateUtils {
    public static String getTimeStringX(Context context, long milliseconds, boolean full) {
        int flags = DateUtils.FORMAT_SHOW_DATE;
        long now = System.currentTimeMillis();
        long tmp = milliseconds - now;
        if (tmp <= 0) {
            Time curTime = new Time();
            curTime.set(now);
            tmp += ((curTime.hour * DateUtils.HOUR_IN_MILLIS)
                    + (curTime.minute * DateUtils.MINUTE_IN_MILLIS)
                    + (curTime.second * DateUtils.SECOND_IN_MILLIS)
            );
            if (tmp >= 0) {
                flags = DateUtils.FORMAT_SHOW_TIME;
            } else if (tmp >= -DateUtils.DAY_IN_MILLIS) {
                if (full) {
                    return (String) DateUtils.getRelativeDateTimeString(context, milliseconds, DateUtils.DAY_IN_MILLIS, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME);
                }
                return (String) DateUtils.getRelativeTimeSpanString(milliseconds, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_SHOW_DATE);
            } else if (tmp >= -DateUtils.DAY_IN_MILLIS * 6) {
                flags = DateUtils.FORMAT_SHOW_WEEKDAY | (full ? DateUtils.FORMAT_SHOW_TIME : 0);
            } else {
                flags = DateUtils.FORMAT_SHOW_DATE | (full ? DateUtils.FORMAT_SHOW_TIME : 0);
            }
        }
        return DateUtils.formatDateTime(context, milliseconds, flags);
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
