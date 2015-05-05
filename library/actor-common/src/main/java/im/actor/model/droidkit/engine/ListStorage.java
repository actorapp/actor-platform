/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

import java.util.List;

public interface ListStorage {

    // Modifications

    void updateOrAdd(ListEngineRecord valueContainer);

    void updateOrAdd(List<ListEngineRecord> items);

    void delete(long key);

    void delete(long[] keys);

    void clear();

    // Reading

    ListEngineRecord loadItem(long key);

    boolean isEmpty();

    int getCount();
}