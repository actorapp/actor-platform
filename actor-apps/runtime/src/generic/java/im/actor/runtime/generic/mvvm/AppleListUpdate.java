/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.runtime.generic.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;
import com.google.j2objc.annotations.Property;

import java.util.ArrayList;

import im.actor.runtime.generic.mvvm.alg.Move;

public class AppleListUpdate {

    @Property
    private ArrayList<Integer> removed;
    @Property
    private ArrayList<Integer> added;
    @Property
    private ArrayList<Move> moved;
    @Property
    private ArrayList<Integer> updated;
    @Property
    private boolean isLoadMore;

    @ObjectiveCName("initWithRemoved:withAdded:withMoved:withUpdated:withLoadMore:")
    public AppleListUpdate(ArrayList<Integer> removed,
                           ArrayList<Integer> added,
                           ArrayList<Move> moved,
                           ArrayList<Integer> updated, boolean isLoadMore) {
        this.removed = removed;
        this.added = added;
        this.moved = moved;
        this.updated = updated;
        this.isLoadMore = isLoadMore;
    }

    @ObjectiveCName("removedCount")
    public int removedCount() {
        return removed.size();
    }

    @ObjectiveCName("getRemoved:")
    public int getRemoved(int index) {
        return removed.get(index);
    }

    @ObjectiveCName("addedCount")
    public int addedCount() {
        return added.size();
    }

    @ObjectiveCName("getAdded:")
    public int getAdded(int index) {
        return added.get(index);
    }

    @ObjectiveCName("movedCount")
    public int movedCount() {
        return moved.size();
    }

    @ObjectiveCName("getMoved:")
    public Move getMoved(int index) {
        return moved.get(index);
    }

    @ObjectiveCName("updatedCount")
    public int updatedCount() {
        return updated.size();
    }

    @ObjectiveCName("getUpdated:")
    public int getUpdated(int index) {
        return updated.get(index);
    }

    @ObjectiveCName("size")
    public int size() {
        return removed.size() + added.size() + moved.size() + updated.size();
    }

    @ObjectiveCName("nonUpdateCount")
    public int nonUpdateCount() {
        return removed.size() + added.size() + moved.size();
    }

    @ObjectiveCName("isLoadMore")
    public boolean isLoadMore() {
        return isLoadMore;
    }
}