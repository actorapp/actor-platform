package im.actor.runtime.android;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import im.actor.runtime.LocaleRuntime;

public class AndroidLocaleProvider implements LocaleRuntime {

    @Override
    public String getCurrentLocale() {
        Locale current = AndroidContext.getContext().getResources().getConfiguration().locale;
        if (current != null) {
            String res = current.getLanguage();
            if (res != null) {
                return res.substring(0, 1).toUpperCase() + res.substring(1, 2).toLowerCase();
            }
        }
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
