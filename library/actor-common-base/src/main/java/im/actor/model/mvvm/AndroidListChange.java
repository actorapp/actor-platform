/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import java.util.ArrayList;

import im.actor.model.log.Log;

public class AndroidListChange<T> {
    private ArrayList<T> list;
    private ArrayList<AndroidListModification<T>> modifications;

    public AndroidListChange(ArrayList<T> list,
                             ArrayList<AndroidListModification<T>> modifications) {
        this.list = list;
        this.modifications = modifications;
    }

    public T getItem(int index) {
        return list.get(index);
    }

    public int getCount() {
        return list.size();
    }

    public AndroidListModification<T> next() {
        if (modifications.size() == 0) {
            return null;
        }

        AndroidListModification<T> modification = modifications.remove(0);
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
            case UPDATE:
                // Do nothing
                break;
        }
        return modification;
    }

    public static <T> AndroidListChange<T> buildAndroidListChange(
            ArrayList<DisplayList.ModificationResult<T>> modificationResults, ArrayList<T> initialList) {

        ArrayList<AndroidListModification<T>> listModifications = new ArrayList<AndroidListModification<T>>();

        Log.d("AndroidListChange", "Start");
        for (DisplayList.ModificationResult<T> res : modificationResults) {
            for (DisplayList.ModificationResult.Operation<T> operation : res.getOperations()) {
                Log.d("AndroidListChange", operation.getType() + " @" + operation.getIndex());
                switch (operation.getType()) {
                    case ADD:
                        listModifications.add(
                                new AndroidListModification<T>(
                                        AndroidListModification.Operation.ADD,
                                        operation.getIndex(),
                                        operation.getItem()));
                        break;
                    case UPDATE:
                        listModifications.add(new AndroidListModification<T>(
                                AndroidListModification.Operation.UPDATE,
                                operation.getIndex(),
                                operation.getItem()));
                        break;
                    case REMOVE:
                        listModifications.add(new AndroidListModification<T>(
                                AndroidListModification.Operation.REMOVE,
                                operation.getIndex()));
                        break;
                    case MOVE:
                        listModifications.add(new AndroidListModification<T>(
                                AndroidListModification.Operation.MOVE,
                                operation.getIndex(), operation.getDestIndex(),
                                1));
                        break;
                }
            }
        }
        Log.d("AndroidListChange", "End");

        return new AndroidListChange<T>(initialList, listModifications);
    }
}