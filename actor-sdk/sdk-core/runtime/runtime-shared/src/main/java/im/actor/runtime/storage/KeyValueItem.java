/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

public interface KeyValueItem {
    @ObjectiveCName("getEngineId")
    long getEngineId();
}
