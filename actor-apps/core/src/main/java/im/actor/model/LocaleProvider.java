/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.HashMap;

/**
 * Provider for i18n support
 * Used to parse required files from resources and provide string map for them
 */
public interface LocaleProvider {
    /**
     * Load locale data
     *
     * @return map of i18n strings
     */
    @ObjectiveCName("loadLocale")
    HashMap<String, String> loadLocale();

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
}
