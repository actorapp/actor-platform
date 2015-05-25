/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

import com.google.j2objc.annotations.ObjectiveCName;

import org.jetbrains.annotations.Nullable;

public interface ListEngineItem {
    @ObjectiveCName("getEngineId")
    long getEngineId();

    @ObjectiveCName("getEngineSort")
    long getEngineSort();

    @Nullable
    @ObjectiveCName("getEngineSearch")
    String getEngineSearch();
}
