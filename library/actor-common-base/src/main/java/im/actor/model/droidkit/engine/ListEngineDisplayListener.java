/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

import java.util.List;

public interface ListEngineDisplayListener<T> {

    void onItemRemoved(long id);

    void onItemsRemoved(long[] ids);

    void addOrUpdate(T item);

    void addOrUpdate(List<T> items);

    void onItemsReplaced(List<T> items);

    void onListClear();
}
