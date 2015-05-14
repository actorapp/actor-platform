/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import java.util.ArrayList;

public abstract class AndroidListModification<T> {

    private Operation operation;
    private int index;
    private int len;

    /* package */ ArrayList<T> items;

    public AndroidListModification(Operation operation, int index, ArrayList<T> items) {
        this.operation = operation;
        this.index = index;
        this.items = items;
        this.len = items.size();
    }

    public AndroidListModification(Operation operation, int index) {
        this.operation = operation;
        this.index = index;
    }

    public AndroidListModification(Operation operation, int index, int len) {
        this.operation = operation;
        this.index = index;
        this.len = len;
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

    /* package */ ArrayList<T> getItems() {
        return items;
    }

    public enum Operation {
        ADD, REMOVE, UPDATE, ADD_RANGE, REMOVE_RANGE
    }
}
