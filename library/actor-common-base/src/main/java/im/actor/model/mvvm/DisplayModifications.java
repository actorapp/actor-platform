/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import java.util.ArrayList;
import java.util.List;

import im.actor.model.droidkit.engine.ListEngineItem;

class DisplayModifications {

    public static <T extends ListEngineItem> DisplayList.Modification<T> addOrUpdate(final T item) {
        ArrayList<T> res = new ArrayList<T>();
        res.add(item);
        return addOrUpdate(res);
    }

    public static <T extends ListEngineItem> DisplayList.Modification<T> addOrUpdate(final List<T> items) {
        return new DisplayList.Modification<T>() {
            @Override
            public DisplayList.ModificationResult<T> modify(ArrayList<T> sourceList) {
                DisplayList.ModificationResult<T> res = new DisplayList.ModificationResult<T>();
                for (T toAdd : items) {
                    addOrUpdate(toAdd, sourceList, res, false);
                }
                return res;
            }
        };
    }

    public static <T extends ListEngineItem> DisplayList.Modification<T> addOnly(final List<T> items) {
        return new DisplayList.Modification<T>() {
            @Override
            public DisplayList.ModificationResult<T> modify(ArrayList<T> sourceList) {
                DisplayList.ModificationResult<T> res = new DisplayList.ModificationResult<T>();
                for (T toAdd : items) {
                    addOrUpdate(toAdd, sourceList, res, true);
                }
                return res;
            }
        };
    }

    public static <T extends ListEngineItem> DisplayList.Modification<T> replace(final List<T> items) {
        return new DisplayList.Modification<T>() {
            @Override
            public DisplayList.ModificationResult<T> modify(ArrayList<T> sourceList) {
                DisplayList.ModificationResult<T> res = new DisplayList.ModificationResult<T>();
                replace(items, sourceList, res);
                return res;
            }
        };
    }

    public static <T extends ListEngineItem> DisplayList.Modification<T> remove(final long dstId) {
        return remove(new long[]{dstId});
    }

    public static <T extends ListEngineItem> DisplayList.Modification<T> remove(final long[] dstIds) {
        return new DisplayList.Modification<T>() {
            @Override
            public DisplayList.ModificationResult<T> modify(ArrayList<T> sourceList) {
                DisplayList.ModificationResult<T> res = new DisplayList.ModificationResult<T>();
                for (int i = 0; i < sourceList.size(); i++) {
                    ListEngineItem src = sourceList.get(i);
                    for (long aDstId : dstIds) {
                        if (src.getEngineId() == aDstId) {
                            sourceList.remove(i);
                            res.appendOperation(new DisplayList.ModificationResult
                                    .Operation(DisplayList.ModificationResult.OperationType.REMOVE, i));
                            i--;
                            break;
                        }
                    }
                }
                return res;
            }
        };
    }

    public static <T> DisplayList.Modification<T> clear() {
        return new DisplayList.Modification<T>() {
            @Override
            public DisplayList.ModificationResult<T> modify(ArrayList<T> sourceList) {
                DisplayList.ModificationResult<T> res = new DisplayList.ModificationResult<T>();
                if (sourceList.size() != 0) {
                    sourceList.clear();
                    res.appendRemove(0, sourceList.size());
                }
                return res;
            }
        };
    }

    private static <T extends ListEngineItem> void replace(List<T> items, ArrayList<T> sourceList,
                                                           DisplayList.ModificationResult<T> result) {
        // Remove missing items
        outer:
        for (int i = 0; i < sourceList.size(); i++) {
            long id = sourceList.get(i).getEngineId();

            for (T itm : items) {
                if (itm.getEngineId() == id) {
                    continue outer;
                }
            }

            result.appendRemove(i, 1);
            sourceList.remove(i);
            i--;
        }

        for (T itm : items) {
            addOrUpdate(itm, sourceList, result, false);
        }
    }

    private static <T extends ListEngineItem> void addOrUpdate(T item,
                                                               ArrayList<T> sourceList,
                                                               DisplayList.ModificationResult<T> result,
                                                               boolean isAddOnly) {
        long id = item.getEngineId();
        long sortKey = item.getEngineSort();

        // Finding suitable place for item
        int removedIndex = -1;
        int addedIndex = -1;
        for (int i = 0; i < sourceList.size(); i++) {
            T srcItem = sourceList.get(i);
            if (srcItem.getEngineId() == id) {
                if (isAddOnly) {
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
            result.appendUpdate(addedIndex, item);
        } else if (removedIndex >= 0) {
            // Movement + update occurred
            // Move to new place, then update element
            // This order is required for iOS lists
            result.appendMove(removedIndex, addedIndex);
            result.appendUpdate(addedIndex, item);
        } else {
            // No old element found: add new element
            result.appendAdd(addedIndex, item);
        }
    }

}
