package im.actor.core.modules;

import java.util.HashMap;

public final class Errors {

    // Phone number errors

    public static final String LOCAL_EMPTY_PHONE = "LOCAL_EMPTY_PHONE";
    public static final String PHONE_NUMBER_INVALID = "PHONE_NUMBER_INVALID";
    public static final String LOCAL_INCORRECT_PHONE = "LOCAL_INCORRECT_PHONE";

    // Activation code errors

    public static final String PHONE_CODE_EMPTY = "PHONE_CODE_EMPTY";
    public static final String LOCAL_CODE_EMPTY = "LOCAL_CODE_EMPTY";
    public static final String PHONE_CODE_INVALID = "PHONE_CODE_INVALID";
    public static final String PHONE_CODE_EXPIRED = "PHONE_CODE_EXPIRED";

    private static final HashMap<String, String> keyToTitle = new HashMap<String, String>();

    static {

        // Phone numbers

        keyToTitle.put(LOCAL_EMPTY_PHONE, "ErrorEmptyPhone");
        keyToTitle.put(PHONE_NUMBER_INVALID, "ErrorIncorrectPhone");
        keyToTitle.put(LOCAL_INCORRECT_PHONE, "ErrorIncorrectPhone");

        // Code

        keyToTitle.put(PHONE_CODE_EMPTY, "ErrorCodeEmpty");
        keyToTitle.put(LOCAL_CODE_EMPTY, "ErrorCodeEmpty");
        keyToTitle.put(PHONE_CODE_INVALID, "ErrorCodeIncorrect");
        keyToTitle.put(PHONE_CODE_EXPIRED, "ErrorCodeExpired");
    }

    public static String mapError(String tag) {
        return mapError(tag, "ErrorInternal");
    }

    public static String mapError(String tag, String defVal) {
        if (keyToTitle.containsKey(tag)) {
            return keyToTitle.get(tag);
        } else {
            return defVal;
        }
    }
}