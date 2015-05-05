/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.droidkit.engine;

import java.util.List;

import im.actor.model.droidkit.bser.BserObject;

public interface ListEngine<T extends BserObject & ListEngineItem> {

    // Write

    void addOrUpdateItem(T item);

    void addOrUpdateItems(List<T> items);

    void replaceItems(List<T> items);

    void removeItem(long key);

    void removeItems(long[] keys);

    void clear();

    // Read

    T getValue(long key);

    T getHeadValue();

    boolean isEmpty();

    int getCount();
}