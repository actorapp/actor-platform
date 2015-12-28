/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.mvvm;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.ArrayList;

public interface JsEntityConverter<F, T extends JavaScriptObject> {

    boolean isSupportOverlays();

    JavaScriptObject buildOverlay(F prev, F current, F next);

    T convert(F item);
}
