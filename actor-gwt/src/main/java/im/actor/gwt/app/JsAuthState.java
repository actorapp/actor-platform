package im.actor.gwt.app;

import org.timepedia.exporter.client.Export;

/**
 * Created by ex3ndr on 21.02.15.
 */
@Export
public enum JsAuthState {
    AUTH_START,
    CODE_VALIDATION,
    SIGN_UP,
    LOGGED_IN,
}
