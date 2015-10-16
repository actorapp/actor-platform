package com.droidkit.pickers.file.util;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import im.actor.messenger.R;

/**
 * Created by kiolt_000 on 15/09/2014.
 */
public class TimeUtils {
    public static int unixtime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    static ThreadLocal<DateFormat> dateInstanceGetter = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        }
    };
    static ThreadLocal<DateFormat> timeInstanceGetter = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return SimpleDateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
        }
    };

    public static String formatFileTime(long time, Context context) {

        Date currentDate = new Date(System.currentTimeMillis());
        Date convertableDate = new Date(time);

        String convertedDate = dateInstanceGetter.get().format(convertableDate);
        String convertedTime = timeInstanceGetter.get().format(convertableDate);
        /*if (currentDate.getYear() == convertableDate.getYear()) {
            if (currentDate.getMonth() == convertableDate.getMonth()) {
                if (currentDate.getDay() == convertableDate.getDay()) {
                    if (currentDate.getHours() == convertableDate.getHours()) {
                        if (currentDate.getMinutes() == convertableDate.getMinutes()) {
                            return context.getString(R.string.picker_time_minute_ago);
                        }
                        int minutesAgo = currentDate.getMinutes() - convertableDate.getMinutes();
                        return context.getResources().getQuantityString(R.plurals.picker_time_minutes_ago, minutesAgo, minutesAgo);
                        // to do android-i18n-plurals implementation

                    } else {
                        if (currentDate.getHours() - 1 == convertableDate.getHours()) {
                            return context.getString(R.string.picker_time_hour_ago);
                        }
                    }
                    return context.getString(R.string.picker_time_today_at, convertedTime);
                } else {
                    if (currentDate.getDay() - 1 == convertableDate.getDay()) {
                        return context.getString(R.string.picker_time_yesterday_at, convertedTime);
                    }
                }
            }
            int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
            convertedDate = DateUtils.formatDateTime(context, time, flags);
        }*/
        return context.getString(R.string.picker_time_at, convertedDate, convertedTime);
    }
}
