/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.modules;

import java.util.HashSet;

public interface JsFileLoadedListener {
    void onFileLoaded(HashSet<Long> fileIds);
}
