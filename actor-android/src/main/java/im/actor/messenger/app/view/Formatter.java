package im.actor.messenger.app.view;

import android.content.Context;

import im.actor.messenger.R;
import im.actor.messenger.core.AppContext;
import im.actor.messenger.model.UserModel;
import im.actor.messenger.model.UserPresence;
import im.actor.messenger.storage.scheme.users.Sex;
import im.actor.messenger.util.TextUtils;

import java.util.Calendar;
import java.util.Date;

import static im.actor.messenger.storage.KeyValueEngines.users;

/**
 * Created by ex3ndr on 20.10.14.
 */
public class Formatter {
    private static Context context;

    private static final int[] MONTHS = new int[]{
            R.string.time_short_jan,
            R.string.time_short_feb,
            R.string.time_short_mar,
            R.string.time_short_apr,
            R.string.time_short_may,
            R.string.time_short_jun,
            R.string.time_short_jul,
            R.string.time_short_aug,
            R.string.time_short_sep,
            R.string.time_short_oct,
            R.string.time_short_nov,
            R.string.time_short_dec,
    };

    public static void init(Context context) {
        Formatter.context = context;
    }

    private static ThreadLocal<java.text.DateFormat> TIME_FORMATTER = new ThreadLocal<java.text.DateFormat>() {
        @Override
        protected java.text.DateFormat initialValue() {
            return android.text.format.DateFormat.getTimeFormat(context);
        }
    };

    private static ThreadLocal<java.text.DateFormat> SEEN_DATE_FORMATTER = new ThreadLocal<java.text.DateFormat>() {
        @Override
        protected java.text.DateFormat initialValue() {
            return android.text.format.DateFormat.getDateFormat(context);
        }
    };

    private static ThreadLocal<Calendar> CALENDAR = new ThreadLocal<Calendar>() {
        @Override
        protected Calendar initialValue() {
            return Calendar.getInstance();
        }
    };

    public static String formatLastSeen(long time, Sex sex) {
        int delta = (int) ((System.currentTimeMillis() / 1000L - time));
        if (delta < 60) {
            return sex == Sex.UNKNOWN
                    ? AppContext.getContext().getString(R.string.online_ls_now)
                    : sex == Sex.MALE
                    ? AppContext.getContext().getString(R.string.online_ls_now_male)
                    : AppContext.getContext().getString(R.string.online_ls_now_female);
        } else if (delta < 24 * 60 * 60) {
            if (TextUtils.areSameDays(time * 1000L, System.currentTimeMillis())) {
                return (sex == Sex.UNKNOWN
                        ? AppContext.getContext().getString(R.string.online_ls_today)
                        : sex == Sex.MALE
                        ? AppContext.getContext().getString(R.string.online_ls_today_male)
                        : AppContext.getContext().getString(R.string.online_ls_today_female))
                        .replace("{0}", TIME_FORMATTER.get().format(new Date(time * 1000L)));
            } else {
                return (sex == Sex.UNKNOWN
                        ? AppContext.getContext().getString(R.string.online_ls_yesterday)
                        : sex == Sex.MALE
                        ? AppContext.getContext().getString(R.string.online_ls_yesterday_male)
                        : AppContext.getContext().getString(R.string.online_ls_yesterday_female))
                        .replace("{0}", TIME_FORMATTER.get().format(new Date(time * 1000L)));
            }
        } else if (delta < 14 * 24 * 60 * 60) {

            return (sex == Sex.UNKNOWN
                    ? AppContext.getContext().getString(R.string.online_ls_date_time)
                    : sex == Sex.MALE
                    ? AppContext.getContext().getString(R.string.online_ls_date_time_male)
                    : AppContext.getContext().getString(R.string.online_ls_date_time_female))
                    .replace("{0}", SEEN_DATE_FORMATTER.get().format(new Date(time * 1000L)))
                    .replace("{1}", TIME_FORMATTER.get().format(new Date(time * 1000L)));
        } else if (delta < 6 * 30 * 24 * 60 * 60) {
            return (sex == Sex.UNKNOWN
                    ? AppContext.getContext().getString(R.string.online_ls_date)
                    : sex == Sex.MALE
                    ? AppContext.getContext().getString(R.string.online_ls_date_male)
                    : AppContext.getContext().getString(R.string.online_ls_date_female))
                    .replace("{0}", SEEN_DATE_FORMATTER.get().format(new Date(time * 1000L)));
        } else {
            return AppContext.getContext().getString(R.string.online_off);
        }
    }

    private static String formatTwoDigit(int v) {
        if (v < 0) {
            return "" + v;
        } else if (v < 10) {
            return "0" + v;
        } else {
            return "" + v;
        }
    }

    public static String duration(int duration) {
        if (duration < 60) {
            return formatTwoDigit(0) + ":" + formatTwoDigit(duration);
        } else if (duration < 60 * 60) {
            return formatTwoDigit(duration / 60) + ":" + formatTwoDigit(duration % 60);
        } else {
            return formatTwoDigit(duration / 3600) + ":" + formatTwoDigit(duration / 60) + ":" + formatTwoDigit(duration % 60);
        }
    }

    public static String formatFileSize(long size) {
        if (size < 0) {
            size = 0;
        }

        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return size / 1024 + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return size / (1024 * 1024) + " MB";
        } else {
            return size / (1024 * 1024 * 1024) + " GB";
        }
    }

    public static String formatPresence(UserPresence value, Sex sex) {
        switch (value.getState()) {
            default:
            case UNKNOWN:
                return null;
            case OFFLINE:
                if (value.getLastSeen() == 0) {
                    return AppContext.getContext().getString(R.string.online_off);
                } else {
                    return Formatter.formatLastSeen(value.getLastSeen(), sex);
                }
            case ONLINE:
                return AppContext.getContext().getString(R.string.online_on);
        }
    }

    public static String formatShortDate(long date) {
        long now = System.currentTimeMillis();
        long delta = now - date;
        if (delta < 60 * 1000) {
            return AppContext.getContext().getString(R.string.time_short_now);
        } else if (delta < 60 * 60 * 1000) {
            return AppContext.getContext().getString(R.string.time_short_minutes).replace("{0}", "" + delta / 60000);
        } else if (delta < 24 * 60 * 60 * 1000) {
            return AppContext.getContext().getString(R.string.time_short_hours).replace("{0}", "" + delta / 3600000);
        } else if (delta < 2 * 24 * 60 * 60 * 1000) {
            return AppContext.getContext().getString(R.string.time_short_yesterday);
        } else {
            Calendar calendar = CALENDAR.get();
            calendar.setTimeInMillis(date);
            return calendar.get(Calendar.DATE) + " " + AppContext.getContext().getString(MONTHS[calendar.get(Calendar.MONTH)]);
        }
    }

    public static String formatTyping(int[] uids) {
        if (uids.length == 1) {
            UserModel u = users().get(uids[0]);
            if (u != null) {
                return AppContext.getContext().getString(R.string.typing_group)
                        .replace("{0}", u.getName());
            } else {
                return AppContext.getContext().getString(R.string.typing_private);
            }
        } else {
            return AppContext.getContext().getString(R.string.typing_group_many)
                    .replace("{0}", "" + uids.length);
        }
    }
}
