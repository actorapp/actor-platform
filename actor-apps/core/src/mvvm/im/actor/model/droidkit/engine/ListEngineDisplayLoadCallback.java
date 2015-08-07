/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

public interface ListEngineDisplayLoadCallback<T> {
    @ObjectiveCName("onLoadedWithItems:withTopKey:withBottomKey:")
    void onLoaded(List<T> items, long topSortKey, long bottomSortKey);
}