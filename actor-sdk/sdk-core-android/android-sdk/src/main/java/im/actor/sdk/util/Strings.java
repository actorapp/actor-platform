package im.actor.sdk.util;

import java.util.HashMap;
import java.util.Map;

public class Strings {

    private static final Map<Character, String> charMap = new HashMap<>();

    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String transliterate(String string) {
        if (charMap.size() == 0) {
            synchronized (charMap) {
                if (charMap.size() == 0) {
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
            }
        }
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
