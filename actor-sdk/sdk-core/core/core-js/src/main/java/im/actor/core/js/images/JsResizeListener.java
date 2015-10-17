/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.js.images;

public interface JsResizeListener {
    void onResized(String thumb, int thumbW, int thumbH, int fullW, int fullH);
}
