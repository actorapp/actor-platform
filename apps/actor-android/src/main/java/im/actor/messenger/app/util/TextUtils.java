package im.actor.messenger.app.util;

import im.actor.messenger.app.AppContext;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TextUtils {

    private static ThreadLocal<java.text.DateFormat> TIME_FORMATTER = new ThreadLocal<java.text.DateFormat>() {
        @Override
        protected java.text.DateFormat initialValue() {
            return android.text.format.DateFormat.getTimeFormat(AppContext.getContext());
        }
    };

    private static ThreadLocal<Calendar> CALENDAR = new ThreadLocal<Calendar>() {
        @Override
        protected Calendar initialValue() {
            return Calendar.getInstance();
        }
    };

    private static ThreadLocal<SimpleDateFormat> DATE_YEAR_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd '%s' ''yy");
        }
    };

    private static ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd '%s'");
        }
    };

    private static ThreadLocal<SimpleDateFormat> MONTH_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MMMM");
        }
    };

    public static String formatTime(long time) {
        return TIME_FORMATTER.get().format(new Date(time));
    }

    public static String formatDate(long date) {
        String month = MONTH_FORMATTER.get().format(date).toUpperCase();
        Calendar calendar = CALENDAR.get();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentYear = calendar.get(Calendar.YEAR);
        calendar.setTimeInMillis(date);

        if (calendar.get(Calendar.YEAR) == currentYear) {
            return String.format(DATE_FORMATTER.get().format(date), month);
        } else {
            return String.format(DATE_YEAR_FORMATTER.get().format(date), month);
        }
    }

    public static boolean areSameDays(long a, long b) {
        Calendar calendar = CALENDAR.get();
        calendar.setTimeInMillis(a);
        int y1 = calendar.get(Calendar.YEAR);
        int m1 = calendar.get(Calendar.MONTH);
        int d1 = calendar.get(Calendar.DATE);
        calendar.setTimeInMillis(b);
        int y2 = calendar.get(Calendar.YEAR);
        int m2 = calendar.get(Calendar.MONTH);
        int d2 = calendar.get(Calendar.DATE);

        return y1 == y2 && m1 == m2 && d1 == d2;
    }

    private static final Map<Character, String> charMap = new HashMap<Character, String>();

    static {
        charMap.put('а', "a");
        charMap.put('б', "b");
        charMap.put('в', "v");
        charMap.put('г', "g");
        charMap.put('д', "d");
        charMap.put('е', "e");
        charMap.put('ё', "e");
        charMap.put('ж', "zh");
        charMap.put('з', "z");
        charMap.put('и', "i");
        charMap.put('й', "i");
        charMap.put('к', "k");
        charMap.put('л', "l");
        charMap.put('м', "m");
        charMap.put('н', "n");
        charMap.put('о', "o");
        charMap.put('п', "p");
        charMap.put('р', "r");
        charMap.put('с', "s");
        charMap.put('т', "t");
        charMap.put('у', "u");
        charMap.put('ф', "f");
        charMap.put('х', "h");
        charMap.put('ц', "c");
        charMap.put('ч', "ch");
        charMap.put('ш', "sh");
        charMap.put('щ', "sh");
        charMap.put('ъ', "'");
        charMap.put('ы', "y");
        charMap.put('ь', "'");
        charMap.put('э', "e");
        charMap.put('ю', "u");
        charMap.put('я', "ya");

        charMap.put('a', "а");
        charMap.put('b', "б");
        charMap.put('c', "ц");
        charMap.put('d', "д");
        charMap.put('e', "е");
        charMap.put('f', "ф");
        charMap.put('g', "г");
        charMap.put('h', "х");
        charMap.put('i', "и");
        charMap.put('j', "дж");
        charMap.put('k', "к");
        charMap.put('l', "л");
        charMap.put('m', "м");
        charMap.put('n', "н");
        charMap.put('o', "о");
        charMap.put('p', "п");
        charMap.put('q', "к");
        charMap.put('r', "р");
        charMap.put('s', "с");
        charMap.put('t', "т");
        charMap.put('u', "ю");
        charMap.put('v', "в");
        charMap.put('w', "в");
        charMap.put('x', "кс");
        charMap.put('y', "й");
        charMap.put('z', "з");
    }

    public static String transliterate(String string) {
        StringBuilder transliteratedString = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            Character ch = string.charAt(i);
            String charFromMap = charMap.get(ch);
            if (charFromMap == null) {
                transliteratedString.append(ch);
            } else {
                transliteratedString.append(charFromMap);
            }
        }
        return transliteratedString.toString();
    }
}

