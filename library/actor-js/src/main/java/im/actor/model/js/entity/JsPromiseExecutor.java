/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

public interface JsPromiseExecutor {
    void execute(JsPromiseResolve resolve, JsPromiseReject reject);
}
