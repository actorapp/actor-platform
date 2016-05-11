package im.actor.runtime.intl;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import im.actor.runtime.LocaleRuntime;
import im.actor.runtime.Runtime;
import im.actor.runtime.intl.plurals.PluralEngine;
import im.actor.runtime.intl.plurals.PluralFactory;
import im.actor.runtime.intl.plurals.PluralType;
import im.actor.runtime.json.JSONException;
import im.actor.runtime.json.JSONObject;

public class IntlEngine {

    private static final String[] EMPTY = new String[0];
    private static final String[] SHORT = new String[]{"short"};
    private static final String[] COMPACT = new String[]{"compact"};
    private static final String[] FULL = new String[]{"full"};
    private static final String[] MONTHS = new String[]{
            "months.january",
            "months.february",
            "months.march",
            "months.april",
            "months.may",
            "months.june",
            "months.july",
            "months.august",
            "months.september",
            "months.october",
            "months.november",
            "months.december",
    };

    private final LocaleRuntime runtime;
    private final HashMap<String, String> keys = new HashMap<>();
    private final PluralEngine plurals;
    private final HashMap<String, String> fallbackKeys = new HashMap<>();
    private final PluralEngine fallbackPlurals;
    private final String appName;
    private final String localeName;
    private final String you;
    private final String thee;

    public IntlEngine(@NotNull String localization) throws JSONException {
        this(localization, null);
    }

    public IntlEngine(@NotNull String localization, @Nullable String fallbackLocalization) throws JSONException {
        traverseObject(keys, new JSONObject(localization), "");
        plurals = PluralFactory.getPluralForLanguage(keys.get("language.code"));
        if (fallbackLocalization != null) {
            traverseObject(fallbackKeys, new JSONObject(localization), "");
            fallbackPlurals = PluralFactory.getPluralForLanguage(fallbackKeys.get("language.code"));
        } else {
            fallbackPlurals = null;
        }

        appName = get("app.name");
        localeName = get("language.code");
        you = get("language.referencing.you");
        thee = get("language.referencing.thee");

        runtime = Runtime.getLocaleRuntime();
    }


    //
    // Dictionary Access
    //

    @ObjectiveCName("get:withVariations:")
    public String get(String key, String[] variations) {
        String res = keys.get(key);
        if (res != null) {
            return res;
        }
        for (String v : variations) {
            res = keys.get(key + "." + v);
            if (res != null) {
                return res;
            }
        }

        if (fallbackKeys.size() > 0) {
            res = fallbackKeys.get(key);
            if (res != null) {
                return res;
            }
            for (String v : variations) {
                res = fallbackKeys.get(key + "." + v);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    @ObjectiveCName("get:")
    public String get(String key) {
        return get(key, EMPTY);
    }

    @ObjectiveCName("getPlural:withCount:")
    public String getPlural(String key, int count) {

        // If no plurals specified
        String res = keys.get(key);
        if (res != null) {
            return res;
        }

        // Searching for main string
        String type = PluralType.toType(plurals.getPluralType(count));
        res = keys.get(key + "." + type);
        if (res != null) {
            return res;
        }

        // Searching in fallback
        if (fallbackPlurals != null) {
            type = PluralType.toType(fallbackPlurals.getPluralType(count));
            res = fallbackKeys.get(key + "." + type);
            if (res != null) {
                return res;
            }
        }

        return null;
    }

    @ObjectiveCName("getApplicationName")
    public String getAppName() {
        return appName;
    }

    @ObjectiveCName("getLocaleName")
    public String getLocaleName() {
        return localeName;
    }

    @ObjectiveCName("getYouVerb")
    public String getYouVerb() {
        return you;
    }

    @ObjectiveCName("getTheeVerb")
    public String getTheeVerb() {
        return thee;
    }

    //
    // Formatting
    //

    @ObjectiveCName("formatShortDate:")
    public String formatShortDate(long date) {
        // Not using Calendar for GWT
        long delta = new Date().getTime() - date;
        if (delta < 60 * 1000) {
            return get("language.format.time.now");
        } else if (delta < 60 * 60 * 1000) {
            return get("language.format.time.minutes", SHORT)
                    .replace("{minutes}", "" + delta / 60000);
        } else if (delta < 24 * 60 * 60 * 1000) {
            return get("language.format.time.hours", SHORT)
                    .replace("{hours}", "" + delta / 3600000);
        } else if (delta < 2 * 24 * 60 * 60 * 1000) {
            return get("language.format.time.yesterday", SHORT);
        } else {
            // Not using Calendar for GWT
            Date date1 = new Date(date);
            int month = date1.getMonth();
            int d = date1.getDate();
            return d + " " + get(MONTHS[month], COMPACT);
        }
    }

    @ObjectiveCName("formatMonth:")
    public String formatMonth(Date date) {
        int month = date.getMonth();
        int d = date.getDate();
        return d + " " + get(MONTHS[month], FULL);
    }

    @ObjectiveCName("formatTime:")
    public String formatTime(long date) {
        return runtime.formatTime(date);
    }

    @ObjectiveCName("formatDate:")
    public String formatDate(long date) {
        return runtime.formatDate(date);
    }

    @ObjectiveCName("formatSequence:")
    public String formatSequence(List<String> values) {
        String res = values.get(0);
        for (int i = 1; i < values.size(); i++) {
            if (i == values.size() - 1) {
                res += get("language.sequence.and");
            } else {
                res += get("language.sequence.or");
            }
            res += values.get(i);
        }
        return res;
    }

    @ObjectiveCName("formatTwoDigit:")
    public String formatTwoDigit(int v) {
        if (v < 0) {
            return "00";
        } else if (v < 10) {
            return "0" + v;
        } else if (v < 100) {
            return "" + v;
        } else {
            String res = "" + v;
            return res.substring(res.length() - 2);
        }
    }

    @ObjectiveCName("areSameDaysWithA:withB:")
    public boolean areSameDays(long a, long b) {
        Date date1 = new Date(a);
        int y1 = date1.getYear();
        int m1 = date1.getMonth();
        int d1 = date1.getDate();
        Date date2 = new Date(b);
        int y2 = date2.getYear();
        int m2 = date2.getMonth();
        int d2 = date2.getDate();

        return y1 == y2 && m1 == m2 && d1 == d2;
    }

    @ObjectiveCName("formatDuration:")
    public String formatDuration(int duration) {
        if (duration < 60) {
            return formatTwoDigit(0) + ":" + formatTwoDigit(duration);
        } else if (duration < 60 * 60) {
            return formatTwoDigit(duration / 60) + ":" + formatTwoDigit(duration % 60);
        } else {
            return formatTwoDigit(duration / 3600) + ":" + formatTwoDigit(duration / 60) + ":" + formatTwoDigit(duration % 60);
        }
    }

    @ObjectiveCName("formatFileSize:")
    public String formatFileSize(int bytes) {
        if (bytes < 0) {
            bytes = 0;
        }

        // TODO: Better rounding
        if (bytes < 1024) {
            return get("language.file_size.bytes")
                    .replace("{bytes}", "" + bytes);
        } else if (bytes < 1024 * 1024) {
            return get("language.file_size.kbytes")
                    .replace("{kbytes}", "" + (bytes / 1024));
        } else if (bytes < 1024 * 1024 * 1024) {
            return get("language.file_size.mbytes")
                    .replace("{mbytes}", "" + (bytes / (1024 * 1024)));
        } else {
            return get("language.file_size.gbytes")
                    .replace("{gbytes}", "" + (bytes / (1024 * 1024 * 1024)));
        }
    }

    @ObjectiveCName("formatFastName:")
    public String formatFastName(String name) {
        if (name.length() > 1) {
            if (Character.isLetter(name.charAt(0))) {
                return name.substring(0, 1).toUpperCase();
            } else {
                return "#";
            }
        } else {
            return "#";
        }
    }

    //
    // Tools
    //

    private void traverseObject(HashMap<String, String> keys, JSONObject src, String prefix) throws JSONException {
        for (String s : src.keySet()) {
            Object itm = src.get(s);
            if (itm instanceof String) {
                keys.put(prefix + s, (String) itm);
            } else if (itm instanceof JSONObject) {
                traverseObject(keys, (JSONObject) itm, prefix + s + ".");
            } else {
                throw new RuntimeException("Unexpected object: " + itm);
            }
        }
    }
}