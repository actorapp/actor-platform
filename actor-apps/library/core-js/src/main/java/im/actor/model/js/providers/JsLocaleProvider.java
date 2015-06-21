/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers;

import com.google.gwt.core.client.GWT;

import java.util.HashMap;

import im.actor.model.js.providers.locale.LocaleBundle;
import im.actor.model.LocaleProvider;

public class JsLocaleProvider implements LocaleProvider {
    private HashMap<String, String> locale = new HashMap<String, String>();

    public JsLocaleProvider() {
        LocaleBundle bundle = GWT.create(LocaleBundle.class);
        applyFile(bundle.AppText().getText());
        applyFile(bundle.Months().getText());
    }

    private void applyFile(String text) {
        String[] lines = text.split("\n");
        for (String s : lines) {
            s = s.trim();
            if (s.length() == 0) {
                continue;
            } else if (s.startsWith("#")) {
                continue;
            }
            String[] kv = s.split("=", 2);
            locale.put(kv[0].trim(), kv[1].trim());
        }
    }

    @Override
    public HashMap<String, String> loadLocale() {
        return locale;
    }

    @Override
    public boolean is24Hours() {
        return true;
    }
}
