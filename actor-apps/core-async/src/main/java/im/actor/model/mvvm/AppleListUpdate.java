/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import com.google.j2objc.annotations.ObjectiveCName;

import java.util.ArrayList;

public class AppleListUpdate<T> {
    private ArrayList<ChangeDescription<T>> changes;

    @ObjectiveCName("initWithChanges:")
    public AppleListUpdate(ArrayList<ChangeDescription<T>> changes) {
        this.changes = changes;
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