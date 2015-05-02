/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

import java.util.List;

public interface KeyValueStorage {
    void addOrUpdateItem(long id, byte[] data);

    void addOrUpdateItems(List<KeyValueRecord> values);

    void removeItem(long id);

    void removeItems(long[] ids);

    void clear();

    byte[] getValue(long id);
}
