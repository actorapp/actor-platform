/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import java.util.ArrayList;

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

    public AndroidListModification next() {
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
}