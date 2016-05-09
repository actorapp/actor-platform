package im.actor.sdk.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import im.actor.runtime.android.AndroidContext;

public class DateFormatting {

    private static ThreadLocal<java.text.DateFormat> TIME_FORMATTER = new ThreadLocal<>();
    private static ThreadLocal<SimpleDateFormat> DATE_YEAR_FORMATTER = new ThreadLocal<>();
    private static ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<>();
    private static ThreadLocal<SimpleDateFormat> MONTH_FORMATTER = new ThreadLocal<>();
    private static ThreadLocal<Calendar> CALENDAR = new ThreadLocal<>();

    public static String formatTime(long time) {
        return getTimeFormatter().format(new Date(time));
    }

    public static String formatDate(long date) {
        String month = getMonthFormatter().format(date).toUpperCase();
        Calendar calendar = getCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(date);

        if (calendar.get(Calendar.YEAR) == currentYear) {
            return String.format(getDateFormatter().format(date), month);
        } else {
            return String.format(getDateYearFormatter().format(date), month);
        }
    }

    public static boolean areSameDays(long a, long b) {
        Calendar calendar = getCalendar();
        calendar.setTimeInMillis(a);
        int y1 = calendar.get(Calendar.YEAR);
        int m1 = calendar.get(Calendar.MONTH);
        int d1 = calendar.get(Calendar.DATE);
        calendar.setTimeInMillis(b);
        int y2 = calendar.get(Calendar.YEAR);
        int m2 = calendar.get(Calendar.MONTH);
        int d2 = calendar.get(Calendar.DATE);

        return y1 == y2 && m1 == m2 && d1 == d2;
    }

    private static java.text.DateFormat getTimeFormatter() {
        DateFormat dateFormat = TIME_FORMATTER.get();
        if (dateFormat == null) {
            dateFormat = android.text.format.DateFormat.getTimeFormat(AndroidContext.getContext());
            TIME_FORMATTER.set(dateFormat);
        }
        return dateFormat;
    }

    private static Calendar getCalendar() {
        Calendar calendar = CALENDAR.get();
        if (calendar == null) {
            calendar = Calendar.getInstance();
            CALENDAR.set(calendar);
        }
        return calendar;
    }

    private static SimpleDateFormat getDateYearFormatter() {
        SimpleDateFormat res = DATE_YEAR_FORMATTER.get();
        if (res == null) {
            res = new SimpleDateFormat("dd '%s' ''yy");
            DATE_YEAR_FORMATTER.set(res);
        }
        return res;
    }

    private static SimpleDateFormat getMonthFormatter() {
        SimpleDateFormat res = MONTH_FORMATTER.get();
        if (res == null) {
            res = new SimpleDateFormat("MMMM");
            MONTH_FORMATTER.set(res);
        }
        return res;
    }

    private static SimpleDateFormat getDateFormatter() {
        SimpleDateFormat res = DATE_FORMATTER.get();
        if (res == null) {
            res = new SimpleDateFormat("dd '%s'");
            DATE_FORMATTER.set(res);
        }
        return res;
    }
}
