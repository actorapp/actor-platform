package im.actor.model.jvm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import im.actor.model.LocaleProvider;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class JavaLocale implements LocaleProvider {

    private HashMap<String, String> items;

    public JavaLocale(String name) {
        items = new HashMap<String, String>();
        loadPart("AppText", name);
        loadPart("Months", name);
    }

    private void loadPart(String name, String locale) {
        String fileName = locale.equals("En") ? name + ".properties" : name + locale + ".properties";
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Object key : properties.keySet()) {
            String sKey = (String) key;
            String sValue = (String) properties.get(key);
            items.put(sKey, sValue);
        }
    }

    @Override
    public HashMap<String, String> loadLocale() {
        return items;
    }
}
