package im.actor.core.util;

public class StringUtil {

    public static boolean isNullOrEmpty(String src) {
        return src == null || src.isEmpty();
    }

    public static String ellipsize(String src, int maxLength) {
        if (src.length() > maxLength) {
            return src.substring(0, maxLength - 1) + "…";
        } else {
            return src;
        }
    }
}
