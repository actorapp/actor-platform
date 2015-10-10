/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.entity;

import im.actor.core.AuthState;
import im.actor.core.entity.MessageState;

public class Enums {
    public static String convert(AuthState state) {
        switch (state) {
            default:
            case AUTH_START:
                return "start";
            case CODE_VALIDATION_PHONE:
                return "code";
            case CODE_VALIDATION_EMAIL:
                return "code_email";
            case GET_OAUTH_PARAMS:
                return "get_oauth_params";
            case COMPLETE_OAUTH:
                return "complete_oauth";
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
