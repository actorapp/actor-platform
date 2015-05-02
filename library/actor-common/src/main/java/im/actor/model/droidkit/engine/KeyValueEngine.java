/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

import java.util.List;

public interface KeyValueEngine<V extends KeyValueItem> {
    void addOrUpdateItem(V item);

    void addOrUpdateItems(List<V> values);

    void removeItem(long id);

    void removeItems(long[] ids);

    void clear();

    V getValue(long id);
}