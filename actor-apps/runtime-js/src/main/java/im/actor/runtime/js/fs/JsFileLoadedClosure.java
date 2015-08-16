/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.js.fs;

import com.google.gwt.typedarrays.shared.ArrayBuffer;

public interface JsFileLoadedClosure {
    void onLoaded(ArrayBuffer message);
}
