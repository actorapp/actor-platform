/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core;

/**
 * State of Authentication
 */
public enum AuthState {
    AUTH_START,
    AUTH_EMAIL,
    AUTH_PHONE,
    CODE_VALIDATION_PHONE,
    CODE_VALIDATION_EMAIL,
    GET_OAUTH_PARAMS,
    COMPLETE_OAUTH,
    SIGN_UP,
    LOGGED_IN,
}
