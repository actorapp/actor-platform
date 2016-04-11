package im.actor.core.modules.messaging.router.entity;

import java.util.List;

import im.actor.core.modules.messaging.history.entity.DialogHistory;

public class RouterApplyDialogsHistory implements RouterMessageOnlyActive {

    private List<DialogHistory> dialogs;
    private Runnable executeAfter;

    public RouterApplyDialogsHistory(List<DialogHistory> dialogs, Runnable executeAfter) {
        this.dialogs = dialogs;
        this.executeAfter = executeAfter;
    }

    public List<DialogHistory> getDialogs() {
        return dialogs;
    }

    public Runnable getExecuteAfter() {
        return executeAfter;
    }
}
