/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.model.mvvm;

import java.util.ArrayList;

public class AppleListUpdate<T> {
    private ArrayList<ChangeDescription<T>> changes;

    public AppleListUpdate(ArrayList<ChangeDescription<T>> changes) {
        this.changes = changes;
    }

    public ArrayList<ChangeDescription<T>> getChanges() {
        return changes;
    }
}
