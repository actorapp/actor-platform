/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.sequence.internal;

import im.actor.core.api.rpc.ResponseLoadArchived;

public class ArchivedDialogLoaded extends InternalUpdate {
    private ResponseLoadArchived dialogs;

    public ArchivedDialogLoaded(ResponseLoadArchived dialogs) {
        this.dialogs = dialogs;
    }

    public ResponseLoadArchived getDialogs() {
        return dialogs;
    }
}