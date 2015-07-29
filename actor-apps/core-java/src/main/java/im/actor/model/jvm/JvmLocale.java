package im.actor.model.jvm;/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;

import im.actor.model.LocaleProvider;

public class JvmLocale implements LocaleProvider {

    private HashMap<String, String> items;

    public JvmLocale(String name) {
        items = new HashMap<String, String>();
        loadPart("AppText", name);
        loadPart("Months", name);
    }

    private void loadPart(String name, String locale) {
        String fileName = locale.equals("En") ? name + ".properties" : name + "_" + locale + ".properties";
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Object key : properties.keySet()) {
            String sKey = (String) key;
            String sValue = (String) properties.get(key);
            try {
                items.put(sKey, new String(sValue.getBytes("ISO-8859-1"), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public HashMap<String, String> loadLocale() {
        return items;
    }

    @Override
    public boolean is24Hours() {
        return true;
    }
}
