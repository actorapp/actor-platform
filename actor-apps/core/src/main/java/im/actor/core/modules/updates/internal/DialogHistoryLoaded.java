/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates.internal;

import im.actor.core.api.rpc.ResponseLoadDialogs;

public class DialogHistoryLoaded extends InternalUpdate {
    private ResponseLoadDialogs dialogs;

    public DialogHistoryLoaded(ResponseLoadDialogs dialogs) {
        this.dialogs = dialogs;
    }

    public ResponseLoadDialogs getDialogs() {
        return dialogs;
    }
}