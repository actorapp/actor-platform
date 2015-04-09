package im.actor.model;

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
    public HashMap<String, String> loadLocale();

    /**
     * Is local settings use 24 hours format
     *
     * @return is 24 hour format
     */
    public boolean is24Hours();
}
