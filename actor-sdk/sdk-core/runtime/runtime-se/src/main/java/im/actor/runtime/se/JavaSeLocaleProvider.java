package im.actor.runtime.se;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import im.actor.runtime.LocaleRuntime;

public class JavaSeLocaleProvider implements LocaleRuntime {

    @Override
    public String getCurrentLocale() {
        // TODO: Implement
        return null;
    }

    @Override
    public String formatDate(long date) {
        // TODO: Implement
        return new Date(date).toString();
    }

    @Override
    public String formatTime(long date) {
        // TODO: Implement
        return new Date(date).toString();
    }
}
