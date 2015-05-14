/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import java.util.ArrayList;

public class DefferedListModification<T> {

    private Operation operation;
    private int index;
    private int len;
    private int destIndex;

    /* package */ ArrayList<T> items;

    public DefferedListModification(Operation operation, int index, T item) {
        this.operation = operation;
        this.index = index;
        this.items = new ArrayList<T>();
        this.items.add(item);
        this.len = 1;
    }

    public DefferedListModification(Operation operation, int index, ArrayList<T> items) {
        this.operation = operation;
        this.index = index;
        this.items = items;
        this.len = items.size();
    }

    public DefferedListModification(Operation operation, int index) {
        this.operation = operation;
        this.index = index;
    }

    public DefferedListModification(Operation operation, int index, int destIndex, int len) {
        this.operation = operation;
        this.index = index;
        this.len = len;
        this.destIndex = destIndex;
    }

    public DefferedListModification(Operation operation, int index, int len) {
        this.operation = operation;
        this.index = index;
        this.len = len;
    }

    public int getDestIndex() {
        return destIndex;
    }

    public Operation getOperation() {
        return operation;
    }

    public int getIndex() {
        return index;
    }

    public int getLength() {
        return len;
    }

    /* package */ void expand(T item) {
        items.add(item);
        len++;
        if (operation == Operation.ADD) {
            operation = Operation.ADD_RANGE;
        } else if (operation == Operation.REMOVE) {
            operation = Operation.REMOVE_RANGE;
        } else if (operation == Operation.UPDATE) {
            operation = Operation.UPDATE_RANGE;
        }
    }

    /* package */ void replace(T item) {
        items = new ArrayList<T>();
        items.add(item);
        len = 1;
    }

    /* package */ void changeDest(int index) {
        destIndex = index;
    }

    /* package */ void changeIndex(int index) {
        this.index = index;
    }

    /* package */ ArrayList<T> getItems() {
        return items;
    }

    public enum Operation {
        ADD, REMOVE, UPDATE, UPDATE_RANGE, ADD_RANGE, REMOVE_RANGE, MOVE
    }
}
