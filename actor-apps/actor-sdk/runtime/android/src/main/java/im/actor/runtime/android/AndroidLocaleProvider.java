package im.actor.runtime.android;

import java.text.DateFormat;
import java.util.Date;

import im.actor.runtime.LocaleRuntime;

/**
 * Created by ex3ndr on 10.08.15.
 */
public class AndroidLocaleProvider implements LocaleRuntime {

    @Override
    public String getCurrentLocale() {
        return null;
    }

    @Override
    public String formatDate(long date) {
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(AndroidContext.getContext());
        return dateFormat.format(new Date(date));
    }

    @Override
    public String formatTime(long date) {
        DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(AndroidContext.getContext());
        return dateFormat.format(new Date(date));
    }
}
