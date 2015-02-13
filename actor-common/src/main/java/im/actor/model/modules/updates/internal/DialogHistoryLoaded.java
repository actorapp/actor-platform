package im.actor.model.modules.updates.internal;

import im.actor.model.api.rpc.ResponseLoadDialogs;

/**
 * Created by ex3ndr on 13.02.15.
 */
public class DialogHistoryLoaded extends InternalUpdate {
    private ResponseLoadDialogs dialogs;

    public DialogHistoryLoaded(ResponseLoadDialogs dialogs) {
        this.dialogs = dialogs;
    }

    public ResponseLoadDialogs getDialogs() {
        return dialogs;
    }
}