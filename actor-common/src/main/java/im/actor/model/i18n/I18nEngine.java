package im.actor.model.i18n;

import java.util.Date;
import java.util.HashMap;

import im.actor.model.LocaleProvider;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class I18nEngine {
    private HashMap<String, String> locale;
    private final String[] MONTHS_SHORT;
    private final String[] MONTHS;

    public I18nEngine(LocaleProvider provider) {
        this.locale = provider.loadLocale();
        MONTHS_SHORT = new String[]{
                locale.get("JanShort"),
                locale.get("FebShort"),
                locale.get("MarShort"),
                locale.get("AprShort"),
                locale.get("MayShort"),
                locale.get("JunShort"),
                locale.get("JulShort"),
                locale.get("AugShort"),
                locale.get("SepShort"),
                locale.get("OctShort"),
                locale.get("NovShort"),
                locale.get("DecShort"),
        };

        MONTHS = new String[]{
                locale.get("JanFull"),
                locale.get("FebFull"),
                locale.get("MarFull"),
                locale.get("AprFull"),
                locale.get("MayFull"),
                locale.get("JunFull"),
                locale.get("JulFull"),
                locale.get("AugFull"),
                locale.get("SepFull"),
                locale.get("OctFull"),
                locale.get("NovFull"),
                locale.get("DecFull"),
        };
    }

    private String formatTwoDigit(int v) {
        if (v < 0) {
            return "" + v;
        } else if (v < 10) {
            return "0" + v;
        } else {
            return "" + v;
        }
    }

    public String formatShortDate(long date) {
        // Not using Calendar for GWT
        long delta = new Date().getTime() - date;
        if (delta < 60 * 1000) {
            return locale.get("TimeShortNow");
        } else if (delta < 60 * 60 * 1000) {
            return locale.get("TimeShortMinutes").replace("{minutes}", "" + delta / 60000);
        } else if (delta < 24 * 60 * 60 * 1000) {
            return locale.get("TimeShortHours").replace("{hours}", "" + delta / 3600000);
        } else if (delta < 2 * 24 * 60 * 60 * 1000) {
            return locale.get("TimeShortYesterday").replace("{hours}", "" + delta / 3600000);
        } else {
            // Not using Calendar for GWT
            Date date1 = new Date(date);
            int month = date1.getMonth();
            int d = date1.getDate();
            return d + " " + MONTHS_SHORT[month].toUpperCase();
        }
    }
}