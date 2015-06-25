/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.providers.http;

public interface JsHttpRequestHandler {
    void onStateChanged(JsHttpRequest request);
}
