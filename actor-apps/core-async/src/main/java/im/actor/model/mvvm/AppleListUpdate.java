/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;

public class AppleListUpdate<T> {
    private ArrayList<ChangeDescription<T>> changes;
    private boolean isLoadMore;

    @ObjectiveCName("initWithChanges:withLoadMore:")
    public AppleListUpdate(ArrayList<ChangeDescription<T>> changes, boolean isLoadMore) {
        this.changes = changes;
        this.isLoadMore = isLoadMore;
    }

    @ObjectiveCName("isLoadMore")
    public boolean isLoadMore() {
        return isLoadMore;
    }

    @ObjectiveCName("changes")
    public ArrayList<ChangeDescription<T>> getChanges() {
        return changes;
    }

    @ObjectiveCName("size")
    public int size() {
        return changes.size();
    }

    @ObjectiveCName("changeAt:")
    public ChangeDescription<T> getChangeAt(int index) {
        return changes.get(index);
    }
}