/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.mvvm.alg;

import java.util.ArrayList;
import java.util.List;

import im.actor.runtime.generic.mvvm.ChangeDescription;
import im.actor.runtime.storage.ListEngineItem;

public class Modifications {

    public static <T extends ListEngineItem> Modification<T> noOp() {
        return new Modification<T>() {
            @Override
            public List<ChangeDescription<T>> modify(ArrayList<T> sourceList) {
                return new ArrayList<ChangeDescription<T>>();
            }
        };
    }

    public static <T extends ListEngineItem> Modification<T> addOrUpdate(final T item) {
        ArrayList<T> res = new ArrayList<T>();
        res.add(item);
        return addOrUpdate(res);
    }

    public static <T extends ListEngineItem> Modification<T> addOrUpdate(final List<T> items) {
        return new Modification<T>() {
            @Override
            public List<ChangeDescription<T>> modify(ArrayList<T> sourceList) {
                ArrayList<ChangeDescription<T>> res = new ArrayList<ChangeDescription<T>>();
                for (T toAdd : items) {
                    addOrUpdate(toAdd, sourceList, res, false);
                }
                return res;
            }
        };
    }

    public static <T extends ListEngineItem> Modification<T> addLoadMore(final List<T> items) {
        return new Modification<T>() {
            @Override
            public List<ChangeDescription<T>> modify(ArrayList<T> sourceList) {
                ArrayList<ChangeDescription<T>> res = new ArrayList<ChangeDescription<T>>();
                for (T toAdd : items) {
                    addOrUpdate(toAdd, sourceList, res, true);
                }
                return res;
            }
        };
    }

    public static <T extends ListEngineItem> Modification<T> replace(final List<T> items) {
        return new Modification<T>() {
            @Override
            public List<ChangeDescription<T>> modify(ArrayList<T> sourceList) {
                ArrayList<ChangeDescription<T>> res = new ArrayList<ChangeDescription<T>>();
                replace(items, sourceList, res);
                return res;
            }
        };
    }

    public static <T extends ListEngineItem> Modification<T> remove(final long dstId) {
        return remove(new long[]{dstId});
    }

    public static <T extends ListEngineItem> Modification<T> remove(final long[] dstIds) {
        return new Modification<T>() {
            @Override
            public List<ChangeDescription<T>> modify(ArrayList<T> sourceList) {
                ArrayList<ChangeDescription<T>> res = new ArrayList<ChangeDescription<T>>();
                for (int i = 0; i < sourceList.size(); i++) {
                    ListEngineItem src = sourceList.get(i);
                    for (long aDstId : dstIds) {
                        if (src.getEngineId() == aDstId) {
                            sourceList.remove(i);
                            res.add(ChangeDescription.<T>remove(i));
                            i--;
                            break;
                        }
                    }
                }
                return res;
            }
        };
    }

    public static <T> Modification<T> clear() {
        return new Modification<T>() {
            @Override
            public List<ChangeDescription<T>> modify(ArrayList<T> sourceList) {
                ArrayList<ChangeDescription<T>> res = new ArrayList<ChangeDescription<T>>();
                if (sourceList.size() != 0) {
                    res.add(ChangeDescription.<T>remove(0, sourceList.size()));
                    sourceList.clear();
                }
                return res;
            }
        };
    }

    private static <T extends ListEngineItem> void replace(List<T> items,
                                                           ArrayList<T> sourceList,
                                                           ArrayList<ChangeDescription<T>> changes) {
        // Remove missing items
        outer:
        for (int i = 0; i < sourceList.size(); i++) {
            long id = sourceList.get(i).getEngineId();

            for (T itm : items) {
                if (itm.getEngineId() == id) {
                    continue outer;
                }
            }

            changes.add(ChangeDescription.<T>remove(i));
            sourceList.remove(i);
            i--;
        }

        for (T itm : items) {
            addOrUpdate(itm, sourceList, changes, false);
        }
    }

    private static <T extends ListEngineItem> void addOrUpdate(T item,
                                                               ArrayList<T> sourceList,
                                                               ArrayList<ChangeDescription<T>> changes,
                                                               boolean isLoadMore) {
        long id = item.getEngineId();
        long sortKey = item.getEngineSort();

        // Finding suitable place for item
        int removedIndex = -1;
        int addedIndex = -1;
        for (int i = 0; i < sourceList.size(); i++) {
            T srcItem = sourceList.get(i);
            if (srcItem.getEngineId() == id) {
                if (isLoadMore) {
                    return;
                }
                // Remove old item
                sourceList.remove(i);
                if (addedIndex >= 0) {
                    removedIndex = i - 1;
                } else {
                    removedIndex = i;
                }
                i--;
                continue;
            } else {
                // TODO: Fix ADD ONLY
                if ((addedIndex < 0) && sortKey > srcItem.getEngineSort()) {
                    addedIndex = i;
                    sourceList.add(i, item);
                    i++;
                }
            }

            // Already founded
            if (addedIndex >= 0 && removedIndex >= 0) {
                break;
            }
        }

        // If no place for insert: insert to end
        if (addedIndex < 0) {
            addedIndex = sourceList.size();
            sourceList.add(sourceList.size(), item);
        }

        if (addedIndex == removedIndex) {
            // If there are no movement: just update item in place
            changes.add(ChangeDescription.update(addedIndex, item));
        } else if (removedIndex >= 0) {
            // Movement + update occurred
            changes.add(ChangeDescription.update(removedIndex, item));
            changes.add(ChangeDescription.<T>move(removedIndex, addedIndex));
        } else {
            // No old element found: add new element
            changes.add(ChangeDescription.add(addedIndex, item));
        }
    }

}
