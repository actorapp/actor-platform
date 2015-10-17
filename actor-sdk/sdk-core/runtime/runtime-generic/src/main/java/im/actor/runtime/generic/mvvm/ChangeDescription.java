/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.mvvm;

import java.util.ArrayList;
import java.util.List;

public class ChangeDescription<T> {

    public static <T> ChangeDescription<T> mergeAdd(ChangeDescription<T> a, ChangeDescription<T> b) {
        ArrayList<T> items = new ArrayList<T>();
        items.addAll(a.getItems());
        items.addAll(b.getItems());
        return new ChangeDescription<T>(OperationType.ADD, a.getIndex(), 0, items.size(), items);
    }

    public static <T> ChangeDescription<T> add(int index, T item) {
        ArrayList<T> items = new ArrayList<T>();
        items.add(item);
        return add(index, items);
    }

    public static <T> ChangeDescription<T> add(int index, List<T> items) {
        return new ChangeDescription<T>(OperationType.ADD, index, 0, items.size(),
                new ArrayList<T>(items));
    }

    public static <T> ChangeDescription<T> remove(int index) {
        return remove(index, 1);
    }

    public static <T> ChangeDescription<T> remove(int index, int length) {
        return new ChangeDescription<T>(OperationType.REMOVE, index, 0, length, null);
    }

    public static <T> ChangeDescription<T> update(int index, T item) {
        ArrayList<T> items = new ArrayList<T>();
        items.add(item);
        return update(index, items);
    }

    public static <T> ChangeDescription<T> update(int index, List<T> items) {
        return new ChangeDescription<T>(OperationType.UPDATE, index, 0, items.size(),
                new ArrayList<T>(items));
    }

    public static <T> ChangeDescription<T> move(int index, int destIndex) {
        return new ChangeDescription<T>(OperationType.MOVE, index, destIndex, 1, null);
    }

    private OperationType operationType;
    private int index;
    private int destIndex;
    private int length;
    private ArrayList<T> item;

    private ChangeDescription(OperationType operationType, int index, int destIndex, int length,
                              ArrayList<T> item) {
        this.operationType = operationType;
        this.index = index;
        this.destIndex = destIndex;
        this.length = length;
        this.item = item;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public int getIndex() {
        return index;
    }

    public int getDestIndex() {
        return destIndex;
    }

    public int getLength() {
        return length;
    }

    public ArrayList<T> getItems() {
        return item;
    }

    public <V> ChangeDescription<V> cast() {
        return (ChangeDescription<V>) this;
    }


    public enum OperationType {
        ADD, REMOVE, UPDATE, MOVE
    }

    @Override
    public String toString() {
        return "{" + operationType +
                " | " + index + " -> " + destIndex +
                " | #" + length +
                " | " + item + '}';
    }
}
