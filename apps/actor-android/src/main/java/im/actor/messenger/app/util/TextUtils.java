package im.actor.messenger.app.util;

import im.actor.messenger.app.AppContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TextUtils {

    private static ThreadLocal<java.text.DateFormat> TIME_FORMATTER = new ThreadLocal<java.text.DateFormat>() {
        @Override
        protected java.text.DateFormat initialValue() {
            return android.text.format.DateFormat.getTimeFormat(AppContext.getContext());
        }
    };

    private static ThreadLocal<Calendar> CALENDAR = new ThreadLocal<Calendar>() {
        @Override
        protected Calendar initialValue() {
            return Calendar.getInstance();
        }
    };

    private static ThreadLocal<SimpleDateFormat> DATE_YEAR_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd '%s' ''yy");
        }
    };

    private static ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd '%s'");
        }
    };

    private static ThreadLocal<SimpleDateFormat> MONTH_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MMMM");
        }
    };

    public static String formatTime(long time) {
        return TIME_FORMATTER.get().format(new Date(time));
    }

    public static String formatDate(long date) {
        String month = MONTH_FORMATTER.get().format(date).toUpperCase();
        Calendar calendar = CALENDAR.get();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(date);

        if (calendar.get(Calendar.YEAR) == currentYear) {
            return String.format(DATE_FORMATTER.get().format(date), month);
        } else {
            return String.format(DATE_YEAR_FORMATTER.get().format(date), month);
        }
    }

    public static boolean areSameDays(long a, long b) {
        Calendar calendar = CALENDAR.get();
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
}
