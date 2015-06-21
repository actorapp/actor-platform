/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.js.images;

public interface JsResizeListener {
    void onResized(String thumb, int thumbW, int thumbH, int fullW, int fullH);
}
