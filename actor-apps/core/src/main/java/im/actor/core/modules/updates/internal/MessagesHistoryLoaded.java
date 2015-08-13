/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

package im.actor.core.modules.updates.internal;

import im.actor.core.api.rpc.ResponseLoadHistory;
import im.actor.core.entity.PeerEntity;

public class MessagesHistoryLoaded extends InternalUpdate {
    private PeerEntity peer;
    private ResponseLoadHistory loadHistory;

    public MessagesHistoryLoaded(PeerEntity peer, ResponseLoadHistory loadHistory) {
        this.peer = peer;
        this.loadHistory = loadHistory;
    }

    public PeerEntity getPeer() {
        return peer;
    }

    public ResponseLoadHistory getLoadHistory() {
        return loadHistory;
    }
}
