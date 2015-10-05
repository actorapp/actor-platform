/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.mvvm;

import com.google.gwt.core.client.JavaScriptObject;

public interface JsEntityConverter<F, T extends JavaScriptObject> {
    T convert(F value);
}
