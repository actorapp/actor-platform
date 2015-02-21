package im.actor.gwt.app.helpers;

import im.actor.model.AuthState;

/**
 * Created by ex3ndr on 22.02.15.
 */
public class Enums {
    public static String convert(AuthState state) {
        switch (state) {
            default:
            case AUTH_START:
                return "start";
            case CODE_VALIDATION:
                return "code";
            case SIGN_UP:
                return "signup";
            case LOGGED_IN:
                return "logged_in";
        }
    }
}
