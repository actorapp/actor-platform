package im.actor.desktop.engines;

import im.actor.model.mvvm.ListEngine;
import im.actor.model.mvvm.ListEngineItem;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class SwingListEngine<T extends ListEngineItem> implements ListEngine<T> {

    private ArrayList<T> items = new ArrayList<T>();

    private SwingListModel model = new SwingListModel();

    public ListModel<T> getListModel() {
        return model;
    }

    @Override
    public void addOrUpdateItem(T item) {
        removeItem(item.getListId());
        int index = items.size();
        for (int i = 0; i < items.size(); i++) {
            T c = items.get(i);
            if (c.getSortingKey() <= item.getSortingKey()) {
                index = i;
                break;
            }
        }

        items.add(index, item);
        model.fireAdded(index, index);
    }

    @Override
    public void addOrUpdateItems(List<T> values) {
        for (T v : values) {
            addOrUpdateItem(v);
        }
    }

    @Override
    public void replaceItems(List<T> values) {
        items.clear();
        addOrUpdateItems(values);
    }

    @Override
    public void removeItem(long id) {
        int index = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getListId() == id) {
                index = i;
                break;

            }
        }
        if (index >= 0) {
            items.remove(index);
            model.fireRemoved(index, index);
        }
    }

    @Override
    public void removeItems(long[] ids) {
        for (long l : ids) {
            removeItem(l);
        }
    }

    @Override
    public void clear() {
        int oldSize = items.size();
        items.clear();
        if (oldSize != 0) {
            model.fireRemoved(0, oldSize);
        }
    }

    @Override
    public T getValue(long id) {
        for (T v : items) {
            if (v.getListId() == id) {
                return v;
            }
        }
        return null;
    }

    @Override
    public T getHeadValue() {
        return null;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    private class SwingListModel extends AbstractListModel<T> {

        public void fireRemoved(int start, int end) {
            fireIntervalRemoved(this, start, end);
        }

        public void fireAdded(int start, int end) {
            fireIntervalAdded(this, start, end);
        }

        @Override
        public int getSize() {
            return items.size();
        }

        @Override
        public T getElementAt(int index) {
            return items.get(index);
        }
    }
}
