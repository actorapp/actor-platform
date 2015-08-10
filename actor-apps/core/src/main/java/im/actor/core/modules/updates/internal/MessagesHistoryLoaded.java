/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates.internal;

import im.actor.core.api.rpc.ResponseLoadHistory;
import im.actor.core.entity.Peer;

public class MessagesHistoryLoaded extends InternalUpdate {
    private Peer peer;
    private ResponseLoadHistory loadHistory;

    public MessagesHistoryLoaded(Peer peer, ResponseLoadHistory loadHistory) {
        this.peer = peer;
        this.loadHistory = loadHistory;
    }

    public Peer getPeer() {
        return peer;
    }

    public ResponseLoadHistory getLoadHistory() {
        return loadHistory;
    }
}
