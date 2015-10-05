/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.mvvm;

import java.util.ArrayList;

public class AndroidListUpdate<T> {

    private ArrayList<T> list;
    private ArrayList<ChangeDescription<T>> changes;
    private boolean isLoadMore;

    public AndroidListUpdate(ArrayList<T> list, ArrayList<ChangeDescription<T>> changes, boolean isLoadMore) {
        this.list = new ArrayList<T>(list);
        this.changes = changes;
        this.isLoadMore = isLoadMore;
    }

    public int getSize() {
        return list.size();
    }

    public T getItem(int index) {
        return list.get(index);
    }

    public boolean isLoadMore() {
        return isLoadMore;
    }

    public ChangeDescription<T> next() {
        if (changes.size() == 0) {
            return null;
        }
        ChangeDescription<T> res = changes.remove(0);
        switch (res.getOperationType()) {
            case ADD:
                int addIndex = res.getIndex();
                for (T itm : res.getItems()) {
                    list.add(addIndex++, itm);
                }
                break;
            case UPDATE:
                int updateIndex = res.getIndex();
                for (T itm : res.getItems()) {
                    int index = updateIndex++;
                    list.remove(index);
                    list.add(index, itm);
                }
                break;
            case MOVE:
                T itm = list.remove(res.getIndex());
                list.add(res.getDestIndex(), itm);
                break;
            case REMOVE:
                int removeIndex = res.getIndex();
                for (int i = 0; i < res.getLength(); i++) {
                    list.remove(removeIndex);
                }
                break;
        }
        return res;
    }
}
