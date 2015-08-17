/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.storage;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.List;

public interface ListStorage {

    // Modifications

    @ObjectiveCName("updateOrAddWithValue:")
    void updateOrAdd(ListEngineRecord valueContainer);

    @ObjectiveCName("updateOrAddWithList:")
    void updateOrAdd(List<ListEngineRecord> items);

    @ObjectiveCName("deleteWithKey:")
    void delete(long key);

    @ObjectiveCName("deleteWithKeys:")
    void delete(long[] keys);

    @ObjectiveCName("clear")
    void clear();

    // Reading

    @ObjectiveCName("loadItemWithKey:")
    ListEngineRecord loadItem(long key);

    @ObjectiveCName("isEmpty")
    boolean isEmpty();

    @ObjectiveCName("getCount")
    int getCount();
}