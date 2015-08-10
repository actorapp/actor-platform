package im.actor.runtime;

import com.google.j2objc.annotations.ObjectiveCName;

public interface LocaleRuntime {

    /**
     * Getting current locale
     *
     * @return two-letter locale
     */
    @ObjectiveCName("getCurrentLocale")
    String getCurrentLocale();

    /**
     * Is local settings use 24 hours format
     *
     * @return is 24 hour format
     */
    @ObjectiveCName("is24Hours")
    boolean is24Hours();


    /**
     * Format date
     *
     * @param date date value
     * @return formatted date
     */
    @ObjectiveCName("formatDate:")
    String formatDate(long date);

    /**
     * Format Time
     *
     * @param date date value
     * @return formatted time
     */
    String formatTime(long date);
}
