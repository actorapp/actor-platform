/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import im.actor.model.droidkit.engine.ListEngineItem;

class DisplayModifications {

    public static <T extends ListEngineItem> DisplayList.Modification<T> addOrUpdate(final T item) {
        return new DisplayList.Modification<T>() {
            @Override
            public void modify(List<T> sourceList) {
                long dstId = item.getEngineId();

                Iterator<T> iterator = sourceList.iterator();
                while (iterator.hasNext()) {
                    T src = iterator.next();
                    if (src.getEngineId() == dstId) {
                        iterator.remove();
                    }
                }

                sourceList.add(item);
            }
        };
    }

    public static <T extends ListEngineItem> DisplayList.Modification<T> addOrUpdate(final List<T> items) {
        return new DisplayList.Modification<T>() {
            @Override
            public void modify(List<T> sourceList) {
                HashSet<Long> keys = new HashSet<Long>();
                for (ListEngineItem i : items) {
                    keys.add(i.getEngineId());
                }
                Iterator<T> iterator = sourceList.iterator();
                while (iterator.hasNext()) {
                    T src = iterator.next();
                    if (keys.contains(src.getEngineId())) {
                        iterator.remove();
                    }
                }

                sourceList.addAll(items);
            }
        };
    }

    public static <T extends ListEngineItem> DisplayList.Modification<T> addOnly(final List<T> items) {
        return new DisplayList.Modification<T>() {
            @Override
            public void modify(List<T> sourceList) {

                ArrayList<T> toAdd = new ArrayList<T>();
                outer:
                for (T t : items) {
                    for (T srcT : sourceList) {
                        if (srcT.getEngineId() == t.getEngineId()) {
                            continue outer;
                        }
                    }
                    toAdd.add(t);
                }

                sourceList.addAll(toAdd);
            }
        };
    }

    public static <T extends ListEngineItem> DisplayList.Modification<T> replace(final List<T> items) {
        return new DisplayList.Modification<T>() {
            @Override
            public void modify(List<T> sourceList) {
                sourceList.clear();
                sourceList.addAll(items);
            }
        };
    }

    public static <T extends ListEngineItem> DisplayList.Modification<T> remove(final long dstId) {
        return new DisplayList.Modification<T>() {
            @Override
            public void modify(List<T> sourceList) {
                Iterator<T> iterator = sourceList.iterator();
                while (iterator.hasNext()) {
                    ListEngineItem src = iterator.next();
                    if (src.getEngineId() == dstId) {
                        iterator.remove();
                    }
                }
            }
        };
    }

    public static <T extends ListEngineItem> DisplayList.Modification<T> remove(final long[] dstIds) {
        return new DisplayList.Modification<T>() {
            @Override
            public void modify(List<T> sourceList) {
                Iterator<T> it = sourceList.iterator();
                while (it.hasNext()) {
                    T value = it.next();
                    long srcId = value.getEngineId();
                    for (long dstId : dstIds) {
                        if (dstId == srcId) {
                            it.remove();
                            break;
                        }
                    }
                }
            }
        };
    }

    public static <T> DisplayList.Modification<T> clear() {
        return new DisplayList.Modification<T>() {
            @Override
            public void modify(List<T> sourceList) {
                sourceList.clear();
            }
        };
    }
}
