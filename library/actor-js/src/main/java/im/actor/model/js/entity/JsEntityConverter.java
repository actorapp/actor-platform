/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.entity;

import com.google.gwt.core.client.JavaScriptObject;
import im.actor.model.Messenger;
import im.actor.model.i18n.I18nEngine;
import im.actor.model.js.JsMessenger;

/**
 * Created by ex3ndr on 22.02.15.
 */
public interface JsEntityConverter<F, T extends JavaScriptObject> {
    public T convert(F value, JsMessenger modules);
}
