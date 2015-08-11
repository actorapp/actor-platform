/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.mvvm;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public interface JsDisplayListCallback<T extends JavaScriptObject> {
    void onCollectionChanged(JsArray<T> array);
}
