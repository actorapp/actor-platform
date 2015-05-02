/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

import java.util.List;

public interface ListEngineDisplayLoadCallback<T> {
    void onLoaded(List<T> items, long topSortKey, long bottomSortKey);
}