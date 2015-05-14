/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DefferedListChange<T> {
    private ArrayList<T> list;
    private ArrayList<DefferedListModification<T>> modifications;

    public DefferedListChange(ArrayList<T> list,
                              ArrayList<DefferedListModification<T>> modifications) {
        this.list = list;
        this.modifications = modifications;
    }

    public T getItem(int index) {
        return list.get(index);
    }

    public int getCount() {
        return list.size();
    }

    public DefferedListModification<T> next() {
        if (modifications.size() == 0) {
            return null;
        }

        DefferedListModification<T> modification = modifications.remove(0);
        switch (modification.getOperation()) {
            case ADD:
            case ADD_RANGE:
                int addIndex = modification.getIndex();
                for (T itm : modification.getItems()) {
                    list.add(addIndex++, itm);
                }
                break;
            case REMOVE:
                list.remove(modification.getIndex());
                break;
            case REMOVE_RANGE:
                int removeIndex = modification.getIndex();
                for (int i = 0; i < modification.getLength(); i++) {
                    list.remove(removeIndex);
                }
                break;
            case MOVE:
                T removed = list.remove(modification.getIndex());
                list.add(modification.getDestIndex(), removed);
                break;
            case UPDATE_RANGE:
            case UPDATE:
                // Do nothing
                break;
        }
        return modification;
    }

    public static <T> DefferedListChange<T> buildAndroidListChange(
            ArrayList<DisplayList.ModificationResult<T>> modificationResults, ArrayList<T> initialList) {

        ArrayList<DefferedListModification<T>> listModifications = new ArrayList<DefferedListModification<T>>();

        DefferedListModification<T> prev = null;
        for (DisplayList.ModificationResult<T> res : modificationResults) {
            for (DisplayList.ModificationResult.Operation<T> operation : res.getOperations()) {
                DefferedListModification<T> mod = null;
                switch (operation.getType()) {
                    case ADD:
                        if (prev != null) {
                            if (prev.getOperation() == DefferedListModification.Operation.ADD) {
                                if (prev.getIndex() == operation.getIndex() - 1) {
                                    prev.expand(operation.getItem());
                                    continue;
                                }
                            } else if (prev.getOperation() == DefferedListModification.Operation.ADD_RANGE) {
                                if (prev.getIndex() + prev.getLength() == operation.getIndex()) {
                                    prev.expand(operation.getItem());
                                    continue;
                                }
                            }
                        }

                        mod = new DefferedListModification<T>(
                                DefferedListModification.Operation.ADD,
                                operation.getIndex(),
                                operation.getItem());
                        break;
                    case UPDATE:
                        if (prev != null) {
                            if (prev.getOperation() == DefferedListModification.Operation.UPDATE) {
                                if (prev.getIndex() == operation.getIndex() - 1) {
                                    prev.expand(operation.getItem());
                                    continue;
                                }
                            } else if (prev.getOperation() == DefferedListModification.Operation.UPDATE_RANGE) {
                                if (prev.getIndex() + prev.getLength() == operation.getIndex()) {
                                    prev.expand(operation.getItem());
                                    continue;
                                }
                            }
                        }
                        mod = new DefferedListModification<T>(
                                DefferedListModification.Operation.UPDATE,
                                operation.getIndex(),
                                operation.getItem());
                        break;
                    case REMOVE:
                        if (prev != null) {
                            if (prev.getOperation() == DefferedListModification.Operation.REMOVE) {
                                if (prev.getIndex() == operation.getIndex() - 1) {
                                    prev.expand(operation.getItem());
                                    continue;
                                }
                            } else if (prev.getOperation() == DefferedListModification.Operation.REMOVE_RANGE) {
                                if (prev.getIndex() + prev.getLength() == operation.getIndex()) {
                                    prev.expand(operation.getItem());
                                    continue;
                                }
                            }
                        }
                        mod = new DefferedListModification<T>(
                                DefferedListModification.Operation.REMOVE,
                                operation.getIndex());
                        break;
                    case MOVE:
                        mod = new DefferedListModification<T>(
                                DefferedListModification.Operation.MOVE,
                                operation.getIndex(), operation.getDestIndex(),
                                1);
                        break;
                }
                if (mod != null) {
                    prev = mod;
                    listModifications.add(mod);
                }
            }
        }

        ArrayList<DefferedListModification<T>> optimizedModifications = new ArrayList<DefferedListModification<T>>();

        // Prepare Deletions
        ArrayList<T> tempList = new ArrayList<T>(initialList);
        HashSet<T> removed = new HashSet<T>();
        for (DefferedListModification<T> m : listModifications) {
            if (m.getOperation() == DefferedListModification.Operation.REMOVE
                    || m.getOperation() == DefferedListModification.Operation.REMOVE_RANGE) {
                int removeIndex = m.getIndex();
                for (int i = 0; i < m.getLength(); i++) {
                    T toRemove = tempList.remove(removeIndex);
                    removed.remove(toRemove);

                    for (int j = 0; j < initialList.size(); j++) {
                        if (initialList.get(j) == toRemove) {
                            optimizedModifications.add(new DefferedListModification<T>(DefferedListModification.Operation.REMOVE,
                                    j));
                            break;
                        }
                    }
                }
            } else {
                switch (m.getOperation()) {
                    case MOVE:
                        T itm = tempList.remove(m.getIndex());
                        tempList.add(m.getDestIndex(), itm);
                        break;
                    case ADD:
                    case ADD_RANGE:
                        int index = m.getIndex();
                        for (T i : m.getItems()) {
                            tempList.add(index++, i);
                        }
                        break;
                    default:
                    case UPDATE_RANGE:
                    case UPDATE:
                        // Do Nothing
                        break;
                }
            }
        }

        // Prepare Add
        tempList = new ArrayList<T>(initialList);
        for (DefferedListModification<T> m : optimizedModifications) {
            int index = m.getIndex();
            for (int i = 0; i < index; i++) {
                tempList.remove(i);
            }
        }
        // TODO: IMPLEMENT!!!


        // Prepare Updates

        // Build list with removed elements
        tempList = new ArrayList<T>(initialList);
        for (DefferedListModification<T> m : optimizedModifications) {
            int index = m.getIndex();
            for (int i = 0; i < index; i++) {
                tempList.remove(i);
            }
        }

        HashMap<T, DefferedListModification<T>> updated =
                new HashMap<T, DefferedListModification<T>>();
        outer:
        for (DefferedListModification<T> m : listModifications) {
            if (m.getOperation() == DefferedListModification.Operation.REMOVE ||
                    m.getOperation() == DefferedListModification.Operation.REMOVE_RANGE) {
                // Already processed
                continue;
            }

            if (m.getOperation() == DefferedListModification.Operation.UPDATE ||
                    m.getOperation() == DefferedListModification.Operation.UPDATE_RANGE) {

                int updateIndex = m.getIndex();
                for (int i = 0; i < m.getLength(); i++) {
                    T newElement = m.getItems().get(i);
                    T oldElement = tempList.remove(updateIndex);
                    tempList.add(updateIndex, oldElement);
                    updateIndex++;

                    if (updated.containsKey(oldElement)) {
                        DefferedListModification<T> oldMod = updated.remove(oldElement);
                        oldMod.replace(newElement);
                        updated.put(newElement, oldMod);
                    } else {
                        for (int j = 0; j < initialList.size(); j++) {
                            if (initialList.get(j) == oldElement) {
                                DefferedListModification<T> mod = new DefferedListModification<T>(
                                        DefferedListModification.Operation.UPDATE, j, newElement);
                                updated.put(newElement, mod);
                                optimizedModifications.add(mod);
                                continue outer;
                            }
                        }
                        throw new RuntimeException("Unknown state");
                    }
                }
            }

            switch (m.getOperation()) {
                case MOVE:
                    T itm = tempList.remove(m.getIndex());
                    // TODO: Enable this? Rewrite for better logic?
//                    if (removed.contains(itm)) {
//                        // Ignore if element was removed
//                        continue;
//                    }
                    tempList.add(m.getDestIndex(), itm);
                    break;
                default:
                    // Do Nothing
                    break;
            }
        }

        // Prepare Moves
        // Prepare list with removed elements
        tempList = new ArrayList<T>(initialList);
        for (DefferedListModification<T> m : optimizedModifications) {
            if (m.getOperation() == DefferedListModification.Operation.REMOVE ||
                    m.getOperation() == DefferedListModification.Operation.REMOVE_RANGE) {
                int index = m.getIndex();
                for (int i = 0; i < index; i++) {
                    tempList.remove(i);
                }
            }
        }

        HashMap<T, DefferedListModification<T>> moved =
                new HashMap<T, DefferedListModification<T>>();

        for (DefferedListModification<T> m : listModifications) {
            if (m.getOperation() == DefferedListModification.Operation.MOVE) {
                T element = tempList.get(m.getIndex());
                if (moved.containsKey(element)) {
                    moved.get(element).changeDest(m.getDestIndex());
                } else {
                    for (int j = 0; j < initialList.size(); j++) {
                        if (initialList.get(j) == element) {
                            DefferedListModification<T> mod = new DefferedListModification<T>(
                                    DefferedListModification.Operation.MOVE, j,
                                    m.getDestIndex());
                            optimizedModifications.add(mod);
                        }
                    }

                }
//                    T newElement = m.getItems().get(i);
//                    T oldElement = tempList.remove(updateIndex);
//                    tempList.add(updateIndex, oldElement);
//                    updateIndex++;
//
//                    if (updated.containsKey(oldElement)) {
//                        DefferedListModification<T> oldMod = updated.remove(oldElement);
//                        oldMod.replace(newElement);
//                        updated.put(newElement, oldMod);
//                    } else {
//                        for (int j = 0; j < initialList.size(); j++) {
//                            if (initialList.get(j) == oldElement) {
//                                DefferedListModification<T> mod = new DefferedListModification<T>(
//                                        DefferedListModification.Operation.UPDATE, j, newElement);
//                                updated.put(newElement, mod);
//                                optimizedModifications.add(mod);
//                                continue outer;
//                            }
//                        }
//                        throw new RuntimeException("Unknown state");
//                    }
            }
        }

        return new DefferedListChange<T>(initialList, optimizedModifications);
    }
}