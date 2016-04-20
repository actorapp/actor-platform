package im.actor.runtime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocaleRuntimeProvider implements LocaleRuntime {

    @Override
    public String getCurrentLocale() {
        String lang = Locale.getDefault().getLanguage();
        if (lang != null && lang.length() > 1)
            lang = lang.substring(0,1).toUpperCase() + lang.substring(1,2);
        return lang;
    }

    @Override
    public String formatDate(long date) {
        DateFormat dateFormat = new SimpleDateFormat();
        return dateFormat.format(new Date(date));
    }

    @Override
    public String formatTime(long date) {
        DateFormat dateFormat = new SimpleDateFormat();
        return dateFormat.format(new Date(date));
    }
}
