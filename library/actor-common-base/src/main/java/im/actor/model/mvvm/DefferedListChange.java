/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import java.util.ArrayList;

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

        return new DefferedListChange<T>(initialList, listModifications);
    }
}