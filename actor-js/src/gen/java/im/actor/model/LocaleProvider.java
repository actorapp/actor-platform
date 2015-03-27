package im.actor.model;

import java.util.HashMap;

/**
 * Created by ex3ndr on 22.02.15.
 */
public interface LocaleProvider {
    public HashMap<String, String> loadLocale();

    public boolean is24Hours();
}
