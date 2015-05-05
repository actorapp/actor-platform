/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import im.actor.model.AuthState;
import im.actor.model.entity.MessageState;

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

    public static String convert(MessageState state) {
        switch (state) {
            default:
            case UNKNOWN:
                return "unknown";
            case PENDING:
                return "pending";
            case SENT:
                return "sent";
            case ERROR:
                return "error";
            case READ:
                return "read";
            case RECEIVED:
                return "received";
        }
    }
}
