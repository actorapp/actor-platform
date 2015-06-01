/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;

import im.actor.model.js.JsMessenger;

public interface JsEntityConverter<F, T extends JavaScriptObject> {
    T convert(F value, JsMessenger modules);
}
